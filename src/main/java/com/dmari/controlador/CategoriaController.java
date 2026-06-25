package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.categoriaDAO;
import com.dmari.modelo.categoria;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet para la gestion del catalogo de categorias de DMari.
 *
 * Atiende la ruta {@code /categorias} con dos comportamientos segun el metodo HTTP:
 *       GET /categorias - Publica (sin sesion requerida). Si recibe el
 *       parametro todas=true, devuelve todas las categorias incluyendo las pausadas
 *       (para el admin). Sin el parametro, solo devuelve las activas (para el catalogo publico).
 *       POST /categorias - Restringida (requiere rol Administrador o Proveedor).
 *       Crea, actualiza o cambia el estado de una categoria segun la accion enviada.
 */
@WebServlet(name = "CategoriaController", urlPatterns = {"/categorias"})
public class CategoriaController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // configuramos la respuesta para que el navegador sepa que recibira un json
        response.setContentType("application/json;charset=UTF-8");
        
        categoriaDAO dao = new categoriaDAO();
        ArrayList<categoria> lista;
        
        // atrapamos el parametro que manda javascript cuando es el administrador
        String parametroTodas = request.getParameter("todas");
        
        // si el parametro existe y es true, traemos todo (incluso las pausadas)
        if (parametroTodas != null && parametroTodas.equals("true")) {
            lista = dao.listarTodasCategorias();
        } else {
            // si no, solo mandamos las activas para el catalogo publico
            lista = dao.listarCategoriasActivas();
        }
        
        // construimos el json manualmente para evitar depender de librerias externas
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                categoria cat = lista.get(i);
                json.append("{");
                json.append("\"id_categoria_pk\":").append(cat.getIdCategoriaPk()).append(",");
                json.append("\"nombre\":\"").append(cat.getNombre()).append("\",");
                json.append("\"descripcion\":\"").append(cat.getDescripcion() != null ? cat.getDescripcion() : "").append("\",");
                json.append("\"estado_activo\":").append(cat.isEstado_activo());
                json.append("}");
                
                // agregamos una coma si no es el ultimo elemento
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            
            // enviamos la cadena construida al frontend
            out.print(json.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // capa de seguridad: validamos sesion y roles permitidos (admin y proveedor)
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        if (user.getIdRol() != 1 && user.getIdRol() != 4) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        categoriaDAO dao = new categoriaDAO();
        // buscamos si javascript nos envio una "accion" especifica
        String accion = request.getParameter("accion");

        if ("cambiar_estado".equals(accion)) {
            // logica para pausar o activar
            int id = Integer.parseInt(request.getParameter("id"));
            boolean estado = Boolean.parseBoolean(request.getParameter("estado"));
            
            if (dao.cambiarEstado(id, estado)) response.setStatus(HttpServletResponse.SC_OK);
            else response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
        } else {
            // logica para crear o actualizar (el formulario del modal)
            String idParam = request.getParameter("id");
            String nombre = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");

            boolean exito = (idParam != null && !idParam.isEmpty()) ? dao.actualizarCategoria(Integer.parseInt(idParam), nombre, descripcion) : dao.insertarCategoria(nombre, descripcion);
            
            if (exito) response.setStatus(HttpServletResponse.SC_OK);
            else response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}