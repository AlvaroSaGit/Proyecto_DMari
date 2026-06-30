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

/**
 * Servlet de sincronizacion del carrito de compras con la base de datos.
 *
 * Permite que el carrito del cliente (almacenado en localStorage del navegador)
 * se persista en MySQL para sobrevivir cierres de sesion y cambios de
 * dispositivo.
 * 
 * GET /carrito-db - Lee el carrito guardado en BD para el usuario en sesion.
 * POST /carrito-db - Sobreescribe el carrito en BD con los datos actuales del
 * navegador.
 * 
 *
 * 
 */
@WebServlet(name = "CarritoDBController", urlPatterns = { "/carrito-db" })
public class CarritoDBController extends HttpServlet {

    /**
     * metodo get: lectura del carrito
     * se dispara cuando el frontend (javascript) quiere leer el carrito guardado en
     * mysql.
     * esto pasa generalmente cuando el usuario inicia sesion desde un dispositivo
     * nuevo.
     * 
     * @param request  httpservletrequest: la peticion get entrante.
     * @param response httpservletresponse: envia el arreglo json de productos.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // Si ya existe, la devuelve, si no devuelve null
        HttpSession sesion = request.getSession(false);

        // condicional de autorizacion
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            // extraemos la identidad del usuario para buscar su carrito especifico
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
            carritoDAO dao = new carritoDAO();
            try (PrintWriter out = response.getWriter()) {
                // imprimimos el json directamente hacia el navegador
                out.print(dao.obtenerCarritoJSON(user.getIdUsuario()));
            }
        } else
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * metodo post: escritura y sobreescritura rapida (batch)
     * recibe la canasta de compras actual desde javascript y la manda a guardar a
     * mysql.
     * es un proceso destructivo-creativo: borra lo viejo y guarda lo mas reciente.
     * 
     * @param request  httpservletrequest: intercepta el formulario empaquetado.
     * @param response httpservletresponse: despacha estados de control (200 o 500).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        // condicional de seguridad: ignora los intentos de sincronizar si es un
        // visitante anonimo
        if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
            usuario user = (usuario) sesion.getAttribute("usuarioLogueado");

            // atrapamos los arreglos de datos que javascript nos envio a traves de
            // urlsearchparams
            String[] ids = request.getParameterValues("id_producto");
            String[] cantidades = request.getParameterValues("cantidad");

            ArrayList<detalleCarrito> items = new ArrayList<>();
            // verificamos que la lista no venga vacia o nula
            // condicional vital: previene un 'nullpointerexception' si el carrito enviado
            // esta vacio
            if (ids != null && cantidades != null) {
                // recorremos los arreglos paralelos para armar los objetos en java
                // iteracion paralela: avanza por ambos arreglos simultaneamente para unir el id
                // con su cantidad correspondiente
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
            // condicional transaccional: evalua si el dao aprobo todo el lote de
            // inserciones
            if (dao.sincronizarCarrito(user.getIdUsuario(), items))
                response.setStatus(HttpServletResponse.SC_OK);
            else
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}