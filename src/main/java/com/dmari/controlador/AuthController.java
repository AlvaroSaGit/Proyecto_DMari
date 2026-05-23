package com.dmari.controlador;

import java.io.IOException;

import com.dmari.dao.usuarioDAO;
import com.dmari.modelo.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
    objetivo de este archivo:
    controlador principal de seguridad. se encarga de recibir las peticiones 
    de inicio de sesion (login), registro, cierre de sesion (logout) 
    y de contestar si hay una sesion activa en el navegador.
*/
@WebServlet(name = "AuthController", urlPatterns = {"/login", "/registro", "/logout", "/session"})
public class AuthController extends HttpServlet {

    /*
        doget: responde a las peticiones de lectura.
        en este controlador se usa para consultar el estado de la sesion (saber si hay alguien logueado) 
        o para destruirla (hacer logout).
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ruta = request.getServletPath();
        
        if ("/logout".equals(ruta)) {
            // getsession(false) busca si el usuario tiene una sesion abierta. 
            // el 'false' es crucial: significa "si no la tiene, no crees una nueva vacia".
            HttpSession sesion = request.getSession(false);
            
            if (sesion != null) {
                // invalidate destruye la sesion por completo en la memoria del servidor (cierra la cuenta).
                sesion.invalidate(); 
            }
            // sc_ok (200) le indica a javascript que la operacion se realizo con exito.
            response.setStatus(HttpServletResponse.SC_OK);
            
        } else if ("/session".equals(ruta)) {
            HttpSession sesion = request.getSession(false);
            
            if (sesion != null && sesion.getAttribute("usuarioLogueado") != null) {
                usuario user = (usuario) sesion.getAttribute("usuarioLogueado");
                
                if (user != null) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().print("{\"nombre\": \"" + user.getNombre() + "\"}");
                }
            } else {
                // sc_unauthorized (401) le dice al frontend que el usuario es un invitado (no esta logueado).
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
            }
        }
    }

    /*
        dopost: responde a las peticiones de envio y modificacion de datos.
        aqui recibe de forma segura (oculta en el cuerpo de la peticion) las credenciales 
        para iniciar sesion o los datos para registrar un usuario nuevo.
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String ruta = request.getServletPath();
        usuarioDAO dao = new usuarioDAO();

        if ("/registro".equals(ruta)) {
            // getparameter extrae el valor de los campos que javascript nos envio a traves de la red
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String password = request.getParameter("password");

            usuario nuevoUsuario = new usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setPassword(password);

            if (dao.registrarUsuario(nuevoUsuario)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // sc_bad_request (400) indica que la peticion fallo (ej. correo ya registrado).
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
            }

        } else if ("/login".equals(ruta)) {
            String correo = request.getParameter("correo");
            String password = request.getParameter("password");

            usuario usuarioLogueado = dao.verificarLogin(correo, password);

            if (usuarioLogueado != null) {
                // getsession(true) fuerza la creacion de un espacio en memoria para guardar quien es el usuario.
                request.getSession(true).setAttribute("usuarioLogueado", usuarioLogueado);
                response.setStatus(HttpServletResponse.SC_OK); 
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
            }
        }
    }
}