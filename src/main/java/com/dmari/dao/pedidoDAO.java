package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.detallePedido;

public class pedidoDAO {
    
    databaseHelper db = new databaseHelper();

    /*
        metodo transaccional para guardar la compra completa.
        guarda primero en la tabla 'pedido' para obtener el id, 
        y luego guarda todos los productos en 'detalle_pedido'.
    */
    public boolean registrarPedido(int idCliente, double totalPagar, ArrayList<detallePedido> carrito, int idMetodoPago, String numeroCuenta) {
        // preparamos la instruccion sql para insertar la cabecera del pedido (el recibo principal)
        String sqlPedido = "INSERT INTO pedido (id_cliente_fk, total_pagar, estado_pedido) VALUES (?, ?, 'Pendiente')";
        // preparamos la instruccion sql para insertar cada producto comprado en el detalle del pedido
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        // instruccion para restar permanentemente el stock del producto de inmediato al comprar
        // el tercer parametro asegura que mysql jamas permita que el inventario quede en negativo
        String sqlDescontarStock = "UPDATE producto SET stock = stock - ? WHERE id_producto_pk = ? AND stock >= ?";
        // instruccion para guardar el comprobante financiero en la tabla pago
        String sqlPago = "INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, numero_cuenta_ahorro, comision_dmari, monto_total, estado_activo, estado_pago) VALUES (?, ?, ?, ?, ?, 1, 'Aprobado')";
        
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el autocommit para que mysql no guarde nada hasta que todo el carrito se procese bien
            con.setAutoCommit(false);
            
            int idPedidoGenerado = 0;
            
            // paso 1: insertamos el pedido maestro
            try (PreparedStatement psPedido = con.prepareStatement(sqlPedido, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psPedido.setInt(1, idCliente);
                psPedido.setDouble(2, totalPagar);
                psPedido.executeUpdate();
                
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    if (rs.next()) idPedidoGenerado = rs.getInt(1);
                }
            }
            
            // paso 2: si mysql nos dio el id del pedido maestro, guardamos los detalles
            if (idPedidoGenerado > 0) {
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                     PreparedStatement psStock = con.prepareStatement(sqlDescontarStock)) {
                    for (detallePedido item : carrito) {
                        psDetalle.setInt(1, idPedidoGenerado);
                        psDetalle.setInt(2, item.getIdProductoFk());
                        psDetalle.setInt(3, item.getCantidad());
                        psDetalle.setDouble(4, item.getPrecioUnitario());
                        psDetalle.setDouble(5, item.getSubtotal());
                        
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
                    for (int res : resultadosStock) {
                        if (res == 0) {
                            // si el resultado es 0, significa que no habia stock suficiente en la bd.
                            // mysql bloqueo la venta para evitar numeros negativos. abortamos la compra.
                            con.rollback();
                            return false; 
                        }
                    }
                    
                    // paso 3: registramos el comprobante de pago asociado a la factura
                    try (PreparedStatement psPago = con.prepareStatement(sqlPago)) {
                        psPago.setInt(1, idPedidoGenerado);
                        psPago.setInt(2, idMetodoPago);
                        psPago.setString(3, numeroCuenta);
                        // simulamos que dmari retiene el 5% de comision por vender los productos de los proveedores
                        double comision = totalPagar * 0.05;
                        double totalProveedor = totalPagar - comision;
                        psPago.setDouble(4, comision);
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
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("error al registrar el pedido transaccional: " + e.getMessage());
            return false;
        } finally {
            // restauramos el modo normal de mysql para futuras conexiones
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    /*
        metodo que responde a tu logica: permite que el proveedor vea los pedidos 
        que incluyen sus productos, mostrando el nombre del cliente y el estado.
    */
    public ArrayList<detallePedido> listarPedidosPorProveedor(int idUsuarioProveedor) {
        ArrayList<detallePedido> lista = new ArrayList<>();
        
        // preparamos la consulta uniendo 6 tablas para revelar el camino de datos desde el pedido hasta el proveedor
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, u.nombre AS nombre_cliente, " +
                     // traemos la direccion y telefonos (principal y secundario) del cliente para que el proveedor sepa a donde enviar
                     "c.direccion_envio, c.telefono_secundario, c.referencia_ubicacion, t.numero_telefonico, prod.nombre_producto, dp.cantidad, dp.subtotal " +
                     // tabla principal de la consulta: el pedido maestro
                     "FROM pedido p " +
                     // inner join: el pedido debe tener un usuario real asociado si o si
                     "INNER JOIN usuario u ON p.id_cliente_fk = u.id_usuario_pk " +
                     // left join: cruzamos con el perfil del cliente para traer su direccion fisica.
                     "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
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
                 while(rs.next()) {
                     detallePedido dp = new detallePedido();
                     dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                     dp.setFechaPedido(rs.getString("fecha"));
                     dp.setEstadoPedido(rs.getString("estado_pedido"));
                     
                     // armamos un texto completo con todos los datos de envio para inyectarlo en el nombre
                     String dir = rs.getString("direccion_envio");
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
        } catch (SQLException e) { System.out.println("error al listar pedidos del proveedor: " + e.getMessage()); }
        return lista;
    }

    /*
        metodo para obtener el historial de compras de un cliente especifico.
    */
    public ArrayList<detallePedido> listarPedidosPorCliente(int idCliente) {
        ArrayList<detallePedido> lista = new ArrayList<>();
        // preparamos la consulta sql para traer el historial completo de un cliente
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, " +
                     // traemos el nombre del producto y sus datos monetarios
                     "prod.nombre_producto, dp.cantidad, dp.precio_unitario, dp.subtotal " +
                     // comenzamos desde la tabla maestra de pedidos
                     "FROM pedido p " +
                     // join 1: cruzamos con el detalle para sacar los productos de cada factura
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     // join 2: cruzamos con producto para saber el nombre de lo que compro
                     "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                     // filtramos para traer solo los pedidos del cliente que esta consultando, del mas nuevo al mas viejo
                     "WHERE p.id_cliente_fk = ? ORDER BY p.fecha DESC";
                     
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setInt(1, idCliente);
             try (ResultSet rs = ps.executeQuery()) {
                 while(rs.next()) {
                     detallePedido dp = new detallePedido();
                     dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                     dp.setFechaPedido(rs.getString("fecha"));
                     dp.setEstadoPedido(rs.getString("estado_pedido"));
                     dp.setNombreProducto(rs.getString("nombre_producto"));
                     dp.setCantidad(rs.getInt("cantidad"));
                     dp.setPrecioUnitario(rs.getDouble("precio_unitario"));
                     dp.setSubtotal(rs.getDouble("subtotal"));
                     lista.add(dp);
                 }
             }
        } catch (SQLException e) { System.out.println("error al listar pedidos del cliente: " + e.getMessage()); }
        return lista;
    }

    /*
        metodo para obtener el historial completo de la tienda (para el administrador).
    */
    public ArrayList<detallePedido> listarTodosLosPedidos() {
        ArrayList<detallePedido> lista = new ArrayList<>();
        String sql = "SELECT p.id_pedido_pk, p.fecha, p.estado_pedido, u.nombre AS nombre_cliente, " +
                     "c.direccion_envio, c.telefono_secundario, c.referencia_ubicacion, t.numero_telefonico, prod.nombre_producto, dp.cantidad, dp.precio_unitario, dp.subtotal " +
                     "FROM pedido p " +
                     "INNER JOIN usuario u ON p.id_cliente_fk = u.id_usuario_pk " +
                     "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                     "LEFT JOIN telefono t ON u.id_usuario_pk = t.id_usuario_fk " +
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                     "ORDER BY p.id_pedido_pk DESC";
                     
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
             while(rs.next()) {
                 detallePedido dp = new detallePedido();
                 dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                 dp.setFechaPedido(rs.getString("fecha"));
                 dp.setEstadoPedido(rs.getString("estado_pedido"));
                 dp.setNombreCliente(rs.getString("nombre_cliente"));
                 dp.setNombreProducto(rs.getString("nombre_producto"));
                 dp.setCantidad(rs.getInt("cantidad"));
                 dp.setPrecioUnitario(rs.getDouble("precio_unitario"));
                 dp.setSubtotal(rs.getDouble("subtotal"));
                 
                 // armamos el texto completo con la direccion, telefono y referencia para el administrador
                 String dir = rs.getString("direccion_envio");
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
        } catch (SQLException e) { System.out.println("error al listar todos los pedidos: " + e.getMessage()); }
        return lista;
    }

    /*
        metodo para que el administrador o proveedor cambien el estado del paquete (ej: 'preparando', 'entregado')
    */
    public boolean actualizarEstadoPedido(int idPedido, String nuevoEstado) {
        String sql = "UPDATE pedido SET estado_pedido = ? WHERE id_pedido_pk = ?";
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el autocommit para proteger la logica de devolucion de inventario
            con.setAutoCommit(false);
            
            // 1. Averiguamos el estado actual antes de cambiarlo para evitar devolver stock duplicado
            String estadoAnterior = "";
            String sqlEstadoAnterior = "SELECT estado_pedido FROM pedido WHERE id_pedido_pk = ?";
            try (PreparedStatement psVer = con.prepareStatement(sqlEstadoAnterior)) {
                psVer.setInt(1, idPedido);
                try (ResultSet rs = psVer.executeQuery()) {
                    if (rs.next()) estadoAnterior = rs.getString("estado_pedido");
                }
            }
            
            // 2. Aplicamos el nuevo estado de envio/cancelacion
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idPedido);
                int afectadas = ps.executeUpdate();
                
                if (afectadas > 0) {
                    // 3. LOGICA DE STOCK: Si el pedido se marca como "Cancelado" (y antes no lo estaba), 
                    // regresamos los productos fisicos a los mostradores de la tienda.
                    if ("Cancelado".equalsIgnoreCase(nuevoEstado) && !"Cancelado".equalsIgnoreCase(estadoAnterior)) {
                        String sqlDetalles = "SELECT id_producto_fk, cantidad FROM detalle_pedido WHERE id_pedido_fk = ?";
                        String sqlDevolverStock = "UPDATE producto SET stock = stock + ? WHERE id_producto_pk = ?";
                        
                        try (PreparedStatement psDetalles = con.prepareStatement(sqlDetalles);
                             PreparedStatement psStock = con.prepareStatement(sqlDevolverStock)) {
                             
                            psDetalles.setInt(1, idPedido);
                            try (ResultSet rsDetalles = psDetalles.executeQuery()) {
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
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("error al actualizar el estado del pedido: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}