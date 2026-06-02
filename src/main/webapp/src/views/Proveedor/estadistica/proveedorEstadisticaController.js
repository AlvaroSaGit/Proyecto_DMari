/*
   objetivo de este archivo:
   controlar la carga de datos de ventas privadas para el proveedor logueado.
*/
import { cargarComponente } from '../../../services/uiService.js';

// inicializa la vista privada del proveedor
export async function cargarVistaProveedorEstadistica() {
    await cargarComponente('component-main', './src/views/Proveedor/estadistica/proveedorEstadistica.html');
    renderizarVentasPrivadas();
}

// consulta al servlet filtrando solo lo que pertenece a este proveedor
async function renderizarVentasPrivadas() {
    try {
        const respuesta = await fetch('api-estadisticas');
        if (!respuesta.ok) throw new Error('error al obtener ventas');
        
        const data = await respuesta.json();

        // mapeamos los datos a los ids de la vista de proveedor
        document.getElementById('prov-ingresos').innerText = `$${data.total_ingresos.toFixed(2)}`;
        document.getElementById('prov-pedidos').innerText = data.cantidad_pedidos;
        document.getElementById('prov-productos').innerText = data.total_productos_vendidos;
        
    } catch (error) {
        console.error('error al cargar panel de proveedor:', error);
    }
}