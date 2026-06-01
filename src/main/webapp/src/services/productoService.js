// Servicio encargado de la comunicacion con el API de productos (Java)

/**
 * se conecta al backend, obtiene los productos y maneja errores de red
 * @param {string} parametros extra opcionales para filtrar la ruta
 * @returns {Promise<Array|null>} retorna un arreglo de productos o null si falla
 */
export async function obtenerProductos(parametros = '') {
    try {
        // creamos un destructor de cache (timestamp unico).
        // verificamos si la ruta ya tiene un '?' para concatenar correctamente con '&'
        const separador = parametros.includes('?') ? '&' : '?';
        const cacheBuster = separador + 't=' + new Date().getTime();
        
        // le agregamos los parametros a la ruta y burlamos la memoria del navegador
        const respuesta = await fetch('listar' + parametros + cacheBuster); 
        
        if (!respuesta.ok) {
            throw new Error(`error http: ${respuesta.status}`);
        }
        
        return await respuesta.json();
    } catch (error) {
        console.error('error en el servicio de productos:', error);
        return null;
    }
}

/**
 * envia el formulario de producto al servidor para crear o actualizar
 */
export async function guardarProducto(parametros, esEdicion) {
    // decidimos la ruta del servlet segun la bandera de edicion
    const ruta = esEdicion ? 'actualizar' : 'insertar';
    // realizamos el fetch enviando los datos multipart en el body
    const respuesta = await fetch(ruta, { method: 'POST', body: parametros });
    // validamos la respuesta del servidor antes de retornar
    if (!respuesta.ok) throw new Error('Error al guardar producto');
    return true;
}

/**
 * solicita la eliminacion fisica de un producto por su id
 */
export async function eliminarProducto(id) {
    // empaquetamos el id en formato de urlsearchparams
    const parametros = new URLSearchParams();
    parametros.append('id', id);
    
    // ejecutamos la peticion post hacia el controlador de borrado
    const respuesta = await fetch('eliminar', { method: 'POST', body: parametros });
    if (!respuesta.ok) throw new Error('Error al eliminar producto');
    return true;
}

export async function cambiarEstadoProducto(id, nuevoEstado) {
    const parametros = new URLSearchParams();
    parametros.append('id', id);
    parametros.append('estado', nuevoEstado);
    
    const respuesta = await fetch('cambiar-estado', { method: 'POST', body: parametros });
    if (!respuesta.ok) throw new Error('Error al cambiar el estado');
    return true;
}