/*
    objetivo de este archivo:
    este archivo es un controlador (servlet) que sirve como puente de comunicacion
    entre el frontend (javascript) y la base de datos (dao) para la gestion de productos.
    se encarga de recibir las peticiones web (crear, leer, actualizar, borrar y cambiar estado),
    procesar los datos y devolver las respuestas correspondientes en formato json al navegador.
 */
package com.dmari.controlador;

// Import del archivo productoDao y producto
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.productoDAO;
import com.dmari.modelo.producto;
import com.dmari.modelo.usuario;
import com.dmari.helper.jsonHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


/*
    @webservlet - mapea este servlet a las direcciones de producto
*/
@WebServlet(name = "ProductoController", urlPatterns = {"/listar","/insertar","/actualizar","/eliminar","/cambiar-estado"})
public class ProductoController extends HttpServlet {
    /*
    doget: responde a peticiones de tipo lectura.
    aqui es donde pedimos la lista a la base de datos para mostrarla
    en el frontend.
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        /*
            se define que la respuesta es un json,
            el estandar para que javascript entienda los datos.
        */
        response.setContentType("application/json;charset=UTF-8");
        
        // leemos de la url si debemos filtrar solo los activos
        String paramActivos = request.getParameter("activos");
        boolean soloActivos = paramActivos != null && paramActivos.equals("true");
        
        // leemos si la peticion viene exclusivamente de un proveedor
        String paramProveedor = request.getParameter("proveedor");
        boolean esProveedor = paramProveedor != null && paramProveedor.equals("true");
        
        productoDAO dao = new productoDAO();
        ArrayList<producto> lista = new ArrayList<>();
        
        if (esProveedor) {
            // obtenemos la sesion actual sin crear una nueva
            HttpSession sesion = request.getSession(false);
            if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                lista = dao.listarProductosPorProveedor(user.getIdUsuario());
            }
        } else {
            // si no es proveedor, aplicamos la logica del cliente (activos) o admin (todos)
            lista = dao.listarProductos(soloActivos);
        }
        
        /*
            armar el json usando stringbuilder
        */
        try(PrintWriter out = response.getWriter()){
            
            // instanciamos nuestro nuevo helper para construir la respuesta limpia
            jsonHelper helper = new jsonHelper();
            String jsonString = helper.productosAJson(lista);
            
            out.print(jsonString);
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

        } else if ("/cambiar-estado".equals(ruta)) {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean estado = Boolean.parseBoolean(request.getParameter("estado"));
            
            if (dao.actualizarEstado(id, estado)) {
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
            
            String catParam = request.getParameter("id_categoria");
            if (catParam != null && !catParam.isEmpty()) {
                prod.setIdCategoriaFk(Integer.parseInt(catParam));
            }
            prod.setEstado(Boolean.parseBoolean(request.getParameter("estado")));

            if (dao.actualizarProducto(prod)) {
                // SC_OK (200): El producto se actualizo correctamente en la base de datos
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // SC_BAD_REQUEST (400): Algo fallo, quiza el ID no existe o hubo un error SQL
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } else if ("/eliminar".equals(ruta)) {
            // capturamos el id del producto que se desea eliminar
            int id = Integer.parseInt(request.getParameter("id"));
            
            if (dao.eliminarProducto(id)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
}