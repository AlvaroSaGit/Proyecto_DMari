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
import com.dmari.helper.jsonHelper;
import com.dmari.modelo.usuario;

/*
    objetivo de este archivo:
    controlador encargado de recibir las compras del carrito y transformarlas 
    en pedidos reales en la base de datos, ademas de devolver el historial de facturas.
    tambien gestiona los cambios de estado (preparando, enviado, etc.) solicitados 
    por administradores o proveedores.
*/
@WebServlet(name = "PedidoController", urlPatterns = {"/pedido"})
public class PedidoController extends HttpServlet {

    /*
        dopost: procesa las peticiones de modificacion de datos.
        tiene dos funciones principales dependientes de lo que envia el frontend:
        1. cambiar el estado de un pedido (exclusivo para admins/proveedores).
        2. registrar una nueva compra desde el carrito (para los clientes), 
           desempacando los arreglos de productos y calculando totales.
    */
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
        
        // Identificar si la peticion es para cambiar el estado de un pedido existente
        String accion = request.getParameter("accion");
        if ("cambiar_estado".equals(accion)) {
            
            // Medida de seguridad: Solo Administradores (1) y Proveedores (4) pueden hacer esto
            if (user.getIdRol() != 1 && user.getIdRol() != 4) {
                // sc_forbidden (403): detiene a cualquier intruso o cliente sin permisos
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            int idPedido = Integer.parseInt(request.getParameter("id_pedido"));
            String nuevoEstado = request.getParameter("estado");
            
            pedidoDAO dao = new pedidoDAO();
            boolean exito = dao.actualizarEstadoPedido(idPedido, nuevoEstado);
            
            if (exito) {
                response.setStatus(HttpServletResponse.SC_OK); // 200: estado actualizado con exito
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500: fallo interno
            }
            return; // Cortamos la ejecucion aqui para que no intente guardar un carrito
        }
        
        // 2. Atrapamos los arreglos de datos que envio el pedidoService.js
        // getParameterValues atrapa multiples datos con el mismo nombre (porque es un carrito con varios items)
        String[] idsProductos = request.getParameterValues("id_producto");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precio");
        
        // Si el carrito llego vacio o corrupto, rechazamos la peticion
        if (idsProductos == null || idsProductos.length == 0) {
            // sc_bad_request (400): el servidor rechaza la peticion porque faltan datos clave del carrito
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
            // sc_ok (200): todo salio perfecto, el pedido se registro en mysql
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // sc_internal_server_error (500): fallo critico (ej. no habia stock o error sql)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /*
        doget: atiende las peticiones de lectura del historial de compras.
        aplica un filtro de seguridad y privacidad basado en el rol del usuario:
        - rol 1 (administrador): extrae absolutamente todos los pedidos del sistema.
        - rol 4 (proveedor): extrae unicamente los pedidos que incluyen sus productos.
        - rol 2 (cliente): extrae exclusivamente su historial de compras personal.
        finalmente empaca todo en un arreglo json y lo envia al navegador.
    */
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
        
        // Filtramos de forma inteligente dependiendo del rol del usuario
        if (user.getIdRol() == 1) { // 1 = Administrador (ve toda la tienda)
            lista = dao.listarTodosLosPedidos();
        } else if (user.getIdRol() == 4) { // 4 = Proveedor (ve solo sus ventas)
            lista = dao.listarPedidosPorProveedor(user.getIdUsuario());
        } else { // 2 = Cliente
            lista = dao.listarPedidosPorCliente(user.getIdUsuario());
        }
        
        // Utilizamos el helper para convertir la lista a JSON de forma segura y limpia
        jsonHelper helper = new jsonHelper();
        String jsonString = helper.pedidosAJson(lista);
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(jsonString);
    }
}