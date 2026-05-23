/**
 * realiza una busqueda de texto general en una lista de productos.
 * busca en nombre, categoria y etiquetas.
 *
 * @param {Array} lista - el arreglo de productos.
 * @param {string} termino - la palabra a buscar.
 * @returns {Array} un nuevo arreglo con los productos que coinciden.
 */
export function busquedaGeneral(lista, termino) {
    // si el termino de busqueda esta vacio o nulo, no hay nada que filtrar, se devuelve la lista intacta
    if (!termino || termino.trim() === '') {
        return lista;
    }
    
    // normalizamos el termino a minusculas y sin espacios extra para evitar fallos de coincidencia
    const terminoLower = termino.toLowerCase().trim();
    
    // iteramos sobre cada producto para verificar si cumple la condicion de busqueda
    return lista.filter(prod => {
        // extraemos el nombre y la categoria. usamos el operador || (or) como fallback de seguridad:
        // si la propiedad no existe, le asignamos una cadena vacia '' para que .toLowerCase() no rompa la aplicacion.
        const nombre = (prod.nombreProducto || prod.nombre || '').toLowerCase();
        const categoria = (prod.categoria || prod.nombreCategoria || '').toLowerCase();
        
        // extraemos las etiquetas. si el producto no tiene la propiedad etiquetas, inicializamos un arreglo vacio []
        // esto garantiza que podamos usar el metodo de arreglos .some() en la siguiente linea sin lanzar errores.
        const etiquetas = prod.etiquetas || [];

        // verificamos si la palabra escrita por el usuario existe dentro del nombre o de la categoria
        const coincideNombreOCategoria = nombre.includes(terminoLower) || categoria.includes(terminoLower);
        
        // el metodo .some() revisa si al menos una etiqueta dentro del arreglo contiene el termino buscado
        const coincideEtiqueta = etiquetas.some(tag => tag.toLowerCase().includes(terminoLower));
        
        // el producto sobrevive al filtro si coincide en cualquiera de los tres lugares evaluados
        return coincideNombreOCategoria || coincideEtiqueta;
    });
}

/**
 * filtra una lista de productos por un rango de precios.
 * @param {Array} lista - el arreglo de productos.
 * @param {number} min - precio minimo.
 * @param {number} max - precio maximo.
 * @returns {Array} un nuevo arreglo con los productos dentro del rango.
 */
export function filtrarPorPrecio(lista, min = 0, max = Infinity) {
    // evalua cada producto y retorna unicamente aquellos cuyo valor se encuentre dentro de los limites matematicos indicados
    return lista.filter(prod => prod.precio >= min && prod.precio <= max);
}

/**
 * pilar 1: filtrar por categoria exacta.
 * exige que el producto pertenezca a una familia principal especifica (ej. 'velas' o 'floristeria').
 * no usa busqueda difusa, requiere coincidencia exacta para no mezclar familias por errores de texto.
 *
 * @param {Array} lista - el arreglo de productos disponibles.
 * @param {string} nombreCategoria - nombre exacto de la categoria (ignora mayusculas).
 * @returns {Array} un nuevo arreglo solo con los productos de esa categoria.
 */
export function filtrarPorCategoriaExacta(lista, nombreCategoria) {
    if (!nombreCategoria) return lista;
    
    // limpiamos el termino de la categoria a buscar
    const catBuscada = nombreCategoria.toLowerCase().trim();
    
    return lista.filter(prod => {
        // aplicamos el fallback de cadena vacia por seguridad y normalizamos a minusculas
        const catProducto = (prod.categoria || prod.nombreCategoria || '').toLowerCase().trim();
        // se usa 'includes' por si la base de datos devuelve 'donas' y se busca 'dona'
        return catProducto === catBuscada || catProducto.includes(catBuscada);
    });
}

/**
 * pilar 2: filtrar por etiqueta (tags cruzados).
 * permite encontrar productos a traves de multiples categorias que compartan una caracteristica
 * (ej. 'dia de la madre' traera tanto donas como floristeria que tengan ese tag).
 *
 * @param {Array} lista - el arreglo de productos.
 * @param {string} nombreEtiqueta - la etiqueta a buscar.
 * @returns {Array} un nuevo arreglo con los productos etiquetados.
 */
export function filtrarPorEtiqueta(lista, nombreEtiqueta) {
    if (!nombreEtiqueta) return lista;
    
    // limpiamos la etiqueta que vamos a buscar
    const tagBuscado = nombreEtiqueta.toLowerCase().trim();
    
    return lista.filter(prod => {
        // aplicamos el fallback de arreglo vacio por seguridad
        const etiquetas = prod.etiquetas || [];
        // verificamos si al menos una de las etiquetas del producto es exactamente igual a la que se necesita
        return etiquetas.some(tag => tag.toLowerCase().trim() === tagBuscado);
    });
}

/**
 * toma una lista de productos y la organiza segun el criterio seleccionado.
 * utiliza desestructuracion [...lista] para crear una copia y no alterar la lista original en la memoria.
 *
 * @param {Array} lista - el arreglo de productos previamente filtrado.
 * @param {string} criterio - la opcion elegida en la interfaz (ej: 'precio-asc', 'nombre-desc').
 * @returns {Array} un nuevo arreglo con el orden matematico o alfabetico aplicado.
 */
export function ordenarProductos(lista, criterio) {
    // ordenamientos numericos (restar a - b da un ordenamiento ascendente)
    if (criterio === 'precio-asc') return [...lista].sort((a, b) => a.precio - b.precio);
    if (criterio === 'precio-desc') return [...lista].sort((a, b) => b.precio - a.precio);
    
    // ordenamientos alfabeticos (localecompare compara textos respetando caracteres latinos como la ñ)
    if (criterio === 'nombre-asc') return [...lista].sort((a, b) => a.nombreProducto.localeCompare(b.nombreProducto));
    if (criterio === 'nombre-desc') return [...lista].sort((a, b) => b.nombreProducto.localeCompare(a.nombreProducto));
    
    // por defecto (relevancia o sin filtro definido) devolvemos la lista tal como llego
    return lista; 
}