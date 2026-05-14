// centralizamos la url de la api
const API_URL = 'http://localhost:8080/DMari_Backend/listar';

/**
 * se conecta al backend, obtiene los productos y maneja errores de red
 * @returns {Promise<Array|null>} retorna un arreglo de productos o null si falla
 */
export async function obtenerProductos() {
    try {
        const respuesta = await fetch(API_URL);
        
        if (!respuesta.ok) {
            throw new Error(`error http: ${respuesta.status}`);
        }
        
        return await respuesta.json();
    } catch (error) {
        console.error('error en el servicio de productos:', error);
        return null;
    }
}