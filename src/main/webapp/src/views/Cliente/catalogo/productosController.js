/**
 * objetivo de este archivo:
 * controlador principal de la vista del catalogo de productos para el cliente.
 * se encarga de solicitar los productos al servidor, manejar los filtros de 
 * busqueda cruzada en memoria (texto, categorias, etiquetas y precios), y controlar 
 * las interacciones del usuario como agregar al carrito o seleccionar filtros dinamicos.
 */

import { cargarComponente } from '../../../services/uiService.js';
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
// importamos nuestro nuevo componente ui de tarjeta
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';
// importamos los pilares de filtrado y ordenamiento logico
import { busquedaGeneral, filtrarPorCategoriaExacta, filtrarPorEtiqueta, ordenarProductos } from '../../../services/filtroService.js';

// variables globales para manejar el estado del catalogo sin recargar la bd
let todosLosProductos = [];
// variables de estado para recordar que categoria estamos viendo
let terminoBusquedaActual = ''; 
let tipoFiltroActual = 'general';

// variables para los filtros simples de la vista (texto, etiqueta, orden)
let textoFiltroSimple = '';
let etiquetaFiltroSimple = '';
let ordenFiltroSimple = 'relevancia';

/**
 * funcion principal (entry point) para inicializar la vista del catalogo.
 * 1. inyecta el esqueleto html de la pagina.
 * 2. desencadena la carga de productos desde la base de datos.
 * 3. llena dinamicamente el filtro de etiquetas.
 * 4. expone la funcion de filtrado al entorno global para que el menu lateral pueda interactuar con ella.
 */
export async function cargarVistaCatalogo() {
    // 1. inyectamos el contenedor principal para el catalogo (se añade ./ a la ruta)
    await cargarComponente('component-main', './src/views/Cliente/catalogo/catalogo.html');
    
    // 2. obtenemos y mostramos los productos desde el backend
    renderizarProductosCatalogo();
    
    // 3. cargamos las etiquetas de la base de datos de forma dinamica en el select
    cargarEtiquetasFiltro();
    
    // exponemos la funcion al objeto global (window) para que el sidebar pueda usarla
    // sin sufrir el error de "clones vacios" por las rutas de importacion
    window.aplicarFiltroCatalogo = aplicarFiltroInteligente;
    
    // oidos para la actualizacion silenciosa
    if (!window.escuchadorStockCatalogo) {
        window.addEventListener('inventarioActualizado', () => {
            const vistaActiva = window.location.hash.replace(/^#\/?/, '') || 'inicio';
            if (vistaActiva === 'catalogo') renderizarProductosCatalogo();
        });
        window.escuchadorStockCatalogo = true;
    }
}

/**
 * se comunica con el backend para extraer el catalogo completo de productos activos.
 * aplica tecnicas de "cache-busting" para evitar mostrar datos viejos atrapados en el navegador.
 * ademas, utiliza el patron de "delegacion de eventos" para manejar todos los clics de compras 
 * desde un solo escuchador padre, ahorrando memoria ram de forma masiva.
 */
async function renderizarProductosCatalogo() {
    const contenedor = document.getElementById('catalogo-productos-grid');
    if (!contenedor) return;

    // si es una actualizacion silenciosa, no borramos el html viejo para evitar que la pantalla parpadee.
    if (!todosLosProductos || todosLosProductos.length === 0) {
        contenedor.innerHTML = '<p>Cargando productos...</p>';
    }

    // 3. obtenemos los datos limpios directamente mediante fetch (mismo metodo que usa inicio)
    try {
        // cache-busting estricto para evitar el inventario fantasma en el navegador
        const respuesta = await fetch('listar?activos=true&t=' + Date.now(), { cache: 'no-store' });
        if (respuesta.ok) {
            const data = await respuesta.json();
            
            // normalizamos para asegurar compatibilidad estricta con filtroService
            todosLosProductos = data.map(p => {
                p.categoria = p.categoria || p.nombre_categoria || p.nombreCategoria || 'Sin categoria';
                return p;
            });
        } else {
            todosLosProductos = null;
        }
    } catch (error) {
        console.error('Error al traer productos para el catálogo:', error);
        todosLosProductos = null;
    }

    // si el servicio devuelve null, hubo un error de conexion
    if (!todosLosProductos) {
        contenedor.innerHTML = '<p>Hubo un problema al cargar los productos. Por favor, intenta más tarde.</p>';
        return;
    }

    // revisamos si venimos desde el sidebar de categorias con una orden de filtrado en espera
    const filtroPendiente = sessionStorage.getItem('filtroCategoriaSidebar');
    
    if (filtroPendiente !== null) {
        aplicarFiltroInteligente(filtroPendiente, filtroPendiente === '' ? 'general' : 'categoria');
        sessionStorage.removeItem('filtroCategoriaSidebar');
    } else {
        // aplicamos el filtro inteligente para no perder la busqueda del usuario al actualizar
        aplicarFiltroInteligente();
    }

    // 4. asignamos los eventos de clics al nuevo contenedor
    contenedor.addEventListener('click', (evento) => {
        
        // caso a: el usuario hizo clic en el boton del carrito
        const boton = evento.target.closest('.btn-agregar-carrito');
        if (boton) {
            const id = parseInt(boton.dataset.id);
            const nombre = boton.dataset.nombre;
            const precio = parseFloat(boton.dataset.precio);
            // extraemos el stock asegurandonos de que sea un numero. si por alguna razon no viene, enviamos null.
            const stock = boton.dataset.stock ? parseInt(boton.dataset.stock) : null;
            
            agregarAlCarrito(id, nombre, precio, stock);
            return;
        }

        // caso b: el usuario hizo clic en la etiqueta de la categoria de la tarjeta
        const etiqueta = evento.target.closest('.etiqueta');
        if (etiqueta && etiqueta.dataset.filtro) {
            aplicarFiltroInteligente(etiqueta.dataset.filtro, 'categoria');
        }
    });
    
    // 5. asignamos los eventos a los nuevos filtros simples de la vista
    const inputTexto = document.getElementById('filtro-texto');
    const selectEtiqueta = document.getElementById('filtro-etiqueta');
    const selectOrden = document.getElementById('filtro-orden');

    if (inputTexto) {
        inputTexto.addEventListener('input', (e) => {
            textoFiltroSimple = e.target.value;
            aplicarFiltroInteligente(); // refrescamos la vista llamando sin parametros
        });
    }
    if (selectEtiqueta) {
        selectEtiqueta.addEventListener('change', (e) => {
            etiquetaFiltroSimple = e.target.value;
            aplicarFiltroInteligente();
        });
    }
    if (selectOrden) {
        selectOrden.addEventListener('change', (e) => {
            ordenFiltroSimple = e.target.value;
            aplicarFiltroInteligente();
        });
    }
}

/**
 * llama al backend (etiquetacontroller) para obtener todas las etiquetas registradas en el sistema.
 * construye dinamicamente las opciones (<option>) del menu desplegable de filtros,
 * usando extraccion segura para evitar fallos visuales de tipo "undefined".
 */
async function cargarEtiquetasFiltro() {
    try {
        const respuesta = await fetch('etiquetas');
        if (respuesta.ok) {
            const etiquetas = await respuesta.json();
            const selectEtiqueta = document.getElementById('filtro-etiqueta');
            if (!selectEtiqueta) return;
            
            etiquetas.forEach(tag => {
                // extraccion segura: buscamos todas las posibles formas en las que java pudo haber enviado el nombre
                const nombreTag = tag.nombreEtiqueta || tag.nombre_etiqueta || tag.nombre || 'Desconocido';
                
                if (nombreTag !== 'Desconocido') {
                    selectEtiqueta.innerHTML += `<option value="${nombreTag}">${nombreTag}</option>`;
                }
            });
        }
    } catch (error) {
        console.error('Error al cargar etiquetas dinámicas:', error);
    }
}

/**
 * recibe un arreglo de productos (completo o previamente filtrado) y se encarga de pintarlo en la pantalla.
 * incluye proteccion (fail-safe) para que, si la creacion de una tarjeta especifica falla, 
 * no se rompa la vista entera del resto del catalogo.
 * @param {Array} listaProductos - arreglo de objetos producto a iterar y dibujar.
 */
function dibujarGridCatalogo(listaProductos) {
    const contenedor = document.getElementById('catalogo-productos-grid');
    if (!contenedor) return;
    
    contenedor.innerHTML = '';
    
    // failsafe: proteccion por si la lista llega nula desde los servicios de filtrado
    const listaSegura = listaProductos || [];
    
    if (listaSegura.length === 0) {
        contenedor.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #666;">No se encontraron productos con esa búsqueda.</p>';
        return;
    }

    listaSegura.forEach(producto => {
        // 2. blindamos la creacion de cada tarjeta. si una falla, no destruira el resto del catalogo
        try {
            const tarjetaNodo = crearTarjetaHTML(producto);
            contenedor.appendChild(tarjetaNodo);
        } catch (error) {
            console.error('Error silencioso al intentar crear la tarjeta del producto:', producto, error);
        }
    });
}

/**
 * el "cerebro" del sistema de busqueda y filtrado en la memoria del navegador.
 * combina multiples criterios de forma escalonada (cascada) sin volver a consultar a la base de datos:
 * 1. filtro base (proveniente del menu lateral o clic directo en las etiquetas de las tarjetas).
 * 2. filtro de texto libre (barra de busqueda).
 * 3. filtro por etiqueta seleccionada en el 'select'.
 * 4. ordenamiento matematico o alfabetico (ascendente/descendente).
 * finalmente envia la lista resultante al motor de dibujado.
 * @param {string} terminoBusqueda - palabra clave principal o termino de filtro de origen.
 * @param {string} tipoFiltro - categoria logica del filtro a aplicar ('categoria', 'etiqueta', o 'general').
 */
export function aplicarFiltroInteligente(terminoBusqueda, tipoFiltro) {
    // guardamos el termino y el tipo en memoria
    if (terminoBusqueda !== undefined) terminoBusquedaActual = terminoBusqueda;
    if (tipoFiltro !== undefined) tipoFiltroActual = tipoFiltro;

    // aseguramos que siempre arranquemos con un arreglo valido
    let listaFiltrada = todosLosProductos || [];
    
    // proteccion: forzamos el termino a texto para evitar que .trim() lance error
    const terminoTexto = (terminoBusquedaActual || '').toString().trim();

    // 1. aplicamos el filtro principal (del sidebar o clic en una categoria)
    if (terminoTexto !== '') {
        if (tipoFiltroActual === 'categoria') {
            listaFiltrada = filtrarPorCategoriaExacta(listaFiltrada, terminoTexto) || [];
        } else if (tipoFiltroActual === 'etiqueta') {
            listaFiltrada = filtrarPorEtiqueta(listaFiltrada, terminoTexto) || [];
        } else {
            listaFiltrada = busquedaGeneral(listaFiltrada, terminoTexto) || [];
        }
    }
    
    // 2. filtro simple por texto libre
    if (textoFiltroSimple.trim() !== '') {
        listaFiltrada = busquedaGeneral(listaFiltrada, textoFiltroSimple) || [];
    }
    
    // 3. filtro simple por etiqueta seleccionada
    if (etiquetaFiltroSimple !== '') {
        listaFiltrada = filtrarPorEtiqueta(listaFiltrada, etiquetaFiltroSimple) || [];
    }
    
    // 4. aplicamos el orden por precio (mayor/menor)
    if (ordenFiltroSimple !== 'relevancia') {
        listaFiltrada = ordenarProductos(listaFiltrada, ordenFiltroSimple) || listaFiltrada;
    }
    
    // 5. actualizar el titulo para dar feedback visual de que el filtro funciono
    const tituloCatalogo = document.querySelector('.catalogo-header h1');
    if (tituloCatalogo) {
        if (terminoTexto === '') {
            tituloCatalogo.textContent = 'Catálogo de Productos';
        } else {
            // capitalizamos la primera letra (ej. floristeria -> Floristeria)
            const terminoCapitalizado = terminoTexto.charAt(0).toUpperCase() + terminoTexto.slice(1);
            tituloCatalogo.textContent = `Catálogo - ${terminoCapitalizado}`;
        }
    }

    // dibujamos en pantalla el resultado final limpio
    dibujarGridCatalogo(listaFiltrada);
}