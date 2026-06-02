/*
   objetivo de este archivo:
   servir los datos del dashboard en formato json para el frontend.
   identifica si debe entregar datos globales o privados segun el rol.
*/
package com.dmari.controlador;

import com.dmari.dao.DashboardDAO;
import com.dmari.modelo.DashboardEstadistica;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DashboardController", urlPatterns = {"/api-estadisticas"})
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