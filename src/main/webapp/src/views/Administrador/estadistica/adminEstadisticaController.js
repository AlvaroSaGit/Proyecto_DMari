/*
   objetivo de este archivo:
   controlar la carga de datos financieros globales para el administrador.
*/
import { cargarComponente } from '../../../services/uiService.js';

// funcion principal que inyecta el html y pide los numeros al servidor
export async function cargarVistaAdminEstadistica() {
    await cargarComponente('component-main', './src/views/Administrador/estadistica/adminEstadistica.html');
    renderizarEstadisticas();
}

// hace la peticion al servlet para obtener los ingresos y cantidades
async function renderizarEstadisticas() {
    try {
        const respuesta = await fetch('api-estadisticas');
        if (!respuesta.ok) throw new Error('error al obtener datos');
        
        const data = await respuesta.json();

        // inyectamos los resultados en el dom
        document.getElementById('stat-ingresos').innerText = `$${data.total_ingresos.toFixed(2)}`;
        document.getElementById('stat-comisiones').innerText = `$${data.total_comisiones.toFixed(2)}`;
        document.getElementById('stat-pedidos').innerText = data.cantidad_pedidos;
        document.getElementById('stat-productos').innerText = data.total_productos_vendidos;
        
    } catch (error) {
        console.error('fallo la carga de estadisticas:', error);
    }
}