package com.dmari.dao;

/*
    DAO ESTRUCTURA CRUD
*/
/*
    Conexion database
*/
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.producto;



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
             "p.id_categoria_fk, " +
             "i.url_ruta, " +
             "c.nombre AS nombre_categoria " +
             "FROM producto p " +
             "LEFT JOIN imagenes i ON p.id_producto_pk = i.id_producto_fk " +
             "AND i.imagen_principal = 1 " +
             "LEFT JOIN categoria c ON p.id_categoria_fk = c.id_categoria_pk";
        
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
                
                /* Extraemos el ID y el nombre de la categoria */
                prod.setIdCategoriaFk(rs.getInt("id_categoria_fk"));
                prod.setCategoria(rs.getString("nombre_categoria"));
                
                /*Metemos el producto ya lleno en la lista general*/
                lista.add(prod);
            }
        }catch(SQLException error){
            System.out.println("Problema en el DAO: "+error.getMessage());
        }
        
        return lista;
    }
    
    public int insertarProducto(producto nuevoProducto){
        // aca solo dejamos la consulta del producto solo
        String sql = "insert into producto (nombre_producto, precio, stock, id_categoria_fk) values (?, ?, ?, ?)";
        int idGenerado = 0;

        try (Connection con = db.conectar();
                
            // el try con parentesis abre la conexion a la base de datos y la cierra al final para no saturar la memoria
            // el preparestatement prepara la orden sql de forma segura,
            // y el return_generated_keys le exige a mysql que nos devuelva el id automatico que le acaba de asignar a ese producto nuevo   
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // pasamos los datos basicos del producto
            ps.setString(1, nuevoProducto.getNombreProducto());
            ps.setDouble(2, nuevoProducto.getPrecio());
            ps.setInt(3, nuevoProducto.getStock());
            ps.setInt(4, nuevoProducto.getIdCategoriaFk());

            // ejecutamos el insert
            if (ps.executeUpdate() > 0) {
                // getgeneratedkeys sirve para que mysql nos diga que id le puso a la dona o vela
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    // guardamos ese numero en nuestra variable
                    idGenerado = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("error al insertar el producto: " + e.getMessage());
        }

        // devolvemos el id.. si es 0 significa que no se guardo nada
        return idGenerado;

    }
    
    public boolean actualizarProducto(producto prod) {
    // la instruccion sql.. el where es la parte mas importante de todo el codigo
    String sql = "update producto set nombre_producto = ?, precio = ?, stock = ?, id_categoria_fk = ? where id_producto_pk = ?";

    // abrimos conexion y preparamos la consulta de una vez
    try (Connection con = db.conectar();
        PreparedStatement ps = con.prepareStatement(sql)) {

        // reemplazamos los signos de interrogacion con los datos nuevos
        ps.setString(1, prod.getNombreProducto());
        ps.setDouble(2, prod.getPrecio());
        ps.setInt(3, prod.getStock());
        ps.setInt(4, prod.getIdCategoriaFk());
        
        // el quinto parametro es el id, para decirle a mysql cual producto exacto debe cambiar
        ps.setInt(5, prod.getIdProductoPk());

        // ejecutamos la orden y guardamos cuantas filas se modificaron
        int filasAfectadas = ps.executeUpdate();

        // si el numero es mayor a 0, significa que si encontro el producto y lo actualizo
        if (filasAfectadas > 0) {
            return true;
        }

    } catch (SQLException e) {
        System.out.println("hubo un error al intentar actualizar: " + e.getMessage());
    }
    
    // si no lo encontro o hubo error, devuelve falso
    return false;
    }
    
    // metodo para borrar un producto de la tienda
    public boolean eliminarProducto(int id) {
        // la orden delete borra la fila completa segun el id que le mandemos
        String sql = "delete from producto where id_producto_pk = ?";

        try (Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {

            // le pasamos el id del producto que el usuario quiere quitar del catalogo
            ps.setInt(1, id);

            // executeupdate ejecuta el borrado en la base de datos de dmari
            int filasAfectadas = ps.executeUpdate();

            // si se borro algo devolvemos true.. sino es porque ese id no existia
            return filasAfectadas > 0;

        } catch (SQLException e) {
            // este error suele salir si el producto tiene imagenes amarradas (por la llave foranea)
            System.out.println("no se pudo borrar.. puede que tenga fotos asociadas: " + e.getMessage());
            return false;
        }
    }
}
