package com.dmari.controlador;

import com.dmari.dao.pedidoDAO;
import com.dmari.modelo.VentaEstadisticaDTO;
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

@WebServlet(name = "EstadisticaController", urlPatterns = {"/estadisticas"})
public class EstadisticaController extends HttpServlet {

    private pedidoDAO dao = new pedidoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        HttpSession sesion = request.getSession(false);
        
        // validacion de seguridad modulo 5: verificar que exista sesion activa
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

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
            // construccion manual de json para etiquetas y valores requeridos por chart.js (sin librerias)
            StringBuilder etiquetas = new StringBuilder("[");
            StringBuilder valores = new StringBuilder("[");
            
            for (int i = 0; i < datos.size(); i++) {
                // usamos getEtiqueta() que corresponde al DTO definido en el modelo
                etiquetas.append("\"").append(datos.get(i).getEtiqueta()).append("\"");
                valores.append(datos.get(i).getTotal());
                
                // agregamos coma separadora si no es el ultimo elemento del arreglo
                if (i < datos.size() - 1) { etiquetas.append(","); valores.append(","); }
            }
            etiquetas.append("]"); valores.append("]");

            // enviamos el objeto json final al frontend
            out.print("{\"etiquetas\":" + etiquetas + ", \"valores\":" + valores + "}");
            out.flush();
        }
    }
}