
package com.dmari.helper;

/*
    objetivo de este archivo:
    este es el motor principal de conexion del proyecto. 
    su unico trabajo es abrir el puente de comunicacion entre 
    el codigo de java y el servidor de la base de datos mysql.
*/
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class databaseHelper {

    // final indica que estas variables son constantes y nadie puede modificarlas mientras el programa corre
    private static final String URL = "jdbc:mysql://localhost:3306/DMari";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";
    
    public Connection conectar(){
        Connection conexion = null;
        try{
            // class.forname busca e inicializa el archivo .jar (driver) que le enseña a java como hablar el idioma de mysql
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // drivermanager.getconnection usa las credenciales para "llamar" a la base de datos y mantener la linea abierta
            conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("Conexion realizada a la base de datos dmari");
            
        }catch (ClassNotFoundException error){
            System.out.println("No se encontro el driver de mysql - "+error.getMessage());
        }catch (SQLException error){
            System.out.println("Error de credenciales o mysql apagado - "+error.getMessage());
        }
        
        return conexion;
    }

    public void cerrar(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexion: " + e.getMessage());
        }
    }
    
}
