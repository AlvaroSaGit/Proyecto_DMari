import { cargarComponente } from '../../../services/uiService.js';

export async function cargarVistaAdminDevoluciones() {
    await cargarComponente('component-main', './src/views/Administrador/devoluciones/adminDevoluciones.html');
    cargarListaDevoluciones();
}

async function cargarListaDevoluciones() {
    try {
        const respuesta = await fetch('devoluciones');
        if (respuesta.ok) {
            const devoluciones = await respuesta.json();
            const tbody = document.getElementById('tabla-devoluciones-body');
            tbody.innerHTML = '';
            
            if (devoluciones.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No hay devoluciones registradas</td></tr>';
                return;
            }

            devoluciones.forEach(dev => {
                const tr = document.createElement('tr');
                
                let btnHtml = '';
                if (dev.estado_devolucion === 'Solicitada') {
                    btnHtml = `
                        <button class="btn-aprobar" data-id="${dev.id_devolucion_pk}" data-pedido="${dev.id_pedido_fk}" style="background:#28a745; color:white; border:none; padding:5px 10px; border-radius:3px; cursor:pointer;">Aprobar</button>
                        <button class="btn-rechazar" data-id="${dev.id_devolucion_pk}" data-pedido="${dev.id_pedido_fk}" style="background:#dc3545; color:white; border:none; padding:5px 10px; border-radius:3px; cursor:pointer;">Rechazar</button>
                    `;
                }

                tr.innerHTML = `
                    <td>#00${dev.id_devolucion_pk}</td>
                    <td>#00${dev.id_pedido_fk}</td>
                    <td>${dev.nombre_cliente || 'N/A'}</td>
                    <td>${dev.motivo}</td>
                    <td><strong>${dev.estado_devolucion}</strong></td>
                    <td>${dev.fecha_solicitud.split('.')[0]}</td>
                    <td>${btnHtml}</td>
                `;
                tbody.appendChild(tr);
            });
            
            configurarEventosBotones();
        } else {
            document.getElementById('tabla-devoluciones-body').innerHTML = '<tr><td colspan="7" style="text-align:center; color:red;">Error al cargar datos</td></tr>';
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function configurarEventosBotones() {
    document.querySelectorAll('.btn-aprobar').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const idDev = e.target.getAttribute('data-id');
            const idPed = e.target.getAttribute('data-pedido');
            if(confirm('¿Seguro que deseas APROBAR esta devolución? Se devolverá el dinero al cliente y el producto al inventario.')) {
                await procesarDevolucion('aprobar', idDev, idPed);
            }
        });
    });

    document.querySelectorAll('.btn-rechazar').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const idDev = e.target.getAttribute('data-id');
            const idPed = e.target.getAttribute('data-pedido');
            if(confirm('¿Seguro que deseas RECHAZAR esta devolución? El pedido volverá al estado Entregado.')) {
                await procesarDevolucion('rechazar', idDev, idPed);
            }
        });
    });
}

async function procesarDevolucion(accion, idDevolucion, idPedido) {
    const parametros = new URLSearchParams();
    parametros.append('accion', accion);
    parametros.append('idDevolucion', idDevolucion);
    parametros.append('idPedido', idPedido);
    
    try {
        const respuesta = await fetch('devoluciones', {
            method: 'POST',
            body: parametros
        });
        
        if (respuesta.ok) {
            alert(`Devolución ${accion === 'aprobar' ? 'aprobada' : 'rechazada'} exitosamente.`);
            cargarListaDevoluciones(); // Recargar la tabla
        } else {
            alert('Error al procesar la devolución en el servidor.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error de conexión.');
    }
}
