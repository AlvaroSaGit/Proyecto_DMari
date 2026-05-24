package com.dmari.controlador;

import com.dmari.helper.databaseHelper;
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

@WebServlet(name = "ListarProveedoresController", urlPatterns = {"/proveedores"})
public class ListarProveedoresController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        response.setContentType("application/json;charset=UTF-8");
        
        // consulta rapida para traer solo a los usuarios que son proveedores (rol 4)
        String sql = "SELECT id_usuario_pk, nombre FROM usuario WHERE id_rol_fk = 4 AND estado_cuenta = 1";
        databaseHelper db = new databaseHelper();
        
        try (PrintWriter out = response.getWriter();
             Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            StringBuilder json = new StringBuilder("[");
            boolean primero = true;
            
            while (rs.next()) {
                if (!primero) {
                    json.append(",");
                }
                json.append("{");
                json.append("\"id_usuario_pk\":").append(rs.getInt("id_usuario_pk")).append(",");
                json.append("\"nombre\":\"").append(rs.getString("nombre")).append("\"");
                json.append("}");
                primero = false;
            }
            
            json.append("]");
            out.print(json.toString());
            
        } catch (Exception e) { 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
        }
    }
}