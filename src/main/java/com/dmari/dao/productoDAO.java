package com.dmari.dao;

/*
    DAO ESTRUCTURA CRUD
*/
/*
    Conexion database
*/
import com.dmari.helper.databaseHelper;

/*Me traigo la "caja" que es el archivo producto.java*/
import com.dmari.modelo.producto;
import java.sql.Connection;

/*
    El java.sql.preparedstatement, lleva la consulta
    SQL (select) de forma segura a la base de datos
*/
import java.sql.PreparedStatement;
/*
    Resulset es como una tabla temporal o una caja
    donde java guarda la respuesta que manda mysql
*/
import java.sql.ResultSet;

/*
    El java.sql.sqlexception, es la alarma que suena
    si la base de datos esta apagada o algo mal escrito
*/
import java.sql.SQLException;
/*
    Es una lista dinamica
    donde se ira guardando cada producto
    para mandarlos al frontend
*/
import java.util.ArrayList;



public class productoDAO {
    databaseHelper db = new databaseHelper();
    
    /*
        Metodo para traer los productos de la base de datos
    */
    public ArrayList<producto> listarProductos(){
    /*Se crea una nueva lista*/
        ArrayList<producto> lista = new ArrayList<>();
        /*
            La sentencia sql que se ejecutara en el motor de mysql
        */
        String sql = "SELECT " +
             "p.id_producto_pk, " +
             "p.nombre_producto, " +
             "p.precio, " +
             "p.stock, " +
             "i.url_ruta " +
             "FROM producto p " +
             "LEFT JOIN imagenes i ON p.id_producto_pk = i.id_producto_fk " +
             "AND i.imagen_principal = 1";
        
        /*
            Usar el try como try-with-resources,
            esto hace se cierra automaticamente al llegar
            a la llave de cierre como metodo de seguridad
        */
        try(
            /*
                Aqui se llama el metodo conectar() de la clase databasehelper
                Se abre el flujo de datos hacia mysql
            */
            Connection con = db.conectar();
            /*
                Se usa la conexion abierta 'con' para preparar el comando sql
                PreparedStatement es mas seguro porque evita ataques de
                inyeccion sql
            */
            PreparedStatement ps = con.prepareStatement(sql);
            /*
                Manda la orden a mysql
            */
            ResultSet rs = ps.executeQuery()){
            
            /*
                Mientras el resultset tenga filas por leer...
            */
            while(rs.next()){
                /*
                Se crea un objeto producto que viene del archivo
                producto de com.dmari.modelo
                */
                producto prod = new producto();
                /*
                    Sacamos los datos de las columnas mysql
                    y se lo colocamos en los atributos del objeto
                    usando setters
                */
                prod.setIdProductoPk(rs.getInt("id_producto_pk"));
                prod.setNombreProducto(rs.getString("nombre_producto"));
                prod.setPrecio(rs.getDouble("precio"));
                prod.setStock(rs.getInt("stock"));
                prod.setUrlRuta(rs.getString("url_ruta"));
                
                /*Metemos el producto ya lleno en la lista general*/
                lista.add(prod);
            }
        }catch(SQLException error){
            System.out.println("Problema en el DAO: "+error.getMessage());
        }
        
        return lista;
    }
    
    public boolean insertarProducto(producto nuevoProducto){
        int a=0;
        return true;
    }
    
}
