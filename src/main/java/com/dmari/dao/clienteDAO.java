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
import com.dmari.modelo.perfilCliente;

public class clienteDAO {
    
    databaseHelper db = new databaseHelper();

    /**
     * metodo de lectura que cruza las 3 tablas satelite vinculadas al perfil.
     * 
     * @param idUsuario int: el id del usuario del cual extraera los datos.
     * @return perfilcliente: el objeto de transporte (dto) lleno con los atributos del domicilio.
     */
    public perfilCliente obtenerPerfil(int idUsuario) {
        perfilCliente perfil = null;
        String sql = "SELECT d.direccion, c.referencia_ubicacion, c.telefono_secundario, d.direccion_detallada, t.numero_telefonico " +
                     "FROM cliente c " +
                     "LEFT JOIN direccion d ON c.id_cliente_pk = d.id_usuario_fk AND d.direccion_primario = 1 " +
                     "LEFT JOIN telefono t ON c.id_cliente_pk = t.id_usuario_fk " +
                     "WHERE c.id_cliente_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                // condicional: verifica si existe ese usuario en las tablas hijas.
                // si entra, inicializa la clase modelo e inyecta uno a uno los strings.
                if (rs.next()) {
                    perfil = new perfilCliente();
                    perfil.setDireccion(rs.getString("direccion"));
                    perfil.setDireccionDetalle(rs.getString("direccion_detallada"));
                    perfil.setTelefono(rs.getString("numero_telefonico"));
                    perfil.setTelefonoSecundario(rs.getString("telefono_secundario"));
                    perfil.setReferencia(rs.getString("referencia_ubicacion"));
                    return perfil;
                }
            }
        } catch (SQLException e) {
            System.out.println("error al obtener perfil: " + e.getMessage());
        }
        return null;
    }
        
    /**
     * realiza un volcado maestro distribuyendo la nueva informacion hacia las tres
     * tablas del modelo de datos de perfil, garantizando integridad.
     * 
     * @param idUsuario int: dueno de los registros
     * @param direccionPrimaria string: texto de la calle principal
     * @param direccionDetalle string: notas de ubicacion
     * @param numeroTelefono string: telefono a insertar o cambiar
     * @param telefonoSecundario string: telefono opcional
     * @param referencia string: informacion logistica de punto de entrega
     * @return boolean: true si las 3 operaciones sql salieron a la perfeccion, false si hubo error.
     */
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
                // condicional de base de datos: evalua si el update realmente sobreescribio filas.
                // si filas == 0, significa que el usuario jamas habia tenido telefono, por ende
                // cambiamos la instruccion de update a un insert tradicional.
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