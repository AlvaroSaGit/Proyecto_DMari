/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.dmari.controlador;

// Import del archivo productoDao y producto
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.productoDAO;
import com.dmari.modelo.producto;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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
                json.append("\"categoria\":\"").append(p.getCategoria() != null ? p.getCategoria() : "Sin categoria").append("\",");
                
                // Agregamos las etiquetas como un arreglo (array) de JSON ["Vela", "Aromatica"]
                json.append("\"etiquetas\":[");
                ArrayList<String> tags = p.getEtiquetas();
                for(int j = 0; j < tags.size(); j++){
                    json.append("\"").append(tags.get(j)).append("\"");
                    if(j < tags.size() - 1) json.append(",");
                }
                json.append("],");
                
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
                // SC_OK equivale al codigo HTTP 200 (OK). Indica a Javascript que todo salio perfecto.
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // SC_BAD_REQUEST equivale al codigo HTTP 400 (Bad Request). Indica que hubo un error al guardar.
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } else if ("/actualizar".equals(ruta)) {
            producto prod = new producto();
            prod.setIdProductoPk(Integer.parseInt(request.getParameter("id")));
            prod.setNombreProducto(request.getParameter("nombre"));
            prod.setPrecio(Double.parseDouble(request.getParameter("precio")));
            prod.setStock(Integer.parseInt(request.getParameter("stock")));

            if (dao.actualizarProducto(prod)) {
                // SC_OK (200): El producto se actualizo correctamente en la base de datos
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // SC_BAD_REQUEST (400): Algo fallo, quiza el ID no existe o hubo un error SQL
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } else if ("/eliminar".equals(ruta)) {
            int id = Integer.parseInt(request.getParameter("id"));
            // el DAO devuelve un boolean que usamos para el status HTTP. 
            // Usamos un operador ternario: Si devuelve true -> SC_OK (200), si devuelve false -> SC_BAD_REQUEST (400)
            response.setStatus(dao.eliminarProducto(id) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
