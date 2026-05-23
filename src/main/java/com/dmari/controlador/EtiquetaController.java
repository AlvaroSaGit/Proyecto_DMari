/*
    objetivo de este archivo:
    el etiqueta controller, su trabajo es traerse las etiquetas de la base de datos
    y traducirlas (usando jsonhelper) para que el frontend pueda mostrarlas.
*/
package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.etiquetaDAO;
import com.dmari.modelo.etiqueta;
import com.dmari.helper.jsonHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "EtiquetaController", urlPatterns = {"/etiquetas"})
public class EtiquetaController extends HttpServlet {

    /*
        doget: atiende las peticiones de lectura que hace javascript.
        su objetivo es recolectar las etiquetas desde mysql y enviarlas al frontend.
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        
        etiquetaDAO dao = new etiquetaDAO();
        ArrayList<etiqueta> lista = dao.listarEtiquetas();
        
        try (PrintWriter out = response.getWriter()) {
            jsonHelper helper = new jsonHelper();
            String jsonString = helper.etiquetasAJson(lista);
            out.print(jsonString);
        }
    }
}