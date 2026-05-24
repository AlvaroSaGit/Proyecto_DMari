package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.usuarioDAO;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
    objetivo de este archivo:
    escuchar las peticiones del administrador para listar al personal 
    y modificar sus roles de acceso o bloquearlos del sistema.
*/
@WebServlet(name = "UsuarioController", urlPatterns = {"/usuarios"})
public class UsuarioController extends HttpServlet {

    // doget: devuelve la lista de usuarios para pintar la tabla html
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        usuarioDAO dao = new usuarioDAO();
        ArrayList<usuario> lista = dao.listarUsuarios();
        
        // construccion del json manual para asegurarnos que los nombres de variables 
        // coincidan exactamente con lo que espera tu archivo javascript
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                usuario usr = lista.get(i);
                json.append("{");
                json.append("\"idUsuario\":").append(usr.getIdUsuario()).append(",");
                json.append("\"nombre\":\"").append(usr.getNombre()).append("\",");
                json.append("\"apellido\":\"").append(usr.getApellido() != null ? usr.getApellido() : "").append("\",");
                json.append("\"correo\":\"").append(usr.getCorreo() != null ? usr.getCorreo() : "").append("\",");
                json.append("\"idRol\":").append(usr.getIdRol()).append(",");
                json.append("\"estadoCuenta\":").append(usr.isEstadoCuenta());
                json.append("}");
                
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            out.print(json.toString());
        }
    }

    // dopost: recibe el id, el nuevo rol y el nuevo estado para actualizar la base de datos
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            int idRol = Integer.parseInt(request.getParameter("id_rol"));
            boolean estado = Boolean.parseBoolean(request.getParameter("estado"));
            
            usuarioDAO dao = new usuarioDAO();
            
            if (dao.actualizarPermisos(id, idRol, estado)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}