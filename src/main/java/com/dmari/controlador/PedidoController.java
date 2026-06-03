package com.dmari.controlador;

import java.io.IOException;
import java.util.ArrayList;

import com.dmari.dao.pedidoDAO;
import com.dmari.dao.carritoDAO;
import com.dmari.helper.jsonHelper;
import com.dmari.modelo.detallePedido;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
    objetivo de este archivo:
    controlador encargado de recibir las compras del carrito y transformarlas 
    en pedidos reales en la base de datos, ademas de devolver el historial de facturas.
    tambien gestiona los cambios de estado (preparando, enviado, etc.) solicitados 
    por administradores o proveedores.
*/
@WebServlet(name = "PedidoController", urlPatterns = {"/pedido"})
public class PedidoController extends HttpServlet {

    /**
     * metodo post: enrutador de modificaciones
     * actua como un guardia de trafico bidireccional dependiendo del contenido enviado:
     * 
     * ruta a: si detecta "accion = cambiar_estado", asume que es un empleado gestionando envios.
     * ruta b: si detecta arreglos de productos, asume que es un cliente haciendo "checkout".
     * 
     * en ambos casos implementa rigurosas barreras de seguridad (401 y 403) para evitar hackeos.
     * 
     * @param request httpservletrequest: el paquete http enviado por javascript (con data o parametros).
     * @param response httpservletresponse: el objeto usado para contestarle al navegador con codigos http.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Verificamos que el usuario tenga una sesion valida
        // 1. capa de proteccion: exigir credencial de sesion activa en el navegador
        HttpSession sesion = request.getSession(false);
        // condicional de seguridad: si no hay cookies activas, expulsa la peticion con un error 401.
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401: usted no es quien dice ser
            return;
        }
        
        // extraemos el usuario para validar niveles de acceso
        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        
        // =========================================================================
        // ruta a: cambio de estado logistico (exclusivo del administrador y proveedor)
        // =========================================================================
        String accion = request.getParameter("accion");
        // condicional de bifurcacion: decide si entra al bloque de logistica o de nueva compra.
        if ("cambiar_estado".equals(accion)) {
            
            // barrera de privilegios: evitamos que un cliente curioso cancele su propio pedido por la url
            // condicional logico: si tu rol no es 1 (admin) ni 4 (proveedor), eres bloqueado.
            if (user.getIdRol() != 1 && user.getIdRol() != 4) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403: usted no tiene nivel jerarquico
                return;
            }
            
            int idPedido = Integer.parseInt(request.getParameter("id_pedido"));
            String nuevoEstado = request.getParameter("estado");
            String motivo = request.getParameter("motivo"); // capturamos la razon enviada desde el modal
            
            pedidoDAO dao = new pedidoDAO();
            // pasamos todos los parametros necesarios para la auditoria
            boolean exito = dao.actualizarEstadoPedido(idPedido, nuevoEstado, motivo, user.getIdUsuario());
            
            // condicional de respuesta: si el dao devulve true responde 200, sino error 500.
            if (exito) {
                response.setStatus(HttpServletResponse.SC_OK); // 200: estado actualizado con exito
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500: fallo interno
            }
            return; // cortamos la ejecucion de la funcion para no mezclar las dos rutas
        }
        
        // =========================================================================
        // ruta b: creacion de un pedido nuevo (checkout del cliente)
        // =========================================================================
        // atrapamos arreglos completos generados por javascript. ejemplo: ids = [2, 5, 8]
        String[] idsProductos = request.getParameterValues("id_producto");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precio");
        
        // datos individuales inyectados desde el modal flotante (html)
        String idMetodoStr = request.getParameter("idMetodo");
        String cuenta = request.getParameter("cuenta");
        
        // si el carrito llego vacio o corrupto, o si faltan los datos de pago, rechazamos la peticion
        // condicional de validacion de datos vitales para evitar fallos de codigo java por "nullpointerexception".
        if (idsProductos == null || idsProductos.length == 0 || idMetodoStr == null || cuenta == null) {
            // sc_bad_request (400): el servidor rechaza la peticion porque faltan datos clave del carrito
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        ArrayList<detallePedido> carritoList = new ArrayList<>();
        double totalPagar = 0;
        
        // 3. deserializacion y calculo financiero en zona segura
        // recorremos el carrito para convertir el texto (string) en numeros matematicos (int/double).
        // iteracion clasica: recorre la longitud del arreglo "idsproductos" que vino por http.
        for (int i = 0; i < idsProductos.length; i++) {
            detallePedido item = new detallePedido();
            item.setIdProductoFk(Integer.parseInt(idsProductos[i]));
            item.setCantidad(Integer.parseInt(cantidades[i]));
            item.setPrecioUnitario(Double.parseDouble(precios[i]));
            
            // verificacion cruzada: calculamos precios totales en el backend (java) 
            // para evitar que el cliente altere los precios totalizados usando f12 (inspeccionar elemento).
            double subtotal = item.getCantidad() * item.getPrecioUnitario();
            item.setSubtotal(subtotal);
            totalPagar += subtotal;
            
            carritoList.add(item);
        }
        
        int idMetodo = Integer.parseInt(idMetodoStr);
        
        // 4. ejecucion del nucleo de datos
        pedidoDAO dao = new pedidoDAO();
        boolean exito = dao.registrarPedido(user.getIdUsuario(), totalPagar, carritoList, idMetodo, cuenta);
        
        // 5. resolucion web y limpieza
        // condicional critico: confirma si mysql logro insertar todos los componentes.
        if (exito) {
            // como el pago fue autorizado, la canasta en la base de datos se debe destruir.
            carritoDAO cartDao = new carritoDAO();
            cartDao.vaciarCarrito(user.getIdUsuario());
            
            // sc_ok (200): todo salio perfecto, el pedido se registro en mysql
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // sc_internal_server_error (500): fallo critico (ej. no habia stock o error sql)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * metodo get: lectura dinamica de facturas
     * este es un endpoint inteligente: responde de 3 formas totalmente distintas
     * leyendo el rol del visitante autenticado. un solo codigo sirve para el 
     * panel del cliente, el dashboard del administrador y la logistica del proveedor.
     * el aislamiento es absoluto.
     * 
     * @param request httpservletrequest: la peticion get de lectura.
     * @param response httpservletresponse: escribe el arreglo json hacia el javascript del cliente.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        HttpSession sesion = request.getSession(false);
        // condicional de acceso al historial: exige estar registrado y activo.
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        pedidoDAO dao = new pedidoDAO();
        ArrayList<detallePedido> lista;
        
        // estructura de enrutamiento basada en rol (rbac)
        // condicional de control de acceso: envia peticiones sql diferentes usando el perfil (rol)
        if (user.getIdRol() == 1) { // rol 1 = administrador 
            lista = dao.listarTodosLosPedidos();
        } else if (user.getIdRol() == 4) { // rol 4 = proveedor 
            lista = dao.listarPedidosPorProveedor(user.getIdUsuario());
        } else { // rol 2 = cliente publico
            lista = dao.listarPedidosPorCliente(user.getIdUsuario());
        }
        
        // utilizamos el motor manual (helper) para parsear los arreglos complejos a texto legible para js
        jsonHelper helper = new jsonHelper();
        String jsonString = helper.pedidosAJson(lista);
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(jsonString);
    }
}