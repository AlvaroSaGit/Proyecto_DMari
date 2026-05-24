package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.dmari.helper.databaseHelper;

public class clienteDAO {
    
    databaseHelper db = new databaseHelper();

    // metodo magico: inserta los datos de configuracion o los actualiza si el cliente ya los habia llenado
    public boolean guardarOActualizarPerfil(int idUsuario, String direccion, String telefono, String referencia) {
        // preparamos la consulta sql para insertar o modificar el perfil del cliente
        String sql = "INSERT INTO cliente (id_cliente_pk, direccion_envio, telefono_secundario, referencia_ubicacion) " +
                     // enviamos los datos por primera vez si no existe el registro en la base de datos
                     "VALUES (?, ?, ?, ?) " +
                     // si el id del cliente ya existe, mysql sobreescribira los datos antiguos en lugar de dar error
                     "ON DUPLICATE KEY UPDATE direccion_envio = ?, telefono_secundario = ?, referencia_ubicacion = ?";
                     
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // parametros para insertar por primera vez
            ps.setInt(1, idUsuario);
            ps.setString(2, direccion);
            ps.setString(3, telefono);
            ps.setString(4, referencia);
            
            // parametros para actualizar si ya existian los datos
            ps.setString(5, direccion);
            ps.setString(6, telefono);
            ps.setString(7, referencia);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.out.println("error al actualizar el perfil del cliente en configuraciones: " + e.getMessage());
            return false;
        }
    }
    
}