// importamos el servicio necesario para cargar la vista html
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador
import { navegarA } from '../../../router/router.js';

// funcion principal que renderiza la vista de configuracion del cliente
export async function cargarVistaConfiguracion() {
    // Verificamos primero si el usuario tiene sesion activa
    try {
        const respuesta = await fetch('session');
        if (!respuesta.ok) {
            alert('Debes iniciar sesion para acceder a tu configuracion.');
            navegarA('login'); // Lo mandamos a la pantalla de login
            return; // Detenemos la carga de la vista de configuracion
        }
    } catch (error) {
        console.error('Error al verificar sesion:', error);
        return;
    }

    // inyectamos la estructura html de la configuracion en el main
    await cargarComponente('component-main', './src/views/Cliente/configuracion/configuracion.html');
    
    // capturamos el nuevo boton para ir al historial
    const btnIrHistorial = document.getElementById('btn-ir-historial');
    if (btnIrHistorial) {
        btnIrHistorial.addEventListener('click', () => {
            navegarA('historial'); // Llama al router para cambiar de pantalla
        });
    }
}