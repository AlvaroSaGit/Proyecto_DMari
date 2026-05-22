package com.dmari.controlador;

import com.dmari.dao.categoriaDAO;
import com.dmari.modelo.categoria;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "CategoriaController", urlPatterns = {"/categorias"})
public class CategoriaController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        
        categoriaDAO dao = new categoriaDAO();
        ArrayList<categoria> lista = dao.listarCategoriasActivas();
        
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < lista.size(); i++) {
                categoria c = lista.get(i);
                
                json.append("{");
                json.append("\"id\":").append(c.getIdCategoriaPk()).append(",");
                json.append("\"nombre\":\"").append(c.getNombre()).append("\"");
                json.append("}");
                
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            out.print(json.toString());
        }
    }
}