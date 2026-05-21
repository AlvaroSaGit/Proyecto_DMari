package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.usuario;

public class usuarioDAO {
    
    databaseHelper db = new databaseHelper();

    // Metodo para REGISTRAR un nuevo usuario en la Base de Datos
    public boolean registrarUsuario(usuario nuevoUsuario) {
        // Sentencia SQL para insertar. Usamos '?' por seguridad contra inyeccion SQL
        String sql = "INSERT INTO usuarios (nombre, correo, password) VALUES (?, ?, ?)";
        
        try (Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Llenamos los '?' con los datos del objeto usuario
            ps.setString(1, nuevoUsuario.getNombre());
            ps.setString(2, nuevoUsuario.getCorreo());
            ps.setString(3, nuevoUsuario.getPassword()); // En un proyecto real, aqui se encriptaria la clave
            
            // Ejecutamos la orden. Si afecta a más de 0 filas, se guardó con éxito
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    // Metodo para INICIAR SESIÓN (Validar si existe en la BD)
    public usuario verificarLogin(String correo, String password) {
        String sql = "SELECT id_usuario, nombre, correo FROM usuarios WHERE correo = ? AND password = ?";
        usuario usuarioLogueado = null;
        
        try (Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, correo);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                // Si el ResultSet tiene datos (next), las credenciales son correctas
                if (rs.next()) {
                    usuarioLogueado = new usuario();
                    usuarioLogueado.setIdUsuario(rs.getInt("id_usuario"));
                    usuarioLogueado.setNombre(rs.getString("nombre"));
                    usuarioLogueado.setCorreo(rs.getString("correo"));
                    // Nota: No extraemos ni devolvemos el password por motivos de seguridad
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error al verificar login: " + e.getMessage());
        }
        
        // Si devuelve null, significa que el correo o la clave estaban mal
        return usuarioLogueado;
    }
}