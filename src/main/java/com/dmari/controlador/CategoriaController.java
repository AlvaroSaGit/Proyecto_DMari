/*
    objetivo de este archivo:
    este controlador gestiona las peticiones web relacionadas con las categorias.
    se conecta a la base de datos para obtener las categorias activas y
    utiliza el jsonhelper para enviarlas al frontend en un formato entendible.
*/
package com.dmari.controlador;

import com.dmari.dao.categoriaDAO;
import com.dmari.modelo.categoria;
import com.dmari.helper.jsonHelper;

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

    /*
        doget: atiende las peticiones de lectura que hace el navegador.
        su objetivo es pedirle al dao las categorias y devolverlas al frontend en formato json.
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // setcontenttype le avisa al navegador que la respuesta sera un texto en formato json, 
        // y charset=utf-8 asegura que caracteres latinos (como la ñ) no se rompan en el viaje.
        response.setContentType("application/json;charset=UTF-8");
        
        categoriaDAO dao = new categoriaDAO();
        ArrayList<categoria> lista = dao.listarCategoriasActivas();
        
        // printwriter es la herramienta de java que nos permite "escribir" texto directamente en la respuesta http que viaja por internet hacia el cliente.
        try (PrintWriter out = response.getWriter()) {
            jsonHelper helper = new jsonHelper();
            String jsonString = helper.categoriasAJson(lista);
            out.print(jsonString);
        }
    }
    
    /*
        dopost: atiende las peticiones para modificar datos (crear, editar o cambiar estado).
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        // 1. Leemos si JavaScript nos mando una bandera de "accion"
        String accion = request.getParameter("accion");
        categoriaDAO dao = new categoriaDAO();
        
        // 2. Si la accion es cambiar_estado, ejecutamos esto y cortamos la funcion
        if ("cambiar_estado".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean nuevoEstado = Boolean.parseBoolean(request.getParameter("estado"));
            
            if (dao.cambiarEstado(id, nuevoEstado)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return; // Detenemos la ejecucion aqui para no seguir leyendo abajo
        }

        // 3. Si no fue "cambiar_estado", entonces es Crear o Editar una categoria desde el Modal
        String idStr = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String descripcion = request.getParameter("descripcion");
        
        boolean exito;
        
        if (idStr != null && !idStr.isEmpty()) {
            // Si viene con ID, significa que estamos Editando
            int id = Integer.parseInt(idStr);
            exito = dao.actualizarCategoria(id, nombre, descripcion);
        } else {
            // Si no trae ID, es una categoria nueva
            exito = dao.insertarCategoria(nombre, descripcion);
        }
        
        if (exito) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}