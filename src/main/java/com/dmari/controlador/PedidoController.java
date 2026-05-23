/*
    objetivo de este archivo:
    este servlet se encarga de recibir las compras que hace el cliente 
    desde el carrito en el frontend, y guardarlas de forma segura en 
    las tablas transaccionales de la base de datos (pedido y detalle_pedido).
*/
package com.dmari.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.dmari.dao.pedidoDAO;
import com.dmari.modelo.detallePedido;
import com.dmari.modelo.usuario;
import com.dmari.helper.jsonHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "PedidoController", urlPatterns = {"/pedido"})
public class PedidoController extends HttpServlet {

    /*
        doget: responde a la peticion de lectura del historial de pedidos.
        verifica la sesion y devuelve las compras que el cliente ha realizado.
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
            return;
        }
        
        usuario cliente = (usuario) sesion.getAttribute("usuarioLogueado");
        pedidoDAO dao = new pedidoDAO();
        ArrayList<detallePedido> listaPedidos = dao.listarPedidosPorCliente(cliente.getIdUsuario());
        
        try (PrintWriter out = response.getWriter()) {
            jsonHelper helper = new jsonHelper();
            out.print(helper.pedidosAJson(listaPedidos));
        }
    }

    /*
        dopost: aqui recibimos los arreglos de datos del carrito 
        (ids, cantidades, precios) para procesar la transaccion de venta.
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. verificamos que el usuario tenga sesion activa (que sea un cliente real logueado)
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            // si no esta logueado, le lanzamos un 401 para que javascript lo mande al login
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
            return;
        }
        
        // extraemos los datos del cliente desde la memoria de la sesion
        usuario cliente = (usuario) sesion.getAttribute("usuarioLogueado");
        
        // 2. capturamos los arreglos de datos que envia javascript
        // getparametervalues atrapa multiples datos con el mismo nombre y los vuelve un arreglo[]
        String[] idsProductos = request.getParameterValues("id_producto");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precio");
        
        // validacion de seguridad por si el carrito llego vacio o alterado
        if (idsProductos == null || cantidades == null || precios == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // 3. reconstruimos el carrito en java usando nuestro modelo detallepedido
        ArrayList<detallePedido> carritoJava = new ArrayList<>();
        double totalPagar = 0;
        
        try {
            // iteramos sobre los arreglos para armar los objetos uno por uno
            for (int i = 0; i < idsProductos.length; i++) {
                detallePedido item = new detallePedido();
                item.setIdProductoFk(Integer.parseInt(idsProductos[i]));
                
                int cant = Integer.parseInt(cantidades[i]);
                double prec = Double.parseDouble(precios[i]);
                double subtotal = cant * prec;
                
                item.setCantidad(cant);
                item.setPrecioUnitario(prec);
                item.setSubtotal(subtotal);
                
                carritoJava.add(item);
                totalPagar += subtotal; // vamos sumando la cuenta total de la compra
            }
        } catch (NumberFormatException e) {
            System.out.println("error al convertir numeros del carrito: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // 4. enviamos el carrito armado al dao para que haga la magia de guardar transaccionalmente
        pedidoDAO dao = new pedidoDAO();
        boolean exito = dao.registrarPedido(cliente.getIdUsuario(), totalPagar, carritoJava);
        
        // respondemos al frontend segun el exito de la base de datos
        if (exito) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}