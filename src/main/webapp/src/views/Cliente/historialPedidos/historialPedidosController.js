/*
    objetivo de este archivo:
    controlador encargado de gestionar la vista del historial de pedidos del cliente.
    se encarga de solicitar los datos al servidor, procesar la lista plana para agruparla 
    por numero de recibo y dibujar la interfaz correspondiente en la pantalla.
*/

import { cargarComponente } from '../../../services/uiService.js';
import { obtenerHistorialPedidos } from '../../../components/pedido/pedidoService.js';
import { crearBloquePedido } from '../../../components/historialPedidos/historialPedidoComponent.js';

/*
    funcion de arranque de la vista.
    primero inyecta el html base en el contenedor principal y luego 
    desencadena el proceso de carga de datos desde el backend.
*/
export async function cargarVistaHistorialPedidos() {
    await cargarComponente('component-main', './src/views/Cliente/historialPedidos/historialPedidos.html');
    prepararVistaHistorial();
}

/*
    extrae la informacion del servidor, evalua posibles errores 
    (fallos de red o cuenta sin compras previas) y, si todo esta bien,
    manda a agrupar y dibujar la informacion en la pantalla.
*/
async function prepararVistaHistorial() {
    const contenedor = document.getElementById('contenedor-lista-historial');
    if (!contenedor) return;

    // se muestra un mensaje temporal mientras el servidor responde
    contenedor.innerHTML = '<p style="text-align:center; padding: 20px;">cargando historial de compras...</p>';

    // peticion al backend a traves del servicio modularizado
    const listaPlana = await obtenerHistorialPedidos();

    // validacion de seguridad por si falla la conexion o el usuario perdio la sesion
    if (!listaPlana) {
        contenedor.innerHTML = '<p style="text-align:center; color:red; padding: 20px;">error de conexion al cargar el historial.</p>';
        return;
    }

    // si el arreglo viene vacio, significa que el cliente jamas ha realizado un pedido
    if (listaPlana.length === 0) {
        contenedor.innerHTML = '<p style="text-align:center; padding: 20px; color:#666;">aun no has realizado ninguna compra en el sistema.</p>';
        return;
    }

    // en la base de datos cada producto es una fila separada. 
    // se agrupan por identificador de pedido para mostrarlos juntos en un solo bloque visual.
    const pedidosAgrupados = agruparPorPedido(listaPlana);
    renderizarHistorial(pedidosAgrupados, contenedor);
}

/*
    transforma la lista plana que llega de la base de datos en un arreglo de pedidos unificados.
    ejemplo: si el pedido #5 tiene 3 productos, se crea un solo objeto para el pedido #5 
    y adentro se guardan los 3 productos en un sub-arreglo, sumando sus costos.
*/
function agruparPorPedido(listaPlana) {
    const agrupado = {};
    
    listaPlana.forEach(item => {
        // si el identificador del pedido no existe aun en el nuevo objeto, se crea su estructura base
        if (!agrupado[item.idPedido]) {
            agrupado[item.idPedido] = {
                id: item.idPedido,
                fecha: item.fecha,
                estado: item.estado,
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