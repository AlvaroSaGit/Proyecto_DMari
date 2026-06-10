package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.VentaEstadisticaDTO;
import com.dmari.modelo.detallePedido;

public class pedidoDAO {
    
    databaseHelper db = new databaseHelper();

    /**
     * 1. registrar pedido (checkout)
     * metodo transaccional critico para guardar la compra completa.
     * opera bajo la regla todo o nada: guarda la cabecera del pedido,
     * los detalles de cada producto, descuenta el inventario y registra el pago.
     * 
     * @param idCliente int: identificador numerico del comprador.
     * @param totalPagar double: costo total de la compra con decimales.
     * @param carrito arraylist<detallepedido>: arreglo de objetos conteniendo ids de producto y sus cantidades.
     * @param idMetodoPago int: identificador de la forma de pago (ej. 1 para nequi).
     * @param numeroCuenta string: texto con el comprobante, cuenta o celular usado.
     * @return boolean: true si el commit general se ejecuta sin errores, false si hubo un rollback.
     */
    public boolean registrarPedido(int idCliente, double totalPagar, ArrayList<detallePedido> carrito, int idMetodoPago, String numeroCuenta) {
        // sql para insertar la cabecera del pedido.
        // guarda la relacion con el cliente y el monto total de la venta.
        // el estado inicial se establece como pendiente.
        String sqlPedido = "INSERT INTO pedido (id_cliente_fk, total_pagar, estado_pedido) VALUES (?, ?, 'Pendiente')";
        // sql para registrar cada producto comprado.
        // vincula el item al pedido principal usando su id.
        // guarda la cantidad, el precio capturado y el subtotal calculado.
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        // sql para descontar unidades del inventario.
        // resta la cantidad del stock disponible del producto.
        // la clausula stock >= cantidad evita que el inventario sea negativo.
        String sqlDescontarStock = "UPDATE producto SET stock = stock - ? WHERE id_producto_pk = ? AND stock >= ?";
        // sql para registrar el comprobante de pago simulado.
        // amarra el pago al pedido generado.
        // registra la comision de dmari y el monto neto para el proveedor.
        String sqlPago = "INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, numero_cuenta_ahorro, comision_dmari, monto_total, estado_activo, estado_pago) VALUES (?, ?, ?, ?, ?, 1, 'Aprobado')";
        
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el autocommit para que mysql no guarde nada hasta que todo el carrito se procese bien
            con.setAutoCommit(false);
            
            int idPedidoGenerado = 0;
            
            // paso 1: insertamos el pedido maestro
            try (PreparedStatement psPedido = con.prepareStatement(sqlPedido, PreparedStatement.RETURN_GENERATED_KEYS)) {
                // asignamos los parametros del cliente y monto a la consulta
                // configuracion de parametros para la cabecera de la factura
                psPedido.setInt(1, idCliente);
                psPedido.setDouble(2, totalPagar);
                // enviamos el comando de insercion a la base de datos
                // ejecucion de la insercion maestra del pedido
                psPedido.executeUpdate();
                
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    // verificamos si mysql genero una nueva llave primaria
                    // condicional: si mysql pudo generar el id, lo capturamos para amarrarlo a los productos.
                    if (rs.next()) idPedidoGenerado = rs.getInt(1);
                }
            }
            
            // condicional: verifica si el id maestro es valido antes de intentar guardar productos huerfanos.
            if (idPedidoGenerado > 0) {
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                     PreparedStatement psStock = con.prepareStatement(sqlDescontarStock)) {
                    
                    // iteracion: recorre todos los elementos comprados para encolar sus inserciones (batch).
                    for (detallePedido item : carrito) {
                        // configuramos el detalle amarrado al id del pedido principal
                        psDetalle.setInt(1, idPedidoGenerado);
                        psDetalle.setInt(2, item.getIdProductoFk());
                        psDetalle.setInt(3, item.getCantidad());
                        psDetalle.setDouble(4, item.getPrecioUnitario());
                        psDetalle.setDouble(5, item.getSubtotal());
                        
                        // agregamos la operacion al lote de ejecucion masiva
                        // addbatch encola las instrucciones para ejecutarlas todas de golpe (mejor rendimiento)
                        psDetalle.addBatch(); 
                        
                        // configuramos la orden para descontar la cantidad comprada del inventario del producto
                        psStock.setInt(1, item.getCantidad());
                        psStock.setInt(2, item.getIdProductoFk());
                        psStock.setInt(3, item.getCantidad()); // parametro de seguridad: stock >= cantidad
                        psStock.addBatch();
                    }
                    psDetalle.executeBatch(); // disparamos todas las inserciones del carrito
                    
                    // ejecutamos los descuentos de inventario de forma segura
                    int[] resultadosStock = psStock.executeBatch();
                    
                    // iteracion y condicional combinados: revisa la respuesta de cada descuento de stock.
                    for (int res : resultadosStock) {
                        // si un resultado da cero (0), indica que no se afecto ninguna fila porque el stock era insuficiente.
                        if (res == 0) {
                            // si el resultado es 0, significa que no habia stock suficiente en la bd.
                            // mysql bloqueo la venta para evitar numeros negativos. abortamos la compra.
                            con.rollback();
                            return false; 
                        }
                    }
                    
                    // paso 3: registramos el comprobante de pago asociado a la factura
                    try (PreparedStatement psPago = con.prepareStatement(sqlPago)) {
                        // vinculamos el pago con el pedido recien creado
                        psPago.setInt(1, idPedidoGenerado);
                        psPago.setInt(2, idMetodoPago);
                        psPago.setString(3, numeroCuenta);
                        // simulamos que dmari retiene el 5% de comision por vender los productos de los proveedores
                        double comision = totalPagar * 0.05;
                        double totalProveedor = totalPagar - comision;
                        psPago.setDouble(4, comision);
                        // el monto total representa lo que recibe el proveedor tras la comision
                        psPago.setDouble(5, totalProveedor);
                        psPago.executeUpdate();
                    }
                }
                
                // paso 4: todo salio bien, confirmamos la transaccion
                con.commit(); 
                return true;
            }
            
            // si no se genero id, revertimos por seguridad
            con.rollback();
            return false;
            
        } catch (SQLException e) {
            // si explota cualquier cosa (ej. id producto no existe), revertimos todo el carrito
            try { if (con != null) con.rollback(); } catch (SQLException ex) {
                System.err.println("Fallo critico al intentar hacer rollback en pedido: " + ex.getMessage());
            }
            System.out.println("Error al registrar el pedido transaccional: " + e.getMessage());
            return false;
        } finally {
            // restauramos el modo normal de mysql para futuras conexiones
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {
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
     * @return arraylist<detallepedido>: arreglo de objetos java con las ventas de ese proveedor.
     */
    public ArrayList<detallePedido> listarPedidosPorProveedor(int idUsuarioProveedor) {
        ArrayList<detallePedido> lista = new ArrayList<>();
        
        // consulta para el panel del proveedor. 
        // 1. selecciona datos del pedido (id, fecha, estado).
        // 2. une usuario para saber quien compra.
        // 3. left joins con cliente y direccion para datos logisticos (direccion y telefono).
        // 4. inner join con detalle_pedido para ver productos.
        // 5. inner join con producto para el nombre comercial.
        // 6. cruce con proveedor_producto para filtrar solo lo que le pertenece al artesano.
        // 7. ordena por fecha mas reciente.
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, p.motivo_cancelacion, u.nombre AS nombre_cliente, " +
                     // traemos la direccion de la tabla satelite para que el proveedor sepa a donde enviar
                     "d.direccion, d.direccion_detallada, c.telefono_secundario, c.referencia_ubicacion, t.numero_telefonico, prod.nombre_producto, dp.cantidad, dp.subtotal " +
                     // tabla principal de la consulta: el pedido maestro
                     "FROM pedido p " +
                     // inner join: el pedido debe tener un usuario real asociado si o si
                     "INNER JOIN usuario u ON p.id_cliente_fk = u.id_usuario_pk " +
                     // left joins: cruzamos con el perfil del cliente y las tablas satelite
                     "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                     "LEFT JOIN direccion d ON u.id_usuario_pk = d.id_usuario_fk AND d.direccion_primario = 1 " +
                     // left join: traemos el telefono principal de la tabla satelite telefono
                     "LEFT JOIN telefono t ON u.id_usuario_pk = t.id_usuario_fk " +
                     // inner join: estricto para ver que productos exactos compro en esa orden
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                     // inner join: estrictos para enlazar el producto con su dueño
                     "INNER JOIN proveedor_producto pp ON prod.id_producto_pk = pp.id_producto_fk " +
                     "INNER JOIN proveedor pr ON pp.id_proveedor_fk = pr.id_proveedor_pk " +
                     // filtramos para que solo salgan los productos del proveedor logueado, ordenados del mas reciente al mas antiguo
                     "WHERE pr.id_proveedor_pk = ? ORDER BY p.fecha DESC";
                     
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setInt(1, idUsuarioProveedor);
             try (ResultSet rs = ps.executeQuery()) {
                 // iteracion: lee fila por fila los recibos enlazados y los inyecta en el arreglo.
                 while(rs.next()) {
                     detallePedido dp = new detallePedido();
                     dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                     dp.setFechaPedido(rs.getString("fecha"));
                     dp.setEstadoPedido(rs.getString("estado_pedido"));
                     dp.setMotivoCancelacion(rs.getString("motivo_cancelacion"));
                     
                     // armamos un texto completo con todos los datos de envio para inyectarlo en el nombre
                     String dir = rs.getString("direccion");
                     String dirDetalle = rs.getString("direccion_detallada");
                     // condicional: solo agrega el detalle si no esta vacio o nulo
                     if (dirDetalle != null && !dirDetalle.isEmpty()) dir += " (" + dirDetalle + ")";
                     String telPrincipal = rs.getString("numero_telefonico");
                     String telSec = rs.getString("telefono_secundario");
                     
                     String telefonos = (telPrincipal != null && !telPrincipal.isEmpty() ? telPrincipal : "") + 
                                        (telSec != null && !telSec.isEmpty() ? " / " + telSec : "");
                                        
                     String detallesContacto = (dir != null) 
                         ? dir + " | Tel: " + (telefonos.isEmpty() ? "sin numero" : telefonos) + 
                           (rs.getString("referencia_ubicacion") != null && !rs.getString("referencia_ubicacion").isEmpty() ? " | Ref: " + rs.getString("referencia_ubicacion") : "") 
                         : "sin direccion guardada";
                         
                     dp.setNombreCliente(rs.getString("nombre_cliente") + " - " + detallesContacto);
                     
                     dp.setNombreProducto(rs.getString("nombre_producto"));
                     dp.setCantidad(rs.getInt("cantidad"));
                     dp.setSubtotal(rs.getDouble("subtotal"));
                     lista.add(dp);
                 }
             }
        } catch (SQLException e) { System.out.println("Error al listar pedidos del proveedor: " + e.getMessage()); }
        return lista;
    }

    /**
     * 3. listar pedidos por cliente
     * extrae exclusivamente el historial de compras personal del cliente autenticado.
     * incluye informacion financiera basica (metodo de pago usado y precio unitario)
     * para que el cliente pueda revisar cuanto le costo cada dona o vela en el pasado.
     * 
     * @param idCliente int: el id en la base de datos del cliente comprador.
     * @return arraylist<detallepedido>: arreglo de productos comprados.
     */
    public ArrayList<detallePedido> listarPedidosPorCliente(int idCliente) {
        ArrayList<detallePedido> lista = new ArrayList<>();
        // consulta para el historial del cliente.
        // une pedido con detalle y producto para mostrar que compro.
        // usa left joins con pago y metodo para ver la forma de pago.
        // filtra por el id del cliente logueado.
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, p.motivo_cancelacion, " +
                     // traemos el nombre del producto, sus datos monetarios y el metodo de pago
                     "prod.nombre_producto, dp.cantidad, dp.precio_unitario, dp.subtotal, mp.descripcion_pago " +
                     // comenzamos desde la tabla maestra de pedidos
                     "FROM pedido p " +
                     // join 1: cruzamos con el detalle para sacar los productos de cada factura
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     // join 2: cruzamos con producto para saber el nombre de lo que compro
                     "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                     // joins de pago: para mostrar como pago este pedido
                     "LEFT JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                     "LEFT JOIN metodo_pago mp ON pg.id_metodo_pago_fk = mp.id_metodo_pago_pk " +
                     // filtramos para traer solo los pedidos del cliente que esta consultando, del mas nuevo al mas viejo
                     "WHERE p.id_cliente_fk = ? ORDER BY p.fecha DESC";
                     
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setInt(1, idCliente);
             try (ResultSet rs = ps.executeQuery()) {
                 // iteracion: lee historial fila por fila y empaqueta en objetos detallepedido.
                 while(rs.next()) {
                     detallePedido dp = new detallePedido();
                     dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                     dp.setFechaPedido(rs.getString("fecha"));
                     
                     String metodoPago = rs.getString("descripcion_pago");
                     // condicional en linea (operador ternario): si el metodo no es nulo, lo envuelve en parentesis.
                     String estado = rs.getString("estado_pedido") + (metodoPago != null ? " (" + metodoPago + ")" : "");
                     dp.setEstadoPedido(estado);
                     dp.setMotivoCancelacion(rs.getString("motivo_cancelacion"));
                     
                     dp.setNombreProducto(rs.getString("nombre_producto"));
                     dp.setCantidad(rs.getInt("cantidad"));
                     dp.setPrecioUnitario(rs.getDouble("precio_unitario"));
                     dp.setSubtotal(rs.getDouble("subtotal"));
                     lista.add(dp);
                 }
             }
        } catch (SQLException e) { System.out.println("Error al listar pedidos del cliente: " + e.getMessage()); }
        return lista;
    }

    /**
     * 4. listar todos los pedidos (panel de administracion)
     * vista global que no aplica ningun filtro restrictivo. 
     * permite al administrador auditar la tienda completa, ver hacia donde van 
     * todos los paquetes y confirmar todos los montos pagados.
     * 
     * @return arraylist<detallepedido>: arreglo completo con todos los datos financieros y logisticos.
     */
    public ArrayList<detallePedido> listarTodosLosPedidos() {
        ArrayList<detallePedido> lista = new ArrayList<>();
        // consulta global para administracion (8 tablas).
        // extrae la trazabilidad completa: comprador, producto, montos, destino y pagos.
        // los left joins en perfil y direccion evitan que la lista falle si el perfil esta incompleto.
        // ordena por el id del pedido de forma descendente para ver lo mas reciente.
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, p.motivo_cancelacion, u.nombre AS nombre_cliente, " +
                     "d.direccion, d.direccion_detallada, c.telefono_secundario, c.referencia_ubicacion, t.numero_telefonico, prod.nombre_producto, dp.cantidad, dp.precio_unitario, dp.subtotal, mp.descripcion_pago " +
                     "FROM pedido p " +
                     "INNER JOIN usuario u ON p.id_cliente_fk = u.id_usuario_pk " +
                     "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                     "LEFT JOIN direccion d ON u.id_usuario_pk = d.id_usuario_fk AND d.direccion_primario = 1 " +
                     "LEFT JOIN telefono t ON u.id_usuario_pk = t.id_usuario_fk " +
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                     "LEFT JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                     "LEFT JOIN metodo_pago mp ON pg.id_metodo_pago_fk = mp.id_metodo_pago_pk " +
                     "ORDER BY p.id_pedido_pk DESC";
                     // nota: esta consulta no tiene where porque es global para el administrador
                     
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
             // iteracion: recorre absolutamente todas las filas de ventas generadas.
             while(rs.next()) {
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
                 
                 // armamos el texto completo con la direccion, telefono y referencia para el administrador
                 String dir = rs.getString("direccion");
                 String dirDetalle = rs.getString("direccion_detallada");
                 // condicional: asegura que no se impriman parentesis vacios si no hay detalle logistico.
                 if (dirDetalle != null && !dirDetalle.isEmpty()) dir += " (" + dirDetalle + ")";
                 String telPrincipal = rs.getString("numero_telefonico");
                 String telSec = rs.getString("telefono_secundario");
                 
                 String telefonos = (telPrincipal != null && !telPrincipal.isEmpty() ? telPrincipal : "") + 
                                    (telSec != null && !telSec.isEmpty() ? " / " + telSec : "");
                 String detallesContacto = (dir != null) 
                     ? dir + " | Tel: " + (telefonos.isEmpty() ? "sin numero" : telefonos) + 
                       (rs.getString("referencia_ubicacion") != null && !rs.getString("referencia_ubicacion").isEmpty() ? " | Ref: " + rs.getString("referencia_ubicacion") : "") 
                     : "sin direccion guardada";
                     
                 // usamos nombrecliente como portador de los datos si no has modificado la clase modelo aun
                 dp.setNombreCliente(rs.getString("nombre_cliente") + " - " + detallesContacto);
                 
                 lista.add(dp);
             }
        } catch (SQLException e) { System.out.println("Error al listar todos los pedidos: " + e.getMessage()); }
        return lista;
    }

    /**
     * 5. actualizar estado del pedido (logistica)
     * permite a los administradores y proveedores ir moviendo la factura
     * a lo largo de su ciclo logistico (pendiente -> preparando -> en camino -> entregado).
     * 
     * nota de seguridad:
     * si el paquete se marca como "cancelado", este metodo automaticamente devolvera
     * las unidades al mostrador (tabla producto), para no perder dinero ni inventario.
     * 
     * @param idPedido int: identificador de la factura.
     * @param nuevoEstado string: fase logistica.
     * @param motivo string: explicacion de la cancelacion.
     * @param idUsuarioAccion int: id del usuario que opera.
     * @return boolean: true si pudo actualizarse la tabla.
     */
    public boolean actualizarEstadoPedido(int idPedido, String nuevoEstado, String motivo, int idUsuarioAccion) {
        // sql para actualizar la fase logistica del pedido.
        // guarda el nuevo estado, el motivo de cancelacion y el id de quien opera.
        // permite auditar quien cancelo o entrego cada factura.
        String sql = "UPDATE pedido SET estado_pedido = ?, motivo_cancelacion = ?, cancelado_por_id_fk = ? WHERE id_pedido_pk = ?";
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el autocommit para proteger la integridad del stock
            con.setAutoCommit(false);
            
            // 1. averiguamos el estado actual antes de cambiarlo para evitar devolver stock duplicado
            String estadoAnterior = "";
            // consulta el estado previo para saber si hay que revertir inventario.
            String sqlEstadoAnterior = "SELECT estado_pedido FROM pedido WHERE id_pedido_pk = ?";
            // abrimos una consulta rapida para validar el estado previo
            try (PreparedStatement psVer = con.prepareStatement(sqlEstadoAnterior)) {
                psVer.setInt(1, idPedido);
                try (ResultSet rs = psVer.executeQuery()) {
                    // condicional: verifica si el select devolvio resultados para atrapar el estado previo.
                    if (rs.next()) estadoAnterior = rs.getString("estado_pedido");
                }
            }
            
            // 2. aplicamos el nuevo estado de envio/cancelacion
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // inyectamos los datos en la consulta incluyendo los campos de auditoria
                ps.setString(1, nuevoEstado);
                ps.setString(2, motivo);
                // validacion: si no hay un id de usuario valido guardamos nulo en la base de datos
                if (idUsuarioAccion > 0) ps.setInt(3, idUsuarioAccion); 
                else ps.setNull(3, java.sql.Types.INTEGER);
                ps.setInt(4, idPedido);
                int afectadas = ps.executeUpdate();
                
                // condicional: evalua si el update realmente modifico la fila (si > 0).
                if (afectadas > 0) {
                    // verificamos si el nuevo estado es una variante de cancelacion para retornar inventario
                    if (nuevoEstado.startsWith("Cancelado") && !estadoAnterior.startsWith("Cancelado")) {
                        // sql para identificar que productos y cantidades devolver.
                        String sqlDetalles = "SELECT id_producto_fk, cantidad FROM detalle_pedido WHERE id_pedido_fk = ?";
                        // sql para reponer las unidades al inventario principal.
                        String sqlDevolverStock = "UPDATE producto SET stock = stock + ? WHERE id_producto_pk = ?";
                        
                        try (PreparedStatement psDetalles = con.prepareStatement(sqlDetalles);
                             PreparedStatement psStock = con.prepareStatement(sqlDevolverStock)) {
                             
                            psDetalles.setInt(1, idPedido);
                            try (ResultSet rsDetalles = psDetalles.executeQuery()) {
                                // iteracion: recorre que productos especificos tenia ese pedido cancelado 
                                // para devolver la cantidad exacta a su tabla de inventario.
                                while (rsDetalles.next()) {
                                    psStock.setInt(1, rsDetalles.getInt("cantidad"));
                                    psStock.setInt(2, rsDetalles.getInt("id_producto_fk"));
                                    psStock.addBatch();
                                }
                                psStock.executeBatch();
                            }
                        }
                    }
                    con.commit();
                    return true;
                }
            }
            con.rollback();
            return false;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {
                System.err.println("Fallo critico al intentar hacer rollback en actualizacion de estado: " + ex.getMessage());
            }
            System.out.println("Error al actualizar el estado del pedido: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {
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
     * @return double[]: arreglo con [total_ventas, cantidad_pedidos, comisiones_generadas]
     */
    public double[] obtenerEstadisticasVentas(int idProveedor) {
        double[] stats = new double[3];
        String sql;
        
        if (idProveedor == 0) {
            // estadisticas globales para administrador.
            // sum(total_pagar): volumen de ventas bruto.
            // count: numero de transacciones exitosas.
            // sum(* 0.05): calculo de ingresos por comision para la plataforma.
            sql = "SELECT SUM(total_pagar) as total, COUNT(id_pedido_pk) as conteo, SUM(total_pagar * 0.05) as comision " +
                  "FROM pedido WHERE estado_pedido = 'Entregado'";
        } else {
            // estadisticas privadas para el proveedor.
            // sum(dp.subtotal): suma solo el dinero de sus propios productos.
            // inner join con proveedor_producto: garantiza el aislamiento de datos.
            // distinct: evita duplicar el conteo de pedidos con multiples items del mismo dueno.
            sql = "SELECT SUM(dp.subtotal) as total, COUNT(DISTINCT p.id_pedido_pk) as conteo, SUM(dp.subtotal * 0.05) as comision " +
                  "FROM pedido p " +
                  "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                  "INNER JOIN proveedor_producto pp ON dp.id_producto_fk = pp.id_producto_fk " +
                  "WHERE pp.id_proveedor_fk = ? AND p.estado_pedido = 'Entregado'";
        }

        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            // inyectamos el parametro solo si no es una consulta de administrador
            if (idProveedor > 0) ps.setInt(1, idProveedor);
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
     * 7. generar datos para factura
     * extrae la informacion necesaria para armar un pdf.
     * cruza datos de cliente, pago y detalles de productos.
     */
    public ResultSet obtenerDatosFactura(int idPedido) throws SQLException {
        Connection con = db.conectar();
        // consulta para reporte de facturacion (pdf).
        // une usuario, correo, direccion, detalle, producto y pago.
        // extrae toda la informacion necesaria para reconstruir el recibo fisico.
        String sql = "SELECT p.id_pedido_pk, p.fecha, u.nombre, u.apellido, c.correo, " +
                     "d.direccion, dp.cantidad, prod.nombre_producto, dp.precio_unitario, " +
                     "dp.subtotal, pg.monto_total, mp.descripcion_pago " +
                     "FROM pedido p " +
                     "INNER JOIN usuario u ON p.id_cliente_fk = u.id_usuario_pk " +
                     "INNER JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk " +
                     "INNER JOIN direccion d ON u.id_usuario_pk = d.id_usuario_fk AND d.direccion_primario = 1 " +
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                     "INNER JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                     "INNER JOIN metodo_pago mp ON pg.id_metodo_pago_fk = mp.id_metodo_pago_pk " +
                     "WHERE p.id_pedido_pk = ?";
        // ejecutamos la extraccion de datos para el reporte pdf
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idPedido);
        return ps.executeQuery();
        // nota: el llamador debe cerrar la conexion al terminar de procesar el resultset
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
     * extrae la sumatoria de subtotales de productos que pertenecen al proveedor logueado.
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
}