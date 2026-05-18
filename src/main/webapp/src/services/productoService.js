// Servicio encargado de la comunicacion con el API de productos (Java)

/**
 * se conecta al backend, obtiene los productos y maneja errores de red
 * @returns {Promise<Array|null>} retorna un arreglo de productos o null si falla
 */
export async function obtenerProductos() {
    try {
        // Usamos ruta relativa para que funcione en tu localhost y cuando subas el proyecto a internet
        const respuesta = await fetch('listar'); 
        
        if (!respuesta.ok) {
            throw new Error(`error http: ${respuesta.status}`);
        }
        
        return await respuesta.json();
    } catch (error) {
        console.error('error en el servicio de productos:', error);
        return null;
    }
}

export async function guardarProducto(parametros, esEdicion) {
    const ruta = esEdicion ? 'actualizar' : 'insertar';
    const respuesta = await fetch(ruta, { method: 'POST', body: parametros });
    if (!respuesta.ok) throw new Error('Error al guardar producto');
    return true;
}

export async function eliminarProducto(id) {
    const parametros = new URLSearchParams();
    parametros.append('id', id);
    
    const respuesta = await fetch('eliminar', { method: 'POST', body: parametros });
    if (!respuesta.ok) throw new Error('Error al eliminar producto');
    return true;
}