package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.etiquetaDAO;
import com.dmari.modelo.etiqueta;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "EtiquetaController", urlPatterns = {"/etiquetas"})
public class EtiquetaController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        
        etiquetaDAO dao = new etiquetaDAO();
        ArrayList<etiqueta> lista = dao.listarEtiquetas();
        
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < lista.size(); i++) {
                etiqueta e = lista.get(i);
                
                json.append("{");
                json.append("\"id\":").append(e.getIdEtiquetaPk()).append(",");
                json.append("\"nombre\":\"").append(e.getNombreEtiqueta()).append("\"");
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