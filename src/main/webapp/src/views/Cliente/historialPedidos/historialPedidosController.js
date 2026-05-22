// importamos el servicio necesario para cargar la vista html
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador
import { navegarA } from '../../../router/router.js';

// funcion principal que renderiza la vista de historial
export async function cargarVistaHistorialPedidos() {
    // Verificamos primero si el usuario tiene sesion activa
    try {
        const respuesta = await fetch('session');
        if (!respuesta.ok) {
            alert('Debes iniciar sesion para acceder a tu historial de pedidos.');
            navegarA('login');
            return;
        }
    } catch (error) {
        console.error('Error al verificar sesion:', error);
        return;
    }

    // inyectamos la estructura html en el main
    await cargarComponente('component-main', './src/views/Cliente/historialPedidos/historialPedidos.html');
    
    // Cargamos el historial de pedidos
    cargarHistorialPedidos();
}

// funcion para leer los pedidos del localStorage e inyectarlos en la vista
function cargarHistorialPedidos() {
    const listaHistorial = document.getElementById('lista-historial-pedidos');
    if (!listaHistorial) return;

    const historial = JSON.parse(localStorage.getItem('historialPedidosDMari')) || [];

    if (historial.length === 0) {
        listaHistorial.innerHTML = '<p style="color: #666; font-style: italic;">No has realizado ninguna compra todavia.</p>';
        return;
    }

    let html = '';
    for (let i = historial.length - 1; i >= 0; i--) {
        const pedido = historial[i];
        const resumenItems = pedido.items.map(item => `${item.cantidad}x ${item.nombre}`).join(' - ');
        
        let estadoTexto = pedido.estado || 'Pendiente';
        let colorEstado = '#f39c12';
        if (estadoTexto === 'Entregado') colorEstado = '#28a745';
        else if (estadoTexto === 'En Camino' || estadoTexto === 'Preparando') colorEstado = '#17a2b8';
        else if (estadoTexto === 'Cancelado') colorEstado = '#dc3545';

        html += `
            <li class="item-historial" style="margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #eee; list-style: none;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <strong>Pedido #${pedido.id}</strong>
                    <span style="color: ${colorEstado}; font-weight: bold; font-size: 0.9rem;">${estadoTexto}</span>
                </div>
                <div style="font-size: 0.95rem; color: #444; margin-bottom: 5px;">${resumenItems}</div>
                <div style="font-size: 0.85rem; color: #888;">Fecha: ${pedido.fecha} | Total pagado: $${pedido.total.toFixed(2)}</div>
            </li>`;
    }
    listaHistorial.innerHTML = html;
}