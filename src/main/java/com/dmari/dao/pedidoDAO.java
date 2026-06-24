package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.VentaEstadisticaDTO;
import com.dmari.modelo.detallePedido;

public class pedidoDAO {

    databaseHelper db = new databaseHelper();

    /**
     * 1. registrar pedido (checkout)
     * metodo transaccional critico. opera bajo la regla "todo o nada" (atomicidad):
     * si cualquier paso falla, mysql revierte todo como si nada hubiera pasado
     * (rollback).
     *
     * DECISION DE DISEÑO CLAVE:
     * la tabla 'pedido' NO tiene id_usuario_fk directo. el cliente se identifica
     * de forma INDIRECTA a traves de la cadena:
     * pedido.id_carrito_fk -> carrito.id_cliente_fk -> cliente/usuario
     * esto evita redundancia y garantiza que el pedido siempre este ligado al
     * carrito exacto.
     *
     * @param idCarrito    int: id del carrito 'Activo' que se esta convirtiendo en
     *                     pedido.
     * @param idDireccion  int: id de la direccion de envio primaria del cliente
     *                     (NOT NULL en tabla).
     * @param totalPagar   double: suma total calculada en java (no en js para
     *                     evitar manipulacion).
     * @param carrito      ArrayList de detallePedido: los productos con cantidades
     *                     y precios.
     * @param idMetodoPago int: llave foranea hacia la tabla metodo_pago (ej. 1 =
     *                     nequi).
     * @param numeroCuenta String: comprobante del pago (celular, voucher, etc.).
     * @return boolean: true si el commit se ejecuta limpio, false si hubo rollback.
     */
    public boolean registrarPedido(int idCarrito, int idDireccion, double totalPagar, ArrayList<detallePedido> carrito,
            int idMetodoPago, String numeroCuenta) {

        // sql cabecera: id_carrito_fk e id_direccion_fk son NOT NULL segun
        // tablaMysql.sql.
        // sin id_usuario_fk directo: el cliente se navega via el carrito.
        // estado_pedido se fija en 'Pendiente' al crearse porque aun no fue procesado
        // por el proveedor.
        String sqlPedido = "INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES (?, ?, ?, 'Pendiente')";

        // sql detalle: vincula cada producto al pedido maestro.
        // precio_unitario y subtotal se congelan aqui como snapshot inmutable del
        // precio de venta.
        // si el proveedor cambia el precio manana, este registro historico queda
        // intacto.
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";

        // sql stock: descuenta las unidades del inventario del producto.
        // la condicion 'AND stock >= ?' es una barrera de seguridad:
        // si el stock es menor a la cantidad pedida, mysql no afecta ninguna fila
        // (devuelve 0 filas afectadas)
        // y nosotros detectamos ese 0 mas abajo para hacer rollback.
        String sqlDescontarStock = "UPDATE producto SET stock = stock - ? WHERE id_producto_pk = ? AND stock >= ?";

        // sql pago: guarda el comprobante financiero de la transaccion.
        // referencia_transaccion = celular nequi, voucher tarjeta, etc.
        // comision_dmari = 5% del total que retiene la plataforma.
        // monto_total = lo que le llega al proveedor (total - comision).
        String sqlPago = "INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES (?, ?, ?, ?, ?, 'Aprobado')";

        Connection con = null;
        try {
            con = db.conectar();
            // desactivamos el autocommit: mysql no guardara nada hasta que llamemos
            // con.commit() al final.
            // si algo falla en el medio, con.rollback() borra todo lo que se haya hecho
            // hasta ese punto.
            con.setAutoCommit(false);

            // variable para guardar el id autogenerado del pedido recien creado.
            // se usara como llave foranea en detalle_pedido y pago.
            int idPedidoGenerado = 0;

            // ---- PASO 1: insertar la cabecera del pedido ----
            // RETURN_GENERATED_KEYS le pide a mysql que nos devuelva el id autogenerado.
            try (PreparedStatement psPedido = con.prepareStatement(sqlPedido,
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                psPedido.setInt(1, idCarrito); // ? parametro 1: id del carrito origen
                psPedido.setInt(2, idDireccion); // ? parametro 2: id de la direccion de entrega
                psPedido.setDouble(3, totalPagar); // ? parametro 3: monto total de la factura
                psPedido.executeUpdate();

                // leemos el id generado por mysql para el nuevo registro de pedido
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    // condicional: rs.next() devuelve true si mysql inserto la fila y genero el id.
                    if (rs.next())
                        idPedidoGenerado = rs.getInt(1); // columna 1 = el id autogenerado
                }
            }

            // condicional de seguridad: si idPedidoGenerado sigue en 0, el INSERT fallo
            // silenciosamente.
            // no tiene sentido guardar productos o pagos sin un pedido maestro valido.
            if (idPedidoGenerado > 0) {
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                        PreparedStatement psStock = con.prepareStatement(sqlDescontarStock)) {

                    // ---- PASO 2: encolar los detalles y los descuentos de stock (batch) ----
                    // iteracion: cada 'item' es un producto del carrito con su cantidad y precio.
                    for (detallePedido item : carrito) {
                        // configuramos el detalle del producto ligado al id del pedido maestro
                        psDetalle.setInt(1, idPedidoGenerado); // ? 1: id del pedido padre
                        psDetalle.setInt(2, item.getIdProductoFk()); // ? 2: id del producto
                        psDetalle.setInt(3, item.getCantidad()); // ? 3: unidades compradas
                        psDetalle.setDouble(4, item.getPrecioUnitario()); // ? 4: precio congelado
                        psDetalle.setDouble(5, item.getSubtotal()); // ? 5: cantidad * precio
                        psDetalle.addBatch(); // encolamos: no ejecuta aun, guarda para ejecutar de golpe

                        // configuramos el descuento de inventario para este producto
                        psStock.setInt(1, item.getCantidad()); // ? 1: cuanto restar del stock
                        psStock.setInt(2, item.getIdProductoFk()); // ? 2: que producto afectar
                        psStock.setInt(3, item.getCantidad()); // ? 3 (clausula AND): stock >= cantidad
                        psStock.addBatch(); // encolamos junto con el detalle para ejecutar al unisono
                    }

                    // ejecutamos todas las inserciones de detalle de golpe (mas eficiente que una
                    // por una)
                    psDetalle.executeBatch();

                    // ejecutamos todos los descuentos de stock y capturamos cuantas filas afecto
                    // cada uno
                    int[] resultadosStock = psStock.executeBatch();

                    // ---- VALIDACION DE STOCK: si alguno da 0, hubo stock insuficiente ----
                    // iteracion + condicional: revisamos que cada UPDATE de stock afecto
                    // exactamente 1 fila.
                    for (int i = 0; i < resultadosStock.length; i++) {
                        // si resultadosStock[i] == 0, la condicion 'AND stock >= cantidad' fallo:
                        // el cliente intento comprar mas unidades de las que habia en inventario.
                        if (resultadosStock[i] == 0) {
                            // abortamos toda la transaccion: ningun cambio se guarda en la bd.
                            con.rollback();
                            System.out.println("Stock insuficiente para el producto en posicion " + i
                                    + " del carrito. Se hizo rollback.");
                            return false;
                        }
                    }

                    // ---- PASO 3: registrar el comprobante de pago ----
                    try (PreparedStatement psPago = con.prepareStatement(sqlPago)) {
                        psPago.setInt(1, idPedidoGenerado); // ? 1: amarrado al pedido recien creado
                        psPago.setInt(2, idMetodoPago); // ? 2: llave foranea a metodo_pago (nequi, pse, etc)
                        psPago.setString(3, numeroCuenta); // ? 3: referencia_transaccion (el comprobante del cliente)

                        // calculamos la comision de la plataforma: 5% del total de la compra
                        double comision = totalPagar * 0.05;
                        // el proveedor recibe el total menos la comision de dmari
                        double montoProveedor = totalPagar - comision;
                        psPago.setDouble(4, comision); // ? 4: porcentaje que retiene dmari
                        psPago.setDouble(5, montoProveedor); // ? 5: dinero real para el proveedor
                        psPago.executeUpdate();
                    }
                }

                // ---- PASO 4: confirmar toda la transaccion ----
                // con.commit() hace permanentes en la bd todos los cambios encolados.
                // si llegamos aqui, significa que pedido, detalles, stock y pago estan bien.
                con.commit();
                return true;
            }

            // si idPedidoGenerado era 0, el INSERT de cabecera fallo. revertimos por
            // seguridad.
            con.rollback();
            System.out.println("El INSERT de la cabecera del pedido no genero un ID valido. Se hizo rollback.");
            return false;

        } catch (SQLException e) {
            // capturamos cualquier error de mysql (ej. producto inexistente, conexion
            // caida)
            // y revertimos todo lo que se haya hecho en esta transaccion.
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
                System.err.println("Fallo critico al intentar hacer rollback en pedido: " + ex.getMessage());
            }
            System.out.println("Error al registrar el pedido transaccional: " + e.getMessage());
            return false;
        } finally {
            // el bloque finally SIEMPRE se ejecuta, con exito o con error.
            // restauramos el autocommit y cerramos la conexion para no dejarla ocupada.
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexion en pedido: " + e.getMessage());
            }
        }
    }

    /**
     * 2. listar pedidos por proveedor
     * genera un reporte personalizado para los usuarios con rol 'proveedor'.
     * cruza 7 tablas distintas para garantizar que el proveedor solo vea facturas
     * donde exista al menos un producto que le pertenezca. ademas, inyecta la
     * direccion completa del cliente para facilitar el proceso logistico de envio.
     * 
     * @param idUsuarioProveedor int: el id real del usuario con rol 4.
     * @return arraylist<detallepedido>: arreglo de objetos java con las ventas de
     *         ese proveedor.
     */
    public ArrayList<detallePedido> listarPedidosPorProveedor(int idUsuarioProveedor) {
        ArrayList<detallePedido> lista = new ArrayList<>();

        // consulta para el panel del proveedor.
        // 1. selecciona datos del pedido (id, fecha, estado).
        // 2. une usuario para saber quien compra.
        // 3. left joins con cliente y direccion para datos logisticos (direccion y
        // telefono).
        // 4. inner join con detalle_pedido para ver productos.
        // 5. inner join con producto para el nombre comercial.
        // 6. cruce con proveedor_producto para filtrar solo lo que le pertenece al
        // artesano.
        // 7. ordena por fecha mas reciente.
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, p.motivo_cancelacion, u.nombre AS nombre_cliente, "
                +
                // traemos la direccion de la tabla satelite para que el proveedor sepa a donde
                // enviar
                "d.direccion, d.direccion_detallada, c.referencia_ubicacion, " +
                // subconsulta 1 (LIMIT 1): trae unicamente el primer telefono que encuentre del
                // usuario.
                // esto evita que mysql multiplique las filas del pedido si el cliente tiene
                // varios telefonos.
                "(SELECT numero_telefonico FROM telefono WHERE id_usuario_fk = u.id_usuario_pk LIMIT 1) AS numero_telefonico, "
                +
                // subconsulta 2 (LIMIT 1 OFFSET 1): el 'offset 1' le dice a mysql que se salte
                // el primer telefono
                // y traiga unicamente el segundo. asi capturamos el telefono alternativo de
                // forma segura.
                "(SELECT numero_telefonico FROM telefono WHERE id_usuario_fk = u.id_usuario_pk LIMIT 1 OFFSET 1) AS telefono_secundario, "
                +
                "prod.nombre_producto, dp.cantidad, dp.subtotal " +
                // tabla principal de la consulta: el pedido maestro
                "FROM pedido p " +
                // inner join: el pedido debe tener un usuario real asociado si o si
                "INNER JOIN carrito car ON p.id_carrito_fk = car.id_carrito_pk " +
                "INNER JOIN usuario u ON car.id_cliente_fk = u.id_usuario_pk " +
                // left joins: cruzamos con el perfil del cliente y las tablas satelite
                "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                "LEFT JOIN direccion d ON p.id_direccion_fk = d.id_direccion_pk " +
                // inner join: estricto para ver que productos exactos compro en esa orden
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                // inner join: estrictos para enlazar el producto con su dueño
                "INNER JOIN proveedor_producto pp ON prod.id_producto_pk = pp.id_producto_fk " +
                "INNER JOIN proveedor pr ON pp.id_proveedor_fk = pr.id_proveedor_pk " +
                // filtramos para que solo salgan los productos del proveedor logueado,
                // ordenados del mas reciente al mas antiguo
                "WHERE pr.id_proveedor_pk = ? ORDER BY p.fecha DESC";

        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuarioProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                // iteracion: lee fila por fila los recibos enlazados y los inyecta en el
                // arreglo.
                while (rs.next()) {
                    detallePedido dp = new detallePedido();
                    dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                    dp.setFechaPedido(rs.getString("fecha"));
                    dp.setEstadoPedido(rs.getString("estado_pedido"));
                    dp.setMotivoCancelacion(rs.getString("motivo_cancelacion"));

                    // armamos un texto completo con todos los datos de envio para inyectarlo en el
                    // nombre
                    String dir = rs.getString("direccion");
                    String dirDetalle = rs.getString("direccion_detallada");
                    // condicional: solo agrega el detalle si no esta vacio o nulo
                    if (dirDetalle != null && !dirDetalle.isEmpty())
                        dir += " (" + dirDetalle + ")";
                    String telPrincipal = rs.getString("numero_telefonico");
                    String telSec = rs.getString("telefono_secundario");

                    String telefonos = (telPrincipal != null && !telPrincipal.isEmpty() ? telPrincipal : "") +
                            (telSec != null && !telSec.isEmpty() ? " / " + telSec : "");

                    String detallesContacto = (dir != null)
                            ? dir + " | Tel: " + (telefonos.isEmpty() ? "sin numero" : telefonos) +
                                    (rs.getString("referencia_ubicacion") != null
                                            && !rs.getString("referencia_ubicacion").isEmpty()
                                                    ? " | Ref: " + rs.getString("referencia_ubicacion")
                                                    : "")
                            : "sin direccion guardada";

                    dp.setNombreCliente(rs.getString("nombre_cliente") + " - " + detallesContacto);

                    dp.setNombreProducto(rs.getString("nombre_producto"));
                    dp.setCantidad(rs.getInt("cantidad"));
                    dp.setSubtotal(rs.getDouble("subtotal"));
                    lista.add(dp);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar pedidos del proveedor: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 3. listar pedidos por cliente
     * extrae exclusivamente el historial de compras personal del cliente
     * autenticado.
     * incluye informacion financiera basica (metodo de pago usado y precio
     * unitario)
     * para que el cliente pueda revisar cuanto le costo cada dona o vela en el
     * pasado.
     * 
     * @param idCliente int: el id en la base de datos del cliente comprador.
     * @return arraylist<detallepedido>: arreglo de productos comprados.
     */
    public ArrayList<detallePedido> listarPedidosPorCliente(int idCliente) {
        ArrayList<detallePedido> lista = new ArrayList<>();
        // consulta para el historial del cliente.
        // descripcion funcional de las sentencias sql aplicadas:
        // coalesce: retorna el primer valor no nulo de la lista. si 'nombre_marca' es
        // null,
        // selecciona el 'nombre' de usuario para asegurar que exista un valor legible.
        // limit 1: limita el resultado de la subconsulta a una sola fila. previene
        // duplicacion
        // de registros en la consulta principal si el usuario posee multiples
        // telefonos.
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, p.motivo_cancelacion, " +
                "prod.nombre_producto, dp.cantidad, dp.precio_unitario, dp.subtotal, mp.descripcion_pago, " +
                "COALESCE(prov.nombre_marca, u_prov.nombre) AS nombre_proveedor, " +
                "COALESCE((SELECT numero_telefonico FROM telefono WHERE id_usuario_fk = prov.id_proveedor_pk LIMIT 1), (SELECT correo FROM correo WHERE id_usuario_fk = prov.id_proveedor_pk AND correo_primario = true LIMIT 1)) AS contacto_proveedor "
                +
                "FROM pedido p " +

                // inner join: es estricto. asegura que el pedido solo se muestre si de verdad
                // tiene productos adentro.
                // si una orden esta vacia por error, el inner join la esconde por seguridad.
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                "INNER JOIN carrito car ON p.id_carrito_fk = car.id_carrito_pk " +

                // left join: es relajado. intenta buscar el pago o el perfil del proveedor.
                // si todavia no hay pago o perfil asociado, no destruye la orden principal,
                // simplemente rellena esos huecos con valor null pero la orden se sigue
                // mostrando.
                "LEFT JOIN proveedor_producto pp ON prod.id_producto_pk = pp.id_producto_fk " +
                "LEFT JOIN proveedor prov ON pp.id_proveedor_fk = prov.id_proveedor_pk " +
                "LEFT JOIN usuario u_prov ON prov.id_proveedor_pk = u_prov.id_usuario_pk " +
                "LEFT JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                "LEFT JOIN metodo_pago mp ON pg.id_metodo_pago_fk = mp.id_metodo_pago_pk " +
                "WHERE car.id_cliente_fk = ? ORDER BY p.fecha DESC";

        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                // iteracion: lee historial fila por fila y empaqueta en objetos detallepedido.
                while (rs.next()) {
                    detallePedido dp = new detallePedido();
                    dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                    dp.setFechaPedido(rs.getString("fecha"));

                    String metodoPago = rs.getString("descripcion_pago");
                    // condicional en linea (operador ternario): si el metodo no es nulo, lo
                    // envuelve en parentesis.
                    String estado = rs.getString("estado_pedido") + (metodoPago != null ? " (" + metodoPago + ")" : "");
                    dp.setEstadoPedido(estado);
                    dp.setMotivoCancelacion(rs.getString("motivo_cancelacion"));

                    dp.setNombreProducto(rs.getString("nombre_producto"));
                    dp.setNombreProveedor(rs.getString("nombre_proveedor"));

                    // atrapamos el numero en nuestra nueva columna de mysql extraida de la
                    // subconsulta virtual
                    dp.setContactoProveedor(rs.getString("contacto_proveedor"));

                    dp.setCantidad(rs.getInt("cantidad"));
                    dp.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    dp.setSubtotal(rs.getDouble("subtotal"));
                    lista.add(dp);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar pedidos del cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 4. listar todos los pedidos (panel de administracion)
     * vista global que no aplica ningun filtro restrictivo.
     * permite al administrador auditar la tienda completa, ver hacia donde van
     * todos los paquetes y confirmar todos los montos pagados.
     * 
     * @return arraylist<detallepedido>: arreglo completo con todos los datos
     *         financieros y logisticos.
     */
    public ArrayList<detallePedido> listarTodosLosPedidos() {
        ArrayList<detallePedido> lista = new ArrayList<>();
        // consulta global para administracion (8 tablas).
        // extrae la trazabilidad completa: comprador, producto, montos, destino y
        // pagos.
        // los left joins en perfil y direccion evitan que la lista falle si el perfil
        // esta incompleto.
        // ordena por el id del pedido de forma descendente para ver lo mas reciente.
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, p.motivo_cancelacion, u.nombre AS nombre_cliente, "
                +
                "d.direccion, d.direccion_detallada, c.referencia_ubicacion, " +
                // subconsulta 1: limit 1 garantiza que solo se extraiga un numero (el
                // principal),
                // evitando el bug de duplicacion de pedidos en la vista si hay multiples
                // telefonos.
                "(SELECT numero_telefonico FROM telefono WHERE id_usuario_fk = u.id_usuario_pk LIMIT 1) AS numero_telefonico, "
                +
                // subconsulta 2: limit 1 offset 1 descarta el primer numero y captura el
                // segundo (si existe),
                // permitiendo mostrar un telefono de respaldo para la logistica del envio.
                "(SELECT numero_telefonico FROM telefono WHERE id_usuario_fk = u.id_usuario_pk LIMIT 1 OFFSET 1) AS telefono_secundario, "
                +
                "prod.nombre_producto, dp.cantidad, dp.precio_unitario, dp.subtotal, mp.descripcion_pago " +
                "FROM pedido p " +
                "INNER JOIN carrito car ON p.id_carrito_fk = car.id_carrito_pk " +
                "INNER JOIN usuario u ON car.id_cliente_fk = u.id_usuario_pk " +
                "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                "LEFT JOIN direccion d ON p.id_direccion_fk = d.id_direccion_pk " +
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                "LEFT JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                "LEFT JOIN metodo_pago mp ON pg.id_metodo_pago_fk = mp.id_metodo_pago_pk " +
                "ORDER BY p.id_pedido_pk DESC";
        // nota: esta consulta no tiene where porque es global para el administrador

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // iteracion: recorre absolutamente todas las filas de ventas generadas.
            while (rs.next()) {
                detallePedido dp = new detallePedido();
                dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                dp.setFechaPedido(rs.getString("fecha"));

                String metodoPago = rs.getString("descripcion_pago");
                String estado = rs.getString("estado_pedido") + (metodoPago != null ? " (" + metodoPago + ")" : "");
                dp.setEstadoPedido(estado);
                dp.setMotivoCancelacion(rs.getString("motivo_cancelacion"));

                dp.setNombreCliente(rs.getString("nombre_cliente"));
                dp.setNombreProducto(rs.getString("nombre_producto"));
                dp.setCantidad(rs.getInt("cantidad"));
                dp.setPrecioUnitario(rs.getDouble("precio_unitario"));
                dp.setSubtotal(rs.getDouble("subtotal"));

                // armamos el texto completo con la direccion, telefono y referencia para el
                // administrador
                String dir = rs.getString("direccion");
                String dirDetalle = rs.getString("direccion_detallada");
                // condicional: asegura que no se impriman parentesis vacios si no hay detalle
                // logistico.
                if (dirDetalle != null && !dirDetalle.isEmpty())
                    dir += " (" + dirDetalle + ")";
                String telPrincipal = rs.getString("numero_telefonico");
                String telSec = rs.getString("telefono_secundario");

                String telefonos = (telPrincipal != null && !telPrincipal.isEmpty() ? telPrincipal : "") +
                        (telSec != null && !telSec.isEmpty() ? " / " + telSec : "");
                String detallesContacto = (dir != null)
                        ? dir + " | Tel: " + (telefonos.isEmpty() ? "sin numero" : telefonos) +
                                (rs.getString("referencia_ubicacion") != null
                                        && !rs.getString("referencia_ubicacion").isEmpty()
                                                ? " | Ref: " + rs.getString("referencia_ubicacion")
                                                : "")
                        : "sin direccion guardada";

                // usamos nombrecliente como portador de los datos si no has modificado la clase
                // modelo aun
                dp.setNombreCliente(rs.getString("nombre_cliente") + " - " + detallesContacto);

                lista.add(dp);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar todos los pedidos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 5. actualizar estado del pedido (logistica)
     * permite a administradores, proveedores y clientes cambiar la fase del pedido.
     *
     * ciclo de vida del pedido (enum en tablaMysql.sql):
     * Pendiente -> Preparando -> En Camino -> Entregado
     * Pendiente -> Cancelado_por_Cliente (solo el cliente puede hacer esto)
     * Preparando -> Cancelado_por_Proveedor (solo proveedor o admin)
     *
     * efecto secundario critico:
     * si el nuevo estado es cualquier variante de "Cancelado" Y el estado anterior
     * NO era cancelacion,
     * este metodo devuelve automaticamente las unidades al inventario (tabla
     * producto).
     * esto evita que el stock quede bloqueado por pedidos cancelados.
     *
     * @param idPedido        int: id_pedido_pk del registro a actualizar.
     * @param nuevoEstado     String: el nuevo valor del enum estado_pedido.
     * @param motivo          String: texto con la razon (obligatorio si es
     *                        cancelacion, nulo en otros casos).
     * @param idUsuarioAccion int: id del usuario que ejecuta el cambio (para campo
     *                        cancelado_por_id_fk).
     * @return boolean: true si el UPDATE afecto exactamente 1 fila y el commit fue
     *         exitoso.
     */
    public boolean actualizarEstadoPedido(int idPedido, String nuevoEstado, String motivo, int idUsuarioAccion) {

        // sql de actualizacion: guarda el nuevo estado, el motivo (si aplica) y quien
        // lo cambio.
        // cancelado_por_id_fk es una FK a usuario (puede ser null si el cambio no es
        // una cancelacion).
        String sql = "UPDATE pedido SET estado_pedido = ?, motivo_cancelacion = ?, cancelado_por_id_fk = ? WHERE id_pedido_pk = ?";
        Connection con = null;
        try {
            con = db.conectar();
            // desactivamos autocommit para proteger la integridad del stock durante la
            // transaccion
            con.setAutoCommit(false);

            // ---- PRE-CONSULTA: capturamos el estado anterior ----
            // es necesario saber el estado previo para evitar devolver stock dos veces
            // si alguien intenta cancelar un pedido que ya fue cancelado antes.
            String estadoAnterior = "";
            String sqlEstadoAnterior = "SELECT estado_pedido FROM pedido WHERE id_pedido_pk = ?";
            try (PreparedStatement psVer = con.prepareStatement(sqlEstadoAnterior)) {
                psVer.setInt(1, idPedido);
                try (ResultSet rs = psVer.executeQuery()) {
                    // condicional: si el select devuelve fila, leemos el estado actual del pedido.
                    if (rs.next()) {
                        estadoAnterior = rs.getString("estado_pedido");
                    } else {
                        // si el select no devuelve filas, el idPedido no existe en la base de datos.
                        System.out.println(
                                "Advertencia: no se encontro el pedido con id " + idPedido + " en la base de datos.");
                        con.rollback();
                        return false;
                    }
                }
            }

            // REGLA DE NEGOCIO CRITICA:
            // Un pedido solo puede ser 'Cancelado_por_Cliente' si todavia esta 'Pendiente'.
            // Si el proveedor ya lo esta preparando o enviando, el cliente pierde el derecho a cancelar unilateralmente.
            if ("Cancelado_por_Cliente".equals(nuevoEstado) && !"Pendiente".equals(estadoAnterior)) {
                System.out.println("Rechazo: Intento de cancelar por cliente un pedido que ya esta en estado: " + estadoAnterior);
                con.rollback();
                return false;
            }

            // ---- ACTUALIZACION DE ESTADO ----
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado); // ? 1: el nuevo estado del enum (ej. 'Preparando')

                // condicional: si el nuevo estado es una cancelacion, se llenan los campos de
                // auditoria.
                // para estados normales (Preparando, En Camino, Entregado) estos campos deben
                // ser null.
                if (nuevoEstado.startsWith("Cancelado")) {
                    // el motivo es obligatorio en cancelaciones para tener trazabilidad.
                    if (motivo != null && !motivo.trim().isEmpty()) {
                        ps.setString(2, motivo); // ? 2: texto del motivo de cancelacion
                    } else {
                        // si no viene motivo en una cancelacion, lo dejamos null (la bd lo permite).
                        ps.setNull(2, java.sql.Types.VARCHAR);
                    }
                    // guardamos quien cancelo para auditoria (es FK a la tabla usuario).
                    if (idUsuarioAccion > 0) {
                        ps.setInt(3, idUsuarioAccion); // ? 3: id del usuario que cancela
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER); // cancelacion sin usuario identificado
                    }
                } else {
                    // para estados no-cancelacion limpiamos los campos de auditoria con null.
                    ps.setNull(2, java.sql.Types.VARCHAR); // motivo_cancelacion = null
                    ps.setNull(3, java.sql.Types.INTEGER); // cancelado_por_id_fk = null
                }

                ps.setInt(4, idPedido); // ? 4: clausula WHERE para identificar el pedido exacto
                int afectadas = ps.executeUpdate();

                // condicional de exito: si afectadas > 0, el UPDATE modifico la fila
                // correctamente.
                if (afectadas > 0) {

                    // ---- EFECTO SECUNDARIO: devolucion de stock si es cancelacion nueva ----
                    // condicional doble:
                    // - nuevoEstado.startsWith("Cancelado"): es una cancelacion.
                    // - !estadoAnterior.startsWith("Cancelado"): el pedido NO estaba ya cancelado.
                    // si no revisamos el estado anterior, podriamos devolver el stock 2 veces.
                    if (nuevoEstado.startsWith("Cancelado") && !estadoAnterior.startsWith("Cancelado")) {

                        // sql para leer que productos y cantidades hay que devolver al inventario
                        String sqlDetalles = "SELECT id_producto_fk, cantidad FROM detalle_pedido WHERE id_pedido_fk = ?";
                        // sql para reponer unidades: stock = stock + cantidad_devuelta
                        String sqlDevolverStock = "UPDATE producto SET stock = stock + ? WHERE id_producto_pk = ?";

                        try (PreparedStatement psDetalles = con.prepareStatement(sqlDetalles);
                                PreparedStatement psStock = con.prepareStatement(sqlDevolverStock)) {

                            psDetalles.setInt(1, idPedido); // filtramos los productos del pedido cancelado
                            try (ResultSet rsDetalles = psDetalles.executeQuery()) {
                                // iteracion: leemos cada producto del pedido y encolamos la devolucion de
                                // stock.
                                while (rsDetalles.next()) {
                                    psStock.setInt(1, rsDetalles.getInt("cantidad")); // cuanto devolver
                                    psStock.setInt(2, rsDetalles.getInt("id_producto_fk")); // a que producto
                                    psStock.addBatch();
                                }
                                // ejecutamos todas las devoluciones de stock de golpe
                                psStock.executeBatch();
                            }
                        }
                    }

                    // con.commit() hace permanentes: el nuevo estado y la devolucion de stock (si
                    // aplico).
                    con.commit();
                    return true;
                }
            }

            // si afectadas == 0, el UPDATE no encontro el pedido o no cambio nada.
            // revertimos.
            System.out.println(
                    "Advertencia: el UPDATE de estado no afecto ninguna fila para el pedido " + idPedido + ".");
            con.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
                System.err.println(
                        "Fallo critico al intentar hacer rollback en actualizacion de estado: " + ex.getMessage());
            }
            System.out.println("Error al actualizar el estado del pedido: " + e.getMessage());
            return false;
        } finally {
            // restauramos autocommit y cerramos la conexion siempre, con o sin error
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexion en actualizacion de estado: " + e.getMessage());
            }
        }
    }

    /**
     * 6. obtener estadisticas de ventas (dashboard)
     * permite extraer el rendimiento financiero.
     * si el idproveedor es 0, actua como administrador (vista global).
     * si el idproveedor > 0, filtra solo los productos de ese dueno.
     * 
     * @param idproveedor int: id del usuario proveedor o 0 para admin.
     * @return double[]: arreglo con [total_ventas, cantidad_pedidos,
     *         comisiones_generadas]
     */
    public double[] obtenerEstadisticasVentas(int idProveedor) {
        double[] stats = new double[3];
        String sql;

        if (idProveedor == 0) {
            // estadisticas globales para administrador.
            // Usamos SUM(dp.subtotal) para ser consistentes con la granularidad de
            // productos
            // count: numero de transacciones exitosas.
            // sum(* 0.03): calculo de ingresos por comision para la plataforma (3%).
            sql = "SELECT SUM(dp.subtotal) as total, COUNT(DISTINCT p.id_pedido_pk) as conteo, SUM(dp.subtotal * 0.03) as comision "
                    +
                    "FROM pedido p " +
                    "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                    "WHERE p.estado_pedido = 'Entregado'";
        } else {
            // estadisticas privadas para el proveedor.
            // sum(dp.subtotal): suma solo el dinero de sus propios productos.
            // inner join con proveedor_producto: garantiza el aislamiento de datos.
            // distinct: evita duplicar el conteo de pedidos con multiples items del mismo
            // dueno.
            sql = "SELECT SUM(dp.subtotal) as total, COUNT(DISTINCT p.id_pedido_pk) as conteo, SUM(dp.subtotal * 0.03) as comision "
                    +
                    "FROM pedido p " +
                    "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                    "INNER JOIN proveedor_producto pp ON dp.id_producto_fk = pp.id_producto_fk " +
                    "WHERE pp.id_proveedor_fk = ? AND p.estado_pedido = 'Entregado'";
        }

        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            // inyectamos el parametro solo si no es una consulta de administrador
            if (idProveedor > 0)
                ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                // mapeamos los resultados numericos al arreglo de retorno
                if (rs.next()) {
                    stats[0] = rs.getDouble("total");
                    stats[1] = rs.getDouble("conteo");
                    stats[2] = rs.getDouble("comision");
                }
            }
        } catch (SQLException e) {
            System.out.println("error al obtener estadisticas: " + e.getMessage());
        }
        return stats;
    }

    /**
     * 7. generar datos para factura (reporte PDF)
     * extrae la informacion completa de un pedido especifico para construir el comprobante.
     *
     * nota: el join con direccion se hace con la llave del pedido (id_direccion_fk)
     * en lugar de la del usuario, esto asegura que la factura siempre muestre a donde 
     * se envio el paquete en ese momento, aunque el cliente cambie de casa despues.
     */
    public ResultSet obtenerDatosFactura(int idPedido) throws SQLException {
        Connection con = db.conectar();
        String sql = "SELECT p.id_pedido_pk, p.fecha, u.nombre, u.apellido, co.correo, " +
                "d.direccion, dp.cantidad, prod.nombre_producto, dp.precio_unitario, " +
                "dp.subtotal, pg.monto_total, mp.descripcion_pago " +
                "FROM pedido p " +
                // navegamos al cliente via la cadena: pedido -> carrito -> usuario
                "INNER JOIN carrito car ON p.id_carrito_fk = car.id_carrito_pk " +
                "INNER JOIN usuario u ON car.id_cliente_fk = u.id_usuario_pk " +
                // LEFT JOIN en correo: si el cliente no tiene correo, la factura no desaparece
                "LEFT JOIN correo co ON u.id_usuario_pk = co.id_usuario_fk AND co.correo_primario = 1 " +
                // CORRECTO: usamos la FK del pedido (direccion del momento del checkout, no la actual)
                "LEFT JOIN direccion d ON p.id_direccion_fk = d.id_direccion_pk " +
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                "LEFT JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                "LEFT JOIN metodo_pago mp ON pg.id_metodo_pago_fk = mp.id_metodo_pago_pk " +
                "WHERE p.id_pedido_pk = ?";
        // nota: el llamador debe cerrar la conexion al terminar de procesar el resultset
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idPedido);
        return ps.executeQuery();
    }

    /**
     * 8. obtener ventas mensuales globales (admin)
     * extrae el total de dinero de pedidos entregados agrupados por mes.
     * 
     * @return arraylist<ventaestadisticadto>: lista de meses y montos.
     */
    public ArrayList<VentaEstadisticaDTO> obtenerVentasMensualesGlobales() {
        ArrayList<VentaEstadisticaDTO> lista = new ArrayList<>();
        // consulta de ventas mensuales (global).
        // date_format: agrupa las fechas por año y mes (ej: 2024-05).
        // sum: totaliza los ingresos de cada mes.
        // order by: mantiene la linea de tiempo cronologica para la grafica.
        String sql = "SELECT DATE_FORMAT(fecha, '%Y-%m') as mes, SUM(total_pagar) as total " +
                "FROM pedido WHERE estado_pedido = 'Entregado' " +
                "GROUP BY mes ORDER BY mes ASC";

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                VentaEstadisticaDTO v = new VentaEstadisticaDTO();
                // asignamos el nombre del mes como etiqueta
                v.setEtiqueta(rs.getString("mes"));
                // asignamos el monto sumado
                v.setTotal(rs.getDouble("total"));
                lista.add(v);
            }
        } catch (SQLException e) {
            System.out.println("error en obtener ventas mensuales globales: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 9. obtener ventas mensuales por proveedor
     * extrae la sumatoria de subtotales de productos que pertenecen al proveedor
     * logueado.
     * cruza pedido con detalle_pedido y la tabla puente de proveedor_producto.
     * 
     * @param idproveedor int: id del usuario con rol proveedor.
     * @return arraylist<ventaestadisticadto>: lista de meses y sus ventas privadas.
     */
    public ArrayList<VentaEstadisticaDTO> obtenerVentasMensualesProveedor(int idProveedor) {
        ArrayList<VentaEstadisticaDTO> lista = new ArrayList<>();
        // consulta de ventas mensuales (por proveedor).
        // sum(dp.subtotal): totaliza ingresos privados del artesano.
        // joins: vincula el pedido con el dueno del producto.
        // date_format: segmenta los ingresos por mes para alimentar chart.js.
        // where: asegura que solo se sumen facturas entregadas del proveedor logueado.
        String sql = "SELECT DATE_FORMAT(p.fecha, '%Y-%m') as mes, SUM(dp.subtotal) as total " +
                "FROM pedido p " +
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN proveedor_producto pp ON dp.id_producto_fk = pp.id_producto_fk " +
                "WHERE pp.id_proveedor_fk = ? AND p.estado_pedido = 'Entregado' " +
                "GROUP BY mes ORDER BY mes ASC";

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaEstadisticaDTO v = new VentaEstadisticaDTO();
                    v.setEtiqueta(rs.getString("mes"));
                    v.setTotal(rs.getDouble("total"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.out.println("error en obtener ventas mensuales proveedor: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 10. obtener total de productos vendidos globalmente (admin)
     * suma la cantidad de todos los productos en pedidos entregados.
     * 
     * @return long: cantidad total de productos vendidos.
     */
    public long obtenerTotalProductosVendidosGlobales() {
        long total = 0;
        String sql = "SELECT SUM(dp.cantidad) as total_productos " +
                "FROM detalle_pedido dp " +
                "INNER JOIN pedido p ON dp.id_pedido_fk = p.id_pedido_pk " +
                "WHERE p.estado_pedido = 'Entregado'";

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getLong("total_productos");
            }
        } catch (SQLException e) {
            System.out.println("error en obtener total productos vendidos globales: " + e.getMessage());
        }
        return total;
    }

    /**
     * 11. obtener total de productos vendidos por proveedor
     * suma la cantidad de productos vendidos que pertenecen a un proveedor
     * específico.
     * 
     * @param idProveedor int: id del usuario con rol proveedor.
     * @return long: cantidad total de productos vendidos por el proveedor.
     */
    public long obtenerTotalProductosVendidosProveedor(int idProveedor) {
        long total = 0;
        String sql = "SELECT SUM(dp.cantidad) as total_productos " +
                "FROM detalle_pedido dp " +
                "INNER JOIN pedido p ON dp.id_pedido_fk = p.id_pedido_pk " +
                "INNER JOIN proveedor_producto pp ON dp.id_producto_fk = pp.id_producto_fk " +
                "WHERE pp.id_proveedor_fk = ? AND p.estado_pedido = 'Entregado'";

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getLong("total_productos");
                }
            }
        } catch (SQLException e) {
            System.out.println("error en obtener total productos vendidos por proveedor: " + e.getMessage());
        }
        return total;
    }

    /**
     * 12. Obtiene un resumen de ventas por cada proveedor para el administrador.
     * Incluye el total de ingresos, cantidad de pedidos y productos vendidos por
     * proveedor.
     *
     * @return ArrayList<Map<String, Object>>: Lista de mapas con datos de cada
     *         proveedor.
     */
    public ArrayList<Map<String, Object>> obtenerResumenVentasPorProveedorGlobales() {
        ArrayList<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT u.nombre AS nombre_proveedor, " +
                "SUM(dp.subtotal) AS total_ingresos_proveedor, " +
                "COUNT(DISTINCT p.id_pedido_pk) AS cantidad_pedidos_proveedor, " +
                "SUM(dp.cantidad) AS total_productos_vendidos_proveedor " +
                "FROM pedido p " +
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN proveedor_producto pp ON dp.id_producto_fk = pp.id_producto_fk " +
                "INNER JOIN proveedor prov ON pp.id_proveedor_fk = prov.id_proveedor_pk " +
                "INNER JOIN usuario u ON prov.id_proveedor_pk = u.id_usuario_pk " +
                "WHERE p.estado_pedido = 'Entregado' " +
                "GROUP BY u.nombre " +
                "ORDER BY total_ingresos_proveedor DESC";

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> proveedorStats = new HashMap<>();
                proveedorStats.put("nombre_proveedor", rs.getString("nombre_proveedor"));
                proveedorStats.put("total_ingresos_proveedor", rs.getDouble("total_ingresos_proveedor"));
                proveedorStats.put("cantidad_pedidos_proveedor", rs.getLong("cantidad_pedidos_proveedor"));
                proveedorStats.put("total_productos_vendidos_proveedor",
                        rs.getLong("total_productos_vendidos_proveedor"));
                lista.add(proveedorStats);
            }
        } catch (SQLException e) {
            System.out.println("error en obtenerResumenVentasPorProveedorGlobales: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 13. Obtiene los productos más vendidos globalmente para el administrador.
     *
     * @param limit int: El número máximo de productos a retornar (ej. 5 para top
     *              5).
     * @return ArrayList<Map<String, Object>>: Lista de mapas con datos de los
     *         productos.
     */
    public ArrayList<Map<String, Object>> obtenerTopProductosVendidosGlobales(int limit) {
        ArrayList<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT prod.nombre_producto, SUM(dp.cantidad) AS cantidad_vendida, SUM(dp.subtotal) AS ingresos_generados "
                +
                "FROM detalle_pedido dp " +
                "INNER JOIN pedido p ON dp.id_pedido_fk = p.id_pedido_pk " +
                "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                "WHERE p.estado_pedido = 'Entregado' " +
                "GROUP BY prod.nombre_producto " +
                "ORDER BY cantidad_vendida DESC " +
                "LIMIT ?";

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> productoStats = new HashMap<>();
                    productoStats.put("nombre_producto", rs.getString("nombre_producto"));
                    productoStats.put("cantidad_vendida", rs.getLong("cantidad_vendida"));
                    productoStats.put("ingresos_generados", rs.getDouble("ingresos_generados"));
                    lista.add(productoStats);
                }
            }
        } catch (SQLException e) {
            System.out.println("error en obtenerTopProductosVendidosGlobales: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 14. Obtiene el ID del proveedor a partir del ID del usuario
     * Método auxiliar para convertir id_usuario en id_proveedor
     *
     * @param idUsuario int: El ID del usuario (proveedor)
     * @return int: El ID del proveedor, o 0 si no existe
     */
    public int obtenerIdProveedorPorUsuario(int idUsuario) {
        int idProveedor = 0;
        String sql = "SELECT id_proveedor_pk FROM proveedor WHERE id_proveedor_pk = ?";

        try (Connection con = db.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idProveedor = rs.getInt("id_proveedor_pk");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID de proveedor por usuario: " + e.getMessage());
        }
        return idProveedor;
    }
}