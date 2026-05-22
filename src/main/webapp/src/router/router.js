// importamos todos los controladores de vistas del sistema
import { cargarVistaInicio } from '../views/Cliente/inicioCliente/inicioController.js';
import { cargarVistaConfiguracion } from '../views/Cliente/configuracion/configuracionController.js';
import { cargarVistaLogin } from '../views/Auth/login/loginController.js';
import { cargarVistaRegistro } from '../views/Auth/registro/registroController.js';

// Funcion para cambiar la URL, lo que disparara el evento de hashchange
export function navegarA(vista) {
    // Al cambiar el hash, el navegador dispara automaticamente el evento 'hashchange'
    window.location.hash = vista;
}

// Funcion interna que lee el hash actual y carga la vista correspondiente
function manejarRuta() {
    // Leemos el hash de la URL, le quitamos el '#' (ej: de '#login' a 'login')
    // Si no hay hash, usamos 'inicio' por defecto
    let vista = window.location.hash.replace('#', '') || 'inicio';

    // Cargamos el controlador correspondiente segun la vista
    if (vista === 'configuracion') {
        cargarVistaConfiguracion();
    } else if (vista === 'login') {
        cargarVistaLogin();
    } else if (vista === 'registro') {
        cargarVistaRegistro();
    } else {
        cargarVistaInicio(); // Por defecto carga el inicio
    }
}

// Funcion para arrancar la aplicacion la primera vez
export function inicializarEnrutador() {
    // Escuchamos los cambios en el hash (cuando el usuario usa botones atras/adelante o cambiamos location.hash)
    window.addEventListener('hashchange', manejarRuta);

    // Ejecutamos la funcion una vez para cargar la vista inicial al entrar a la pagina o presionar F5
    manejarRuta();
}