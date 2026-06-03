/**
 * controlador para el dashboard principal del administrador.
 * modulo 1: visualizacion de la suma global de ventas de la plataforma.
 */
import { cargarComponente } from '../../../services/uiService.js';

export async function cargarVistaAdminDashboard() {
    // cargamos el contenedor visual del dashboard desde la nueva ruta de estadistica
    await cargarComponente('component-main', './src/views/Administrador/estadistica/adminDashboard.html');
    inicializarGraficaGlobal();
}

async function inicializarGraficaGlobal() {
    const canvas = document.getElementById('graficaVentasGlobales');
    if (!canvas) return;

    try {
        // el servlet estadisticas detecta el rol 1 y devuelve el total global
        const respuesta = await fetch('estadisticas');
        if (!respuesta.ok) throw new Error('error al obtener estadisticas globales');

        const data = await respuesta.json();
        const ctx = canvas.getContext('2d');

        new Chart(ctx, {
            type: 'bar', // para el admin usamos barras para resaltar el volumen total
            data: {
                labels: data.etiquetas,
                datasets: [{
                    label: 'ventas globales de la plataforma ($)',
                    data: data.valores,
                    backgroundColor: '#212529', // color oscuro institucional para el admin
                    borderRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: (value) => '$' + value.toLocaleString()
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('error al renderizar dashboard de administrador:', error);
        const contenedor = canvas.parentElement;
        if (contenedor) {
            contenedor.innerHTML = '<p style="color:red; text-align:center;">error al cargar resumen de ventas globales.</p>';
        }
    }
}