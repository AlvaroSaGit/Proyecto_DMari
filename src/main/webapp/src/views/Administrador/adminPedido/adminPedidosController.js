// importamos la inyeccion html y la misma peticion fetch que usa el cliente
// corregimos la ruta para que apunte a services y no a la carpeta de componentes
// eliminamos obtenerhistorialpedidos ya que el administrador usa una peticion global directa
import { cargarComponente } from '../../../services/uiService.js';
import { mostrarModal, cerrarModalGeneral } from '../../../services/uiService.js';
import { cambiarEstadoPedido, obtenerHistorialPedidos } from '../../../services/pedidoService.js';
import { verFactura } from '../../../services/facturaService.js';

// arranca la vista inyectando el html en el main
export async function cargarVistaAdminPedidos() {
    await cargarComponente('component-main', './src/views/Administrador/adminPedido/adminPedidos.html');
    prepararVistaPedidos();
}

async function prepararVistaPedidos() {
    const contenedor = document.getElementById('contenedor-lista-pedidos');
    if (!contenedor) return;

    // pedimos los datos al servidor (java devolvera todos si eres admin, o solo los tuyos si eres proveedor)
    const listaPlana = await obtenerHistorialPedidos();

    if (!listaPlana) {
        contenedor.innerHTML = '<p style="text-align:center; color:red;">error al cargar los pedidos.</p>';
        return;
    }

    if (listaPlana.length === 0) {
        contenedor.innerHTML = '<p style="text-align:center; color:#666;">no hay pedidos registrados actualmente.</p>';
        return;
    }

    // agrupamos los productos por numero de factura
    const pedidosAgrupados = agruparPorPedido(listaPlana);
    
    contenedor.innerHTML = '';
    pedidosAgrupados.forEach(pedido => {
        contenedor.appendChild(crearBloquePedidoAdmin(pedido));
    });
}

// funcion de agrupacion igual a la del cliente
function agruparPorPedido(listaPlana) {
    const agrupado = {};
    listaPlana.forEach(item => {
        // usamos idpedidofk que es el nombre generado por el dao en java
        if (!agrupado[item.idPedidoFk]) {
            const clienteSeguro = item.nombreCliente || item.nombre_cliente || item.cliente || 'Sin información de entrega';
            agrupado[item.idPedidoFk] = { 
                id: item.idPedidoFk, 
                fecha: item.fecha, 
                estado: item.estado, 
                cliente: clienteSeguro,
                total: 0, 
                productos: [] 
            };
        }
        agrupado[item.idPedidoFk].productos.push(item);
        agrupado[item.idPedidoFk].total += item.subtotal;
    });
    return Object.values(agrupado).sort((a, b) => b.id - a.id);
}

// crea la tarjeta visual con selector para cambiar el estado del paquete
function crearBloquePedidoAdmin(pedido) {
    let htmlProductos = '';
    pedido.productos.forEach(prod => {
        const productoSeguro = prod.nombreProducto || prod.nombre_producto || prod.producto || 'Producto desconocido';
        htmlProductos += `<div style="display:flex; justify-content:space-between; border-bottom:1px solid #eee; padding:5px 0;"><span>${prod.cantidad}x ${productoSeguro}</span><span>$${prod.subtotal.toFixed(2)}</span></div>`;
    });

    const div = document.createElement('div');
    div.style.cssText = 'background:#fff; border:1px solid #ddd; border-radius:8px; padding:20px; margin-bottom:20px; box-shadow:0 2px 5px rgba(0,0,0,0.05);';
    div.innerHTML = `
        <div style="display:flex; justify-content:space-between; border-bottom:2px solid #222; padding-bottom:10px; margin-bottom:10px;">
            <div>
                <h3 style="margin:0;">pedido #00${pedido.id}</h3>
                <p style="margin:5px 0; color:#555; font-size:0.9rem;"><strong>cliente/envio:</strong> ${pedido.cliente}</p>
                <p style="margin:0; color:#888; font-size:0.85rem;">fecha: ${pedido.fecha}</p>
            </div>
            <div style="text-align:right;">
                <p style="margin:0 0 10px 0; font-size:1.2rem; font-weight:bold;">total: $${pedido.total.toFixed(2)}</p>
                <select class="select-estado-pedido" data-id="${pedido.id}" style="padding:8px; border-radius:5px; border:1px solid #ccc; font-weight:bold; cursor:pointer;">
                    <option value="Pendiente" ${pedido.estado === 'Pendiente' ? 'selected' : ''}>Pendiente</option>
                    <option value="Preparando" ${pedido.estado === 'Preparando' ? 'selected' : ''}>Preparando</option>
                    <option value="En Camino" ${pedido.estado === 'En Camino' ? 'selected' : ''}>En Camino</option>
                    <option value="Entregado" ${pedido.estado === 'Entregado' ? 'selected' : ''}>Entregado</option>
                    <option value="Cancelado_por_Proveedor" ${pedido.estado.startsWith('Cancelado') ? 'selected' : ''}>Cancelado</option>
                </select>
            </div>
        </div>
        <div><p style="font-weight:bold; margin-bottom:10px;">productos de la orden:</p>${htmlProductos}</div>
    `;

    // evento para cuando el admin cambie el estado en el select
    const select = div.querySelector('.select-estado-pedido');
    select.addEventListener('change', async (e) => {
        const nuevoEstado = e.target.value;
        const idPedido = pedido.id;
        
        if (nuevoEstado === 'Cancelado_por_Proveedor') {
            mostrarModalCancelacion(idPedido, nuevoEstado, e.target, pedido.estado);
        } else {
            await actualizarEstadoBackend(idPedido, nuevoEstado);
        }
    });

    return div;
}

function mostrarModalCancelacion(idPedido, nuevoEstado, selectElement, estadoAnterior) {
    const htmlModal = `
        <div style="font-family: Arial, sans-serif;">
            <h3 style="margin-top: 0; color: #dc3545; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;">⚠️ Cancelar Pedido</h3>
            <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">
                Por favor, indica el motivo de la cancelación. Este motivo será visible para el cliente y el administrador.
            </p>
            
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem;">Motivo de Cancelación *</label>
                <textarea id="motivo-cancelacion" rows="4" placeholder="Ej: Producto sin stock temporal..." style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; resize: vertical;"></textarea>
            </div>
            
            <div style="text-align: right; margin-top: 20px;">
                <button id="btn-cerrar-modal" style="background: #f1f1f1; color: #333; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; margin-right: 10px; font-weight: bold;">Cerrar</button>
                <button id="btn-confirmar-cancelacion" style="background: #dc3545; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold;">Confirmar Cancelación</button>
            </div>
        </div>
    `;

    mostrarModal(htmlModal);

    document.getElementById('btn-cerrar-modal').addEventListener('click', () => {
        cerrarModalGeneral();
        selectElement.value = estadoAnterior; // Revertir select
    });

    document.getElementById('btn-confirmar-cancelacion').addEventListener('click', async () => {
        const motivo = document.getElementById('motivo-cancelacion').value.trim();
        if (motivo.length < 5) {
            alert('Por favor ingresa un motivo válido (mínimo 5 caracteres).');
            return;
        }
        cerrarModalGeneral();
        await actualizarEstadoBackend(idPedido, nuevoEstado, motivo);
    });
}

async function actualizarEstadoBackend(idPedido, nuevoEstado, motivo = null) {
    const parametros = new URLSearchParams();
    parametros.append('accion', 'actualizar_estado');
    parametros.append('id', idPedido);
    parametros.append('estado', nuevoEstado);
    if (motivo) {
        parametros.append('motivo', motivo);
    }
    
    try { 
        const respuesta = await fetch('pedido', { method: 'POST', body: parametros }); 
        if (respuesta.ok) {
            alert('estado del paquete actualizado'); 
        } else {
            alert('error al actualizar estado en el servidor');
        }
    } catch(err) { 
        alert('error al actualizar estado en el servidor'); 
    }
}
