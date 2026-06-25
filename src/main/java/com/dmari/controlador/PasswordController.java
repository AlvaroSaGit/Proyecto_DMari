package com.dmari.controlador;

import java.io.IOException;

import com.dmari.dao.usuarioDAO;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet dedicado exclusivamente a la actualizacion de contrasenas.
 *
 * <p>Atiende la ruta {@code POST /cambiar-password}. Requiere una sesion activa
 * para garantizar que solo el usuario dueno de la cuenta pueda cambiar su propia
 * clave. Delega la logica de verificacion y actualizacion al
 * {@link com.dmari.dao.usuarioDAO}.</p>
 *
 * @author Alvaro Andres Salazar Herrera
 * @version 1.0
 */
@WebServlet(name = "PasswordController", urlPatterns = {"/cambiar-password"})
public class PasswordController extends HttpServlet {

    /*
        metodo post: actualizacion de contrasena
        recibe la contrasena actual y la nueva desde el formulario de configuracion.
        implementa una barrera de seguridad para asegurar que solo un usuario
        con sesion activa pueda intentar cambiar su propia clave.
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
            String passActual = request.getParameter("passActual");
            String passNueva = request.getParameter("passNueva");
            
            usuarioDAO dao = new usuarioDAO();
            if (dao.cambiarPassword(user.getIdUsuario(), passActual, passNueva)) response.setStatus(HttpServletResponse.SC_OK);
            else response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}