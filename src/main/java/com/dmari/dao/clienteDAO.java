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
        String sql = "SELECT d.direccion, c.referencia_ubicacion, c.telefono_secundario, d.direccion_detallada, t.numero_telefonico, co.correo " +
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
                // condicional: verifica si existe ese usuario en las tablas hijas.
                // si entra, inicializa la clase modelo e inyecta uno a uno los strings.
                if (rs.next()) {
                    perfil = new perfilCliente();
                    perfil.setDireccion(rs.getString("direccion"));
                    perfil.setDireccionDetalle(rs.getString("direccion_detallada"));
                    perfil.setTelefono(rs.getString("numero_telefonico"));
                    perfil.setTelefonoSecundario(rs.getString("telefono_secundario"));
                    perfil.setReferencia(rs.getString("referencia_ubicacion"));
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
        
        // 1. preparamos la instruccion sql para la tabla cliente (referencias generales)
        // usamos on duplicate key update para insertar si es nuevo, o actualizar si ya existe, ahorrando consultas extra
        String sqlCliente = "INSERT INTO cliente (id_cliente_pk, direccion_envio, telefono_secundario, referencia_ubicacion) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE direccion_envio = VALUES(direccion_envio), telefono_secundario = VALUES(telefono_secundario), referencia_ubicacion = VALUES(referencia_ubicacion)";
                            
        // 2. preparamos la instruccion sql para la tabla direccion
        // forzamos el 1 logico en direccion_primaria. actualiza solo la direccion principal de este usuario especifico.
        String sqlDireccion = "INSERT INTO direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) " +
                              "VALUES (?, ?, ?, 1) " +
                              "ON DUPLICATE KEY UPDATE direccion = VALUES(direccion), direccion_detallada = VALUES(direccion_detallada)";
                              
        // 3. preparamos la instruccion sql para la tabla telefono (el numero telefonico es unique en el esquema)
        // usamos insert ignore para evitar que mysql explote si el telefono ya existe para otro usuario
        String sqlTelefono = "INSERT IGNORE INTO telefono (id_usuario_fk, numero_telefonico) " +
                             "VALUES (?, ?)";

        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el guardado automatico (autocommit) para iniciar un bloque transaccional.
            // esto garantiza que si una insercion falla, las demas se anulan para mantener la base de datos limpia.
            con.setAutoCommit(false);

            // ejecucion del bloque 1: actualizacion de la tabla central del cliente
            try (PreparedStatement psCli = con.prepareStatement(sqlCliente)) {
                psCli.setInt(1, idUsuario);
                psCli.setString(2, direccionPrimaria);
                psCli.setString(3, telefonoSecundario);
                psCli.setString(4, referencia);
                psCli.executeUpdate();
            }

            // ejecucion del bloque 2: actualizacion de la tabla satelite de direccion
            try (PreparedStatement psDir = con.prepareStatement(sqlDireccion)) {
                psDir.setInt(1, idUsuario);
                psDir.setString(2, direccionPrimaria);
                psDir.setString(3, direccionDetalle);
                psDir.executeUpdate();
            }

            // ejecucion del bloque 3: actualizacion de la tabla satelite de telefono
            // primero intentamos actualizar el registro asumiendo que el usuario ya tenia telefono asignado
            String sqlActualizarTelefono = "UPDATE telefono SET numero_telefonico = ? WHERE id_usuario_fk = ?";
            try (PreparedStatement psTelUpd = con.prepareStatement(sqlActualizarTelefono)) {
                psTelUpd.setString(1, numeroTelefono);
                psTelUpd.setInt(2, idUsuario);
                int filas = psTelUpd.executeUpdate();
                // validacion de filas afectadas: comprobamos si mysql realmente sobreescribio algo.
                // si filas == 0, significa que el usuario no tenia telefono previo, asi que 
                // procedemos a inyectarle uno completamente nuevo.
                if (filas == 0) {
                    try (PreparedStatement psTelIns = con.prepareStatement(sqlTelefono)) {
                        psTelIns.setInt(1, idUsuario);
                        psTelIns.setString(2, numeroTelefono);
                        psTelIns.executeUpdate();
                    }
                }
            }

            // si todo el codigo anterior fluyo sin lanzar excepciones, guardamos los datos definitivamente
            con.commit();
            return true;
        } catch (SQLException e) {
            // si algo exploto en medio de la transaccion, deshacemos cualquier cambio incompleto
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("Error al guardar perfil del cliente: " + e.getMessage());
            return false;
        } finally {
            // limpieza de memoria: volvemos a encender el autocommit y cerramos la tuberia a la base de datos
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}