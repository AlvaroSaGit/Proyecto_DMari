/**
 * funcion interna para normalizar textos.
 * convierte a minusculas, quita espacios y elimina tildes o acentos para lograr comparaciones exactas.
 */
function limpiarTexto(texto) {
    // usamos string() para evitar errores si llega un numero
    return String(texto || '').toLowerCase().trim().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

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
    
    // limpiamos el termino de busqueda quitando tildes y mayusculas
    const terminoLimpio = limpiarTexto(termino);
    
    // iteramos sobre cada producto para verificar si cumple la condicion de busqueda
    return lista.filter(prod => {
        // extraemos y limpiamos el nombre y la categoria. usamos el operador || (or) como fallback de seguridad.
        const nombre = limpiarTexto(prod.nombreProducto || prod.nombre);
        const categoria = limpiarTexto(prod.categoria || prod.nombreCategoria);
        
        // extraemos las etiquetas. si el producto no tiene la propiedad etiquetas, inicializamos un arreglo vacio []
        const etiquetas = prod.etiquetas || [];

        // verificamos si la palabra escrita por el usuario existe dentro del nombre o de la categoria
        const coincideNombreOCategoria = nombre.includes(terminoLimpio) || categoria.includes(terminoLimpio);
        
        // el metodo .some() revisa si al menos una etiqueta dentro del arreglo contiene el termino buscado
        const coincideEtiqueta = etiquetas.some(tag => limpiarTexto(tag).includes(terminoLimpio));
        
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
    
    // limpiamos el termino de la categoria a buscar (sin tildes)
    const catBuscada = limpiarTexto(nombreCategoria);
    
    return lista.filter(prod => {
        // contingencia: buscar por el id numerico si el texto falla
        const idCat = parseInt(prod.idCategoriaFk || prod.id_categoria_fk || 0);
        if (catBuscada === 'reposteria' && idCat === 1) return true;
        if (catBuscada === 'decoracion' && idCat === 2) return true;
        if (catBuscada === 'floristeria' && idCat === 3) return true;

        // aplicamos el fallback de cadena vacia por seguridad y limpiamos el texto
        const catProducto = limpiarTexto(prod.categoria || prod.nombreCategoria);
        // se usa 'includes' por si la base de datos devuelve 'donas' y se busca 'dona'
        return catProducto === catBuscada || (catProducto !== '' && catProducto.includes(catBuscada));
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
    
    // limpiamos la etiqueta que vamos a buscar (sin tildes)
    const tagBuscado = limpiarTexto(nombreEtiqueta);
    
    return lista.filter(prod => {
        // aplicamos el fallback de arreglo vacio por seguridad
        const etiquetas = prod.etiquetas || [];
        // verificamos si al menos una de las etiquetas del producto es exactamente igual a la que se necesita
        return etiquetas.some(tag => limpiarTexto(tag) === tagBuscado);
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