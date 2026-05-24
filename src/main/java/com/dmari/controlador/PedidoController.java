package com.dmari.controlador;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.dmari.dao.pedidoDAO;
import com.dmari.modelo.detallePedido;
import com.dmari.modelo.usuario;

/**
 * Controlador encargado de recibir las compras del carrito y transformarlas 
 * en pedidos reales en la base de datos, ademas de devolver el historial de facturas.
 */
@WebServlet(name = "PedidoController", urlPatterns = {"/pedido"})
public class PedidoController extends HttpServlet {

    // POST: Recibe el carrito desde JavaScript y lo guarda en la base de datos
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Verificamos que el usuario tenga una sesion valida
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        
        // 2. Atrapamos los arreglos de datos que envio el pedidoService.js
        // getParameterValues atrapa multiples datos con el mismo nombre (porque es un carrito con varios items)
        String[] idsProductos = request.getParameterValues("id_producto");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precio");
        
        // Si el carrito llego vacio o corrupto, rechazamos la peticion
        if (idsProductos == null || idsProductos.length == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        ArrayList<detallePedido> carritoList = new ArrayList<>();
        double totalPagar = 0;
        
        // 3. Desempacamos los datos de texto y armamos los objetos Java
        for (int i = 0; i < idsProductos.length; i++) {
            detallePedido item = new detallePedido();
            item.setIdProductoFk(Integer.parseInt(idsProductos[i]));
            item.setCantidad(Integer.parseInt(cantidades[i]));
            item.setPrecioUnitario(Double.parseDouble(precios[i]));
            
            // Calculamos el subtotal de forma segura en el servidor
            double subtotal = item.getCantidad() * item.getPrecioUnitario();
            item.setSubtotal(subtotal);
            totalPagar += subtotal;
            
            carritoList.add(item);
        }
        
        // 4. Mandamos a guardar todo el bloque usando la transaccion segura del DAO
        pedidoDAO dao = new pedidoDAO();
        boolean exito = dao.registrarPedido(user.getIdUsuario(), totalPagar, carritoList);
        
        // Respondemos a JavaScript segun el resultado
        if (exito) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // GET: Devuelve el historial de pedidos dependiendo de quien pregunte
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        pedidoDAO dao = new pedidoDAO();
        ArrayList<detallePedido> lista;
        
        // El DAO filtra inteligentemente dependiendo del rol
        lista = dao.listarPedidosPorCliente(user.getIdUsuario());
        
        // Construimos el JSON a mano para asegurar compatibilidad exacta con el frontend
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            detallePedido dp = lista.get(i);
            json.append("{")
                .append("\"idPedido\": ").append(dp.getIdPedidoFk()).append(",")
                .append("\"fecha\": \"").append(dp.getFechaPedido()).append("\",")
                .append("\"estado\": \"").append(dp.getEstadoPedido()).append("\",")
                .append("\"nombreProducto\": \"").append(dp.getNombreProducto()).append("\",")
                .append("\"cantidad\": ").append(dp.getCantidad()).append(",")
                .append("\"precioUnitario\": ").append(dp.getPrecioUnitario()).append(",")
                .append("\"subtotal\": ").append(dp.getSubtotal())
                .append("}");
            if (i < lista.size() - 1) json.append(",");
        }
        json.append("]");
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(json.toString());
    }
}