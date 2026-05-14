function generarTarjetaHTML(producto) {
    return `
        <article class="product-card">
            <div class="product-card__image">
                <img src="./src/assets/img/${producto.imagen}" alt="${producto.nombre}">
                ${producto.novedad ? '<span>Nuevo</span>' : ''}
            </div>
            <div class="product-card__info">
                <h3>${producto.nombre}</h3>
                <p class="product-price">$${producto.precio}</p>
                <button class="btn-add" onclick="agregarAlCarrito(${producto.id})">
                    Agregar
                </button>
            </div>
        </article>
    `;
    /* En la linea 5, en caso de que haya cargado imagen, alt y proucto
    tengra un mensaje de nuevo en cas que no, no 
    */
}