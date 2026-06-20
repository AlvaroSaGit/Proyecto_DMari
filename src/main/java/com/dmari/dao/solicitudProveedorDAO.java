package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class solicitudProveedorDAO {
    private databaseHelper db = new databaseHelper();

    /**
     * Crea una nueva solicitud para que un usuario se convierta en proveedor.
     * @param idUsuario El ID del usuario que realiza la solicitud.
     * @param nit El NIT de la empresa.
     * @param marca El nombre de la marca.
     * @param cuenta El número de cuenta bancaria.
     * @param banco El nombre del banco.
     * @param tipoCuenta El tipo de cuenta (Ahorros/Corriente).
     * @return true si la solicitud se creó con éxito.
     */
    public boolean crearSolicitud(int idUsuario, String nit, String marca, String cuenta, String banco, String tipoCuenta) {
        String sql = "INSERT INTO solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) VALUES (?, ?, ?, ?, ?, ?, 'pendiente')";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, nit);
            ps.setString(3, marca);
            ps.setString(4, cuenta);
            ps.setString(5, banco);
            ps.setString(6, tipoCuenta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear solicitud de proveedor: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<java.util.Map<String, String>> listarSolicitudes() {
        java.util.List<java.util.Map<String, String>> lista = new java.util.ArrayList<>();
        String sql = "SELECT sp.*, u.nombre, u.correo FROM solicitud_proveedor sp " +
                     "INNER JOIN usuario u ON sp.id_usuario_fk = u.id_usuario_pk " +
                     "LEFT JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk " +
                     "ORDER BY sp.id_solicitud_pk DESC";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("id", String.valueOf(rs.getInt("id_solicitud_pk")));
                map.put("usuarioNombre", rs.getString("nombre"));
                map.put("usuarioCorreo", rs.getString("correo"));
                map.put("nit", rs.getString("nit_empresa"));
                map.put("marca", rs.getString("nombre_marca"));
                map.put("cuenta", rs.getString("cuenta_bancaria"));
                map.put("banco", rs.getString("banco_nombre"));
                map.put("tipoCuenta", rs.getString("tipo_cuenta"));
                map.put("estado", rs.getString("estado_solicitud"));
                map.put("idUsuarioFk", String.valueOf(rs.getInt("id_usuario_fk")));
                lista.add(map);
            }
        } catch (SQLException e) {
            System.err.println("Error listar solicitudes proveedor: " + e.getMessage());
        }
        return lista;
    }

    public boolean aprobarSolicitud(int idSolicitud) {
        // Obtenemos los datos de la solicitud
        String sqlSelect = "SELECT * FROM solicitud_proveedor WHERE id_solicitud_pk = ?";
        // Transaccion: Aprobar solicitud, Activar usuario, Insertar en proveedor
        String sqlUpdateSol = "UPDATE solicitud_proveedor SET estado_solicitud = 'aprobada' WHERE id_solicitud_pk = ?";
        String sqlUpdateUsr = "UPDATE usuario SET estado_cuenta = 1 WHERE id_usuario_pk = ?";
        String sqlInsertProv = "INSERT INTO proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection con = db.conectar();
        if(con == null) return false;
        
        try {
            con.setAutoCommit(false);
            
            int idUsuario = 0;
            String nit = "", marca = "", cuenta = "", banco = "", tipo = "";
            try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
                psSel.setInt(1, idSolicitud);
                java.sql.ResultSet rs = psSel.executeQuery();
                if(rs.next()) {
                    idUsuario = rs.getInt("id_usuario_fk");
                    nit = rs.getString("nit_empresa");
                    marca = rs.getString("nombre_marca");
                    cuenta = rs.getString("cuenta_bancaria");
                    banco = rs.getString("banco_nombre");
                    tipo = rs.getString("tipo_cuenta");
                } else {
                    return false;
                }
            }
            
            try (PreparedStatement psSol = con.prepareStatement(sqlUpdateSol)) {
                psSol.setInt(1, idSolicitud);
                psSol.executeUpdate();
            }
            
            try (PreparedStatement psUsr = con.prepareStatement(sqlUpdateUsr)) {
                psUsr.setInt(1, idUsuario);
                psUsr.executeUpdate();
            }
            
            try (PreparedStatement psProv = con.prepareStatement(sqlInsertProv)) {
                psProv.setInt(1, idUsuario);
                psProv.setString(2, nit);
                psProv.setString(3, marca);
                psProv.setString(4, cuenta);
                psProv.setString(5, banco);
                psProv.setString(6, tipo);
                psProv.executeUpdate();
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch(SQLException ex) {}
            System.err.println("Error aprobar solicitud proveedor: " + e.getMessage());
            return false;
        } finally {
            try { con.setAutoCommit(true); con.close(); } catch(SQLException ex) {}
        }
    }

    public boolean rechazarSolicitud(int idSolicitud) {
        String sql = "UPDATE solicitud_proveedor SET estado_solicitud = 'rechazada' WHERE id_solicitud_pk = ?";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error rechazar solicitud proveedor: " + e.getMessage());
            return false;
        }
    }
}