package com.dmari.controlador;

import com.dmari.dao.solicitudProveedorDAO;
import com.dmari.helper.jsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/solicitudes-proveedor")
public class SolicitudProveedorController extends HttpServlet {
    private solicitudProveedorDAO solicitudDAO = new solicitudProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("idRol") == null || (int) sesion.getAttribute("idRol") != 1) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Map<String, String>> solicitudes = solicitudDAO.listarSolicitudes();
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < solicitudes.size(); i++) {
            Map<String, String> s = solicitudes.get(i);
            json.append("{")
                .append("\"id\":").append(s.get("id")).append(",")
                .append("\"usuarioNombre\":\"").append(s.get("usuarioNombre") != null ? jsonHelper.escaparTexto(s.get("usuarioNombre")) : "").append("\",")
                .append("\"usuarioCorreo\":\"").append(s.get("usuarioCorreo") != null ? jsonHelper.escaparTexto(s.get("usuarioCorreo")) : "").append("\",")
                .append("\"nit\":\"").append(s.get("nit") != null ? jsonHelper.escaparTexto(s.get("nit")) : "").append("\",")
                .append("\"marca\":\"").append(s.get("marca") != null ? jsonHelper.escaparTexto(s.get("marca")) : "").append("\",")
                .append("\"cuenta\":\"").append(s.get("cuenta") != null ? jsonHelper.escaparTexto(s.get("cuenta")) : "").append("\",")
                .append("\"banco\":\"").append(s.get("banco") != null ? jsonHelper.escaparTexto(s.get("banco")) : "").append("\",")
                .append("\"tipoCuenta\":\"").append(s.get("tipoCuenta") != null ? jsonHelper.escaparTexto(s.get("tipoCuenta")) : "").append("\",")
                .append("\"estado\":\"").append(s.get("estado")).append("\"")
                .append("}");
            if (i < solicitudes.size() - 1) json.append(",");
        }
        json.append("]");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("idRol") == null || (int) sesion.getAttribute("idRol") != 1) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        String accion = request.getParameter("accion");

        if (idStr == null || accion == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean exito = false;
        if ("aprobar".equals(accion)) {
            exito = solicitudDAO.aprobarSolicitud(id);
        } else if ("rechazar".equals(accion)) {
            exito = solicitudDAO.rechazarSolicitud(id);
        }

        if (exito) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("OK");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error al procesar la solicitud.");
        }
    }
}
