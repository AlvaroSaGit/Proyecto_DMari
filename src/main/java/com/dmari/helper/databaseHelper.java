
package com.dmari.helper;

/*
    Es la herramienta es la que crea y mantiene
    la linea abierta entre el java y el mysql
*/
import java.sql.Connection;
/*
    El drivermanager recibe como una llamada
    el link (localhost), el usuario (root) y la clave
*/
import java.sql.DriverManager;
/*
    Esta herramienta atrapa los errores de mysql
*/
import java.sql.SQLException;


public class databaseHelper {

 /*Credenciales para entrar al mysql*/
    /*El url que apunta directo a la base de datos*/
    /* FINAL: indica que ese valor es una constante y un valor intocable*/
    private static final String URL = "jdbc:mysql://localhost:3306/DMari";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";
    
    /*El metodo que llama el dao para pedir el intercambio de informacion*/
    public Connection conectar(){
        /*
            Iniciar la variable en null por si un error ocurre en el try
            el programa tenga algo quedevolver
        */
        
        Connection conexion = null;
        try{
            /*
                Buscar el conector de mysql
            */
            Class.forName("com.mysql.cj.jdbc.Driver");
            /*
                Drivermanager realiza la busqueda de informacion
                con el usuario y clave
            */
            conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("Conexion realizada a la base de datos");
        }catch (ClassNotFoundException error){
            System.out.println("No se encontro el driverde mysql - "+error.getMessage());
        }catch (SQLException error){
            System.out.println("Error de credenciales o mysql apagado - "+error.getMessage());
        }
        
        return conexion;
    }
    
}
