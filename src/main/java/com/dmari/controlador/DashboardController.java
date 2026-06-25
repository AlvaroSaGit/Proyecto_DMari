package com.dmari.controlador;

import com.dmari.dao.DashboardDAO;
import com.dmari.modelo.DashboardEstadistica;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet que sirve los datos de resumen del dashboard en formato JSON.
 * 
 * Atiende la ruta {@code GET /api-dashboard-resumen}. Identifica el rol del
 * usuario en sesion y devuelve estadisticas diferenciadas:
 * 
 * Rol 1 (Administrador): estadisticas globales de toda la plataforma.
 * Rol 4 (Proveedor): estadisticas solo de sus propios productos y ventas.
 * Roles sin acceso (ej. Cliente) reciben un HTTP 403 Forbidden.
 *
 */
@WebServlet(name = "DashboardController", urlPatterns = {"/api-dashboard-resumen"})
public class DashboardController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        HttpSession session = request.getSession(false);
        
        // verificamos que exista una sesion activa antes de mostrar numeros
        if (session == null || session.getAttribute("rolUsuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int rol = Integer.parseInt(session.getAttribute("rolUsuario").toString());
        int idUsuario = Integer.parseInt(session.getAttribute("idUsuario").toString());
        
        DashboardDAO dao = new DashboardDAO();
        DashboardEstadistica est;

        // decidimos que datos traer basado en el rol de la sesion
        if (rol == 1) {
            // administrador: total global
            est = dao.obtenerEstadisticasGlobales();
        } else if (rol == 4) {
            // proveedor: solo sus ventas
            est = dao.obtenerEstadisticasPorProveedor(idUsuario);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // construccion manual del objeto json para cumplir con la regla de no usar librerias
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"total_ingresos\":").append(est.getTotalIngresos()).append(",");
            json.append("\"total_comisiones\":").append(est.getTotalComisiones()).append(",");
            json.append("\"cantidad_pedidos\":").append(est.getCantidadPedidos()).append(",");
            json.append("\"total_productos_vendidos\":").append(est.getTotalProductosVendidos());
            json.append("}");
            
            out.print(json.toString());
        }
    }
}