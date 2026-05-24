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
            String descripcion = request.getParameter("descripcion");
            double precio = Double.parseDouble(request.getParameter("precio"));
            int stock = Integer.parseInt(request.getParameter("stock"));

            // armamos producto
            producto nuevoProd = new producto();
            nuevoProd.setNombreProducto(nombre);
            nuevoProd.setDescripcion(descripcion);
            nuevoProd.setPrecio(precio);
            nuevoProd.setStock(stock);
            String idProveedorParam = request.getParameter("id_proveedor");

            // guardamos y validamos
            int idGenerado = dao.insertarProducto(nuevoProd);
            if (idGenerado > 0) {
                // Si se creo el producto, atrapamos la foto y la guardamos
                guardarImagenFisica(request, idGenerado);
                
                // NUEVO: Verificamos si quien esta creando el producto es un proveedor.
                // Si es asi, lo enlazamos en la tabla puente para que sea el dueno absoluto y pueda verlo.
                HttpSession sesion = request.getSession(false);
                if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                    usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                    // 4 es el ID del rol proveedor en tu base de datos
                    if (user.getIdRol() == 4) {
                        dao.asignarProductoAProveedor(idGenerado, user.getIdUsuario());
                    } else if (user.getIdRol() == 1 && idProveedorParam != null && !idProveedorParam.isEmpty()) {
                        // si es el administrador y eligio un proveedor del selector
                        dao.asignarProductoAProveedor(idGenerado, Integer.parseInt(idProveedorParam));
                    }
                }

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
            prod.setDescripcion(request.getParameter("descripcion"));
            prod.setPrecio(Double.parseDouble(request.getParameter("precio")));
            prod.setStock(Integer.parseInt(request.getParameter("stock")));
            
            String catParam = request.getParameter("id_categoria");
            if (catParam != null && !catParam.isEmpty()) {
                prod.setIdCategoriaFk(Integer.parseInt(catParam));
            }
            prod.setEstado(Boolean.parseBoolean(request.getParameter("estado")));
            String idProveedorParam = request.getParameter("id_proveedor");

            if (dao.actualizarProducto(prod)) {
                // Si se actualizo el producto, verificamos si el Admin subio una foto nueva para reemplazarla
                guardarImagenFisica(request, prod.getIdProductoPk());
                
                // actualizamos la tabla puente proveedor_producto si el admin lo cambio
                HttpSession sesion = request.getSession(false);
                if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                    usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                    if (user.getIdRol() == 1) {
                        int idProv = (idProveedorParam != null && !idProveedorParam.isEmpty()) ? Integer.parseInt(idProveedorParam) : 0;
                        dao.actualizarProveedorDeProducto(prod.getIdProductoPk(), idProv);
                    }
                }
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

    // Metodo auxiliar para procesar el archivo fisico, guardarlo en el servidor y en la BD
    private void guardarImagenFisica(HttpServletRequest request, int idProducto) {
        try {
            Part filePart = request.getPart("imagen"); // "imagen" es el nombre que le diste en JS: formData.append('imagen', ...)
            if (filePart != null && filePart.getSize() > 0) {
                // Extraemos el nombre original de la foto (ej: dona_chocolate.jpg)
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                
                // Buscamos la ruta absoluta de tu servidor Tomcat para guardar el archivo fisico
                String uploadPath = getServletContext().getRealPath("") + File.separator + "src" + File.separator + "img" + File.separator + "productos";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs(); // Si la carpeta no existe, la crea
                
                // Guardamos el archivo. Le pegamos el ID al principio para evitar que dos fotos se llamen igual
                filePart.write(uploadPath + File.separator + idProducto + "_" + fileName);
                
                // Creamos la ruta relativa en formato Web (con diagonales normales) que usara el HTML para pintar el <img>
                String rutaRelativa = "src/img/productos/" + idProducto + "_" + fileName;
                
                // Finalmente usamos el DAO que creaste para enlazar la foto al producto en MySQL
                imagenesDAO imgDao = new imagenesDAO();
                imgDao.borrarImagenesDeProducto(idProducto); // Borramos las fotos viejas si es una actualizacion
                imgDao.insertarImagen(idProducto, rutaRelativa, 1);
            }
        } catch (Exception e) {
            System.out.println("error al subir la foto del producto: " + e.getMessage());
        }
    }
}