package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.carritoDAO;
import com.dmari.modelo.detalleCarrito;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
    objetivo de este archivo:
    gestionar la sincronizacion del carrito entre el navegador y la base de datos.
*/
@WebServlet(name = "CarritoDBController", urlPatterns = {"/carrito-db"})
public class CarritoDBController extends HttpServlet {

    /*
        doget: se dispara cuando el frontend (javascript) quiere leer el carrito guardado en la base de datos.
        esto pasa generalmente cuando el usuario inicia sesion desde un dispositivo nuevo para no perder sus compras.
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        HttpSession sesion = request.getSession(false);
        
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            // extraemos la identidad del usuario para buscar su carrito especifico
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
            carritoDAO dao = new carritoDAO();
            try (PrintWriter out = response.getWriter()) {
                // imprimimos el json directamente hacia el navegador
                out.print(dao.obtenerCarritoJSON(user.getIdUsuario()));
            }
        } else response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /*
        dopost: recibe la canasta de compras actual desde javascript y la manda a guardar a mysql.
        es un proceso destructivo-creativo: borra lo viejo y guarda lo mas reciente para mantener sincronia total.
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
            
            // atrapamos los arreglos de datos que javascript nos envio a traves de urlsearchparams
            String[] ids = request.getParameterValues("id_producto");
            String[] cantidades = request.getParameterValues("cantidad");
            
            ArrayList<detalleCarrito> items = new ArrayList<>();
            // verificamos que la lista no venga vacia o nula
            if (ids != null && cantidades != null) {
                // recorremos los arreglos paralelos para armar los objetos en java
                for (int i = 0; i < ids.length; i++) {
                    detalleCarrito item = new detalleCarrito();
                    item.setIdProductoFk(Integer.parseInt(ids[i]));
                    item.setCantidad(Integer.parseInt(cantidades[i]));
                    // agregamos el item a la lista temporal
                    items.add(item);
                }
            }
            
            // llamamos al dao para que ejecute la transaccion segura
            carritoDAO dao = new carritoDAO();
            if (dao.sincronizarCarrito(user.getIdUsuario(), items)) response.setStatus(HttpServletResponse.SC_OK);
            else response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}