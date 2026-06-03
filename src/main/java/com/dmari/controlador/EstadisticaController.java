package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dmari.dao.pedidoDAO;
import com.dmari.modelo.VentaEstadisticaDTO;
import com.dmari.modelo.usuario;

@WebServlet("/estadisticas")
public class EstadisticaController extends HttpServlet {
    
    private pedidoDAO dao = new pedidoDAO();

    // metodo para procesar la peticion de datos de graficas
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // configuramos la respuesta como json
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        // validamos si existe sesion de usuario activa
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.setStatus(401);
            out.print("{\"error\": \"sesion no iniciada\"}");
            return;
        }

        // recuperamos el objeto de usuario de la sesion
        usuario user = (usuario) session.getAttribute("usuarioLogueado");
        ArrayList<VentaEstadisticaDTO> datos = new ArrayList<>();

        // logica de segmentacion por rol
        // rol 1 es administrador, rol 4 es proveedor
        if (user.getIdRolFk() == 1) {
        if (user.getIdRol() == 1) {
            datos = dao.obtenerEstadisticasGlobales();
        } else if (user.getIdRolFk() == 4) {
            datos = dao.obtenerEstadisticasPorProveedor(user.getIdUsuarioPk());
        } else if (user.getIdRol() == 4) {
            datos = dao.obtenerEstadisticasPorProveedor(user.getIdUsuario());
        }

        // construccion manual del json para evitar dependencias externas
        StringBuilder json = new StringBuilder("{\"etiquetas\":[");
        StringBuilder valores = new StringBuilder("],\"valores\":[");

        // iteramos los datos para llenar los arreglos del json
        for (int i = 0; i < datos.size(); i++) {
            VentaEstadisticaDTO d = datos.get(i);
            json.append("\"").append(d.getEtiqueta()).append("\"");
            valores.append(d.getTotal());
            
            // agregamos coma si no es el ultimo elemento
            if (i < datos.size() - 1) {
                json.append(",");
                valores.append(",");
            }
        }

        // cerramos la estructura del objeto json
        json.append(valores).append("]}");
        
        // enviamos la respuesta al cliente
        out.print(json.toString());
        out.flush();
    }
}