/*
 * OBJETIVO:
 * Data Access Object (DAO) para gestionar el perfil logístico del cliente.
 * Orquesta la lectura y escritura en las tablas satélite: cliente, direccion y telefono.
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
     * Obtiene el perfil logístico completo de un usuario.
     * Realiza un cruce de tablas (JOIN) para consolidar la dirección principal,
     * el teléfono principal y un teléfono secundario opcional.
     * 
     * @param idUsuario El ID del usuario a consultar.
     * @return Un objeto perfilCliente con los datos, o null si no se encuentra.
     */
    public perfilCliente obtenerPerfil(int idUsuario) {
        perfilCliente perfil = null;
        // Se usan alias (AS) para simplificar los nombres de las columnas y hacerlos consistentes.
        String sql = "SELECT d.direccion, c.referencia_ubicacion, d.direccion_detallada AS direccion_detalle, t.numero_telefonico AS telefono, co.correo, " +
                     "(SELECT numero_telefonico FROM telefono WHERE id_usuario_fk = u.id_usuario_pk AND id_telefono_pk != t.id_telefono_pk LIMIT 1) AS telefono_secundario" +
                     "FROM usuario u " +
                     "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                     "LEFT JOIN direccion d ON u.id_usuario_pk = d.id_usuario_fk AND d.direccion_primario = 1 " +
                     "LEFT JOIN telefono t ON u.id_usuario_pk = t.id_usuario_fk " + 
                     "LEFT JOIN correo co ON u.id_usuario_pk = co.id_usuario_fk " +
                     "WHERE u.id_usuario_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                // Si el ResultSet tiene al menos una fila, se encontró el perfil.
                if (rs.next()) {
                    perfil = new perfilCliente();
                    perfil.setDireccion(rs.getString("direccion")); // Columna original
                    perfil.setDireccionDetalle(rs.getString("direccion_detalle")); // Usamos el alias
                    perfil.setTelefono(rs.getString("telefono")); // Usamos el alias
                    perfil.setTelefonoSecundario(rs.getString("telefono_secundario"));
                    perfil.setReferencia(rs.getString("referencia_ubicacion")); // Columna original
                    perfil.setCorreo(rs.getString("correo"));
                    return perfil;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener perfil: " + e.getMessage());
        }
        return null;
    }
        
    /**
     * Guarda o actualiza el perfil logístico de un cliente de forma transaccional.
     * Esta operación "todo o nada" asegura que los datos del perfil se guarden
     * en las tablas 'cliente', 'direccion' y 'telefono' de manera consistente.
     * 
     * @param idUsuario ID del usuario cuyo perfil se está modificando.
     * @param direccionPrimaria Dirección principal de entrega.
     * @param direccionDetalle Información adicional de la dirección (apto, torre).
     * @param numeroTelefono Teléfono principal de contacto.
     * @param telefonoSecundario Teléfono alternativo (opcional).
     * @param referencia Indicaciones para la entrega (ej. "casa esquinera").
     * @return true si la transacción fue exitosa (commit), false si falló (rollback).
     */
    public boolean guardarOActualizarPerfil(int idUsuario, String direccionPrimaria, String direccionDetalle, String numeroTelefono, String telefonoSecundario, String referencia) {
        
        // La tabla cliente solo maneja la referencia de ubicación.
        String sqlCliente = "UPDATE cliente SET referencia_ubicacion = ? WHERE id_cliente_pk = ?";
                            
        // 2. preparamos la instruccion sql para la tabla direccion.
        // Si ya existe una dirección primaria para el usuario, la actualiza. Si no, la crea.
        String sqlDireccion = "INSERT INTO direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) " +
                              "VALUES (?, ?, ?, 1) " +
                              "ON DUPLICATE KEY UPDATE direccion = VALUES(direccion), direccion_detallada = VALUES(direccion_detallada)";
                              
        // INSERT IGNORE previene errores si el teléfono ya existe para otro usuario, priorizando la integridad.
        String sqlTelefono = "INSERT IGNORE INTO telefono (id_usuario_fk, numero_telefonico) " +
                             "VALUES (?, ?)";

        Connection con = null;
        try {
            con = db.conectar();
            // Desactivamos el autocommit para iniciar una transacción manual.
            // Esto garantiza que si una inserción falla, las demás se revierten.
            con.setAutoCommit(false);

            // Bloque 1: Actualizar la tabla 'cliente' con la referencia de ubicación.
            try (PreparedStatement psCli = con.prepareStatement(sqlCliente)) {
                psCli.setString(1, referencia);
                psCli.setInt(2, idUsuario);
                psCli.executeUpdate();
            }

            // Bloque 2: Insertar o actualizar la dirección principal.
            try (PreparedStatement psDir = con.prepareStatement(sqlDireccion)) {
                psDir.setInt(1, idUsuario);
                psDir.setString(2, direccionPrimaria);
                psDir.setString(3, direccionDetalle);
                psDir.executeUpdate();
            }

            // Bloque 3: Actualizar el teléfono principal.
            String sqlActualizarTelefono = "UPDATE telefono SET numero_telefonico = ? WHERE id_usuario_fk = ?";
            try (PreparedStatement psTelUpd = con.prepareStatement(sqlActualizarTelefono)) {
                psTelUpd.setString(1, numeroTelefono);
                psTelUpd.setInt(2, idUsuario);
                int filas = psTelUpd.executeUpdate();
                
                if (telefonoSecundario != null && !telefonoSecundario.trim().isEmpty()) {
                    // TODO: Implementar lógica para un segundo teléfono.
                    // Se podría añadir una columna 'tipo' en la tabla 'telefono'
                    // o manejar una segunda fila para el mismo usuario.
                }

                // Si el UPDATE no afectó filas, significa que el usuario no tenía un teléfono
                // registrado, por lo que procedemos a insertarlo.
                if (filas == 0) {
                    try (PreparedStatement psTelIns = con.prepareStatement(sqlTelefono)) {
                        psTelIns.setInt(1, idUsuario);
                        psTelIns.setString(2, numeroTelefono);
                        psTelIns.executeUpdate();
                    }
                }
            }

            // Si todas las operaciones fueron exitosas, confirmamos los cambios.
            con.commit();
            return true;
        } catch (SQLException e) {
            // Si ocurre cualquier error, revertimos todos los cambios hechos en esta transacción.
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("Error al guardar perfil del cliente: " + e.getMessage());
            return false;
        } finally {
            // En cualquier caso, restauramos el autocommit y cerramos la conexión.
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}