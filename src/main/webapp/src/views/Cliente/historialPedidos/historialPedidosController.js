/**
 * controlador para el historial de compras del cliente.
 * permite listar pedidos y ver el detalle de la factura con datos del proveedor.
 */
import { cargarComponente } from '../../../services/uiService.js';
import { obtenerHistorialPedidos } from '../../../services/pedidoService.js';
import { crearBloquePedido } from '../../../components/pedido/historialPedidoComponent.js';
import { navegarA } from '../../../router/router.js';

// funcion de arranque de la vista de historial.
export async function cargarVistaHistorialPedidos() {
    await cargarComponente('component-main', './src/views/Cliente/historialPedidos/historialPedidos.html');
    prepararVistaHistorial();
}

// coordina la peticion de datos y el manejo de estados de la interfaz.
async function prepararVistaHistorial() {
    const contenedor = document.getElementById('contenedor-lista-historial');
    if (!contenedor) return;

    // mensaje de espera para el usuario
    contenedor.innerHTML = '<p style="text-align:center; padding: 20px;">cargando sus pedidos...</p>';
    
    try {
        // peticion al backend usando el servicio centralizado
        const listaPlana = await obtenerHistorialPedidos();
        
        if (!listaPlana) {
            contenedor.innerHTML = '<p style="text-align:center; color:red;">error al conectar con el servidor.</p>';
            return;
        }

        if (listaPlana.length === 0) {
            contenedor.innerHTML = '<p style="text-align:center; color:#666;">no tienes pedidos registrados.</p>';
            return;
        }

        // mantenemos la logica de agrupamiento necesaria para los bloques
        const pedidosAgrupados = agruparPorPedido(listaPlana);
        renderizarHistorial(pedidosAgrupados, contenedor);
        
    } catch (error) {
        console.error('fallo la carga del historial:', error);
        contenedor.innerHTML = '<p style="text-align:center; color:red;">error interno al procesar el historial.</p>';
    }
}

// transforma las filas de mysql en objetos agrupados por id de factura.
function agruparPorPedido(listaPlana) {
    const agrupado = {};
    listaPlana.forEach(item => {
        // si el identificador del pedido no existe aun en el nuevo objeto, se crea su estructura base
        if (!agrupado[item.idPedido]) {
            agrupado[item.idPedido] = {
                id: item.idPedido,
                fecha: item.fecha || 'fecha no disponible',
                estado: item.estado || 'pendiente',
                motivo_cancelacion: item.motivo_cancelacion || null,
                // capturamos la informacion de entrega para cuando la vista sea usada por admin/proveedor
                cliente: item.nombreCliente || null,
                total: 0,
                productos: []
            };
        }
        // se inserta el producto actual dentro del sub-arreglo del pedido correspondiente
        agrupado[item.idPedido].productos.push(item);
        // se acumula el costo en el total de la factura
        agrupado[item.idPedido].total += item.subtotal;
    });
    
    // object.values convierte el diccionario agrupado en un arreglo tradicional.
    // el metodo sort lo organiza de forma descendente (los pedidos mas nuevos arriba).
    return Object.values(agrupado).sort((a, b) => b.id - a.id);
}

/*
    construye de forma dinamica el codigo html para cada bloque de pedido y sus productos internos.
*/
function renderizarHistorial(pedidosAgrupados, contenedor) {
    // se vacia el mensaje de carga
    contenedor.innerHTML = '';

    pedidosAgrupados.forEach(pedido => {
        // se utiliza el componente importado para generar el bloque y se agrega a la pantalla
        contenedor.appendChild(crearBloquePedido(pedido));
    });
}

// funcion global para el modulo 4: detalle de factura en modal.
window.verDetalleFactura = async function(id) {
    try {
        // pedimos el detalle individual al controlador
        const respuesta = await fetch(`pedido?id=${id}`);
        const detalle = await respuesta.json();
        if (detalle.length === 0) return;

        const principal = detalle[0]; // datos de cabecera

        // construccion de la estructura de la factura (modulo 4)
        let html = `<div class="factura-header">
                        <h3>Detalle de Pedido #FAC-${id}</h3>
                        <p><strong>Estado:</strong> ${principal.estado}</p>
                        ${principal.motivo ? `<p style="color: #d32f2f;"><strong>motivo cancelacion:</strong> ${principal.motivo}</p>` : ''}
                    </div>
                    <hr>
                    <table class="tabla-factura" style="width:100%; border-collapse: collapse;">
                        <thead>
                            <tr style="background: #f4f4f4;"><th>producto</th><th>vendedor (proveedor)</th><th>cant.</th><th>subtotal</th></tr>
                        </thead>
                        <tbody>`;
        
        detalle.forEach(item => {
            html += `<tr style="border-bottom: 1px solid #eee;">
                        <td style="padding:8px;">${item.producto}</td>
                        <td style="padding:8px;">${item.proveedor || 'dmari oficial'} ${item.contacto ? '<br><small>Contacto: ' + item.contacto + '</small>' : ''}</td>
                        <td style="padding:8px; text-align:center;">${item.cantidad}</td>
                        <td style="padding:8px;">$${item.subtotal.toFixed(2)}</td>
                    </tr>`;
        });
        
        html += `</tbody></table><div style="margin-top:15px; text-align:right;"><strong>total factura: $${principal.totalPagar}</strong></div>`;
        
        // aqui se debe invocar al componente global de modales para mostrar el html generado
        console.log('factura generada exitosamente:', html);
        if (window.mostrarModal) {
            window.mostrarModal(html);
        } else {
            alert("detalle de factura listo (ver consola). configure uiService para modales.");
        }

    } catch (e) { console.error('error al ver factura:', e); }
};