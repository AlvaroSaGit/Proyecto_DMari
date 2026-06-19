/*
    objetivo de este archivo:
    este componente visual aisla la logica de construccion html para el historial de compras.
    recibe un objeto de pedido agrupado y retorna un elemento div listo para ser inyectado 
    en la interfaz, mejorando la legibilidad y el mantenimiento del controlador principal.
*/
import { mostrarModal, cerrarModalGeneral } from '../../services/uiService.js';

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

export function crearBloquePedido(pedido) {
    // se define un color por defecto (gris oscuro) para estados desconocidos
    let colorEstado = '#333';
    
    // se asigna un color semantico dependiendo de la fase actual del pedido.
    // usamos includes() porque el estado ahora trae el metodo de pago (ej: "pendiente (nequi)")
    if (pedido.estado.toLowerCase().includes('entregado')) colorEstado = 'green';
    if (pedido.estado.toLowerCase().includes('pendiente')) colorEstado = 'orange';

    // variable para acumular el codigo html de cada producto individual dentro de la factura
    let htmlProductos = '';
    
    // se itera sobre el sub-arreglo de productos que pertenecen exclusivamente a este pedido
    pedido.productos.forEach(prod => {
        const productoSeguro = prod.nombreProducto || prod.nombre_producto || prod.producto || 'Producto sin nombre';
        // se concatena una fila limpia con la cantidad, el nombre y el subtotal formateado a 2 decimales
        htmlProductos += `
            <div style="display:flex; justify-content:space-between; border-bottom:1px solid #eee; padding:5px 0; font-size:0.9rem;">
                <span>${prod.cantidad}x ${productoSeguro}</span>
                <span style="color:#666;">$${prod.subtotal.toFixed(2)}</span>
            </div>
        `;
    });

    // se instancia un nuevo elemento div en la memoria del navegador (dom virtual)
    const div = document.createElement('div');
    
    // se asigna una clase css por si en el futuro se desea aplicar estilos desde una hoja externa
    div.className = 'item-historial';
    
    // se inyectan los estilos en linea para asegurar que la tarjeta se vea como un bloque separado
    div.style.cssText = 'display:block; margin-bottom:15px; border:1px solid #ddd; padding:15px; border-radius:8px; background:#fff; box-shadow: 0 2px 4px rgba(0,0,0,0.02);';
    
    // se ensambla el esqueleto principal de la tarjeta inyectando las variables del pedido y el acumulado de productos
    div.innerHTML = `
        <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:2px solid #f5f5f5; padding-bottom:10px;">
            <div>
                <span style="font-weight:bold; font-size:1.1rem; color:#000;">pedido #00${pedido.id}</span><br>
                <span style="font-size:0.85rem; color:#888;">realizado el: ${pedido.fecha}</span>
            </div>
            <div style="text-align:right;">
                <span style="font-weight:bold; color:${colorEstado}; text-transform:uppercase; font-size:0.85rem;">${pedido.estado}</span><br>
                <span style="font-weight:bold; font-size:1.1rem; color:#000;">total: $${pedido.total.toFixed(2)}</span>
            </div>
        </div>
        
        ${pedido.cliente ? `
        <div style="background-color: #f9f9f9; padding: 12px; border-radius: 6px; margin-bottom: 15px; font-size: 0.9rem; border-left: 4px solid #333;">
            <span style="color: #444;"><b>datos de entrega:</b> ${pedido.cliente}</span>
        </div>
        ` : ''}
        
        <div>
            <p style="margin:0 0 5px 0; font-size:0.85rem; color:#444; font-weight:bold;">resumen de compra</p>
            ${htmlProductos}
        </div>
        
        ${pedido.estado.toLowerCase() === 'pendiente' ? `
        <div style="margin-top: 15px; text-align: right; border-top: 1px solid #eee; padding-top: 10px;">
            <button class="btn-cancelar-pedido" data-id="${pedido.id}" style="background: white; color: #dc3545; border: 1px solid #dc3545; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 0.85rem; transition: background 0.2s;">Cancelar Pedido</button>
        </div>
        ` : ''}

        ${pedido.estado.toLowerCase() === 'entregado' ? `
        <div style="margin-top: 15px; text-align: right; border-top: 1px solid #eee; padding-top: 10px;">
            <button class="btn-devolver-pedido" data-id="${pedido.id}" style="background: white; color: #ff9800; border: 1px solid #ff9800; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 0.85rem; transition: background 0.2s;">Solicitar Devolución</button>
        </div>
        ` : ''}
        
        ${pedido.estado.toLowerCase().includes('cancelado') && pedido.motivo_cancelacion ? `
        <div style="margin-top: 15px; background-color: #fff3f3; padding: 10px; border-radius: 6px; font-size: 0.85rem; border-left: 4px solid #dc3545;">
            <span style="color: #dc3545;"><b>Motivo de Cancelación:</b> ${pedido.motivo_cancelacion}</span>
        </div>
        ` : ''}
    `;
    
    // Configurar evento para botón cancelar si existe
    const btnCancelar = div.querySelector('.btn-cancelar-pedido');
    if (btnCancelar) {
        btnCancelar.addEventListener('click', () => {
            mostrarModalCancelacionCliente(pedido.id);
        });
    }

    // Configurar evento para botón devolución si existe
    const btnDevolver = div.querySelector('.btn-devolver-pedido');
    if (btnDevolver) {
        btnDevolver.addEventListener('click', () => {
            mostrarModalDevolucionCliente(pedido.id);
        });
    }
    
    // se retorna el nodo html completo y listo para ser adjuntado al contenedor principal
    return div;
}

function mostrarModalCancelacionCliente(idPedido) {
    const htmlModal = `
        <div style="font-family: Arial, sans-serif;">
            <h3 style="margin-top: 0; color: #dc3545; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;">⚠️ Solicitar Cancelación</h3>
            <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">
                Como el pedido aún está pendiente, puedes cancelarlo. Por favor dinos el motivo de la cancelación.
            </p>
            
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem;">Motivo *</label>
                <textarea id="motivo-cancelacion-cliente" rows="3" placeholder="Ej: Me equivoqué de producto..." style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; resize: vertical;"></textarea>
            </div>
            
            <div style="text-align: right; margin-top: 20px;">
                <button id="btn-cerrar-modal-cliente" style="background: #f1f1f1; color: #333; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; margin-right: 10px; font-weight: bold;">Cerrar</button>
                <button id="btn-confirmar-cancelacion-cliente" style="background: #dc3545; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold;">Cancelar Pedido</button>
            </div>
        </div>
    `;

    mostrarModal(htmlModal);

    document.getElementById('btn-cerrar-modal-cliente').addEventListener('click', () => {
        cerrarModalGeneral();
    });

    document.getElementById('btn-confirmar-cancelacion-cliente').addEventListener('click', async () => {
        const motivo = document.getElementById('motivo-cancelacion-cliente').value.trim();
        if (motivo.length < 5) {
            mostrarNotificacion('Por favor ingresa un motivo válido (mínimo 5 caracteres).', 'warning');
            return;
        }
        
        cerrarModalGeneral();
        
        const parametros = new URLSearchParams();
        parametros.append('accion', 'actualizar_estado');
        parametros.append('id', idPedido);
        parametros.append('estado', 'Cancelado_por_Cliente');
        parametros.append('motivo', motivo);
        // NOTA: Asumimos que el ID del usuario está en sessionStorage. Si no, el backend lo tomará de la HttpSession.
        
        try { 
            const respuesta = await fetch('pedido', { method: 'POST', body: parametros }); 
            if (respuesta.ok) {
                mostrarNotificacion('Tu pedido ha sido cancelado exitosamente.', 'success'); 
                setTimeout(() => window.location.reload(), 1500);
            } else {
                mostrarNotificacion('Error al procesar la cancelación en el servidor', 'error');
            }
        } catch(err) { 
            mostrarNotificacion('Error de conexión.', 'error'); 
        }
    });
}

function mostrarModalDevolucionCliente(idPedido) {
    const htmlModal = `
        <div style="font-family: Arial, sans-serif;">
            <h3 style="margin-top: 0; color: #ff9800; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;">🔄 Solicitar Devolución</h3>
            <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">
                Por favor indícanos el motivo por el cual deseas devolver este pedido. Nuestro equipo revisará la solicitud.
            </p>
            
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem;">Motivo de Devolución *</label>
                <textarea id="motivo-devolucion-cliente" rows="3" placeholder="Ej: El producto llegó dañado..." style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; resize: vertical;"></textarea>
            </div>
            
            <div style="text-align: right; margin-top: 20px;">
                <button id="btn-cerrar-modal-dev" style="background: #f1f1f1; color: #333; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; margin-right: 10px; font-weight: bold;">Cerrar</button>
                <button id="btn-confirmar-devolucion" style="background: #ff9800; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold;">Enviar Solicitud</button>
            </div>
        </div>
    `;

    mostrarModal(htmlModal);

    document.getElementById('btn-cerrar-modal-dev').addEventListener('click', () => {
        cerrarModalGeneral();
    });

    document.getElementById('btn-confirmar-devolucion').addEventListener('click', async () => {
        const motivo = document.getElementById('motivo-devolucion-cliente').value.trim();
        if (motivo.length < 10) {
            mostrarNotificacion('Por favor ingresa un motivo detallado (mínimo 10 caracteres).', 'warning');
            return;
        }
        
        cerrarModalGeneral();
        
        const parametros = new URLSearchParams();
        parametros.append('accion', 'solicitar');
        parametros.append('idPedido', idPedido);
        parametros.append('motivo', motivo);
        
        try { 
            const respuesta = await fetch('devoluciones', { method: 'POST', body: parametros }); 
            if (respuesta.ok) {
                mostrarNotificacion('Solicitud enviada exitosamente. Revisaremos tu devolución.', 'success'); 
                setTimeout(() => window.location.reload(), 1500);
            } else {
                mostrarNotificacion('Error al procesar la solicitud en el servidor', 'error');
            }
        } catch(err) { 
            mostrarNotificacion('Error de conexión.', 'error'); 
        }
    });
}