/*
    objetivo de este archivo:
    centralizar las peticiones relacionadas con las categorias.
*/

/**
 * obtiene la lista de categorias desde el servidor.
 * @param {boolean} todas - si es true, trae activas e inactivas.
 * @returns {Promise<Array>} - arreglo de objetos categoria.
 */
export async function obtenerCategorias(todas = false) {
    try {
        const respuesta = await fetch(`categorias?todas=${todas}&t=${Date.now()}`);
        if (!respuesta.ok) throw new Error('error al obtener categorias');
        return await respuesta.json();
    } catch (error) {
        console.error('fallo la conexion al listar categorias:', error);
        return [];
    }
}

/**
 * envia una nueva categoria al servidor.
 * @param {string} nombre - nombre de la categoria.
 * @param {string} descripcion - detalle de la categoria.
 * @returns {Promise<boolean>} - true si se guardo correctamente.
 */
export async function crearCategoria(nombre, descripcion) {
    try {
        const parametros = new URLSearchParams();
        parametros.append('nombre', nombre);
        parametros.append('descripcion', descripcion);

        const respuesta = await fetch('categorias', { method: 'POST', body: parametros });
        
        return respuesta.ok;
    } catch (error) {
        console.error('error al conectar con el servlet de categorias:', error);
        return false;
    }
}

/**
 * solicita al servidor cambiar el estado (activar/pausar) de una categoria.
 * @param {number} id - identificador de la categoria.
 * @param {boolean} nuevoEstado - el estado al que se desea cambiar.
 * @returns {Promise<boolean>} - true si la operacion fue exitosa.
 */
export async function cambiarEstadoCategoria(id, nuevoEstado) {
    try {
        const parametros = new URLSearchParams();
        parametros.append('accion', 'cambiar_estado');
        parametros.append('id', id);
        parametros.append('estado', nuevoEstado.toString());

        const respuesta = await fetch('categorias', { method: 'POST', body: parametros });
        
        return respuesta.ok;
    } catch (error) {
        console.error('error al cambiar estado de la categoria:', error);
        return false;
    }
}

/**
 * envia una categoria actualizada al servidor.
 * @param {number} id - id de la categoria a actualizar.
 * @param {string} nombre - nuevo nombre de la categoria.
 * @param {string} descripcion - nueva descripcion de la categoria.
 * @returns {Promise<boolean>} - true si se actualizo correctamente.
 */
export async function actualizarCategoria(id, nombre, descripcion) {
    try {
        const parametros = new URLSearchParams();
        parametros.append('id', id); // el servlet de java usa este 'id' para saber que es una actualizacion
        parametros.append('nombre', nombre);
        parametros.append('descripcion', descripcion);

        const respuesta = await fetch('categorias', { method: 'POST', body: parametros });
        
        return respuesta.ok;
    } catch (error) {
        console.error('error al conectar con el servlet de categorias para actualizar:', error);
        return false;
    }
}