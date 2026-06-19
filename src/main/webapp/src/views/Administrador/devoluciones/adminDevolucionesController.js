import { cargarComponente } from '../../../services/uiService.js';

/**
 * Muestra una notificación elegante en lugar de usar alert()
 * @param {string} mensaje - Texto del mensaje
 * @param {string} tipo - Tipo: 'success', 'error', 'warning', 'info'
 */
function mostrarNotificacion(mensaje, tipo = 'info') {
    const colores = {
        success: { bg: '#d4edda', border: '#c3e6cb', text: '#155724', icon: '✓' },
        error: { bg: '#f8d7da', border: '#f5c6cb', text: '#721c24', icon: '✕' },
        warning: { bg: '#fff3cd', border: '#ffeeba', text: '#856404', icon: '⚠' },
        info: { bg: '#d1ecf1', border: '#bee5eb', text: '#0c5460', icon: 'ℹ' }
    };
    
    const estilo = colores[tipo] || colores.info;
    
    const notif = document.createElement('div');
    notif.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${estilo.bg};
        border: 2px solid ${estilo.border};
        color: ${estilo.text};
        padding: 15px 20px;
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.15);
        font-size: 0.95rem;
        z-index: 10000;
        max-width: 400px;
        animation: slideIn 0.3s ease-in-out;
    `;
    
    notif.innerHTML = `<strong>${estilo.icon}</strong> ${mensaje}`;
    document.body.appendChild(notif);
    
    // Agregar estilos de animación si no existen
    if (!document.getElementById('notif-styles')) {
        const style = document.createElement('style');
        style.id = 'notif-styles';
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(450px); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
        `;
        document.head.appendChild(style);
    }
    
    // Auto-remover después de 3 segundos
    setTimeout(() => {
        notif.style.animation = 'slideIn 0.3s ease-in-out reverse';
        setTimeout(() => notif.remove(), 300);
    }, 3000);
}

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
            const confirm = await mostrarConfirmacion(
                '¿Seguro que deseas APROBAR esta devolución?',
                'Se devolverá el dinero al cliente y el producto al inventario.'
            );
            if (confirm) {
                await procesarDevolucion('aprobar', idDev, idPed);
            }
        });
    });

    document.querySelectorAll('.btn-rechazar').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const idDev = e.target.getAttribute('data-id');
            const idPed = e.target.getAttribute('data-pedido');
            const confirm = await mostrarConfirmacion(
                '¿Seguro que deseas RECHAZAR esta devolución?',
                'El pedido volverá al estado Entregado.'
            );
            if (confirm) {
                await procesarDevolucion('rechazar', idDev, idPed);
            }
        });
    });
}

/**
 * Muestra un modal de confirmación
 */
function mostrarConfirmacion(titulo, mensaje) {
    return new Promise((resolve) => {
        const html = `
            <div style="font-family: Arial, sans-serif;">
                <h3 style="margin-top: 0; color: #ff6b6b; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;">${titulo}</h3>
                <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">${mensaje}</p>
                <div style="text-align: right; margin-top: 20px;">
                    <button id="btn-confirmar-dev" style="background: #4CAF50; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; margin-right: 10px; font-weight: bold;">Aceptar</button>
                    <button id="btn-cancelar-dev" style="background: #f1f1f1; color: #333; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold;">Cancelar</button>
                </div>
            </div>
        `;
        
        if (window.mostrarModal) {
            window.mostrarModal(html);
        } else {
            resolve(confirm(titulo + '\n\n' + mensaje));
            return;
        }
        
        document.getElementById('btn-confirmar-dev').addEventListener('click', () => {
            if (window.cerrarModalGeneral) window.cerrarModalGeneral();
            resolve(true);
        });
        
        document.getElementById('btn-cancelar-dev').addEventListener('click', () => {
            if (window.cerrarModalGeneral) window.cerrarModalGeneral();
            resolve(false);
        });
    });

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
            mostrarNotificacion(`Devolución ${accion === 'aprobar' ? 'aprobada' : 'rechazada'} exitosamente.`, 'success');
            setTimeout(() => cargarListaDevoluciones(), 1500);
        } else {
            mostrarNotificacion('Error al procesar la devolución en el servidor.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarNotificacion('Error de conexión.', 'error');
    }
}
}
