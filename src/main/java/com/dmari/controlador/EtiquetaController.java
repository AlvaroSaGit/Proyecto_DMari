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

/**
 * Servlet de lectura del catalogo de etiquetas (tags) de DMari.
 *
 * Atiende la ruta {@code GET /etiquetas}. Obtiene todas las etiquetas
 * disponibles en la base de datos a traves del {@link com.dmari.dao.etiquetaDAO}
 * y las serializa a JSON usando {@link com.dmari.helper.jsonHelper}.
 *
 * Estas etiquetas se usan en los formularios de productos (para asignarlas)
 * y en los filtros del catalogo publico (para busqueda por tag).
 */
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