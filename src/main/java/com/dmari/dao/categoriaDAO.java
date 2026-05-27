/*
    objetivo de este archivo:
    se encarga de realizar todas las operaciones y consultas sql hacia la tabla 'categoria'
    en la base de datos de dmari. maneja la lectura, insercion, actualizacion y 
    cambios de estado (activo/inactivo) para que el catalogo funcione correctamente.
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

    // metodo que devuelve una lista con las categorias habilitadas para que los clientes las vean
    public ArrayList<categoria> listarCategoriasActivas() {
        ArrayList<categoria> lista = new ArrayList<>();
        
        // preparamos el sql: traemos solo el id y el nombre de la tabla categoria
        // y filtramos usando where para que solo salgan las que tienen estado_activo en 1 (true)
        String sql = "SELECT id_categoria_pk, nombre FROM categoria WHERE estado_activo = 1";
        
        // preparamos la conexion y la consulta en un try-with-resources para que se cierren automaticamente
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            // iteramos sobre cada fila que devuelve la base de datos
            while (rs.next()) {
                // creamos el objeto categoria y lo llenamos con la informacion
                categoria cat = new categoria();
                cat.setIdCategoriaPk(rs.getInt("id_categoria_pk"));
                cat.setNombre(rs.getString("nombre"));
                
                // guardamos la categoria lista en nuestro arreglo
                lista.add(cat);
            }
            
        } catch (SQLException e) {
            System.out.println("Error al listar categorias: " + e.getMessage());
        }
        
        return lista;
    }

    // metodo que devuelve TODAS las categorias (activas e inactivas) con todo su detalle para el admin
    public ArrayList<categoria> listarTodasCategorias() {
        ArrayList<categoria> lista = new ArrayList<>();
        
        // traemos todas las columnas sin filtrar por el estado_activo
        String sql = "SELECT id_categoria_pk, nombre, descripcion, estado_activo FROM categoria";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                categoria cat = new categoria();
                cat.setIdCategoriaPk(rs.getInt("id_categoria_pk"));
                cat.setNombre(rs.getString("nombre"));
                // extraemos los datos adicionales para la tabla del administrador
                cat.setDescripcion(rs.getString("descripcion"));
                cat.setEstado_activo(rs.getBoolean("estado_activo"));
                
                lista.add(cat);
            }
            
        } catch (SQLException e) {
            System.out.println("Error al listar todas las categorias: " + e.getMessage());
        }
        
        return lista;
    }

    // metodo para ocultar o mostrar una categoria sin borrarla de la base de datos
    public boolean cambiarEstado(int idCategoria, boolean nuevoEstado) {
        // actualizamos especificamente la tabla categoria
        // seteamos el nuevo estado en la columna estado_activo buscando la fila exacta con su llave primaria
        String sql = "UPDATE categoria SET estado_activo = ? WHERE id_categoria_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // inyectamos el nuevo estado (true = 1, false = 0) y el id de la categoria
            ps.setBoolean(1, nuevoEstado);
            ps.setInt(2, idCategoria);
            
            // executeupdate ejecuta la orden en mysql y devuelve cuantas filas fueron modificadas
            // si filas afectadas es mayor a 0, significa que se actualizo correctamente
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.out.println("Error al cambiar estado de la categoria: " + e.getMessage());
            return false;
        }
    }

    // metodo para crear una nueva familia de productos (categoria) en la base de datos
    public boolean insertarCategoria(String nombre, String descripcion) {
        // insertamos un nuevo renglon en la tabla categoria indicando sus columnas
        // los valores llevan 1 al final para que la categoria nazca activa por defecto
        String sql = "INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES (?, ?, 1)";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // asignamos el nombre y la descripcion a los signos de interrogacion
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            
            // retornamos true si mysql logro insertar la fila
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("Error al insertar categoria: " + e.getMessage());
            return false;
        }
    }

    // metodo para editar el nombre o la descripcion de una categoria que ya existe
    public boolean actualizarCategoria(int id, String nombre, String descripcion) {
        // actualizamos la tabla categoria cambiando su nombre y descripcion
        // es vital usar la clausula where con su id para no afectar todas las categorias de la tienda
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id_categoria_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // enviamos los datos nuevos y usamos el id en el tercer parametro para encontrar la fila exacta
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