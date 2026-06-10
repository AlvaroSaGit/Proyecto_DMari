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

        // Renderizar gráfica si hay datos mensuales
        if (data.ventas_mensuales && data.ventas_mensuales.length > 0) {
            renderizarGrafica(data.ventas_mensuales);
        }
        
    } catch (error) {
        console.error('fallo la carga de estadisticas:', error);
    }
}

function renderizarGrafica(ventasMensuales) {
    const ctx = document.getElementById('graficaVentasGlobales').getContext('2d');
    
    const etiquetas = ventasMensuales.map(v => v.etiqueta);
    const montos = ventasMensuales.map(v => v.total);

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: etiquetas,
            datasets: [{
                label: 'Ventas Brutas ($)',
                data: montos,
                borderColor: '#d4a373',
                backgroundColor: 'rgba(212, 163, 115, 0.2)',
                borderWidth: 2,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value;
                        }
                    }
                }
            }
        }
    });
}