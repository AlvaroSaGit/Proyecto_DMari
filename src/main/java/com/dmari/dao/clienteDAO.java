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
        // el espacio al final de la linea del subquery es OBLIGATORIO para separarlo del FROM
        // sin ese espacio, mysql recibe "...telefono_secundarioFROM..." y lanza un error de sintaxis
        String sql = "SELECT d.direccion, c.referencia_ubicacion, d.direccion_detallada AS direccion_detalle, t.numero_telefonico AS telefono, co.correo, " +
                     "(SELECT numero_telefonico FROM telefono WHERE id_usuario_fk = u.id_usuario_pk AND id_telefono_pk != t.id_telefono_pk LIMIT 1) AS telefono_secundario " +
                     "FROM usuario u " +
                     "LEFT JOIN cliente c ON u.id_usuario_pk = c.id_cliente_pk " +
                     "LEFT JOIN direccion d ON u.id_usuario_pk = d.id_usuario_fk AND d.direccion_primario = 1 " +
                     "LEFT JOIN telefono t ON u.id_usuario_pk = t.id_usuario_fk " + 
                     "LEFT JOIN correo co ON u.id_usuario_pk = co.id_usuario_fk " +
                     "WHERE u.id_usuario_pk = ? LIMIT 1";
        
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
        // Ahora usamos INSERT ON DUPLICATE KEY UPDATE. Si el usuario se registró pero no tenía fila en 'cliente', la creamos.
        // Si ya existe, simplemente actualizamos su referencia.
        String sqlCliente = "INSERT INTO cliente (id_cliente_pk, referencia_ubicacion) VALUES (?, ?) " +
                            "ON DUPLICATE KEY UPDATE referencia_ubicacion = VALUES(referencia_ubicacion)";
                            
        // 2. estrategia correcta para direccion: intentamos actualizar primero.
        String sqlActualizarDireccion = "UPDATE direccion SET direccion = ?, direccion_detallada = ? " +
                                        "WHERE id_usuario_fk = ? AND direccion_primario = 1";
        String sqlInsertarDireccion  = "INSERT INTO direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) " +
                                       "VALUES (?, ?, ?, 1)";

        Connection con = null;
        try {
            con = db.conectar();
            con.setAutoCommit(false);

            // bloque 1: crear o actualizar la tabla cliente con la referencia de ubicacion.
            try (PreparedStatement psCli = con.prepareStatement(sqlCliente)) {
                psCli.setInt(1, idUsuario);
                psCli.setString(2, referencia);
                psCli.executeUpdate();
            }

            // bloque 2: intentar actualizar la direccion primaria existente del usuario.
            try (PreparedStatement psDir = con.prepareStatement(sqlActualizarDireccion)) {
                psDir.setString(1, direccionPrimaria);
                psDir.setString(2, direccionDetalle);
                psDir.setInt(3, idUsuario);
                int filasDireccion = psDir.executeUpdate();
                
                // si el update no afecto filas, el usuario no tiene direccion aun: insertamos una nueva.
                if (filasDireccion == 0) {
                    try (PreparedStatement psDirIns = con.prepareStatement(sqlInsertarDireccion)) {
                        psDirIns.setInt(1, idUsuario);
                        psDirIns.setString(2, direccionPrimaria);
                        psDirIns.setString(3, direccionDetalle);
                        psDirIns.executeUpdate();
                    }
                }
            }

            // bloque 3: actualizar el telefono principal del usuario.
            // OJO: Si el numero ya pertenece a otra persona, lanzara un IntegrityConstraintViolation, lo que dispara un 500.
            String sqlActualizarTelefono = "UPDATE telefono SET numero_telefonico = ? WHERE id_usuario_fk = ?";
            // insert ignore previene el error si el UPDATE fallo porque la fila no existia, pero igual valida UNIQUE.
            String sqlTelefono = "INSERT IGNORE INTO telefono (id_usuario_fk, numero_telefonico) VALUES (?, ?)";
            try (PreparedStatement psTelUpd = con.prepareStatement(sqlActualizarTelefono)) {
                psTelUpd.setString(1, numeroTelefono);
                psTelUpd.setInt(2, idUsuario);
                int filas = psTelUpd.executeUpdate();

                if (filas == 0) {
                    try (PreparedStatement psTelIns = con.prepareStatement(sqlTelefono)) {
                        psTelIns.setInt(1, idUsuario);
                        psTelIns.setString(2, numeroTelefono);
                        psTelIns.executeUpdate();
                    }
                }
            }

            // Si todo fue exitoso, guardamos cambios permanentemente.
            con.commit();
            return true;
        } catch (SQLException e) {
            // Si ocurre cualquier error, revertimos todos los cambios hechos en esta transacción.
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("=== ERROR SQL AL GUARDAR PERFIL ===");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // En cualquier caso, restauramos el autocommit y cerramos la conexión.
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}