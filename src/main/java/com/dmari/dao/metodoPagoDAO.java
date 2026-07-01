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

// clase que maneja el acceso a datos para los metodos de pago
public class metodoPagoDAO {
    
    // instancia para conectarse a la base de datos
    databaseHelper db = new databaseHelper();

    // obtiene los metodos de pago activos en formato json
    public String obtenerMetodosPagoJSON() {
        // inicia la construccion del json como un arreglo
        StringBuilder json = new StringBuilder("[");
        // consulta sql para seleccionar los metodos activos
        String sql = "SELECT id_metodo_pago_pk, descripcion_pago FROM metodo_pago WHERE estado_activo = 1";
        
        // intenta conectar y preparar la consulta
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             // ejecuta la consulta
             ResultSet rs = ps.executeQuery()) {
            
            // variable para controlar la insercion de comas
            boolean primero = true;
            // itera sobre los resultados
            while (rs.next()) {
                // si no es el primer elemento, anade una coma separadora
                if (!primero) json.append(",");
                // construye el objeto json con id y descripcion
                json.append("{\"id\":").append(rs.getInt("id_metodo_pago_pk"))
                    .append(",\"descripcion\":\"").append(rs.getString("descripcion_pago")).append("\"}");
                primero = false;
            }
        // captura y muestra errores de sql
        } catch (SQLException e) { System.out.println("error al listar metodos de pago: " + e.getMessage()); }
        
        // cierra el arreglo json
        json.append("]");
        // retorna la cadena de texto con formato json
        return json.toString();
    }
}