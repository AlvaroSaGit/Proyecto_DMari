// importamos todos los controladores de vistas del sistema
import { cargarVistaInicio } from '../views/Cliente/inicioCliente/inicioController.js';
import { cargarVistaConfiguracion } from '../views/Cliente/configuracion/configuracionController.js';
import { cargarVistaLogin } from '../views/Auth/login/loginController.js';
import { cargarVistaRegistro } from '../views/Auth/registro/registroController.js';

// Funcion centralizada para cambiar de pantalla en toda la aplicacion
export function navegarA(vista) {
    // 1. Guardamos la vista en memoria para mantenerla si el usuario presiona F5
    sessionStorage.setItem('vistaActual', vista);

    // 2. Cargamos el controlador correspondiente
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

// Funcion para arrancar la aplicacion la primera vez o al recargar con F5
export function inicializarEnrutador() {
    const vistaGuardada = sessionStorage.getItem('vistaActual') || 'inicio';
    navegarA(vistaGuardada);
}