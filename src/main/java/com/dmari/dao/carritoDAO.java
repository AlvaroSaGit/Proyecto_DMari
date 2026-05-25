/*
    objetivo de este archivo:
    gestionar el carrito de compras persistente en la base de datos.
    usa las tablas 'carrito' y 'detalle_carrito' para que el usuario no pierda 
    sus productos al cambiar de dispositivo.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.detalleCarrito;

public class carritoDAO {
    
    databaseHelper db = new databaseHelper();

    /*
        extrae los items guardados en el carrito directamente en formato json.
        une las tablas carrito, detalle_carrito y producto para traer nombres y precios actualizados.
    */
    public String obtenerCarritoJSON(int idCliente) {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT dc.id_producto_fk, dc.cantidad, p.nombre_producto, p.precio, p.stock " +
                     "FROM carrito c " +
                     "INNER JOIN detalle_carrito dc ON c.id_carrito_pk = dc.id_carrito_fk " +
                     "INNER JOIN producto p ON dc.id_producto_fk = p.id_producto_pk " +
                     "WHERE c.id_cliente_fk = ?";
                     
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                boolean primero = true;
                while(rs.next()) {
                    // concatenacion manual de json para evitar librerias externas pesadas
                    if (!primero) json.append(",");
                    json.append("{")
                        .append("\"id\":").append(rs.getInt("id_producto_fk")).append(",")
                        .append("\"nombre\":\"").append(rs.getString("nombre_producto")).append("\",")
                        .append("\"precio\":").append(rs.getDouble("precio")).append(",")
                        .append("\"cantidad\":").append(rs.getInt("cantidad")).append(",")
                        .append("\"stock\":").append(rs.getInt("stock"))
                        .append("}");
                    primero = false;
                }
            }
        } catch (SQLException e) { System.out.println("error al obtener carrito bd: " + e.getMessage()); }
        json.append("]");
        return json.toString();
    }

    /*
        metodo transaccional para guardar o sobreescribir el carrito de un cliente.
        garantiza que si hay un error a la mitad del proceso, no se borre el carrito viejo por accidente.
    */
    public boolean sincronizarCarrito(int idCliente, ArrayList<detalleCarrito> items) {
        // preparacion de las 4 ordenes sql necesarias para este complejo proceso
        String sqlVerificar = "SELECT id_carrito_pk FROM carrito WHERE id_cliente_fk = ?";
        String sqlCrear = "INSERT INTO carrito (id_cliente_fk) VALUES (?)";
        String sqlLimpiar = "DELETE FROM detalle_carrito WHERE id_carrito_fk = ?";
        String sqlInsertarItem = "INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad) VALUES (?, ?, ?)";
        
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el guardado automatico para iniciar la transaccion
            con.setAutoCommit(false);
            int idCarrito = 0;
            
            // 1. buscar si el cliente ya tiene una canasta (carrito) asignada
            try (PreparedStatement psVer = con.prepareStatement(sqlVerificar)) {
                psVer.setInt(1, idCliente);
                try (ResultSet rs = psVer.executeQuery()) { if (rs.next()) idCarrito = rs.getInt("id_carrito_pk"); }
            }
            // 2. si no tiene, se la creamos
            if (idCarrito == 0) {
                try (PreparedStatement psCrear = con.prepareStatement(sqlCrear, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psCrear.setInt(1, idCliente);
                    psCrear.executeUpdate();
                    // atrapamos el numero de canasta que mysql le acaba de asignar
                    try (ResultSet rsKeys = psCrear.getGeneratedKeys()) { if (rsKeys.next()) idCarrito = rsKeys.getInt(1); }
                }
            }
            // 3. vaciamos la canasta e insertamos los items mas recientes
            if (idCarrito > 0) {
                try (PreparedStatement psLimpiar = con.prepareStatement(sqlLimpiar)) {
                    psLimpiar.setInt(1, idCarrito);
                    psLimpiar.executeUpdate();
                }
                // 4. si el usuario envio productos nuevos, los guardamos
                if (items != null && !items.isEmpty()) {
                    try (PreparedStatement psInsert = con.prepareStatement(sqlInsertarItem)) {
                        for (detalleCarrito item : items) {
                            psInsert.setInt(1, idCarrito);
                            psInsert.setInt(2, item.getIdProductoFk());
                            psInsert.setInt(3, item.getCantidad());
                            // addbatch agrupa los inserts para mandarlos todos juntos al mismo tiempo (muy rapido)
                            psInsert.addBatch();
                        }
                        // disparamos el bloque de inserts a mysql
                        psInsert.executeBatch();
                    }
                }
            }
            // si el codigo llega aqui sin explotar, le decimos a mysql que guarde los cambios fijos
            con.commit();
            return true;
        } catch (SQLException e) {
            // boton de panico: si falla, deshacemos cualquier borrado o insercion a medias
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}