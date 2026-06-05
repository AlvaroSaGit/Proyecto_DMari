/**
 * objetivo de este archivo:
 * servicio encargado de la comunicacion entre el frontend y el backend
 * para todo lo relacionado con el procesamiento de pedidos y facturacion.
 */

/**
 * envia la informacion de la compra al servidor java para registrar el pedido.
 * 
 * @param {Array} carrito - lista de productos en la canasta [{id, cantidad, precio}, ...]
 * @param {string} idMetodo - identificador del medio de pago seleccionado.
 * @param {string} cuenta - numero de cuenta o comprobante ingresado.
 * @returns {Promise<boolean>} - true si el pedido se proceso con exito en mysql.
 */
export async function enviarPedido(carrito, idMetodo, cuenta) {
    // validacion de seguridad modulo 5: expresion regular para asegurar que la cuenta sea numerica.
    // permite longitudes de 10 a 15 digitos comunes en cuentas y celulares.
    const regexCuenta = /^[0-9]{10,15}$/;
    if (!regexCuenta.test(cuenta)) {
        console.error('error: el formato de la cuenta o telefono es invalido en el frontend.');
        return false;
    }

    // preparamos los parametros de la peticion usando urlsearchparams.
    // se utiliza para emular el envio de un formulario html estandar x-www-form-urlencoded.
    const parametros = new URLSearchParams();

    // documentacion de los datos enviados a java (servlet pedido):
    // 1. id_producto: identificador unico del articulo en la tabla producto.
    // 2. cantidad: numero de unidades que el cliente desea adquirir.
    // 3. precio: valor unitario capturado para validar integridad en el backend.
    carrito.forEach(item => {
        parametros.append('id_producto', item.id);
        parametros.append('cantidad', item.cantidad);
        parametros.append('precio', item.precio);
    });
    
    // inyectamos los datos financieros finales.
    // idmetodo: llave foranea hacia la tabla metodo_pago.
    // cuenta: el numero validado anteriormente para la tabla pago.
    parametros.append('idMetodo', idMetodo);
    parametros.append('cuenta', cuenta);
    
    try {
        // disparamos la peticion post hacia el endpoint /pedido definido en el controlador java.
        // usamos fetch para una comunicacion asincrona sin recargar la pagina.
        const respuesta = await fetch('pedido', { method: 'POST', body: parametros });
        
        // si el servidor responde con un codigo ok (200), la compra fue exitosa.
        return respuesta.ok; 
    } catch (error) {
        // capturamos errores de red o caidas del servidor netbeans.
        console.error('error de red al intentar enviar el pedido al servidor:', error);
        return false;
    }
}

/**
 * consulta al servidor el historial de compras del cliente actual.
 * @returns {Promise<Array|null>} - retorna la lista de pedidos o nulo si falla.
 */
export async function obtenerHistorialPedidos() {
    try {
        // agregamos cache-busting con un timestamp para evitar pedidos viejos en el navegador.
        const respuesta = await fetch('pedido?t=' + Date.now());
        if (!respuesta.ok) throw new Error('error al obtener historial');
        return await respuesta.json();
    } catch (error) {
        console.error('falla de conexion al solicitar el historial al servidor:', error);
        return null;
    }
}

/**
 * modulo 2: actualiza el estado logistico de un pedido.
 * permite cancelar pedidos o marcarlos como entregados.
 * 
 * documentacion de parametros para java:
 * - accion: define que el servlet debe ejecutar la rama de actualizacion.
 * - id: id_pedido_pk en la base de datos.
 * - estado: el nuevo valor para el enum estado_pedido.
 * - motivo: texto que se guardara en motivo_cancelacion si aplica.
 * 
 * @param {number} id - id del pedido.
 * @param {string} nuevoEstado - estado destino (ej: Cancelado_por_Proveedor).
 * @param {string} motivo - explicacion de la cancelacion (opcional si no es cancelado).
 * @returns {Promise<boolean>} - true si se actualizo correctamente.
 */
export async function cambiarEstadoPedido(id, nuevoEstado, motivo = "") {
    try {
        const parametros = new URLSearchParams();
        parametros.append('accion', 'actualizar_estado');
        parametros.append('id', id);
        parametros.append('estado', nuevoEstado);
        parametros.append('motivo', motivo);

        const respuesta = await fetch('pedido', { method: 'POST', body: parametros });
        if (!respuesta.ok) throw new Error('error en el servidor al actualizar estado');
        
        return respuesta.ok;
    } catch (error) {
        console.error('error fatal al cambiar estado del pedido:', error);
        return false;
    }
}