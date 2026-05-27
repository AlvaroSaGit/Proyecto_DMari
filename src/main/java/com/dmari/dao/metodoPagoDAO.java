/*
    objetivo de este archivo:
    extraer la lista de metodos de pago habilitados en la base de datos.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dmari.helper.databaseHelper;

public class metodoPagoDAO {
    
    databaseHelper db = new databaseHelper();

    public String obtenerMetodosPagoJSON() {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT id_metodo_pago_pk, descripcion_pago FROM metodo_pago WHERE estado_activo = 1";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{\"id\":").append(rs.getInt("id_metodo_pago_pk"))
                    .append(",\"descripcion\":\"").append(rs.getString("descripcion_pago")).append("\"}");
                primero = false;
            }
        } catch (SQLException e) { System.out.println("Error al listar metodos de pago: " + e.getMessage()); }
        
        json.append("]");
        return json.toString();
    }
}