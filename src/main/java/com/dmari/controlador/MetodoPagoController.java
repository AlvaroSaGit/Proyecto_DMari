package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;

import com.dmari.dao.metodoPagoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet de lectura de metodos de pago disponibles en DMari.
 *
 * Atiende la ruta GET /metodos-pago. Obtiene de la base de datos
 * los metodos de pago activos (bancos, PSE, etc.) y los devuelve en formato
 * JSON para poblar el selector del modal de checkout del carrito.
 */
@WebServlet(name = "MetodoPagoController", urlPatterns = { "/metodos-pago" })
public class MetodoPagoController extends HttpServlet {

    /**
     * metodo get: lectura de opciones
     * invoca al dao para recolectar que bancos o medios estan activos.
     * 
     * @param request  httpservletrequest: disparo ajax del frontend.
     * @param response httpservletresponse: envia un json stringificado.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        metodoPagoDAO dao = new metodoPagoDAO();
        try (PrintWriter out = response.getWriter()) {
            out.print(dao.obtenerMetodosPagoJSON());
        }
    }
}