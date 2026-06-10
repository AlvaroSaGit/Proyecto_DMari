package com.dmari.controlador;

import com.dmari.dao.DevolucionDAO;
import com.dmari.modelo.Devolucion;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "DevolucionController", urlPatterns = {"/devoluciones"})
public class DevolucionController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        HttpSession sesion = request.getSession(false);
        
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        DevolucionDAO dao = new DevolucionDAO();
        ArrayList<Devolucion> lista;

        if (user.getIdRol() == 1) {
            // Administrador ve todas
            lista = dao.listarDevoluciones();
        } else if (user.getIdRol() == 2) {
            // Cliente ve las suyas
            lista = dao.listarDevolucionesPorCliente(user.getIdUsuario());
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Devolucion d = lista.get(i);
                json.append("{");
                json.append("\"id_devolucion_pk\":").append(d.getIdDevolucionPk()).append(",");
                json.append("\"id_pedido_fk\":").append(d.getIdPedidoFk()).append(",");
                json.append("\"motivo\":\"").append(d.getMotivo() != null ? d.getMotivo().replace("\"", "\\\"").replace("\n", "\\n") : "").append("\",");
                json.append("\"estado_devolucion\":\"").append(d.getEstadoDevolucion()).append("\",");
                
                if (d.getNombreCliente() != null) {
                    json.append("\"nombre_cliente\":\"").append(d.getNombreCliente()).append("\",");
                }
                
                json.append("\"fecha_solicitud\":\"").append(d.getFechaSolicitud() != null ? d.getFechaSolicitud().toString() : "").append("\"");
                json.append("}");
                
                if (i < lista.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        String accion = request.getParameter("accion");
        DevolucionDAO dao = new DevolucionDAO();
        boolean exito = false;

        try {
            if ("solicitar".equals(accion)) {
                // Solo clientes
                if (user.getIdRol() != 2) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                int idPedido = Integer.parseInt(request.getParameter("idPedido"));
                String motivo = request.getParameter("motivo");
                exito = dao.solicitarDevolucion(idPedido, user.getIdUsuario(), motivo);
                
            } else if ("aprobar".equals(accion) || "rechazar".equals(accion)) {
                // Solo administradores
                if (user.getIdRol() != 1) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                int idDevolucion = Integer.parseInt(request.getParameter("idDevolucion"));
                int idPedido = Integer.parseInt(request.getParameter("idPedido"));
                
                if ("aprobar".equals(accion)) {
                    exito = dao.aprobarDevolucion(idDevolucion, idPedido);
                } else {
                    exito = dao.rechazarDevolucion(idDevolucion, idPedido);
                }
            }

            if (exito) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
