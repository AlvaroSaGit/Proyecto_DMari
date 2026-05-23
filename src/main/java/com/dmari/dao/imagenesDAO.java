/*
    objetivo de este archivo:
    gestiona la insercion y borrado de las rutas (url) de las imagenes
    que estan asociadas a los productos en la base de datos.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.dmari.helper.databaseHelper;

public class imagenesDAO {
    databaseHelper db = new databaseHelper();
// guarda la foto usando el id que nos da el producto
    public boolean insertarImagen(int idProducto, String ruta, int principal) {
        String sql = "insert into imagenes (id_producto_fk, url_ruta, imagen_principal) values (?, ?, ?)";

        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.setString(2, ruta);
            ps.setInt(3, principal);

            // si afecta mas de 0 filas es que guardo bien
            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error al guardar foto: " + e.getMessage());
            return false;
        }
    }

    // borra las fotos amarradas a un id para cuando se quiera eliminar un producto del catalogo
    public boolean borrarImagenesDeProducto(int idProducto) {
        String sql = "delete from imagenes where id_producto_fk = ?";

        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            
            // ejecuta el borrado en mysql
            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("error al borrar fotos: " + e.getMessage());
            return false;
        }
    }
    
}
