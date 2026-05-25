/*
    objetivo de este archivo:
    este servicio actua como un puente de comunicacion entre el frontend y el backend.
    su responsabilidad exclusiva es gestionar el envio de pedidos y compras,
    asegurando que los datos viajen correctamente hacia el servidor.
*/

/**
 * empaqueta los items del carrito y los envia al servidor para procesar la venta.
 * @param {Array} carrito - el arreglo de productos que el usuario desea comprar.
 * @param {number} idMetodo - el id del metodo de pago seleccionado.
 * @param {string} cuenta - el numero de cuenta o telefono del cliente.
 * @returns {boolean} - retorna verdadero si el pedido se guardo en la base de datos, falso si ocurrio un error.
 */
export async function enviarPedido(carrito, idMetodo, cuenta) {
    // se utiliza urlsearchparams para emular el envio de un formulario html estandar.
    // esto evita el uso de json complejo y facilita la lectura directa en el backend.
    const parametros = new URLSearchParams();
    
    // se recorre cada producto dentro del arreglo del carrito.
    // al enviar multiples campos con el mismo nombre ('id_producto', 'cantidad', etc.),
    // el servidor los recibira e interpretara automaticamente como arreglos.
    carrito.forEach(item => {
        parametros.append('id_producto', item.id);
        parametros.append('cantidad', item.cantidad);
        parametros.append('precio', item.precio);
    });
    
    // inyectamos los datos financieros
    parametros.append('idMetodo', idMetodo);
    parametros.append('cuenta', cuenta);
    
    try {
        // se realiza la peticion asincrona al endpoint '/pedido' usando el metodo post.
        // la ejecucion se pausa (await) hasta que el servidor emita una respuesta.
        const respuesta = await fetch('pedido', { method: 'POST', body: parametros });
        
        // si el codigo de estado http es 200 (ok), la compra se registro con exito.
        return respuesta.ok; 
    } catch (error) {
        // se captura cualquier falla de conexion (ej. servidor caido o sin internet)
        // para evitar que la aplicacion colapse repentinamente.
        console.error('error de red al intentar enviar el pedido:', error);
        return false;
    }
}

/**
 * consulta al servidor el historial de compras del cliente actual.
 * @returns {Promise<Array|null>} - retorna la lista de pedidos o nulo si falla.
 */
export async function obtenerHistorialPedidos() {
    try {
        // Agregamos Cache-Busting para evitar que el navegador muestre pedidos viejos o incompletos
        const respuesta = await fetch('pedido?t=' + Date.now());
        if (!respuesta.ok) throw new Error('error al obtener historial');
        return await respuesta.json();
    } catch (error) {
        console.error('falla de conexion al solicitar el historial:', error);
        return null;
    }
}