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
    
    // capturamos los botones de las pestanas
    const tabPerfil = document.getElementById('tab-perfil');
    const tabHistorial = document.getElementById('tab-historial');
    
    // capturamos los contenedores de informacion de cada pestana
    const contenidoPerfil = document.getElementById('contenido-perfil');
    const contenidoHistorial = document.getElementById('contenido-historial');
    
    // validacion de seguridad: si no existen los contenedores, salimos
    if (!contenidoPerfil || !contenidoHistorial) return;

    // Cargamos el historial de pedidos desde el localStorage
    cargarHistorialPedidos();

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

// funcion para leer los pedidos del localStorage e inyectarlos en la vista
function cargarHistorialPedidos() {
    const listaHistorial = document.getElementById('lista-historial-pedidos');
    if (!listaHistorial) return;

    // recuperamos el historial de compras simuladas del carrito
    const historial = JSON.parse(localStorage.getItem('historialPedidosDMari')) || [];

    if (historial.length === 0) {
        listaHistorial.innerHTML = '<p style="color: #666; font-style: italic;">No has realizado ninguna compra todavia.</p>';
        return;
    }

    let html = '';
    // recorremos de atras hacia adelante para ver la compra mas reciente primero
    for (let i = historial.length - 1; i >= 0; i--) {
        const pedido = historial[i];
        
        // armamos un texto resumiendo los articulos (ej: "2x Pan dulce, 1x Pastel")
        const resumenItems = pedido.items.map(item => `${item.cantidad}x ${item.nombre}`).join(' - ');
        
        // Logica para dar color dependiendo del estado dinamico
        let estadoTexto = pedido.estado || 'Pendiente'; // fallback por si tenias compras de prueba viejas guardadas sin estado
        let colorEstado = '#f39c12'; // Naranja por defecto para Pendiente
        
        if (estadoTexto === 'Entregado') colorEstado = '#28a745'; // verde
        else if (estadoTexto === 'En Camino' || estadoTexto === 'Preparando') colorEstado = '#17a2b8'; // azul
        else if (estadoTexto === 'Cancelado') colorEstado = '#dc3545'; // rojo

        html += `
            <li class="item-historial" style="margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #eee; list-style: none;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <strong>Pedido #${pedido.id}</strong>
                    <span style="color: ${colorEstado}; font-weight: bold; font-size: 0.9rem;">${estadoTexto}</span>
                </div>
                <div style="font-size: 0.95rem; color: #444; margin-bottom: 5px;">${resumenItems}</div>
                <div style="font-size: 0.85rem; color: #888;">Fecha: ${pedido.fecha} | Total pagado: $${pedido.total.toFixed(2)}</div>
            </li>
        `;
    }

    listaHistorial.innerHTML = html;
}