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
    
    public int insertarProducto(producto nuevoProducto){
        // aca solo dejamos la consulta del producto solo
        String sql = "insert into producto (nombre_producto, precio, stock) values (?, ?, ?)";
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
    String sql = "update producto set nombre_producto = ?, precio = ?, stock = ? where id_producto_pk = ?";

    // abrimos conexion y preparamos la consulta de una vez
    try (Connection con = db.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        // reemplazamos los signos de interrogacion con los datos nuevos
        ps.setString(1, prod.getNombreProducto());
        ps.setDouble(2, prod.getPrecio());
        ps.setInt(3, prod.getStock());
        
        // el cuarto parametro es el id, para decirle a mysql cual producto exacto debe cambiar
        ps.setInt(4, prod.getIdProductoPk());

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
