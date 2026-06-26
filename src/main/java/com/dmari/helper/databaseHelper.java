
package com.dmari.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
    objetivo de este archivo:
    este es el motor principal de conexion del proyecto.
    su unico trabajo es abrir el puente de comunicacion entre
    el codigo de java y el servidor de la base de datos mysql.
*/
public class databaseHelper {

    // final indica que estas variables son constantes y nadie puede modificarlas mientras el programa corre
    // agregar useUnicode y characterEncoding es OBLIGATORIO para que AES_DECRYPT de MySQL
    // pueda comparar su resultado binario con el String que Java envia correctamente
    private static final String URL = "jdbc:mysql://localhost:3306/DMari?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    /**
     * Abre y devuelve una conexion activa con la base de datos MySQL.
     * Primero carga el driver de MySQL y luego usa las credenciales para conectar.
     *
     * @return objeto Connection listo para ejecutar consultas, o null si fallo la conexion.
     */
    public Connection conectar() {
        Connection conexion = null;
        try {
            // class.forname busca e inicializa el archivo .jar (driver) que le ensena a java como hablar el idioma de mysql
            Class.forName("com.mysql.cj.jdbc.Driver");

            // drivermanager.getconnection usa las credenciales para llamar a la base de datos y mantener la linea abierta
            conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);

        } catch (ClassNotFoundException error) {
            System.out.println("No se encontro el driver de mysql - " + error.getMessage());
        } catch (SQLException error) {
            System.out.println("Error de credenciales o mysql apagado - " + error.getMessage());
        }

        return conexion;
    }

    /**
     * Cierra una conexion activa para liberar recursos del servidor.
     * Se debe llamar en el bloque finally de cualquier metodo que maneje conexiones manuales.
     *
     * @param con la conexion que se quiere cerrar. Si es null, no hace nada.
     */
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
