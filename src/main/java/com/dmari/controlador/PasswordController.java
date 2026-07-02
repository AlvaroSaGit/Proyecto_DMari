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
 * objetivo de este archivo:
 * controlador (servlet) dedicado exclusivamente a gestionar la actualizacion
 * de contrasenas de los usuarios. escucha las peticiones post en la ruta
 * '/cambiar-password' y se comunica con el dao para validar y ejecutar el
 * cambio.
 */
@WebServlet(name = "PasswordController", urlPatterns = { "/cambiar-password" })
public class PasswordController extends HttpServlet {

    /*
     * metodo post: actualizacion de contrasena
     * recibe la contrasena actual y la nueva desde el formulario de configuracion.
     * implementa una barrera de seguridad para asegurar que solo un usuario
     * con sesion activa pueda intentar cambiar su propia clave.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // recuperamos la sesion activa sin crear una nueva
        HttpSession sesion = request.getSession(false);
        // validamos que exista una sesion y que el usuario este logueado
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            // extraemos los datos del usuario en sesion
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
            // capturamos la contrasena actual y la nueva desde la peticion
            String passActual = request.getParameter("passActual");
            String passNueva = request.getParameter("passNueva");

            // instanciamos el dao para interactuar con la base de datos
            usuarioDAO dao = new usuarioDAO();
            // intentamos ejecutar el cambio de contrasena y evaluamos el resultado
            if (dao.cambiarPassword(user.getIdUsuario(), passActual, passNueva))
                // devolvemos estado 200 de exito si se logro el cambio
                response.setStatus(HttpServletResponse.SC_OK);
            else
                // devolvemos estado 400 si la clave actual era incorrecta o hubo fallo
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else
            // bloqueamos con un 401 a los intrusos sin sesion valida
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}