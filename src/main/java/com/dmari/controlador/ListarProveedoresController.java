package com.dmari.controlador;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Servlet de consulta de proveedores activos en el sistema DMari.
 *
 * <p>Atiende la ruta {@code GET /proveedores}. Ejecuta una consulta directa
 * a la tabla {@code usuario} para obtener unicamente los usuarios con rol
 * Proveedor (id_rol_fk = 4) y estado de cuenta activo.</p>
 *
 * <p>Este listado se usa principalmente en el panel de administracion para
 * llenar los menus desplegables al momento de crear o editar productos,
 * permitiendo asignar un proveedor dueno a cada articulo.</p>
 *
 * @author Alvaro Andres Salazar Herrera
 * @version 1.0
 */
@WebServlet(name = "ListarProveedoresController", urlPatterns = {"/proveedores"})
public class ListarProveedoresController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        response.setContentType("application/json;charset=UTF-8");
        
        // consulta rapida para traer solo a los usuarios que son proveedores (rol 4)
        String sql = "SELECT id_usuario_pk, nombre FROM usuario WHERE id_rol_fk = 4 AND estado_cuenta = 1";
        databaseHelper db = new databaseHelper();
        
        ArrayList<usuario> lista = new ArrayList<>();
        
        // 1. Primero extraemos los datos de la base de datos de forma segura
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario_pk"));
                u.setNombre(rs.getString("nombre"));
                lista.add(u);
            }
            
        } catch (Exception e) { 
            System.out.println("Error en ListarProveedoresController: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            return; // Cortamos la ejecucion si fallo la bd
        }
        
        // 2. Si todo salio bien con la bd, ahora si enviamos la respuesta al Frontend
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                usuario u = lista.get(i);
                String nombreLimpio = u.getNombre() != null ? u.getNombre().replace("\"", "\\\"") : "";
                
                json.append("{")
                    .append("\"id_usuario_pk\":").append(u.getIdUsuario()).append(",")
                    .append("\"nombre\":\"").append(nombreLimpio).append("\"")
                    .append("}");
                    
                if (i < lista.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
    }
}