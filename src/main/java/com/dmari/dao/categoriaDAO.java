package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.categoria;

public class categoriaDAO {
    
    databaseHelper db = new databaseHelper();

    public ArrayList<categoria> listarCategoriasActivas() {
        ArrayList<categoria> lista = new ArrayList<>();
        // Solo traemos categorias que existan y esten activas (estado_activo = 1 / true)
        String sql = "SELECT id_categoria_pk, nombre FROM categoria WHERE estado_activo = 1";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                categoria cat = new categoria();
                cat.setIdCategoriaPk(rs.getInt("id_categoria_pk"));
                cat.setNombre(rs.getString("nombre"));
                lista.add(cat);
            }
            
        } catch (SQLException e) {
            System.out.println("Error al listar categorias: " + e.getMessage());
        }
        
        return lista;
    }
}