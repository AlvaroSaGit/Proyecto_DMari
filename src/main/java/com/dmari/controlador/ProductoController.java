/*
    objetivo de este archivo:
    este archivo es un controlador (servlet) que sirve como puente de comunicacion
    entre el frontend (javascript) y la base de datos (dao) para la gestion de productos.
    se encarga de procesar los datos y devolver las respuestas en formato json.
 */
package com.dmari.controlador;

// Import del archivo productoDao y producto
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import com.dmari.dao.etiquetaDAO;
import com.dmari.dao.imagenesDAO;
import com.dmari.dao.productoDAO;
import com.dmari.helper.jsonHelper;
import com.dmari.modelo.producto;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;


/*
    @multipartconfig: permiso especial que le damos al servlet para leer archivos fisicos (fotos).
    le indica a tomcat que este controlador recibira paquetes de tipo multipart/form-data.
*/
@MultipartConfig(
    // limite de memoria ram antes de guardar el archivo temporalmente en disco duro (1mb).
    fileSizeThreshold = 1024 * 1024,
    // peso maximo permitido para una sola foto (10mb).
    maxFileSize = 1024 * 1024 * 10,
    // peso maximo de toda la peticion completa (50mb).
    maxRequestSize = 1024 * 1024 * 50
)
/*
    @webservlet - mapea este servlet a las direcciones url relacionadas con productos.
*/
@WebServlet(name = "ProductoController", urlPatterns = {"/listar","/insertar","/actualizar","/eliminar","/cambiar-estado"})
public class ProductoController extends HttpServlet {

    // doget: responde a peticiones de tipo lectura para mostrar la lista en el frontend.
    // metodo para procesar solicitudes de lectura. filtra por productos activos para el catalogo 
    // o por productos vinculados a un proveedor especifico segun la sesion activa.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        try {
            String paramActivos = request.getParameter("activos");
            boolean soloActivos = paramActivos != null && paramActivos.equals("true");
            
            String paramProveedor = request.getParameter("proveedor");
            boolean esProveedor = paramProveedor != null && paramProveedor.equals("true");
            
            String paramIdProveedor = request.getParameter("id_proveedor");
            boolean filtrarPorProveedor = paramIdProveedor != null && !paramIdProveedor.isEmpty();
            
            productoDAO dao = new productoDAO();
            ArrayList<producto> lista = new ArrayList<>();
            
            if (esProveedor) {
                HttpSession sesion = request.getSession(false);
                if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                    usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                    lista = dao.listarProductosPorProveedor(user.getIdUsuario());
                }
            } else if (filtrarPorProveedor) {
                lista = dao.listarProductosPorProveedor(Integer.parseInt(paramIdProveedor));
            } else {
                lista = dao.listarProductos(soloActivos);
            }
            // instanciamos nuestro nuevo helper para construir la respuesta limpia
            jsonHelper helper = new jsonHelper();
            String jsonString = helper.productosAJson(lista);
            
            out.print(jsonString);
        } catch (Exception e) {
            // Capturamos fallos internos (como 'con' siendo null) para que no rompan el servidor
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // Enviamos un mensaje claro al frontend en formato JSON
            out.print("{\"error\": \"Error al conectar con la base de datos: " + e.getMessage() + "\"}");
            // imprime el rastro de la excepcion en la consola para identificar la linea exacta del fallo
            e.printStackTrace();
        }
    }

    // dopost: responde a peticiones para modificar datos (crear, actualizar, borrar).
    // funciona como un despachador que lee la ruta de la url (servletpath) para 
    // decidir si debe insertar, actualizar, eliminar o cambiar el estado de un producto.
    // @param request peticion con los datos del formulario (multipart/form-data).
    // @param response objeto para devolver codigos de estado ok (200) o error (400).
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
            
            // capturamos la categoria y el estado que faltaban
            String catParam = request.getParameter("id_categoria");
            if (catParam != null && !catParam.isEmpty()) {
                nuevoProd.setIdCategoriaFk(Integer.parseInt(catParam));
            }
            nuevoProd.setEstado(Boolean.parseBoolean(request.getParameter("estado")));
            
            String idProveedorParam = request.getParameter("id_proveedor");
            String etiquetasParam = request.getParameter("etiquetas");

            // guardamos y validamos
            int idGenerado = dao.insertarProducto(nuevoProd);
            if (idGenerado > 0) {
                // Si se creo el producto, atrapamos la foto y la guardamos
                guardarImagenFisica(request, idGenerado);
                
                // NUEVO: Procesamos y guardamos las etiquetas
                if (etiquetasParam != null) {
                    etiquetaDAO etiqDao = new etiquetaDAO();
                    etiqDao.actualizarEtiquetasDeProducto(idGenerado, etiquetasParam);
                }
                
                // NUEVO: Verificamos si quien esta creando el producto es un proveedor.
                // Si es asi, lo enlazamos en la tabla puente para que sea el dueno absoluto y pueda verlo.
                HttpSession sesion = request.getSession(false);
                if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                    usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                    // 4 es el ID del rol proveedor en tu base de datos
                    if (user.getIdRol() == 4) {
                        dao.asignarProductoAProveedor(idGenerado, user.getIdUsuario());
                    } else if (user.getIdRol() == 1) {
                        // si el admin eligio un proveedor de la lista, usamos ese ID
                        if (idProveedorParam != null && !idProveedorParam.isEmpty()) {
                            dao.asignarProductoAProveedor(idGenerado, Integer.parseInt(idProveedorParam));
                        }
                        // IMPORTANTE: Si no eligio a nadie, simplemente no lo enlazamos.
                        // Al no estar en 'proveedor_producto', el sistema asume que es un producto oficial de DMari.
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
            String etiquetasParam = request.getParameter("etiquetas");

            if (dao.actualizarProducto(prod)) {
                // Si se actualizo el producto, verificamos si el Admin subio una foto nueva para reemplazarla
                guardarImagenFisica(request, prod.getIdProductoPk());
                
                // Procesamos y guardamos las etiquetas modificadas
                if (etiquetasParam != null) {
                    etiquetaDAO etiqDao = new etiquetaDAO();
                    etiqDao.actualizarEtiquetasDeProducto(prod.getIdProductoPk(), etiquetasParam);
                }
                
                // actualizamos la tabla puente proveedor_producto si el admin lo cambio
                HttpSession sesion = request.getSession(false);
                if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                    usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                    if (user.getIdRol() == 1) {
                        // Si eligio a alguien, lo pasamos. Si eligio "Sin proveedor", pasamos 0.
                        int idProv = (idProveedorParam != null && !idProveedorParam.isEmpty()) ? Integer.parseInt(idProveedorParam) : 0;
                        // El DAO borrara el enlace viejo y, como le mandamos 0, no creara uno nuevo (volviendolo de DMari)
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

    // metodo auxiliar para procesar el archivo fisico, guardarlo en el servidor y en la bd.
    // gestiona la subida de imagenes transformando el nombre del archivo para evitar espacios.
    // guarda la foto en la carpeta temporal de tomcat y la sincroniza con la carpeta de desarrollo.
    // @param request objeto que contiene la parte binaria del archivo (part).
    // @param idproducto identificador numerico para nombrar el archivo de forma unica.
    private void guardarImagenFisica(HttpServletRequest request, int idProducto) {
        try {
            Part filePart = request.getPart("imagen");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                // IMPORTANTE: Quitamos los espacios en el nombre de la foto porque rompen la URL en HTML
                fileName = fileName.replaceAll("\\s+", "_");
                
                // Obtenemos la raiz del proyecto donde Tomcat esta ejecutandose
                String appPath = request.getServletContext().getRealPath("");
                
                // 1. Guardamos en el servidor temporal (Tomcat) para que cargue inmediatamente en la pagina
                File targetDir = new File(appPath, "src" + File.separator + "img" + File.separator + "productos");
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                
                // Guardamos el archivo fisico temporal
                File targetFile = new File(targetDir, idProducto + "_" + fileName);
                filePart.write(targetFile.getAbsolutePath());
                
                // 2. magia local dinamica: calculamos la ruta fuente cortando el path de despliegue.
                // cuando ejecutas en tu ide, apppath contiene la carpeta "target" o "build".
                // cortamos esa palabra para retroceder a la raiz de tu proyecto, sin importar la pc.
                if (appPath.contains("target")) {
                    String rutaRaiz = appPath.substring(0, appPath.indexOf("target"));
                    String rutaProyecto = rutaRaiz + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "src" + File.separator + "img" + File.separator + "productos";
                    
                    File sourceDir = new File(rutaProyecto);
                    if (!sourceDir.exists()) sourceDir.mkdirs();
                    
                    File sourceFile = new File(sourceDir, idProducto + "_" + fileName);
                    Files.copy(targetFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    System.out.println("FOTO TEMPORAL (TOMCAT): " + targetFile.getAbsolutePath());
                    System.out.println("FOTO PERMANENTE (DINAMICA): " + sourceFile.getAbsolutePath());
                } else {
                    System.out.println("FOTO TEMPORAL (TOMCAT): " + targetFile.getAbsolutePath());
                }
                
                // ruta que va a la base de datos (siempre con diagonales normales '/' para la web).
                String rutaRelativa = "src/img/productos/" + idProducto + "_" + fileName;
                
                imagenesDAO imgDao = new imagenesDAO();
                // borramos las fotos viejas si es una actualizacion.
                imgDao.borrarImagenesDeProducto(idProducto);
                imgDao.insertarImagen(idProducto, rutaRelativa, 1);
            }
        } catch (Exception e) {
            // en caso de fallo en la subida, se imprime el error para depuracion tecnica
            System.out.println("error critico al subir la foto: " + e.getMessage());
            // printstacktrace ayuda a ver si el problema es de permisos de carpeta o de tamaño de archivo
            e.printStackTrace();
        }
    }
}