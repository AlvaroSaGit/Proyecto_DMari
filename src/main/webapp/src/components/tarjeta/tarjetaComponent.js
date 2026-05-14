/**
 * genera el elemento html de una tarjeta de producto de forma segura
 * @param {Object} producto - datos del producto desde la bd
 * @returns {HTMLElement} nodo dom estructurado de la tarjeta
 */
export function crearTarjetaHTML(producto) {
    // formateamos el precio a moneda local
    const precioFormateado = producto.precio.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 });

    // contenedor principal
    const article = document.createElement('article');
    article.classList.add('tarjeta');

    // contenedor de imagen
    const divImg = document.createElement('div');
    divImg.classList.add('tarjeta-img');
    const img = document.createElement('img');
    img.src = producto.imagen;
    img.alt = producto.nombre;
    divImg.appendChild(img);
    article.appendChild(divImg);

    // contenedor del cuerpo
    const divCuerpo = document.createElement('div');
    divCuerpo.classList.add('tarjeta-cuerpo');

    // tags (si tiene categoria)
    const divTags = document.createElement('div');
    divTags.classList.add('tarjeta-tags');
    if (producto.categoria) {
        const spanEtiqueta = document.createElement('span');
        spanEtiqueta.classList.add('etiqueta');
        spanEtiqueta.textContent = producto.categoria;
        divTags.appendChild(spanEtiqueta);
    }
    divCuerpo.appendChild(divTags);

    // titulo
    const h3 = document.createElement('h3');
    h3.textContent = producto.nombre;
    divCuerpo.appendChild(h3);

    // descripcion
    const pDesc = document.createElement('p');
    pDesc.classList.add('descripcion');
    pDesc.textContent = producto.descripcion || 'delicioso producto artesanal.';
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
    btnAgregar.classList.add('btn-agregar-catalogo');
    btnAgregar.dataset.id = producto.id;
    btnAgregar.dataset.nombre = producto.nombre;
    btnAgregar.dataset.precio = producto.precio;

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