/*
   objetivo de este archivo:
   servlet encargado de recibir las peticiones de categorias desde el frontend.
   actua como puente para listar solicitudes del admin o crearlas desde el proveedor.
*/
package com.dmari.controlador;

import com.dmari.dao.SolicitudCategoriaDAO;
import com.dmari.modelo.SolicitudCategoria;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "SolicitudCategoriaController", urlPatterns = {"/solicitudes-categorias"})
public class SolicitudCategoriaController extends HttpServlet {

    // responde a las peticiones de lectura para mostrar la tabla en el panel de administrador
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        SolicitudCategoriaDAO dao = new SolicitudCategoriaDAO();
        // obtenemos la lista de objetos desde el dao
        ArrayList<SolicitudCategoria> lista = dao.listarTodas();

        try (PrintWriter out = response.getWriter()) {
            // construccion manual de json para evitar dependencias externas
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                SolicitudCategoria s = lista.get(i);
                json.append("{");
                json.append("\"id_solicitud_pk\":").append(s.getIdSolicitudPk()).append(",");
                json.append("\"nombre_proveedor\":\"").append(s.getNombreProveedor()).append("\",");
                json.append("\"nombre_sugerido\":\"").append(s.getNombreSugerido()).append("\",");
                json.append("\"estado_solicitud\":\"").append(s.getEstadoSolicitud()).append("\",");
                json.append("\"fecha_creacion\":\"").append(s.getFechaCreacion()).append("\"");
                json.append("}");
                if (i < lista.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
    }

    // procesa las acciones de crear, aprobar o rechazar una solicitud
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        SolicitudCategoriaDAO dao = new SolicitudCategoriaDAO();
        // capturamos la accion que envia javascript (aprobar, rechazar o crear)
        String accion = request.getParameter("accion");
        String idParam = request.getParameter("id");

        boolean exito = false;

        // evaluamos que camino tomar segun la accion solicitada
        if ("aprobar".equals(accion)) {
            String nombre = request.getParameter("nombre");
            exito = dao.aprobarSolicitud(Integer.parseInt(idParam), nombre);
        } else if ("rechazar".equals(accion)) {
            exito = dao.actualizarEstado(Integer.parseInt(idParam), "RECHAZADA");
        } else if ("crear".equals(accion)) {
            // flujo para el proveedor: extraemos su id directamente de la sesion por seguridad
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("idUsuario") != null) {
                int idProv = (int) session.getAttribute("idUsuario");
                String nombre = request.getParameter("nombre");
                String just = request.getParameter("justificacion");
                exito = dao.insertar(idProv, nombre, just);
            }
        }

        // enviamos respuesta ok o error de servidor segun el resultado del dao
        if (exito) response.setStatus(HttpServletResponse.SC_OK);
        else response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
