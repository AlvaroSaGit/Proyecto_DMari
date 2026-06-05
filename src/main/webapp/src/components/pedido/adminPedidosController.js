// importamos los servicios necesarios para la gestion de la interfaz y los datos de pedidos.
// corregimos las rutas de importacion usando rutas relativas solidas para evitar el error 404.
import { cargarComponente, cerrarModalGeneral } from "../../services/uiService.js";
import { cambiarEstadoPedido } from "../../services/pedidoService.js";
import { verFactura } from "../../services/facturaService.js";

export async function cargarVistaAdminPedidos() {
    await cargarComponente('component-main', './src/views/Administrador/pedidos/adminPedidos.html');
    listarPedidosGlobales();
}

async function listarPedidosGlobales() {
    const tbody = document.getElementById('tabla-admin-pedidos-body');
    if (!tbody) return;

    try {
        // peticion al servlet para traer todos los pedidos sin filtros
        const respuesta = await fetch('pedido?admin=true');
        const pedidos = await respuesta.json();

        tbody.innerHTML = '';
        pedidos.forEach(p => {
            const tr = document.createElement('tr');
            // construccion dinamica de la fila con el selector de estados
            // corregimos los nombres de las propiedades (p.idpedido y p.cliente) para que coincidan con el jsonhelper.java
            tr.innerHTML = `
                <td>#FAC-${p.idPedido}</td>
                <td>${p.cliente || 'sin nombre'}</td>
                <td>$${parseFloat(p.subtotal).toFixed(2)}</td>
                <td>
                    <select class="select-estado-pedido" data-id="${p.idPedido}" data-actual="${p.estado}">
                        <option value="Pendiente" ${p.estado.includes('Pendiente') ? 'selected' : ''}>Pendiente</option>
                        <option value="Preparando" ${p.estado.includes('Preparando') ? 'selected' : ''}>Preparando</option>
                        <option value="En Camino" ${p.estado.includes('En Camino') ? 'selected' : ''}>En Camino</option>
                        <option value="Entregado" ${p.estado.includes('Entregado') ? 'selected' : ''}>Entregado</option>
                        <option value="Cancelado_por_Proveedor" ${p.estado.includes('Cancelado') ? 'selected' : ''}>Cancelar (Prov)</option>
                        <option value="Devuelto" ${p.estado.includes('Devuelto') ? 'selected' : ''}>Devuelto</option>
                    </select>
                </td>
                <td><button class="btn-ver-factura" data-id="${p.idPedido}">ver factura</button></td>
            `;
            tbody.appendChild(tr);
        });

        // delegacion de eventos para los botones de ver factura
        // este bloque es vital para que verdetallefactura funcione, ya que los modales no ven funciones globales.
        document.querySelectorAll('.btn-ver-factura').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.currentTarget.getAttribute('data-id');
                // llamamos al servicio de factura para mostrar el detalle importante.
                verFactura(id);
            });
        });

        // escuchamos cambios en los selectores de estado para activar la logica de auditoria
        document.querySelectorAll('.select-estado-pedido').forEach(select => {
            select.addEventListener('change', (e) => manejarCambioEstado(e.target));
        });

    } catch (e) { console.error('error al listar pedidos globales:', e); }
}

/**
 * gestiona la logica de cambio de estado. 
 * si es cancelacion, abre el modal de auditoria.
 */
async function manejarCambioEstado(select) {
    // extraccion de metadatos del elemento select para procesar el cambio
    const idPedido = select.dataset.id;
    const nuevoEstado = select.value;
    // registro del estado previo por si se requiere rollback visual
    const estadoAnterior = select.dataset.actual;

    // modulo 2: si el estado empieza por cancelado o devuelto, pedimos el motivo
    if (nuevoEstado.startsWith('Cancelado') || nuevoEstado === 'Devuelto') {
        window.mostrarModal(`
            <div class="modal-auditoria">
                <h4>confirmar cancelacion de pedido #${idPedido}</h4>
                <p>esta accion ejecutara un rollback de stock en el inventario.</p>
                <textarea id="motivo-cancelacion" placeholder="escriba el motivo de la cancelacion..." style="width:100%; height:80px; margin: 10px 0;"></textarea>
                <div style="display:flex; gap:10px; justify-content: flex-end;">
                    <button id="btn-abortar-cancel" style="background:#eee; border:none; padding:8px;">regresar</button>
                    <button id="btn-confirmar-cancel" style="background:#d32f2f; color:white; border:none; padding:8px;">confirmar y devolver stock</button>
                </div>
            </div>
        `);

        document.getElementById('btn-abortar-cancel').onclick = () => {
            select.value = estadoAnterior;
            cerrarModalGeneral();
        };

        document.getElementById('btn-confirmar-cancel').onclick = async () => {
            const motivo = document.getElementById('motivo-cancelacion').value;
            if (!motivo || motivo.trim().length < 5) {
                alert('debe ingresar un motivo valido para la auditoria.');
                return;
            }
            
            const exito = await cambiarEstadoPedido(idPedido, nuevoEstado, motivo);
            if (exito) { alert('pedido cancelado y stock devuelto.'); listarPedidosGlobales(); }
            cerrarModalGeneral();
        };
    } else {
        // cambio de estado normal (logistica)
        const exito = await cambiarEstadoPedido(idPedido, nuevoEstado);
        if (exito) select.dataset.actual = nuevoEstado;
    }
}
