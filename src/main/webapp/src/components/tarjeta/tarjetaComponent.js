/**
 * genera el elemento html de una tarjeta de producto de forma segura
 * @param {Object} producto - datos del producto desde la bd
 * @returns {HTMLElement} nodo dom estructurado de la tarjeta
 */
export function crearTarjetaHTML(producto) {
    // Adaptamos las variables a como Java (Jackson/Gson) las envia en el JSON
    const idProd = producto.idProductoPk || producto.id;
    const nombreProd = producto.nombreProducto || producto.nombre || 'Producto';
    const catProd = producto.categoria || producto.nombreCategoria || '';
    const etiquetas = producto.etiquetas || [];
    const rutaImg = producto.urlRuta || producto.imagen;
    const imagenUrl = (rutaImg && rutaImg !== 'null') ? rutaImg : './src/assets/img/default.jpg';

    // formateamos el precio a moneda local
    const precioFormateado = producto.precio.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 });

    // contenedor principal
    const article = document.createElement('article');
    article.classList.add('tarjeta');

    // contenedor de imagen
    const divImg = document.createElement('div');
    divImg.classList.add('tarjeta-img');
    const img = document.createElement('img');
    img.src = imagenUrl;
    img.alt = nombreProd;
    divImg.appendChild(img);
    article.appendChild(divImg);

    // contenedor del cuerpo
    const divCuerpo = document.createElement('div');
    divCuerpo.classList.add('tarjeta-cuerpo');

    // tags (si tiene categoria)
    const divTags = document.createElement('div');
    divTags.classList.add('tarjeta-tags');
    if (catProd) {
        const spanCat = document.createElement('span');
        spanCat.classList.add('etiqueta');
        // Le damos un color elegante y oscuro a la categoria principal para que destaque
        spanCat.style.backgroundColor = '#333333';
        spanCat.style.color = '#ffffff';
        spanCat.style.cursor = 'pointer';
        spanCat.dataset.filtro = catProd;
        spanCat.textContent = catProd;
        divTags.appendChild(spanCat);
    }
    // Dibujamos las etiquetas reales traidas de la tabla producto_etiqueta
    etiquetas.forEach(tag => {
        const spanTag = document.createElement('span');
        spanTag.classList.add('etiqueta');
        spanTag.style.cursor = 'pointer';
        spanTag.dataset.filtro = tag;
        spanTag.textContent = tag;
        divTags.appendChild(spanTag);
    });
    divCuerpo.appendChild(divTags);

    // titulo
    const h3 = document.createElement('h3');
    h3.textContent = nombreProd;
    divCuerpo.appendChild(h3);

    // descripcion (o stock si esta vacia)
    const pDesc = document.createElement('p');
    pDesc.classList.add('descripcion');
    pDesc.textContent = producto.descripcion || `Stock disponible: ${producto.stock || 0} uds.`;
    divCuerpo.appendChild(pDesc);

    // footer de la tarjeta
    const divFooter = document.createElement('div');
    divFooter.classList.add('tarjeta-footer');

    // precio
    const pPrecio = document.createElement('p');
    pPrecio.classList.add('precio');
    pPrecio.textContent = precioFormateado;
    divFooter.appendChild(pPrecio);

    // boton agregar
    const btnAgregar = document.createElement('button');
    // IMPORTANTE: Le damos la clase exacta que busca inicioController.js para el carrito
    btnAgregar.classList.add('btn-agregar-carrito');
    btnAgregar.dataset.id = idProd;
    btnAgregar.dataset.nombre = nombreProd;
    btnAgregar.dataset.precio = producto.precio;
    btnAgregar.dataset.stock = producto.stock || 0;

    // icono del boton
    const spanIcono = document.createElement('span');
    spanIcono.classList.add('material-symbols-outlined');
    spanIcono.textContent = 'add';
    btnAgregar.appendChild(spanIcono);

    divFooter.appendChild(btnAgregar);
    divCuerpo.appendChild(divFooter);
    article.appendChild(divCuerpo);

    return article;
}