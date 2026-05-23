package com.dmari.controlador;

import com.dmari.dao.productoDAO;
import com.dmari.helper.jsonHelper;
import com.dmari.modelo.producto;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Controlador (Servlet) encargado de gestionar las peticiones web relacionadas
 * con los productos especificos de un proveedor.
 * Protege la ruta verificando que el usuario tenga una sesion activa y un rol valido
 * antes de consultar la Base de Datos.
 */
@WebServlet(name = "ProductosProveedorController", urlPatterns = {"/productosProveedor"})
public class productosProveedorController extends HttpServlet {
    
    private productoDAO prodDAO = new productoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuramos la respuesta para que el navegador sepa que le enviaremos JSON en formato UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // SEGURIDAD: Verificamos que el proveedor tenga una sesion activa real en el servidor
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"No autorizado\"}");
            return;
        }

        try {
            // Extraemos el usuario completo de la sesion y luego le sacamos el ID
            usuario user = (usuario) session.getAttribute("usuarioLogueado");
            int idUsuario = user.getIdUsuario();

            // Consultamos al DAO para traer unicamente los productos atados a este ID
            ArrayList<producto> misProductos = prodDAO.listarProductosPorProveedor(idUsuario);
            
            jsonHelper helper = new jsonHelper();
            // Convertimos la lista de Java a una cadena JSON y la enviamos al navegador
            response.getWriter().write(helper.productosAJson(misProductos));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
