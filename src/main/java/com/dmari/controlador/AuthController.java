package com.dmari.controlador;

import com.dmari.dao.usuarioDAO;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
    Mapeamos este controlador a dos rutas diferentes.
    Dependiendo de a cual llamemos en el fetch, haremos una u otra cosa.
*/
@WebServlet(name = "AuthController", urlPatterns = {"/login", "/registro", "/logout", "/session"})
public class AuthController extends HttpServlet {

    // GET lo usamos para cerrar sesión y consultar quién está logueado
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ruta = request.getServletPath();
        
        if ("/logout".equals(ruta)) {
            // Invalida (destruye) la sesión actual
            request.getSession().invalidate();
            response.setStatus(HttpServletResponse.SC_OK);
            
        } else if ("/session".equals(ruta)) {
            // Verifica si hay un usuario en la sesión
            usuario user = (usuario) request.getSession().getAttribute("usuarioLogueado");
            
            if (user != null) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                // Devolvemos un pequeño JSON con el nombre del usuario
                response.getWriter().print("{\"nombre\": \"" + user.getNombre() + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Saber a qué ruta exacta hizo la petición el Frontend
        String ruta = request.getServletPath();
        usuarioDAO dao = new usuarioDAO();

        if ("/registro".equals(ruta)) {
            // Capturamos los datos que envió Javascript (gracias al URLSearchParams)
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String password = request.getParameter("password");

            // Armamos el objeto usuario
            usuario nuevoUsuario = new usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setPassword(password);

            // Intentamos guardar en la BD
            if (dao.registrarUsuario(nuevoUsuario)) {
                response.setStatus(HttpServletResponse.SC_OK); // 200 - OK
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 - Error
            }

        } else if ("/login".equals(ruta)) {
            String correo = request.getParameter("correo");
            String password = request.getParameter("password");

            usuario usuarioLogueado = dao.verificarLogin(correo, password);

            if (usuarioLogueado != null) {
                // Si las credenciales son correctas, GUARDAMOS EL USUARIO EN SESIÓN
                request.getSession(true).setAttribute("usuarioLogueado", usuarioLogueado);
                response.setStatus(HttpServletResponse.SC_OK); // 200 - Login correcto
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 - No autorizado (Malas credenciales)
            }
        }
    }
}