package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;

import com.dmari.dao.metodoPagoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
    objetivo de este archivo:
    proveer la lista de opciones financieras al modal del carrito de compras.
*/
@WebServlet(name = "MetodoPagoController", urlPatterns = {"/metodos-pago"})
public class MetodoPagoController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        metodoPagoDAO dao = new metodoPagoDAO();
        try (PrintWriter out = response.getWriter()) {
            out.print(dao.obtenerMetodosPagoJSON());
        }
    }
}