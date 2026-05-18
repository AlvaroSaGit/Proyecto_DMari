/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.dmari.controlador;

// Import del archivo productoDao y producto
import com.dmari.dao.productoDAO;
import com.dmari.modelo.producto;

/*
    Maneja errores especificos del servidor web
*/
import jakarta.servlet.ServletException;
/*
    Es la etiqueta que define la ruta de acceso.
    Al buscar "/listar" se activara este codigo
*/
import jakarta.servlet.annotation.WebServlet;
/*
    La clase base que debemos extender para que este
    archvo sea un controlador
*/
import jakarta.servlet.http.HttpServlet;
/*
    Representa la peticion que llega desde el cliente
    (frontend o navegador)
*/
import jakarta.servlet.http.HttpServletRequest;
/*
    Es la herramienta para construir
    y enviar la respuesta de vuelta al cliente
*/
import jakarta.servlet.http.HttpServletResponse;
/*
    Captura de errores de comunicacion o escritura de datos
*/
import java.io.IOException;
/*
    El objeto que nos permite "escribir" el texto
    (el JSON) en la respuesta
*/
import java.io.PrintWriter;
/*
    Para manejar la lista de productos que nos entregue el DAO
*/
import java.util.ArrayList;


/*
    @WebServlet - Mapea este servlet a la direccion URL "/listar".
*/
@WebServlet(name = "ProductoController", urlPatterns = {"/listar","/insertar","/actualizar","/eliminar"})
public class ProductoController extends HttpServlet {
    /*
    doGet: Responde a peticiones de tipo lectura
    Aqui es donde pedimos la lista a la base de datos para mostrarla
    en el front
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        /*
            Se define que la respuesta es un JSON,
            application/json es el estandar para que
            javascript entienda los datos
        */
        response.setContentType("application/json;charset=UTF-8");
        
        /*Instanciar el DAO y ejecutar el metodo de listar productos*/
        productoDAO dao = new productoDAO();
        ArrayList<producto> lista = dao.listarProductos();
        /*
            Armar el JSON usando stringbuilder
        */
        try(PrintWriter out = response.getWriter()){
            StringBuilder json = new StringBuilder();
            json.append("["); // Inicia la lista JSON

            for (int i = 0; i < lista.size(); i++) {
                producto p = lista.get(i);

                json.append("{");
                json.append("\"id\":").append(p.getIdProductoPk()).append(",");
                json.append("\"nombre\":\"").append(p.getNombreProducto()).append("\",");
                json.append("\"precio\":").append(p.getPrecio()).append(",");
                json.append("\"stock\":").append(p.getStock()).append(",");
                
                /* * Agregamos la ruta de la imagen que traemos desde la tabla 'imagenes'
                 * Asegurate de que en tu clase producto.java el metodo se llame getUrlRuta()
                 */
                json.append("\"imagen\":\"").append(p.getUrlRuta()).append("\"");
                
                json.append("}");

                /* Si no es el ultimo elemento, agregamos una coma para separar los objetos */
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]"); // Cierra la lista JSON
            
            /* Enviamos el texto final al cliente (navegador/frontend) */
            out.print(json.toString());
        }
    }

    /*
    doPost: Responde a peticiones para modificar datos (crear, actualizar, borrar)
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String ruta = request.getServletPath();
        productoDAO dao = new productoDAO();

        if ("/insertar".equals(ruta)) {
            // capturamos parametros
            String nombre = request.getParameter("nombre");
            double precio = Double.parseDouble(request.getParameter("precio"));
            int stock = Integer.parseInt(request.getParameter("stock"));

            // armamos producto
            producto nuevoProd = new producto();
            nuevoProd.setNombreProducto(nombre);
            nuevoProd.setPrecio(precio);
            nuevoProd.setStock(stock);

            // guardamos y validamos
            if (dao.insertarProducto(nuevoProd) > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } else if ("/actualizar".equals(ruta)) {
            producto prod = new producto();
            prod.setIdProductoPk(Integer.parseInt(request.getParameter("id")));
            prod.setNombreProducto(request.getParameter("nombre"));
            prod.setPrecio(Double.parseDouble(request.getParameter("precio")));
            prod.setStock(Integer.parseInt(request.getParameter("stock")));

            if (dao.actualizarProducto(prod)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } else if ("/eliminar".equals(ruta)) {
            int id = Integer.parseInt(request.getParameter("id"));
            // el DAO devuelve un boolean que usamos para el status HTTP
            response.setStatus(dao.eliminarProducto(id) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
