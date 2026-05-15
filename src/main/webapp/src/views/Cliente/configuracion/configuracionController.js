// importamos el servicio necesario para cargar la vista html
import { cargarComponente } from '../../../services/uiService.js';

// funcion principal que renderiza la vista de configuracion del cliente
export async function cargarVistaConfiguracion() {
    // inyectamos la estructura html de la configuracion en el main
    await cargarComponente('component-main', './src/views/Cliente/configuracion/configuracion.html');
    
    // capturamos los botones de las pestanas
    const tabPerfil = document.getElementById('tab-perfil');
    const tabHistorial = document.getElementById('tab-historial');
    
    // capturamos los contenedores de informacion de cada pestana
    const contenidoPerfil = document.getElementById('contenido-perfil');
    const contenidoHistorial = document.getElementById('contenido-historial');
    
    // validacion de seguridad: si no existen los contenedores, salimos
    if (!contenidoPerfil || !contenidoHistorial) return;

    // asignamos el evento de clic a la pestana de perfil (mis datos)
    if (tabPerfil) tabPerfil.addEventListener('click', () => {
        // marcamos esta pestana como activa visualmente y desmarcamos la otra
        tabPerfil.classList.add('activo');
        tabHistorial.classList.remove('activo');
        // mostramos el contenido de perfil y ocultamos el de historial
        contenidoPerfil.classList.remove('oculto');
        contenidoHistorial.classList.add('oculto');
    });

    // asignamos el evento de clic a la pestana de historial de pedidos
    if (tabHistorial) tabHistorial.addEventListener('click', () => {
        // marcamos esta pestana como activa visualmente y desmarcamos la otra
        tabHistorial.classList.add('activo');
        tabPerfil.classList.remove('activo');
        // mostramos el contenido de historial y ocultamos el de perfil
        contenidoHistorial.classList.remove('oculto');
        contenidoPerfil.classList.add('oculto');
    });
}