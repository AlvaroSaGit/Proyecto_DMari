/*
    objetivo de este archivo:
    data access object (dao) para gestionar el perfil del cliente.
    abarca la lectura y escritura en 3 tablas: cliente, direccion y telefono.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dmari.helper.databaseHelper;

public class clienteDAO {
    
    databaseHelper db = new databaseHelper();

    // metodo para leer los datos actuales del cliente uniendo las 3 tablas
    public String[] obtenerPerfil(int idUsuario) {
        String[] perfil = new String[5];
        String sql = "SELECT c.direccion_envio, c.referencia_ubicacion, c.telefono_secundario, d.direccion_detallada, t.numero_telefonico " +
                     "FROM cliente c " +
                     "LEFT JOIN direccion d ON c.id_cliente_pk = d.id_usuario_fk " +
                     "LEFT JOIN telefono t ON c.id_cliente_pk = t.id_usuario_fk " +
                     "WHERE c.id_cliente_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    perfil[0] = rs.getString("direccion_envio");
                    perfil[1] = rs.getString("direccion_detallada");
                    perfil[2] = rs.getString("numero_telefonico");
                    perfil[3] = rs.getString("telefono_secundario");
                    perfil[4] = rs.getString("referencia_ubicacion");
                    return perfil;
                }
            }
        } catch (SQLException e) {
            System.out.println("error al obtener perfil: " + e.getMessage());
        }
        return null;
    }
        
    // metodo para guardar o actualizar el perfil usando las 3 tablas satelite
    public boolean guardarOActualizarPerfil(int idUsuario, String direccionPrimaria, String direccionDetalle, String numeroTelefono, String telefonoSecundario, String referencia) {
        
        // 1. sentencia para la tabla cliente (referencias generales)
        String sqlCliente = "INSERT INTO cliente (id_cliente_pk, direccion_envio, telefono_secundario, referencia_ubicacion) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE direccion_envio = VALUES(direccion_envio), telefono_secundario = VALUES(telefono_secundario), referencia_ubicacion = VALUES(referencia_ubicacion)";
                            
        // 2. sentencia para la tabla direccion
        // asumimos que si no existe, se crea. si existe, se actualiza la primera direccion encontrada de ese usuario.
        String sqlDireccion = "INSERT INTO direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) " +
                              "VALUES (?, ?, ?, 1) " +
                              "ON DUPLICATE KEY UPDATE direccion = VALUES(direccion), direccion_detallada = VALUES(direccion_detallada)";
                              
        // 3. sentencia para la tabla telefono (el numero telefonico es unique en tu sql)
        String sqlTelefono = "INSERT IGNORE INTO telefono (id_usuario_fk, numero_telefonico) " +
                             "VALUES (?, ?)";

        Connection con = null;
        try {
            con = db.conectar();
            // iniciamos transaccion para asegurar que las 3 tablas se llenen al mismo tiempo
            con.setAutoCommit(false);

            // insertar/actualizar en cliente
            try (PreparedStatement psCli = con.prepareStatement(sqlCliente)) {
                psCli.setInt(1, idUsuario);
                psCli.setString(2, direccionPrimaria);
                psCli.setString(3, telefonoSecundario);
                psCli.setString(4, referencia);
                psCli.executeUpdate();
            }

            // insertar/actualizar en direccion
            try (PreparedStatement psDir = con.prepareStatement(sqlDireccion)) {
                psDir.setInt(1, idUsuario);
                psDir.setString(2, direccionPrimaria);
                psDir.setString(3, direccionDetalle);
                psDir.executeUpdate();
            }

            // insertar en telefono
            // usamos update manual si el numero cambia, o insert ignore si es nuevo
            String sqlActualizarTelefono = "UPDATE telefono SET numero_telefonico = ? WHERE id_usuario_fk = ?";
            try (PreparedStatement psTelUpd = con.prepareStatement(sqlActualizarTelefono)) {
                psTelUpd.setString(1, numeroTelefono);
                psTelUpd.setInt(2, idUsuario);
                int filas = psTelUpd.executeUpdate();
                // si no habia telefono para actualizar, lo insertamos
                if (filas == 0) {
                    try (PreparedStatement psTelIns = con.prepareStatement(sqlTelefono)) {
                        psTelIns.setInt(1, idUsuario);
                        psTelIns.setString(2, numeroTelefono);
                        psTelIns.executeUpdate();
                    }
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("error al guardar perfil del cliente: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}