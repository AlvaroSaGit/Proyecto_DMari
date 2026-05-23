/*
    objetivo de este archivo:
    este componente crea un modal dinamico en la pantalla para mostrar 
    los detalles completos de un producto, incluyendo sus etiquetas secundarias.
    se inyecta temporalmente en el dom y se destruye al cerrarse.
*/

import { agregarAlCarrito } from '../carritoSideBar/carritoController.js';

export function abrirModalDetalle(producto) {
    // se adaptan las variables del producto para asegurar que no haya valores nulos
    const idProd = producto.idProductoPk || producto.id;
    const nombreProd = producto.nombreProducto || producto.nombre || 'Producto';
    const catProd = producto.categoria || producto.nombreCategoria || 'Sin categoria';
    const descripcion = (producto.descripcion && producto.descripcion.trim() !== '') ? producto.descripcion : 'no hay descripcion detallada para este producto.';
    const etiquetas = producto.etiquetas || [];
    const rutaImg = producto.urlRuta || producto.imagen;
    const imagenUrl = (rutaImg && rutaImg !== 'null') ? rutaImg : './src/assets/img/default.jpg';
    const precioFormateado = producto.precio.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 });
    const stock = producto.stock || 0;

    // se arman visualmente las etiquetas (tags) con un estilo de pildora
    let htmlEtiquetas = '';
    if (etiquetas.length > 0) {
        etiquetas.forEach(tag => {
            htmlEtiquetas += `<span style="background: #e0e0e0; color: #333; padding: 4px 10px; border-radius: 12px; font-size: 0.8rem; margin-right: 5px; margin-bottom: 5px; display: inline-block;">#${tag}</span>`;
        });
    } else {
        htmlEtiquetas = '<span style="color: #999; font-size: 0.85rem;">sin etiquetas</span>';
    }

    // se crea el fondo oscuro semitransparente del modal
    const overlay = document.createElement('div');
    overlay.style.cssText = 'position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 2000; display: flex; justify-content: center; align-items: center; padding: 20px; box-sizing: border-box;';

    // se crea la caja de contenido blanca
    const caja = document.createElement('div');
    caja.style.cssText = 'background: white; border-radius: 10px; max-width: 700px; width: 100%; display: flex; flex-wrap: wrap; overflow: hidden; position: relative; box-shadow: 0 5px 25px rgba(0,0,0,0.3);';

    // se inyecta la informacion del producto dividida en dos columnas
    caja.innerHTML = `
        <button id="btn-cerrar-detalle" style="position: absolute; top: 10px; right: 15px; background: none; border: none; font-size: 2rem; cursor: pointer; color: #888; line-height: 1;">&times;</button>
        
        <div style="flex: 1 1 300px; background: #f9f9f9; display: flex; align-items: center; justify-content: center; padding: 20px;">
            <img src="${imagenUrl}" alt="${nombreProd}" style="max-width: 100%; max-height: 300px; object-fit: contain; border-radius: 8px;">
        </div>
        
        <div style="flex: 1 1 300px; padding: 30px 25px; display: flex; flex-direction: column;">
            <span style="color: #666; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 1px; font-weight: bold;">${catProd}</span>
            <h2 style="margin: 5px 0 15px 0; color: #222; font-size: 1.6rem;">${nombreProd}</h2>
            
            <div style="margin-bottom: 15px;">${htmlEtiquetas}</div>
            
            <!-- white-space: pre-wrap respeta los saltos de linea (enters) que el administrador haya escrito -->
            <p style="color: #555; font-size: 0.95rem; line-height: 1.6; flex-grow: 1; white-space: pre-wrap;">${descripcion}</p>
            
            <div style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 20px;">
                <p style="font-size: 1.5rem; font-weight: bold; color: #111; margin: 0 0 5px 0;">${precioFormateado}</p>
                <p style="font-size: 0.85rem; color: ${stock > 0 ? '#28a745' : '#dc3545'}; margin: 0 0 15px 0; font-weight: bold;">
                    ${stock > 0 ? `en stock (${stock} disponibles)` : 'agotado temporalmente'}
                </p>
                
                <button id="btn-agregar-detalle" style="width: 100%; background: #222; color: white; border: none; padding: 12px; border-radius: 6px; font-weight: bold; cursor: pointer; font-size: 1rem; transition: background 0.2s;">agregar al carrito</button>
            </div>
        </div>
    `;

    overlay.appendChild(caja);
    document.body.appendChild(overlay);

    // logica para destruir el modal al cerrar o hacer clic afuera
    const cerrarModal = () => document.body.removeChild(overlay);
    document.getElementById('btn-cerrar-detalle').addEventListener('click', cerrarModal);
    overlay.addEventListener('click', (e) => { if (e.target === overlay) cerrarModal(); });

    // conectar el boton de compra interno
    document.getElementById('btn-agregar-detalle').addEventListener('click', () => {
        agregarAlCarrito(idProd, nombreProd, producto.precio, stock);
        cerrarModal(); 
    });
}