package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import com.dmari.dao.pedidoDAO;
import com.dmari.modelo.VentaEstadisticaDTO;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Corregimos la URL para que coincida con la petición del Frontend
@WebServlet(name = "EstadisticaController", urlPatterns = {"/api-estadisticas"})
public class EstadisticaController extends HttpServlet {

    private pedidoDAO dao = new pedidoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // seteamos el formato de salida como un objeto json
        response.setContentType("application/json;charset=UTF-8");
        HttpSession sesion = request.getSession(false);
        
        // validacion de seguridad modulo 5: verificar que exista sesion activa
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // segmentamos las estadisticas segun el tipo de usuario logueado
        // recuperamos el objeto de usuario de la sesion para segmentar datos
        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        ArrayList<VentaEstadisticaDTO> datos = new ArrayList<>();
        double[] summaryStats = new double[3]; // [total_ventas, cantidad_pedidos, comisiones_generadas]
        long totalProductosVendidos = 0;

        // modulo 1: aislamiento de datos segun el rol del usuario
        if (user.getIdRol() == 4) {
            // el proveedor (rol 4) solo puede ver el resumen de sus propias ventas
            // primero obtenemos su id de proveedor usando el metodo del dao
            int idProveedor = dao.obtenerIdProveedorPorUsuario(user.getIdUsuario());
            
            if (idProveedor > 0) {
                // si tenemos un id de proveedor valido, consultamos sus estadisticas
                datos = dao.obtenerVentasMensualesProveedor(idProveedor);
                summaryStats = dao.obtenerEstadisticasVentas(idProveedor);
                totalProductosVendidos = dao.obtenerTotalProductosVendidosProveedor(idProveedor);
            } else {
                // si el usuario proveedor no tiene registro en la tabla proveedor, devolvemos datos vacios
                datos = new ArrayList<>();
                summaryStats = new double[]{0, 0, 0};
                totalProductosVendidos = 0;
            }
        } else if (user.getIdRol() == 1) {
            // el administrador (rol 1) puede ver las ventas globales de la plataforma
            datos = dao.obtenerVentasMensualesGlobales();
            summaryStats = dao.obtenerEstadisticasVentas(0); // 0 para estadisticas globales
            totalProductosVendidos = dao.obtenerTotalProductosVendidosGlobales();
            
            // NUEVOS DATOS PARA EL ADMINISTRADOR
            // Solo se obtienen si el rol es administrador
            ArrayList<Map<String, Object>> ventasPorProveedor = dao.obtenerResumenVentasPorProveedorGlobales();
            ArrayList<Map<String, Object>> topProductos = dao.obtenerTopProductosVendidosGlobales(5); // Top 5 productos
        }

        try (PrintWriter out = response.getWriter()) {
            // summaryStats[0] = total_ventas
            // summaryStats[1] = cantidad_pedidos
            // summaryStats[2] = comisiones_generadas

            // Construimos un JSON que incluya los totales para las tarjetas y la lista para la gráfica
            StringBuilder json = new StringBuilder("{");
            json.append("\"total_ingresos\":").append(summaryStats[0]).append(",");
            json.append("\"cantidad_pedidos\":").append((long) summaryStats[1]).append(","); // Cast a long para el conteo
            // Agregamos las comisiones, que el frontend de admin espera
            json.append("\"total_comisiones\":").append(summaryStats[2]).append(",");
            // Agregamos el total de productos vendidos
            json.append("\"total_productos_vendidos\":").append(totalProductosVendidos).append(",");
            
            json.append("\"ventas_mensuales\":[");
            for (int i = 0; i < datos.size(); i++) {
                json.append("{\"etiqueta\":\"").append(datos.get(i).getEtiqueta()).append("\",");
                json.append("\"total\":").append(datos.get(i).getTotal()).append("}");
                if (i < datos.size() - 1) json.append(",");
            }
            json.append("]}");

            // Enviamos el objeto estructurado como el JS lo requiere
            out.print(json.toString());
            out.flush();
        }
    }

    // Helper method to escape JSON strings
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}