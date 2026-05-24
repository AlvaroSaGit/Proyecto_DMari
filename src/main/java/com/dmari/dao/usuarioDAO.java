/*
    objetivo de este archivo:
    data access object (dao) que se encarga de las consultas sql relacionadas 
    con los usuarios. maneja comandos avanzados de mysql como transacciones 
    seguras, extraccion de ids autogenerados y funciones nativas de encriptacion 
    (aes_encrypt / aes_decrypt) para proteger las credenciales.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.usuario;

public class usuarioDAO {
    
    databaseHelper db = new databaseHelper();
    
    // llave secreta que mysql utilizara como algoritmo para mezclar la contrasena
    private static final String LLAVE_SECRETA = "llave_dmari";

    public boolean registrarUsuario(usuario nuevoUsuario) {
        
        String sqlUsuario = "INSERT INTO usuario (nombre, id_rol_fk, estado_cuenta) VALUES (?, (SELECT id_rol_pk FROM rol WHERE tipo_rol = 'cliente' LIMIT 1), 1)";
        String sqlCorreo = "INSERT INTO correo (id_usuario_fk, correo, correo_primario) VALUES (?, ?, 1)";
        
        // aes_encrypt es un comando nativo de mysql que convierte el texto en codigo ilegible (formato binario blob). 
        // sin la llave secreta, es matematicamente imposible revertirlo a texto plano.
        String sqlCredenciales = "INSERT INTO credenciales (id_usuario, passwd_encript) VALUES (?, AES_ENCRYPT(?, ?))";
        
        Connection con = null;
        try {
            con = db.conectar();
            // setautocommit(false) pausa el guardado automatico de mysql. inicia una "transaccion".
            // esto es vital: si la tabla 'correo' falla, podemos cancelar la tabla 'usuario' para no dejar registros huerfanos.
            con.setAutoCommit(false);
            
            int idGenerado = 0;
            
            // return_generated_keys le ordena a mysql que, despues de insertar el usuario, nos devuelva el numero de id autoincrementable que acaba de crear.
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psUsuario.setString(1, nuevoUsuario.getNombre());
                psUsuario.executeUpdate();
                try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                    if (rs.next()) idGenerado = rs.getInt(1);
                }
            }
            
            if (idGenerado > 0) {
                try (PreparedStatement psCorreo = con.prepareStatement(sqlCorreo)) {
                    psCorreo.setInt(1, idGenerado);
                    psCorreo.setString(2, nuevoUsuario.getCorreo());
                    psCorreo.executeUpdate();
                }
                
                try (PreparedStatement psCred = con.prepareStatement(sqlCredenciales)) {
                    psCred.setInt(1, idGenerado);
                    psCred.setString(2, nuevoUsuario.getPassword()); 
                    psCred.setString(3, LLAVE_SECRETA); 
                    psCred.executeUpdate();
                }
                
                // commit es la orden final que le dice a mysql: "todo salio perfecto, aplica los cambios definitivamente".
                con.commit(); 
                return true;
            }
            
            // rollback es el boton de panico. si no se genero id, deshace cualquier insert que se haya hecho en este intento.
            con.rollback(); 
            return false;
            
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("\n=== error critico al registrar ===");
            System.err.println("Motivo: " + e.getMessage());
            System.err.println("==================================\n");
            return false;
        } finally {
            try { 
                if (con != null) {
                    // se debe restaurar el comportamiento normal de la conexion antes de devolverla a la memoria
                    con.setAutoCommit(true); 
                    con.close(); 
                }
            } catch (SQLException e) {}
        }
    }

    public usuario verificarLogin(String correo, String password) {
        
        // aes_decrypt hace el proceso inverso: usa la llave secreta para destrabar el blob y lo compara con el texto digitado.
        // traemos tambien el estado_cuenta para validarlo desde java y poder darle un mensaje especifico al usuario
        String sql = "SELECT u.id_usuario_pk, u.nombre, u.id_rol_fk, c.correo, u.estado_cuenta " +
                     "FROM usuario u " +
                     "INNER JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk " +
                     "INNER JOIN credenciales cr ON u.id_usuario_pk = cr.id_usuario " +
                     "WHERE c.correo = ? AND AES_DECRYPT(cr.passwd_encript, ?) = ?";
                     
        usuario usuarioLogueado = null;
        
        try (Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, correo);
            ps.setString(2, LLAVE_SECRETA);
            ps.setString(3, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                // si avanza el cursor (rs.next), significa que encontro el correo y la contrasena era la correcta.
                if (rs.next()) {
                    usuarioLogueado = new usuario();
                    usuarioLogueado.setIdUsuario(rs.getInt("id_usuario_pk"));
                    usuarioLogueado.setNombre(rs.getString("nombre"));
                    usuarioLogueado.setCorreo(rs.getString("correo"));
                    usuarioLogueado.setIdRol(rs.getInt("id_rol_fk"));
                    usuarioLogueado.setEstadoCuenta(rs.getBoolean("estado_cuenta"));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("error al verificar login: " + e.getMessage());
        }
        
        return usuarioLogueado;
    }

    // metodo para listar a todos los usuarios del sistema (para el panel del administrador)
    public ArrayList<usuario> listarUsuarios() {
        ArrayList<usuario> lista = new ArrayList<>();
        // cruzamos la tabla usuario con el correo usando left join (por si algun usuario no tiene correo registrado)
        String sql = "SELECT u.id_usuario_pk, u.nombre, u.apellido, u.id_rol_fk, u.estado_cuenta, c.correo " +
                     "FROM usuario u " +
                     "LEFT JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk";
                     
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while(rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario_pk"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido")); 
                u.setIdRol(rs.getInt("id_rol_fk"));
                u.setCorreo(rs.getString("correo"));
                u.setEstadoCuenta(rs.getBoolean("estado_cuenta"));
                lista.add(u);
            }
        } catch (SQLException e) { System.out.println("error al listar usuarios: " + e.getMessage()); }
        return lista;
    }

    // metodo transaccional para cambiar el rol y el estado de la cuenta de un usuario
    public boolean actualizarPermisos(int idUsuario, int idRol, boolean estadoCuenta) {
        String sql = "UPDATE usuario SET id_rol_fk = ?, estado_cuenta = ? WHERE id_usuario_pk = ?";
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            ps.setBoolean(2, estadoCuenta);
            ps.setInt(3, idUsuario);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}