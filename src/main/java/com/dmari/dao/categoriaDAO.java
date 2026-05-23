/*
    objetivo de este archivo:
    se encarga de realizar las consultas sql rapidas hacia la tabla 'categoria',
    especificamente filtrando aquellas que se encuentran activas para el catalogo.
*/
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

    public boolean cambiarEstado(int idCategoria, boolean nuevoEstado) {
        // Hacemos un UPDATE solo a la columna de estado
        String sql = "UPDATE categoria SET estado_activo = ? WHERE id_categoria_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setBoolean(1, nuevoEstado);
            ps.setInt(2, idCategoria);
            
            // Si filasAfectadas es mayor a 0, significa que se actualizó correctamente
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.out.println("Error al cambiar estado de la categoria: " + e.getMessage());
            return false;
        }
    }

    // Metodo para crear una nueva categoria en la BD
    public boolean insertarCategoria(String nombre, String descripcion) {
        String sql = "INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES (?, ?, 1)";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al insertar categoria: " + e.getMessage());
            return false;
        }
    }

    // Metodo para editar el nombre o descripcion de una categoria
    public boolean actualizarCategoria(int id, String nombre, String descripcion) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id_categoria_pk = ?";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar categoria: " + e.getMessage());
            return false;
        }
    }

}