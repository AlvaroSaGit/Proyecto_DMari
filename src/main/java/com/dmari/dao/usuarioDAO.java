package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.usuario;

public class usuarioDAO {
    
    databaseHelper db = new databaseHelper();
    
    // CLAVE DE ENCRIPTACION: Debe ser exactamente la misma que usaste en tus INSERTS (.sql)
    private static final String LLAVE_SECRETA = "llave_dmari";

    // metodo para registrar un nuevo usuario de forma segura en la base de datos
    public boolean registrarUsuario(usuario nuevoUsuario) {
        // la informacion del usuario se divide e inserta en 3 tablas: usuario, correo y credenciales
        // Buscamos dinamicamente el rol 'cliente' para evitar errores de llave foranea si el ID no es 2
        String sqlUsuario = "INSERT INTO usuario (nombre, id_rol_fk, estado_cuenta) VALUES (?, (SELECT id_rol_pk FROM rol WHERE tipo_rol = 'cliente' LIMIT 1), 1)";
        String sqlCorreo = "INSERT INTO correo (id_usuario_fk, correo, correo_primario) VALUES (?, ?, 1)";
        // usamos AES_ENCRYPT para convertir la contrasena en formato binario antes de guardarla en el BLOB
        String sqlCredenciales = "INSERT INTO credenciales (id_usuario, passwd_encript) VALUES (?, AES_ENCRYPT(?, ?))";
        
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el autocommit para iniciar una transaccion de base de datos
            // esto asegura que, si falla una tabla, no se guarde nada a medias en las demas
            con.setAutoCommit(false);
            
            int idGenerado = 0;
            
            // paso 1: insertamos el registro principal en la tabla usuario
            // usamos RETURN_GENERATED_KEYS para exigirle a mysql que nos devuelva el id autoincrementable que le acaba de asignar
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psUsuario.setString(1, nuevoUsuario.getNombre());
                psUsuario.executeUpdate();
                try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                    if (rs.next()) idGenerado = rs.getInt(1);
                }
            }
            
            // paso 2: si mysql nos devolvio un id valido, lo usamos para amarrar los datos secundarios
            if (idGenerado > 0) {
                // insertamos el registro en la tabla correo
                try (PreparedStatement psCorreo = con.prepareStatement(sqlCorreo)) {
                    psCorreo.setInt(1, idGenerado);
                    psCorreo.setString(2, nuevoUsuario.getCorreo());
                    psCorreo.executeUpdate();
                }
                // insertamos la contrasena en la tabla credenciales aplicando la encriptacion AES
                try (PreparedStatement psCred = con.prepareStatement(sqlCredenciales)) {
                    psCred.setInt(1, idGenerado);
                    psCred.setString(2, nuevoUsuario.getPassword()); // contrasena plana del formulario
                    psCred.setString(3, LLAVE_SECRETA);           // semilla o llave de seguridad
                    psCred.executeUpdate();
                }
                
                // confirmamos la transaccion para guardar todo definitivamente en la base de datos
                con.commit(); 
                return true;
            }
            
            // si algo salio mal y no se genero el id, cancelamos los cambios para no dejar datos huerfanos
            con.rollback(); 
            return false;
            
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("\n=== ERROR CRITICO AL REGISTRAR ===");
            System.err.println("Motivo: " + e.getMessage());
            System.err.println("==================================\n");
            return false;
        } finally {
            try { 
                if (con != null) {
                    con.setAutoCommit(true); // Restauramos autocommit para evitar bugs si usas Pool de conexiones
                    con.close(); 
                }
            } catch (SQLException e) {}
        }
    }

    // metodo para verificar las credenciales de inicio de sesion
    public usuario verificarLogin(String correo, String password) {
        // usamos un inner join para extraer los datos cruzando las 3 tablas donde vive la informacion
        // usamos AES_DECRYPT para descifrar la contrasena en tiempo real usando la llave secreta
        // incluimos u.id_rol_fk para poder gestionar que sidebar o vistas cargar en el frontend
        String sql = "SELECT u.id_usuario_pk, u.nombre, u.id_rol_fk, c.correo " +
                     "FROM usuario u " +
                     "INNER JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk " +
                     "INNER JOIN credenciales cr ON u.id_usuario_pk = cr.id_usuario " +
                     "WHERE c.correo = ? AND AES_DECRYPT(cr.passwd_encript, ?) = ?";
                     
        usuario usuarioLogueado = null;
        
        try (Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, correo);
            ps.setString(2, LLAVE_SECRETA); // pasamos la llave para abrir el candado de la encriptacion
            ps.setString(3, password);     // pasamos la contrasena en texto plano digitada por el usuario
            
            try (ResultSet rs = ps.executeQuery()) {
                // si el resultset avanza, significa que encontro una coincidencia exacta en mysql
                if (rs.next()) {
                    usuarioLogueado = new usuario();
                    usuarioLogueado.setIdUsuario(rs.getInt("id_usuario_pk"));
                    usuarioLogueado.setNombre(rs.getString("nombre"));
                    usuarioLogueado.setCorreo(rs.getString("correo"));
                    usuarioLogueado.setIdRol(rs.getInt("id_rol_fk")); // guardamos el rol para las validaciones del sistema
                    // omitimos extraer o devolver la contrasena hacia el frontend por motivos de seguridad
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error al verificar login: " + e.getMessage());
        }
        
        // si el objeto sigue nulo, significa que el correo o contrasena son incorrectos
        return usuarioLogueado;
    }
}