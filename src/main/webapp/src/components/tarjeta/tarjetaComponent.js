import { abrirModalDetalle } from './detalleProductoModal.js';

/**
 * genera el elemento html de una tarjeta de producto de forma segura
 * @param {Object} producto - datos del producto desde la bd
 * @returns {HTMLElement} nodo dom estructurado de la tarjeta
 */
export function crearTarjetaHTML(producto) {
    // adaptamos las variables a como java las envia en el json usando extraccion segura
    const idProd = producto.idProductoPk || producto.id_producto_pk || producto.id;
    const nombreProd = producto.nombreProducto || producto.nombre_producto || producto.nombre || 'producto sin nombre';
    const catProd = producto.categoria || producto.nombre_categoria || producto.nombreCategoria || '';
    const etiquetas = producto.etiquetas || [];
    
    // validacion a prueba de fallos para la ruta de la imagen
    let rutaImg = producto.urlRuta || producto.url_ruta || producto.imagen;
    if (!rutaImg || rutaImg === 'null' || rutaImg === 'undefined') {
        rutaImg = 'src/img/productos/default/gato_programador.jpg';
    }

    // formateamos el precio a moneda local
    const precioFormateado = producto.precio.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 });

    // contenedor principal de la tarjeta (se usa la etiqueta article por buena semantica web)
    const article = document.createElement('article');
    article.classList.add('tarjeta');
    article.style.height = '100%'; // permite que todas las tarjetas midan lo mismo

    // contenedor superior exclusivo para la imagen del producto
    const divImg = document.createElement('div');
    divImg.classList.add('tarjeta-img');
    const img = document.createElement('img');
    img.src = rutaImg;
    img.alt = nombreProd;
    
    // MAGIA DE JAVASCRIPT: Si el archivo fisico se borro por culpa del Clean and Build de NetBeans,
    // el navegador disparara un error 404. Este 'onerror' lo atrapa y pone la foto del gato.
    img.onerror = function() {
        this.onerror = null; // evita bucles infinitos si el gato tampoco existe
        this.src = 'src/img/productos/default/gato_programador.jpg';
    };
    divImg.appendChild(img);
    article.appendChild(divImg);

    // contenedor del cuerpo
    const divCuerpo = document.createElement('div');
    divCuerpo.classList.add('tarjeta-cuerpo');
    divCuerpo.style.display = 'flex';
    divCuerpo.style.flexDirection = 'column';
    divCuerpo.style.height = 'calc(100% - 200px)'; // resta el tamano de la imagen para ocupar el resto

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
    // las etiquetas secundarias se removieron de la vista principal para mantener un diseno limpio.
    // ahora se mostraran exclusivamente en el modal de detalles.
    divCuerpo.appendChild(divTags);

    // titulo
    const h3 = document.createElement('h3');
    h3.textContent = nombreProd;
    divCuerpo.appendChild(h3);

    // descripcion (o stock si esta vacia)
    const pDesc = document.createElement('p');
    pDesc.classList.add('descripcion');
    // truncamos visualmente a 2 lineas por si la descripcion es muy larga
    pDesc.style.display = '-webkit-box';
    pDesc.style.webkitLineClamp = '2';
    pDesc.style.webkitBoxOrient = 'vertical';
    pDesc.style.overflow = 'hidden';
    pDesc.textContent = producto.descripcion || `Stock disponible: ${producto.stock || 0} uds.`;
    divCuerpo.appendChild(pDesc);

    // footer de la tarjeta
    // usamos flexbox para separar el precio a la izquierda y agrupar los botones a la derecha,
    // garantizando que todo quede en la misma linea horizontal (align-items: center).
    const divFooter = document.createElement('div');
    divFooter.classList.add('tarjeta-footer');
    divFooter.style.display = 'flex';
    divFooter.style.justifyContent = 'space-between';
    divFooter.style.alignItems = 'center';
    divFooter.style.marginTop = 'auto'; // empuja el footer siempre hacia abajo (alinea botones iguales en todas las tarjetas)
    divFooter.style.paddingTop = '15px';

    // precio
    // se inyecta el texto del precio. se fuerza el margen a cero para evitar que el css
    // global empuje el texto hacia arriba y desalinee la vista respecto a los botones.
    const pPrecio = document.createElement('p');
    pPrecio.classList.add('precio');
    pPrecio.style.margin = '0';
    pPrecio.style.fontSize = '1.15rem';
    pPrecio.textContent = precioFormateado;
    divFooter.appendChild(pPrecio);
    
    // sub-contenedor exclusivo para que los dos botones esten pegados uno al lado del otro
    const divBotones = document.createElement('div');
    divBotones.style.display = 'flex';
    divBotones.style.gap = '5px';

    // boton para abrir detalles
    const btnDetalles = document.createElement('button');
    btnDetalles.classList.add('btn-ver-detalles');
    btnDetalles.textContent = 'Detalles';
    btnDetalles.style.cssText = 'background: #f0f0f0; color: #333; border: 1px solid #ddd; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 0.85rem;';
    btnDetalles.addEventListener('click', () => abrirModalDetalle(producto));

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

    divBotones.appendChild(btnDetalles);
    divBotones.appendChild(btnAgregar);

    divFooter.appendChild(divBotones);
    divCuerpo.appendChild(divFooter);
    article.appendChild(divCuerpo);

    return article;
}