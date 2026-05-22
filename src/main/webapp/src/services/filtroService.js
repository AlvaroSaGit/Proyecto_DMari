/**
 * Filtra una lista de productos basandose en un termino de busqueda textual
 * y un rango de precios opcional. La busqueda ignora mayusculas y minusculas.
 *
 * @param {Array} lista - El arreglo con todos los productos disponibles.
 * @param {string} termino - Palabra clave a buscar (busca en nombre, categoria o etiquetas).
 * @param {number} min - Precio minimo aceptado (por defecto 0).
 * @param {number} max - Precio maximo aceptado (por defecto Infinity).
 * @returns {Array} Un nuevo arreglo que contiene unicamente los productos que cumplen las condiciones.
 */
export function filtrarProductos(lista, termino, min = 0, max = Infinity) {
    return lista.filter(prod => {
        // 1. Validacion de coincidencia de texto
        let coincideTexto = true;
        
        // Comprobamos si el usuario escribio algo en el buscador
        if (termino && termino.trim() !== '') {
            // Convertimos a minusculas para que la busqueda sea exacta sin importar como escribio
            const terminoLower = termino.toLowerCase().trim();
            
            // Extraemos y aseguramos que los datos del producto sean texto y esten en minusculas
            const nombre = (prod.nombreProducto || prod.nombre || '').toLowerCase();
            const categoria = (prod.categoria || prod.nombreCategoria || '').toLowerCase();
            const etiquetas = prod.etiquetas || [];

            // Verificamos si la palabra esta en el titulo o en la categoria principal
            const coincideNombreOCategoria = nombre.includes(terminoLower) || categoria.includes(terminoLower);
            
            // Verificamos si la palabra coincide con alguna de las etiquetas secundarias de la BD
            const coincideEtiqueta = etiquetas.some(tag => tag.toLowerCase().includes(terminoLower));
            
            // Si esta en cualquiera de los 3 lugares, es valido
            coincideTexto = coincideNombreOCategoria || coincideEtiqueta;
        }

        // 2. Validacion de los limites de precio
        const coincidePrecio = prod.precio >= min && prod.precio <= max;

        // El producto solo se mostrara en el grid si pasa ambas pruebas (texto y precio)
        return coincideTexto && coincidePrecio;
    });
}

/**
 * Toma una lista de productos y la organiza segun el criterio seleccionado por el usuario.
 * Utiliza desestructuracion [...lista] para crear una copia y no alterar la lista original.
 *
 * @param {Array} lista - El arreglo de productos previamente filtrado.
 * @param {string} criterio - La opcion elegida en el combobox (ej: 'precio-asc', 'nombre-desc').
 * @returns {Array} Un nuevo arreglo con el orden aplicado.
 */
export function ordenarProductos(lista, criterio) {
    // Ordenamientos numericos
    if (criterio === 'precio-asc') return [...lista].sort((a, b) => a.precio - b.precio);
    if (criterio === 'precio-desc') return [...lista].sort((a, b) => b.precio - a.precio);
    
    // Ordenamientos alfabeticos (localeCompare respeta caracteres especiales como la ñ)
    if (criterio === 'nombre-asc') return [...lista].sort((a, b) => a.nombreProducto.localeCompare(b.nombreProducto));
    if (criterio === 'nombre-desc') return [...lista].sort((a, b) => b.nombreProducto.localeCompare(a.nombreProducto));
    
    // Por defecto (relevancia) devolvemos la lista tal como llego
    return lista; 
}