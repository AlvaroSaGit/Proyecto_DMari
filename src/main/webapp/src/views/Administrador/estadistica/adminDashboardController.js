// controlador para el dashboard global del administrador modulo 1
import { cargarComponente } from '../../../services/uiService.js';

/**
 * inicializa la vista del dashboard global inyectando el html y cargando la grafica.
 */
export async function cargarVistaAdminDashboard() {
    // inyeccion del componente visual en el contenedor principal
    await cargarComponente('component-main', './src/views/Administrador/estadistica/adminDashboard.html');
    
    // renderizado de la grafica de tendencia utilizando chart.js
    inicializarGraficaVentas();
}

/**
 * consulta los datos al servidor y genera la representacion visual de ingresos globales.
 */
async function inicializarGraficaVentas() {
    const canvas = document.getElementById('graficaVentasGlobales');
    if (!canvas) return;

    try {
        // peticion al servlet de estadisticas (el backend segmenta por id_rol de la sesion)
        const respuesta = await fetch('estadisticas?global=true');
        if (!respuesta.ok) throw new Error('no se pudo obtener la informacion de ventas');
        
        const datos = await respuesta.json();
        
        // creacion de la instancia de chart.js siguiendo la identidad visual de dmari
        new Chart(canvas, {
            type: 'line',
            data: {
                labels: datos.meses, // ej: ['Enero', 'Febrero', 'Marzo']
                datasets: [{
                    label: 'Ingresos Mensuales',
                    data: datos.valores,
                    borderColor: '#d4a373', // color cafe artesanal de la marca
                    backgroundColor: 'rgba(212, 163, 115, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: '#f0f0f0' } },
                    x: { grid: { display: false } }
                }
            }
        });
    } catch (error) { console.error('error al cargar la grafica del dashboard:', error); }
}