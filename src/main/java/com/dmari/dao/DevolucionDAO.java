package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.Devolucion;

import java.sql.*;
import java.util.ArrayList;

public class DevolucionDAO {
    private databaseHelper db = new databaseHelper();

    public ArrayList<Devolucion> listarDevoluciones() {
        ArrayList<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT d.*, u.nombre as nombre_cliente FROM devolucion d " +
                     "INNER JOIN usuario u ON d.id_cliente_fk = u.id_usuario_pk " +
                     "ORDER BY d.fecha_solicitud DESC";
        
        try (Connection con = db.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                Devolucion dev = new Devolucion();
                dev.setIdDevolucionPk(rs.getInt("id_devolucion_pk"));
                dev.setIdPedidoFk(rs.getInt("id_pedido_fk"));
                dev.setIdClienteFk(rs.getInt("id_cliente_fk"));
                dev.setMotivo(rs.getString("motivo"));
                dev.setEstadoDevolucion(rs.getString("estado_devolucion"));
                dev.setFechaSolicitud(rs.getTimestamp("fecha_solicitud"));
                dev.setFechaResolucion(rs.getTimestamp("fecha_resolucion"));
                dev.setNombreCliente(rs.getString("nombre_cliente"));
                lista.add(dev);
            }
        } catch (SQLException e) {
            System.err.println("Error listarDevoluciones: " + e.getMessage());
        }
        return lista;
    }

    public ArrayList<Devolucion> listarDevolucionesPorCliente(int idCliente) {
        ArrayList<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT * FROM devolucion WHERE id_cliente_fk = ? ORDER BY fecha_solicitud DESC";
        
        try (Connection con = db.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setInt(1, idCliente);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Devolucion dev = new Devolucion();
                    dev.setIdDevolucionPk(rs.getInt("id_devolucion_pk"));
                    dev.setIdPedidoFk(rs.getInt("id_pedido_fk"));
                    dev.setIdClienteFk(rs.getInt("id_cliente_fk"));
                    dev.setMotivo(rs.getString("motivo"));
                    dev.setEstadoDevolucion(rs.getString("estado_devolucion"));
                    dev.setFechaSolicitud(rs.getTimestamp("fecha_solicitud"));
                    dev.setFechaResolucion(rs.getTimestamp("fecha_resolucion"));
                    lista.add(dev);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listarDevolucionesPorCliente: " + e.getMessage());
        }
        return lista;
    }

    public boolean solicitarDevolucion(int idPedido, int idCliente, String motivo) {
        Connection con = null;
        try {
            con = db.conectar();
            con.setAutoCommit(false);
            
            // 1. Insertar devolucion
            String sqlInsert = "INSERT INTO devolucion (id_pedido_fk, id_cliente_fk, motivo) VALUES (?, ?, ?)";
            try (PreparedStatement pstInsert = con.prepareStatement(sqlInsert)) {
                pstInsert.setInt(1, idPedido);
                pstInsert.setInt(2, idCliente);
                pstInsert.setString(3, motivo);
                pstInsert.executeUpdate();
            }
            
            // 2. Actualizar estado del pedido a Reembolso_Solicitado
            String sqlUpdatePedido = "UPDATE pedido SET estado_pedido = 'Reembolso_Solicitado' WHERE id_pedido_pk = ?";
            try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdatePedido)) {
                pstUpdate.setInt(1, idPedido);
                pstUpdate.executeUpdate();
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            db.cerrar(con);
        }
    }

    public boolean aprobarDevolucion(int idDevolucion, int idPedido) {
        Connection con = null;
        try {
            con = db.conectar();
            con.setAutoCommit(false);
            
            // 1. Marcar devolucion como Aprobada
            String sqlUpdateDev = "UPDATE devolucion SET estado_devolucion = 'Aprobada', fecha_resolucion = CURRENT_TIMESTAMP WHERE id_devolucion_pk = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdateDev)) {
                pst.setInt(1, idDevolucion);
                pst.executeUpdate();
            }
            
            // 2. Actualizar pedido a Devuelto
            String sqlUpdatePedido = "UPDATE pedido SET estado_pedido = 'Devuelto' WHERE id_pedido_pk = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdatePedido)) {
                pst.setInt(1, idPedido);
                pst.executeUpdate();
            }
            
            // 3. Devolver stock
            String sqlDetalles = "SELECT id_producto_fk, cantidad FROM detalle_pedido WHERE id_pedido_fk = ?";
            String sqlDevolverStock = "UPDATE producto SET stock = stock + ? WHERE id_producto_pk = ?";
            
            try (PreparedStatement pstDetalles = con.prepareStatement(sqlDetalles);
                 PreparedStatement pstStock = con.prepareStatement(sqlDevolverStock)) {
                 
                pstDetalles.setInt(1, idPedido);
                try (ResultSet rsDetalles = pstDetalles.executeQuery()) {
                    while (rsDetalles.next()) {
                        pstStock.setInt(1, rsDetalles.getInt("cantidad"));
                        pstStock.setInt(2, rsDetalles.getInt("id_producto_fk"));
                        pstStock.addBatch();
                    }
                    pstStock.executeBatch();
                }
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            db.cerrar(con);
        }
    }

    public boolean rechazarDevolucion(int idDevolucion, int idPedido) {
        Connection con = null;
        try {
            con = db.conectar();
            con.setAutoCommit(false);
            
            String sqlUpdateDev = "UPDATE devolucion SET estado_devolucion = 'Rechazada', fecha_resolucion = CURRENT_TIMESTAMP WHERE id_devolucion_pk = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdateDev)) {
                pst.setInt(1, idDevolucion);
                pst.executeUpdate();
            }
            
            // Devolvemos el estado a Entregado porque la devolución no fue aceptada
            String sqlUpdatePedido = "UPDATE pedido SET estado_pedido = 'Entregado' WHERE id_pedido_pk = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdatePedido)) {
                pst.setInt(1, idPedido);
                pst.executeUpdate();
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            db.cerrar(con);
        }
    }
}
