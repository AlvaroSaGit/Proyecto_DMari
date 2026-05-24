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
    public boolean registrarPedido(int idCliente, double totalPagar, ArrayList<detallePedido> carrito) {
        // preparamos la instruccion sql para insertar la cabecera del pedido (el recibo principal)
        String sqlPedido = "INSERT INTO pedido (id_cliente_fk, total_pagar, estado_pedido) VALUES (?, ?, 'Pendiente')";
        // preparamos la instruccion sql para insertar cada producto comprado en el detalle del pedido
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        
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
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    for (detallePedido item : carrito) {
                        psDetalle.setInt(1, idPedidoGenerado);
                        psDetalle.setInt(2, item.getIdProductoFk());
                        psDetalle.setInt(3, item.getCantidad());
                        psDetalle.setDouble(4, item.getPrecioUnitario());
                        psDetalle.setDouble(5, item.getSubtotal());
                        
                        // addbatch encola las instrucciones para ejecutarlas todas de golpe (mejor rendimiento)
                        psDetalle.addBatch(); 
                    }
                    psDetalle.executeBatch(); // disparamos todas las inserciones del carrito
                }
                
                // paso 3: todo salio bien, confirmamos la transaccion
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
                     // traemos la direccion y telefono del cliente para que el proveedor sepa a donde enviar
                     "c.direccion_envio, c.telefono_secundario, prod.nombre_producto, dp.cantidad, dp.subtotal " +
                     // tabla principal de la consulta: el pedido maestro
                     "FROM pedido p " +
                     // inner join: el pedido debe tener un usuario real asociado si o si
                     "INNER JOIN usuario u ON p.id_cliente_fk = u.id_usuario_pk " +
                     // left join: cruzamos con el perfil del cliente para traer su direccion fisica.
                     // usamos left join porque si un usuario se acaba de registrar y compra, pero su perfil esta incompleto, queremos que el pedido siga saliendo en pantalla (con direccion en null) para no perder la venta.
                     "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                     // inner join: estricto para ver que productos exactos compro en esa orden
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     "INNER JOIN producto prod ON dp.id_producto_fk = prod.id_producto_pk " +
                     // inner join: estrictos para enlazar el producto con su dueño
                     "INNER JOIN proveedor_producto pp ON prod.id_producto_pk = pp.id_producto_fk " +
                     "INNER JOIN proveedor pr ON pp.id_proveedor_fk = pr.id_proveedor_pk " +
                     // filtramos para que solo salgan los productos del proveedor logueado, ordenados del mas reciente al mas antiguo
                     "WHERE pr.id_datos_proveedor_fk = ? ORDER BY p.fecha DESC";
                     
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setInt(1, idUsuarioProveedor);
             try (ResultSet rs = ps.executeQuery()) {
                 while(rs.next()) {
                     detallePedido dp = new detallePedido();
                     dp.setIdPedidoFk(rs.getInt("id_pedido_pk"));
                     dp.setFechaPedido(rs.getString("fecha"));
                     dp.setEstadoPedido(rs.getString("estado_pedido"));
                     dp.setNombreCliente(rs.getString("nombre_cliente"));
                     
                     // IMPORTANTE: Recuerda crear estos dos atributos en tu modelo `detallePedido.java`
                     // dp.setDireccionEnvio(rs.getString("direccion_envio"));
                     // dp.setTelefonoSecundario(rs.getString("telefono_secundario"));
                     
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
}