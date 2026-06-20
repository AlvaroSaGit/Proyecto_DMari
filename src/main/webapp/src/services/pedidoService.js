/**
 * objetivo de este archivo:
 * servicio encargado de la comunicacion entre el frontend y el backend
 * para todo lo relacionado con el procesamiento de pedidos y facturacion.
 */

/**
 * envia la informacion de la compra al servidor java para registrar el pedido.
 * 
 * @param {number} idCarrito - id del carrito activo en mysql.
 * @param {number} idDireccion - id de la direccion seleccionada.
 * @param {string} idMetodo - identificador del medio de pago seleccionado.
 * @param {string} cuenta - numero de cuenta o comprobante ingresado.
 * @returns {Promise<boolean>} - true si el pedido se proceso con exito en mysql.
 */
export async function enviarPedido(carrito, idMetodo, cuenta) {
    // validacion de seguridad modulo 5: expresion regular flexible para aceptar 
    // numeros de cuenta, telefonos (con espacios, + o guiones) entre 4 y 25 caracteres.
    const regexCuenta = /^[0-9\-\s\+]{4,25}$/;
    if (!regexCuenta.test(cuenta)) {
        console.error('error: el formato de la cuenta o telefono es invalido en el frontend. Valor ingresado:', cuenta);
        return false;
    }

    // Validación extra para evitar enviar datos corruptos
    if (!carrito || carrito.length === 0) {
        console.error('error: el carrito está vacío.');
        return false;
    }

    for (const item of carrito) {
        if (isNaN(item.cantidad) || item.cantidad <= 0 || isNaN(item.precio) || item.precio < 0) {
            console.error('error: cantidad o precio inválido en el carrito.');
            return false;
        }
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
    // enviamos el método de pago y la cuenta a procesar
    parametros.append('idMetodo', idMetodo);
    parametros.append('cuenta', cuenta);
    
    // el servlet recibira la lista y debe usar una transaccion para pasar de carrito a detalle_pedido
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

/**
 * obtiene el listado global de pedidos para el administrador o el filtrado para proveedor.
 * el backend decide que devolver segun el rol de la sesion.
 * @returns {Promise<Array|null>}
 */
export async function obtenerTodosLosPedidos() {
    try {
        const respuesta = await fetch('pedido?admin=true&t=' + Date.now());
        if (!respuesta.ok) throw new Error('error al obtener lista de pedidos');
        return await respuesta.json();
    } catch (error) {
        console.error('error al solicitar pedidos de gestion:', error);
        return null;
    }
}