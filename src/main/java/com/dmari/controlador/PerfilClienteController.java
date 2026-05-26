package com.dmari.controlador;

import java.io.IOException;

import com.dmari.dao.clienteDAO;
import com.dmari.modelo.perfilCliente;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
    objetivo de este archivo:
    controlador (servlet) para gestionar el perfil logistico del cliente.
    escucha las peticiones de javascript en la ruta '/perfil-cliente' 
    para leer (get) o guardar (post) los datos de envio y contacto.
*/
@WebServlet(name = "PerfilClienteController", urlPatterns = {"/perfil-cliente"})
public class PerfilClienteController extends HttpServlet {

    /**
     * metodo get: leer el perfil
     * se ejecuta cuando la pagina "perfil.html" termina de cargar y hace un fetch().
     * busca en las tablas satelite (direccion, telefono) y devuelve un json al frontend.
     * 
     * @param request httpservletrequest: peticion inicial get.
     * @param response httpservletresponse: despacha el json stringificado a la pagina.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. verificamos que el usuario tenga una sesion valida (que no sea un visitante anonimo)
        HttpSession sesion = request.getSession(false);
        // condicional de seguridad: bloquea a los intrusos devolviendo un error de no autorizacion (401).
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401: no autorizado
            return;
        }
        
        // extraemos el objeto usuario de la sesion para saber de quien estamos hablando
        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        
        // 2. llamamos al dao para extraer los datos combinados de las 3 tablas
        clienteDAO dao = new clienteDAO();
        perfilCliente perfil = dao.obtenerPerfil(user.getIdUsuario());
        
        // configuramos la respuesta http como texto json
        response.setContentType("application/json;charset=UTF-8");
        
        // 3. construimos el json. si el dao nos devolvio datos, los inyectamos.
        // si el dao devolvio null (porque es un usuario nuevo), enviamos un json vacio {}.
        // condicional: verifica si el dao logro encontrar al menos un registro previo.
        if (perfil != null) {
            
            // 3.1 candado: aseguramos que el correo jamas llegue vacio cruzando con la sesion actual
            String correoSeguro = "";
            if (perfil.getCorreo() != null && !perfil.getCorreo().trim().isEmpty()) {
                correoSeguro = perfil.getCorreo();
            } else if (user.getCorreo() != null) {
                correoSeguro = user.getCorreo();
            }

            // extraemos y saneamos el nombre y apellido para que no rompan el json si tienen comillas
            String nombreSeguro = user.getNombre() != null ? user.getNombre().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "";
            String apellidoSeguro = user.getApellido() != null ? user.getApellido().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "";

            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"nombre\":\"").append(nombreSeguro).append("\",")
                .append("\"apellido\":\"").append(apellidoSeguro).append("\",")
                // reemplazamos los nulls por textos vacios para que javascript no imprima la palabra "null"
                // se usa un operador ternario en linea (condicion ? verdadero : falso) para evitar imprimir "null".
                .append("\"direccion\":\"").append(perfil.getDireccion() != null ? perfil.getDireccion().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "").append("\",")
                .append("\"direccion_detallada\":\"").append(perfil.getDireccionDetalle() != null ? perfil.getDireccionDetalle().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "").append("\",")
                .append("\"telefono\":\"").append(perfil.getTelefono() != null ? perfil.getTelefono().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "").append("\",")
                .append("\"telefono_secundario\":\"").append(perfil.getTelefonoSecundario() != null ? perfil.getTelefonoSecundario().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "").append("\",")
                .append("\"referencia_ubicacion\":\"").append(perfil.getReferencia() != null ? perfil.getReferencia().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") : "").append("\",")
                .append("\"correo\":\"").append(correoSeguro).append("\"")
                .append("}");
            
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            response.getWriter().print(json.toString());
        } else {
            String nombreSeguro = user.getNombre() != null ? user.getNombre().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "";
            String apellidoSeguro = user.getApellido() != null ? user.getApellido().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "";
            String correoSeguro = user.getCorreo() != null ? user.getCorreo().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "";
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print("{\"nombre\":\"" + nombreSeguro + "\", \"apellido\":\"" + apellidoSeguro + "\", \"correo\":\"" + correoSeguro + "\"}");
        }
    }

    /**
     * metodo post: guardar o actualizar el perfil
     * se dispara cuando el cliente da clic en "guardar perfil".
     * recibe los parametros de texto desde javascript y le ordena al dao que haga los insert o update.
     * 
     * @param request httpservletrequest: objeto que contiene los parametros (datos) digitados en el input.
     * @param response httpservletresponse: envia un codigo 200 de exito o un fallo.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. candado de seguridad: solo logueados pueden guardar datos
        HttpSession sesion = request.getSession(false);
        // condicional estricto: evita que se modifiquen perfiles sin loguearse primero
        if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            return;
        }
        
        usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
        
        // 2. extraemos los textos enviados desde `perfilcontroller.js` (params.append)
        // deben llamarse exactamente igual a como se empaquetaron en javascript.
        String numeroTelefono = request.getParameter("numeroTelefono");
        String direccionPrimaria = request.getParameter("direccionPrimaria");
        String direccionDetalle = request.getParameter("direccionDetalle");
        String referencia = request.getParameter("referencia");
        String telefonoSecundario = request.getParameter("telefonoSecundario");
        
        // 3. validamos que al menos vengan los campos obligatorios
        // condicional de formato: rechaza la operacion si los campos vitales vienen nulos o con texto vacio.
        if (numeroTelefono == null || direccionPrimaria == null || numeroTelefono.trim().isEmpty() || direccionPrimaria.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400: faltan datos
            return;
        }
        
        // 4. se los pasamos a nuestro dao transaccional para que llene las tablas correspondientes
        clienteDAO dao = new clienteDAO();
        boolean exito = dao.guardarOActualizarPerfil(user.getIdUsuario(), direccionPrimaria, direccionDetalle, numeroTelefono, telefonoSecundario, referencia);
        
        // 5. respondemos al navegador
        // condicional final: envia una senal http ok solo si la transaccion completa de las 3 tablas salio bien
        if (exito) {
            response.setStatus(HttpServletResponse.SC_OK); // 200: guardado perfecto
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500: error en base de datos
        }
    }
}
