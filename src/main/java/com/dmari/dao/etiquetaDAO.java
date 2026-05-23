/*
    objetivo de este archivo:
    gestiona la extraccion de etiquetas desde la base de datos.
    permite traer todas las etiquetas globales o cruzar tablas (joins)
    para encontrar las etiquetas que pertenecen a un producto especifico.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.etiqueta;

public class etiquetaDAO {
    
    databaseHelper db = new databaseHelper();

    // Metodo para listar TODAS las etiquetas (Para mostrarlas en los botones de filtro)
    public ArrayList<etiqueta> listarEtiquetas() {
        ArrayList<etiqueta> lista = new ArrayList<>();
        String sql = "SELECT id_etiqueta_pk, nombre_etiqueta FROM etiqueta";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                etiqueta e = new etiqueta();
                e.setIdEtiquetaPk(rs.getInt("id_etiqueta_pk"));
                e.setNombreEtiqueta(rs.getString("nombre_etiqueta"));
                lista.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar etiquetas: " + e.getMessage());
        }
        return lista;
    }
    
    // Metodo para obtener las etiquetas que le pertenecen a UN producto en especifico
    public ArrayList<String> listarEtiquetasPorProducto(int idProducto) {
        ArrayList<String> lista = new ArrayList<>();
        // Cruzamos la tabla puente producto_etiqueta con etiqueta
        String sql = "SELECT e.nombre_etiqueta FROM etiqueta e " +
                     "INNER JOIN producto_etiqueta pe ON e.id_etiqueta_pk = pe.id_etiqueta " +
                     "WHERE pe.id_producto = ?";
                     
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idProducto);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("nombre_etiqueta"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar etiquetas del producto: " + e.getMessage());
        }
        return lista;
    }
}