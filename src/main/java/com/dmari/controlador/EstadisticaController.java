package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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

        // modulo 1: aislamiento de datos segun el rol del usuario
        if (user.getIdRol() == 4) {
            // el proveedor (rol 4) solo puede ver el resumen de sus propias ventas
            datos = dao.obtenerVentasMensualesProveedor(user.getIdUsuario());
        } else if (user.getIdRol() == 1) {
            // el administrador (rol 1) puede ver las ventas globales de la plataforma
            datos = dao.obtenerVentasMensualesGlobales();
        }

        try (PrintWriter out = response.getWriter()) {
            // Calculamos el total de ingresos sumando los datos obtenidos
            double totalIngresos = 0;
            for (VentaEstadisticaDTO d : datos) {
                totalIngresos += d.getTotal();
            }

            // Construimos un JSON que incluya los totales para las tarjetas y la lista para la gráfica
            StringBuilder json = new StringBuilder("{");
            json.append("\"total_ingresos\":").append(totalIngresos).append(",");
            json.append("\"cantidad_pedidos\":").append(datos.size()).append(","); // Conteo base de meses con pedidos
            json.append("\"total_productos_vendidos\":").append(0).append(",");     // Espacio para futura implementación
            
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
}