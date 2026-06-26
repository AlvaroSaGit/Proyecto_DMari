package com.dmari.controlador;

// importacion del dao de solicitudes para interactuar con la base de datos
import com.dmari.dao.solicitudProveedorDAO;
// importacion de la utilidad json para limpiar textos
import com.dmari.helper.jsonHelper;
// importacion de excepciones de entrada y salida
import java.io.IOException;
// importacion de colecciones de lista
import java.util.List;
// importacion de colecciones de mapas de clave valor
import java.util.Map;
// importaciones de jakarta servlet para compatibilidad con tomcat 10
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// mapeo de la url para atender las peticiones de solicitudes de proveedores
@WebServlet("/solicitudes-proveedor")
public class SolicitudProveedorController extends HttpServlet {
    // instanciacion del dao que maneja la persistencia de las peticiones
    private solicitudProveedorDAO solicitudDAO = new solicitudProveedorDAO();

    // metodo que maneja las solicitudes http get para listar las peticiones
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // obtenemos la sesion actual sin crear una nueva si no existe
        HttpSession sesion = request.getSession(false);
        // validamos si la sesion es nula, si el rol no esta definido o si no es administrador (rol 1)
        if (sesion == null || sesion.getAttribute("rolUsuario") == null || (int) sesion.getAttribute("rolUsuario") != 1) {
            // devolvemos un codigo de estado 403 de acceso prohibido
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // terminamos la ejecucion del metodo
            return;
        }

        // llamamos al dao para obtener la lista de todas las solicitudes de proveedores
        List<Map<String, String>> solicitudes = solicitudDAO.listarSolicitudes();
        
        // creamos un stringbuilder para armar la respuesta json de forma eficiente
        StringBuilder json = new StringBuilder("[");
        // recorremos la lista de solicitudes obtenidas de la base de datos
        for (int i = 0; i < solicitudes.size(); i++) {
            // extraemos el mapa de la solicitud actual
            Map<String, String> s = solicitudes.get(i);
            // concatenamos los campos formateados en json escapando caracteres peligrosos
            json.append("{")
                .append("\"id\":").append(s.get("id")).append(",")
                .append("\"usuarioNombre\":\"").append(s.get("usuarioNombre") != null ? jsonHelper.escaparTexto(s.get("usuarioNombre")) : "").append("\",")
                .append("\"usuarioCorreo\":\"").append(s.get("usuarioCorreo") != null ? jsonHelper.escaparTexto(s.get("usuarioCorreo")) : "").append("\",")
                .append("\"nit\":\"").append(s.get("nit") != null ? jsonHelper.escaparTexto(s.get("nit")) : "").append("\",")
                .append("\"marca\":\"").append(s.get("marca") != null ? jsonHelper.escaparTexto(s.get("marca")) : "").append("\",")
                .append("\"cuenta\":\"").append(s.get("cuenta") != null ? jsonHelper.escaparTexto(s.get("cuenta")) : "").append("\",")
                .append("\"banco\":\"").append(s.get("banco") != null ? jsonHelper.escaparTexto(s.get("banco")) : "").append("\",")
                .append("\"tipoCuenta\":\"").append(s.get("tipoCuenta") != null ? jsonHelper.escaparTexto(s.get("tipoCuenta")) : "").append("\",")
                .append("\"estado\":\"").append(s.get("estado")).append("\"")
                .append("}");
            // si no es el ultimo elemento de la lista agregamos una coma para separar los objetos
            if (i < solicitudes.size() - 1) json.append(",");
        }
        // cerramos el arreglo json
        json.append("]");

        // configuramos el tipo de contenido de la respuesta como json
        response.setContentType("application/json");
        // establecemos la codificacion de caracteres en utf-8 para admitir tildes y caracteres especiales
        response.setCharacterEncoding("UTF-8");
        // escribimos la cadena json construida en el flujo de respuesta del cliente
        response.getWriter().write(json.toString());
    }

    // metodo que maneja las solicitudes http post para procesar una aprobacion o rechazo
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // obtenemos la sesion actual sin crear una nueva
        HttpSession sesion = request.getSession(false);
        // verificamos que exista una sesion valida y que corresponda al administrador (rol 1)
        if (sesion == null || sesion.getAttribute("rolUsuario") == null || (int) sesion.getAttribute("rolUsuario") != 1) {
            // denegamos la operacion con un estado 403
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // finalizamos la ejecucion del metodo
            return;
        }

        // capturamos el id de la solicitud enviado desde el cliente
        String idStr = request.getParameter("id");
        // capturamos la accion a realizar (aprobar o rechazar)
        String accion = request.getParameter("accion");

        // validamos que ninguno de los parametros obligatorios sea nulo
        if (idStr == null || accion == null) {
            // devolvemos un estado 400 de peticion incorrecta
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // finalizamos el flujo
            return;
        }

        // declaramos la variable numerica para el id de la solicitud
        int id;
        try {
            // intentamos convertir la cadena de texto recibida en un entero
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            // si falla la conversion devolvemos un estado 400
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // terminamos la ejecucion por parametro invalido
            return;
        }

        // declaramos una bandera para rastrear el exito de la transaccion
        boolean exito = false;
        // bifurcamos la logica de negocio segun la accion solicitada
        if ("aprobar".equals(accion)) {
            // llamamos al metodo del dao para aprobar la solicitud comercial y activar la cuenta
            exito = solicitudDAO.aprobarSolicitud(id);
        } else if ("rechazar".equals(accion)) {
            // llamamos al metodo del dao para rechazar la solicitud en la base de datos
            exito = solicitudDAO.rechazarSolicitud(id);
        }

        // evaluamos si la operacion fue exitosa en la base de datos
        if (exito) {
            // configuramos el codigo de estado 200 de exito
            response.setStatus(HttpServletResponse.SC_OK);
            // enviamos una respuesta de texto plano simple indicando ok
            response.getWriter().write("OK");
        } else {
            // si la operacion fallo devolvemos un error interno de servidor 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // escribimos el mensaje informativo de fallo
            response.getWriter().write("Error al procesar la solicitud.");
        }
    }
}
