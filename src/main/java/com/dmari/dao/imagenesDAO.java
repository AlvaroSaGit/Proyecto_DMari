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

// clase que maneja la interaccion con la base de datos para las imagenes
public class imagenesDAO {
    // instancia de conexion
    databaseHelper db = new databaseHelper();

    /*
        guarda la ruta relativa de la foto en la base de datos, 
        vinculandola con el id del producto recien creado o actualizado.
        'imagen_principal' permite a futuro tener galerias de varias fotos por producto.
    */
    public boolean insertarImagen(int idProducto, String ruta, int principal) {
        // consulta para insertar nueva imagen
        String sql = "insert into imagenes (id_producto_fk, url_ruta, imagen_principal) values (?, ?, ?)";

        // ejecuta y prepara la sentencia
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // pasa parametros a la consulta
            ps.setInt(1, idProducto);
            ps.setString(2, ruta);
            ps.setInt(3, principal);

            // si afecta mas de 0 filas es que guardo bien
            int filas = ps.executeUpdate();
            return filas > 0;

        // atrapa errores de base de datos
        } catch (SQLException e) {
            System.out.println("error al guardar foto: " + e.getMessage());
            return false;
        }
    }

    /*
        borra todas las fotos amarradas a un id de producto especifico.
        es util cuando el administrador sube una foto nueva para reemplazar la vieja, o cuando borra el producto.
    */
    public boolean borrarImagenesDeProducto(int idProducto) {
        // consulta para eliminar imagenes del producto
        String sql = "delete from imagenes where id_producto_fk = ?";

        // intenta conectar y preparar la ejecucion
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // asigna el id del producto
            ps.setInt(1, idProducto);
            
            // ejecuta el borrado en mysql
            int filas = ps.executeUpdate();
            return filas > 0;

        // captura problemas con la conexion o consulta
        } catch (SQLException e) {
            System.out.println("error al borrar fotos: " + e.getMessage());
            return false;
        }
    }
    
    
}
