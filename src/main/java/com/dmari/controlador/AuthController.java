package com.dmari.controlador;

import java.io.IOException;

import com.dmari.dao.usuarioDAO;
import com.dmari.dao.solicitudProveedorDAO; // Verificamos que este import esté presente
import com.dmari.helper.validacionHelper;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * objetivo de este archivo:
 * controlador principal de seguridad. se encarga de recibir las peticiones 
 * de inicio de sesion (login), registro, cierre de sesion (logout) 
 * y de contestar si hay una sesion activa en el navegador.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/login", "/registro", "/logout", "/session"})
public class AuthController extends HttpServlet {

    /**
     * metodo get: lectura y destruccion de sesion
     * responde a las peticiones de lectura. se usa para consultar el estado 
     * de la sesion (saber si hay alguien logueado) o para destruirla (logout).
     * 
     * @param request httpservletrequest: intercepta la peticion url.
     * @param response httpservletresponse: envia la confirmacion al cliente.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ruta = request.getServletPath();
        
        // condicional de enrutamiento: bifurca la logica segun la url visitada
        if ("/logout".equals(ruta)) {
            HttpSession sesion = request.getSession(false);
            
            // condicional de limpieza: verifica si existe una sesion para destruirla
            if (sesion != null) {
                // invalidate destruye la sesion por completo en la memoria del servidor (cierra la cuenta).
                sesion.invalidate(); 
            }
            // sc_ok (200) le indica a javascript que la operacion se realizo con exito.
            response.setStatus(HttpServletResponse.SC_OK);
            
        } else if ("/session".equals(ruta)) {
            HttpSession sesion = request.getSession(false);
            
            // condicional de seguridad: comprueba si la sesion es activa y legitima
            if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                
                // condicional secundario: evita errores nulos y devuelve el nombre del usuario
                if (user != null) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    // devolvemos el nombre y el rol para que el frontend sepa que permisos tiene el usuario
                    response.getWriter().print("{\"nombre\": \"" + user.getNombre() + "\", \"idRol\": " + user.getIdRol() + "}");
                }
            } else {
                // sc_unauthorized (401) le dice al frontend que el usuario es un invitado (no esta logueado).
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
            }
        }
    }

    /**
     * metodo post: autenticacion y registro
     * responde a las peticiones de envio y modificacion de datos.
     * aqui recibe de forma segura las credenciales para iniciar sesion 
     * o los datos para registrar un usuario nuevo.
     * 
     * @param request httpservletrequest: recolecta cajas de texto (credenciales).
     * @param response httpservletresponse: envia estados 200, 400, o 403.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String ruta = request.getServletPath();
        usuarioDAO dao = new usuarioDAO();

        // condicional de enrutamiento post
        if ("/registro".equals(ruta)) {
            // getparameter extrae el valor de los campos que javascript nos envio a traves de la red
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String password = request.getParameter("password");
            String rol = request.getParameter("rol");

            // validamos el nombre campo por campo para enviar mensajes precisos
            if (!validacionHelper.validarNombre(nombre)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("El nombre es inválido o muy corto.");
                return;
            }
            // validamos el correo buscando el @ y el punto com
            if (!validacionHelper.validarCorreo(correo)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("el correo debe tener @ y terminar en .com");
                return;
            }
            // validamos que la contrasena sea segura segun nuestras reglas
            if (!validacionHelper.validarPassword(password)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("La contraseña requiere 8+ caracteres, 1 mayúscula y 1 número.");
                return;
            }

            usuario nuevoUsuario = new usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellido(request.getParameter("apellido"));
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setPassword(password);
            nuevoUsuario.setIdRol(Integer.parseInt(rol));

            // condicional de insercion: si fue exitoso (correo no repetido) devuelve 200
            int idUsuarioGenerado = dao.registrarUsuario(nuevoUsuario);
            if (idUsuarioGenerado > 0) {
                // Si el registro es de un proveedor, guardamos su solicitud
                if ("4".equals(rol)) {
                    String nit = request.getParameter("nit");
                    String marca = request.getParameter("marca");
                    String cuenta = request.getParameter("cuenta");
                    String banco = request.getParameter("banco");
                    String tipoCuenta = request.getParameter("tipoCuenta");

                    // Validaciones básicas para los campos de proveedor
                    if (nit == null || nit.trim().isEmpty() || marca == null || marca.trim().isEmpty()) {                        
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().print("El NIT y el nombre de la marca son obligatorios para proveedores.");
                        // Opcional: podrías borrar el usuario recién creado para consistencia
                        return;
                    }

                    solicitudProveedorDAO solicitudDao = new solicitudProveedorDAO();
                    solicitudDao.crearSolicitud(idUsuarioGenerado, nit, marca, cuenta, banco, tipoCuenta);
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // sc_bad_request (400) indica que la peticion fallo (ej. correo ya registrado).
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("Este correo electrónico ya está en uso.");
            }

        } else if ("/login".equals(ruta)) {
            String correo = request.getParameter("correo");
            String password = request.getParameter("password");

            // validamos el correo en el inicio de sesion
            if (!validacionHelper.validarCorreo(correo)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print("formato de correo no valido");
                return;
            }
            // validamos la contrasena en el inicio de sesion
            if (!validacionHelper.validarPassword(password)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print("la contrasena no cumple con los requisitos");
                return;
            }

            // Paso 1: Verificamos si el correo existe independientemente de la contraseña
            if (!dao.existeCorreo(correo)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("El correo ingresado no está registrado en nuestro sistema.");
                return;
            }

            // Paso 2: Si el correo existe, intentamos validar la contraseña
            usuario usuarioLogueado = dao.verificarLogin(correo, password);

            // condicional de exito de login
            if (usuarioLogueado != null) {
                // verificamos si el administrador lo bloqueo
                // condicional punitivo: rechaza el acceso (403) si la cuenta esta suspendida
                if (!usuarioLogueado.isEstadoCuenta()) {
                    // sc_forbidden (403) le indica al frontend que la accion esta prohibida
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                // getsession(true) fuerza la creacion de un espacio en memoria para guardar quien es el usuario.
                HttpSession session = request.getSession(true);
                session.setAttribute("usuarioLogueado", usuarioLogueado);
                session.setAttribute("rolUsuario", usuarioLogueado.getIdRol());
                session.setAttribute("idUsuario", usuarioLogueado.getIdUsuario());
                
                // devolvemos el id del rol en formato json para que javascript sepa a donde redirigir
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK); 
                response.getWriter().print("{\"idRol\": " + usuarioLogueado.getIdRol() + "}");
            } else {
                // Si llegamos aquí, el correo existe pero la contraseña es incorrecta
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
                response.getWriter().print("La contraseña es incorrecta. Inténtalo de nuevo.");
            }
        }
    }
}