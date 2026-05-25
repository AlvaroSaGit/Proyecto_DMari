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

/*
    objetivo de este archivo:
    recibir la peticion de actualizacion de contrasena desde la vista de configuracion
*/
@WebServlet(name = "PasswordController", urlPatterns = {"/cambiar-password"})
public class PasswordController extends HttpServlet {

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