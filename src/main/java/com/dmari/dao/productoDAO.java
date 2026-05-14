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
        // armamos las sentencias sql, los signos de interrogacion son los espacios a llenar luego
    String sqlProducto = "insert into producto (nombre_producto, precio, stock) values (?, ?, ?)";
    String sqlImagen = "insert into imagenes (id_producto_fk, url_ruta, imagen_principal) values (?, ?, ?)";

    // el try abre la conexion y el catch final atrapa los errores si la base de datos no responde...
    try (Connection con = db.conectar()) {
        // apagamos el guardado automatico para controlar la transaccion y poder hacer el revert despues
        con.setAutoCommit(false);

        // preparamos el insert y le avisamos a mysql que necesitamos que nos devuelva el id creado
        try (PreparedStatement psPro = con.prepareStatement(sqlProducto, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // reemplazamos los signos de interrogacion con los datos reales que llegan del frontend
            psPro.setString(1, nuevoProducto.getNombreProducto());
            psPro.setDouble(2, nuevoProducto.getPrecio());
            psPro.setInt(3, nuevoProducto.getStock());

            // executeupdate manda la orden a mysql y nos devuelve el numero de filas que se insertaron
            int filasProducto = psPro.executeUpdate();

            // si el numero es mayor a 0 significa que el producto si se guardo en la tabla...
            if (filasProducto > 0) {
                // getgeneratedkeys sirve para recuperar ese id que mysql le acaba de asignar al producto
                ResultSet rs = psPro.getGeneratedKeys();
                
                if (rs.next()) {
                    // idgenerado guarda ese numero para usarlo abajo como la llave foranea de la foto
                    int idGenerado = rs.getInt(1);

                    // ahora preparamos el insert de la tabla de imagenes
                    try (PreparedStatement psImg = con.prepareStatement(sqlImagen)) {
                        psImg.setInt(1, idGenerado); // aca amarramos la imagen al producto recien creado
                        psImg.setString(2, nuevoProducto.getUrlRuta());
                        psImg.setInt(3, 1); // un 1 para indicar que es la imagen principal
                        
                        // mandamos a guardar la imagen en su respectiva tabla
                        psImg.executeUpdate();
                    }
                }
                
                // el commit es el que guarda todos los cambios de forma definitiva en el disco duro
                con.commit();
                return true;
            }
        } catch (SQLException e) {
            // el rollback echa para atras todo si ocurre un error... asi evitamos dejar productos sin foto
            con.rollback();
            System.out.println("hubo un error en los inserts, deshaciendo cambios: " + e.getMessage());
        }
    } catch (SQLException e) {
        // este catch es solo por si falla la conexion inicial a la base de datos
        System.out.println("error al conectar con el servidor: " + e.getMessage());
    }
    return false; // si llega hasta aca es porque algo fallo arriba

    }
    
}
