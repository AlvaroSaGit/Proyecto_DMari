/*
   objetivo de este archivo:
   controlar la carga de datos de ventas privadas para el proveedor logueado.
   maneja la comunicacion con el servlet de estadisticas y renderiza graficas.
*/
import { cargarComponente } from '../../../services/uiService.js';

// inicializa la vista privada del proveedor
export async function cargarVistaProveedorEstadistica() {
    await cargarComponente('component-main', './src/views/Proveedor/estadistica/proveedorEstadistica.html');
    inicializarGraficasProveedor();
}

// controlador para el panel de estadisticas del proveedor.
// maneja la comunicacion con el servlet de estadisticas y renderiza graficas.
export async function inicializarGraficasProveedor() {
    const canvas = document.getElementById('graficaVentasProveedor');
    if (!canvas) {
        console.warn('no se encontro el elemento canvas para la grafica de ventas del proveedor.');
        return; // proteccion si el elemento no existe en el html
    }

    try {
        // peticion al backend para obtener los ultimos 6 meses de ventas
        // el servlet 'estadisticas' ya filtra por rol de usuario
        const respuesta = await fetch('estadisticas');
        if (!respuesta.ok) throw new Error('no se pudieron cargar las estadisticas');
        
        const data = await respuesta.json();

        // configuracion de chart.js
        // nota: asegurese de tener el script de chart.js cargado en su index.html
        const ctx = canvas.getContext('2d');
        new Chart(ctx, {
            type: 'line', // grafica de lineas para ver la tendencia
            data: {
                labels: data.etiquetas.reverse(), // mostramos del mas antiguo al mas reciente
                datasets: [{
                    label: 'ventas mensuales ($)',
                    data: data.valores.reverse(),
                    borderColor: '#e91e63', // color representativo de dmari
                    backgroundColor: 'rgba(233, 30, 99, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.3 // curva suave en la linea
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            // formateo de moneda simple en el eje y
                            callback: function(value) {
                                return '$' + value.toLocaleString();
                            }
                        }
                    }
                }
            }
        });
        
    } catch (error) {
        console.error('error al renderizar la grafica de ventas del proveedor:', error);
        // opcional: mostrar un mensaje de error en la interfaz de usuario
        const contenedorGrafica = canvas.parentElement;
        if (contenedorGrafica) {
            contenedorGrafica.innerHTML = '<p style="color: red; text-align: center;">no se pudieron cargar las estadisticas de ventas.</p>';
        }
    }
}