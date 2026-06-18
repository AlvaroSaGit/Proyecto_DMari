/*
   objetivo de este archivo:
   controlar la carga de datos del dashboard global para el administrador.
   maneja la comunicacion con el servlet de estadisticas y renderiza graficas globales.
*/
import { cargarComponente } from '../../../services/uiService.js';

/**
 * carga la vista del dashboard y dispara la inicializacion de datos
 */
export async function cargarVistaAdminDashboard() {
    await cargarComponente('component-main', './src/views/Administrador/estadistica/adminDashboard.html');
    inicializarGraficasGlobales();
}

/**
 * solicita datos al servidor y renderiza la grafica de ventas globales
 */
export async function inicializarGraficasGlobales() {
    const canvas = document.getElementById('graficaVentasGlobales');
    if (!canvas) return;

    try {
        const respuesta = await fetch('api-estadisticas');
        if (!respuesta.ok) throw new Error('no se pudieron cargar las estadisticas globales');
        
        const data = await respuesta.json();

        let etiquetas = [];
        let valores = [];
        
        if (data.ventas_mensuales && data.ventas_mensuales.length > 0) {
            etiquetas = data.ventas_mensuales.map(v => v.etiqueta);
            valores = data.ventas_mensuales.map(v => v.total);
        }
        
        // Actualizar tarjetas de resumen (si existen en el HTML)
        const totalIngresosElement = document.getElementById('admin-total-ingresos');
        const cantidadPedidosElement = document.getElementById('admin-cantidad-pedidos');
        const totalComisionesElement = document.getElementById('admin-total-comisiones');
        const totalProductosVendidosElement = document.getElementById('admin-total-productos-vendidos');

        if (totalIngresosElement) totalIngresosElement.innerText = `$${data.total_ingresos.toFixed(2)}`;
        if (cantidadPedidosElement) cantidadPedidosElement.innerText = data.cantidad_pedidos;
        if (totalComisionesElement) totalComisionesElement.innerText = `$${data.total_comisiones.toFixed(2)}`;
        if (totalProductosVendidosElement) totalProductosVendidosElement.innerText = data.total_productos_vendidos;

        // Renderizar tabla de ventas por proveedor
        const tablaVentasProveedor = document.getElementById('tabla-ventas-proveedor');
        if (tablaVentasProveedor && data.ventas_por_proveedor) {
            tablaVentasProveedor.innerHTML = '';
            data.ventas_por_proveedor.forEach(prov => {
                const row = `
                    <tr>
                        <td>${prov.nombre_proveedor}</td>
                        <td>$${prov.total_ingresos_proveedor.toFixed(2)}</td>
                        <td>${prov.cantidad_pedidos_proveedor}</td>
                        <td>${prov.total_productos_vendidos_proveedor}</td>
                    </tr>
                `;
                tablaVentasProveedor.innerHTML += row;
            });
        }

        // Renderizar tabla de top productos vendidos
        const tablaTopProductos = document.getElementById('tabla-top-productos');
        if (tablaTopProductos && data.top_productos_vendidos) {
            tablaTopProductos.innerHTML = '';
            data.top_productos_vendidos.forEach(prod => {
                const row = `<tr><td>${prod.nombre_producto}</td><td>${prod.cantidad_vendida}</td><td>$${prod.ingresos_generados.toFixed(2)}</td></tr>`;
                tablaTopProductos.innerHTML += row;
            });
        }

        const ctx = canvas.getContext('2d');
        
        // renderizado de grafica global
        new Chart(ctx, {
            type: 'bar', 
            data: {
                labels: etiquetas,
                datasets: [{
                    label: 'Ventas Totales Mensuales ($)',
                    data: valores,
                    backgroundColor: '#e91e63',
                    borderRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
        
    } catch (error) {
        console.error('error al inicializar el dashboard admin:', error);
    }
}