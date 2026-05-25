package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;

import com.dmari.dao.clienteDAO;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
    objetivo de este archivo:
    manejar las peticiones de la vista de "perfil" del cliente.
    permite leer y actualizar su telefono y multiples campos de direccion.
*/
@WebServlet(name = "ClienteController", urlPatterns = {"/perfil-cliente"})
public class ClienteController extends HttpServlet {

    // doget: trae los datos actuales para llenar las cajas de texto automaticamente
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        HttpSession sesion = request.getSession(false);
        
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
            clienteDAO dao = new clienteDAO();
            String[] perfil = dao.obtenerPerfil(user.getIdUsuario());
            
            try (PrintWriter out = response.getWriter()) {
                if (perfil != null) {
                    // armamos un json simple con los datos encontrados (direccion, detalle, telefono, referencia)
                    out.print("{\"direccion\":\"" + (perfil[0] != null ? perfil[0] : "") + "\", " +
                              "\"direccionDetalle\":\"" + (perfil[1] != null ? perfil[1] : "") + "\", " +
                              "\"telefono\":\"" + (perfil[2] != null ? perfil[2] : "") + "\", " +
                              "\"referencia\":\"" + (perfil[3] != null ? perfil[3] : "") + "\"}");
                } else {
                    out.print("{}"); // si esta vacio, mandamos un objeto json vacio
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // dopost: recibe los datos nuevos y los envia al dao para el insert/update magico
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
            String direccion = request.getParameter("direccion");
            String direccionDetalle = request.getParameter("direccionDetalle");
            String telefono = request.getParameter("telefono");
            String referencia = request.getParameter("referencia");
            
            clienteDAO dao = new clienteDAO();
            if (dao.guardarOActualizarPerfil(user.getIdUsuario(), direccion, direccionDetalle, telefono, referencia)) response.setStatus(HttpServletResponse.SC_OK);
            else response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}