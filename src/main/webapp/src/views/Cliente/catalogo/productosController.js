/**
 * Objetivo de este archivo:
 * Controlador principal de la vista del Catálogo de Productos para el Cliente.
 * Se encarga de solicitar los productos al servidor, manejar los filtros de 
 * búsqueda cruzada en memoria (texto, categorías, etiquetas y precios), y controlar 
 * las interacciones del usuario como agregar al carrito o seleccionar filtros dinámicos.
 */

import { cargarComponente } from '../../../services/uiService.js';
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
// importamos nuestro nuevo componente ui de tarjeta
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';
// importamos los pilares de filtrado y ordenamiento logico
import { busquedaGeneral, filtrarPorCategoriaExacta, filtrarPorEtiqueta, ordenarProductos } from '../../../services/filtroService.js';

// Variables globales para manejar el estado del catalogo sin recargar la BD
let todosLosProductos = [];
// Variables de estado para recordar que categoria estamos viendo
let terminoBusquedaActual = ''; 
let tipoFiltroActual = 'general';

// Variables para los filtros simples de la vista (texto, etiqueta, orden)
let textoFiltroSimple = '';
let etiquetaFiltroSimple = '';
let ordenFiltroSimple = 'relevancia';

/**
 * Función principal (Entry Point) para inicializar la vista del catálogo.
 * 1. Inyecta el esqueleto HTML de la página.
 * 2. Desencadena la carga de productos desde la base de datos.
 * 3. Llena dinámicamente el filtro de etiquetas.
 * 4. Expone la función de filtrado al entorno global para que el menú lateral pueda interactuar con ella.
 */
export async function cargarVistaCatalogo() {
    // 1. inyectamos el contenedor principal para el catalogo (Se añade ./ a la ruta)
    await cargarComponente('component-main', './src/views/Cliente/catalogo/catalogo.html');
    
    // 2. obtenemos y mostramos los productos desde el backend
    renderizarProductosCatalogo();
    
    // 3. Cargamos las etiquetas de la base de datos de forma dinamica en el select
    cargarEtiquetasFiltro();
    
    // Exponemos la funcion al objeto global (window) para que el sidebar pueda usarla
    // sin sufrir el error de "clones vacios" por las rutas de importacion
    window.aplicarFiltroCatalogo = aplicarFiltroInteligente;
}

/**
 * Se comunica con el backend para extraer el catálogo completo de productos activos.
 * Aplica técnicas de "Cache-Busting" para evitar mostrar datos viejos atrapados en el navegador.
 * Ademas, utiliza el patrón de "Delegación de Eventos" para manejar todos los clics de compras 
 * desde un solo escuchador padre, ahorrando memoria RAM de forma masiva.
 */
async function renderizarProductosCatalogo() {
    const contenedor = document.getElementById('catalogo-productos-grid');
    if (!contenedor) return;

    // mostramos un estado de carga mientras esperamos los datos
    contenedor.innerHTML = '<p>Cargando productos...</p>';

    // 3. Obtenemos los datos limpios directamente mediante fetch (mismo metodo que usa inicio)
    try {
        // Agregamos un timestamp (&t=...) para obligar al navegador a pedirle los datos a Java y burlar el caché
        const respuesta = await fetch('listar?activos=true&t=' + new Date().getTime());
        if (respuesta.ok) {
            todosLosProductos = await respuesta.json();
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
        // si no habia ordenes del sidebar, dibujamos todos los productos inicialmente
        dibujarGridCatalogo(todosLosProductos);
    }

    // 4. Asignamos los eventos de clics al nuevo contenedor
    contenedor.addEventListener('click', (evento) => {
        
        // CASO A: El usuario hizo clic en el boton del carrito
        const boton = evento.target.closest('.btn-agregar-carrito');
        if (boton) {
            const id = parseInt(boton.dataset.id);
            const nombre = boton.dataset.nombre;
            const precio = parseFloat(boton.dataset.precio);
            // Extraemos el stock asegurandonos de que sea un numero. Si por alguna razon no viene, enviamos null.
            const stock = boton.dataset.stock ? parseInt(boton.dataset.stock) : null;
            
            agregarAlCarrito(id, nombre, precio, stock);
            return;
        }

        // CASO B: El usuario hizo clic en la etiqueta de la categoria de la tarjeta
        const etiqueta = evento.target.closest('.etiqueta');
        if (etiqueta && etiqueta.dataset.filtro) {
            aplicarFiltroInteligente(etiqueta.dataset.filtro, 'categoria');
        }
    });
    
    // 5. Asignamos los eventos a los nuevos filtros simples de la vista
    const inputTexto = document.getElementById('filtro-texto');
    const selectEtiqueta = document.getElementById('filtro-etiqueta');
    const selectOrden = document.getElementById('filtro-orden');

    if (inputTexto) {
        inputTexto.addEventListener('input', (e) => {
            textoFiltroSimple = e.target.value;
            aplicarFiltroInteligente(); // Refrescamos la vista llamando sin parametros
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
 * Llama al backend (EtiquetaController) para obtener todas las etiquetas registradas en el sistema.
 * Construye dinámicamente las opciones (<option>) del menú desplegable de filtros,
 * usando extracción segura para evitar fallos visuales de tipo "undefined".
 */
async function cargarEtiquetasFiltro() {
    try {
        const respuesta = await fetch('etiquetas');
        if (respuesta.ok) {
            const etiquetas = await respuesta.json();
            const selectEtiqueta = document.getElementById('filtro-etiqueta');
            if (!selectEtiqueta) return;
            
            etiquetas.forEach(tag => {
                // Extraccion segura: buscamos todas las posibles formas en las que Java pudo haber enviado el nombre
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
 * Recibe un arreglo de productos (completo o previamente filtrado) y se encarga de pintarlo en la pantalla.
 * Incluye protección (Fail-safe) para que, si la creación de una tarjeta específica falla, 
 * no se rompa la vista entera del resto del catálogo.
 * @param {Array} listaProductos - Arreglo de objetos producto a iterar y dibujar.
 */
function dibujarGridCatalogo(listaProductos) {
    const contenedor = document.getElementById('catalogo-productos-grid');
    if (!contenedor) return;
    
    contenedor.innerHTML = '';
    
    // Failsafe: Proteccion por si la lista llega nula desde los servicios de filtrado
    const listaSegura = listaProductos || [];
    
    if (listaSegura.length === 0) {
        contenedor.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #666;">No se encontraron productos con esa búsqueda.</p>';
        return;
    }

    listaSegura.forEach(producto => {
        // 2. Blindamos la creacion de cada tarjeta. Si una falla, no destruira el resto del catalogo
        try {
            const tarjetaNodo = crearTarjetaHTML(producto);
            contenedor.appendChild(tarjetaNodo);
        } catch (error) {
            console.error('Error silencioso al intentar crear la tarjeta del producto:', producto, error);
        }
    });
}

/**
 * El "Cerebro" del sistema de búsqueda y filtrado en la memoria del navegador.
 * Combina múltiples criterios de forma escalonada (Cascada) sin volver a consultar a la base de datos:
 * 1. Filtro base (proveniente del menú lateral o clic directo en las etiquetas de las tarjetas).
 * 2. Filtro de texto libre (barra de búsqueda).
 * 3. Filtro por etiqueta seleccionada en el 'select'.
 * 4. Ordenamiento matemático o alfabético (Ascendente/Descendente).
 * Finalmente envía la lista resultante al motor de dibujado.
 * @param {string} terminoBusqueda - Palabra clave principal o término de filtro de origen.
 * @param {string} tipoFiltro - Categoría lógica del filtro a aplicar ('categoria', 'etiqueta', o 'general').
 */
export function aplicarFiltroInteligente(terminoBusqueda, tipoFiltro) {
    // Guardamos el termino y el tipo en memoria
    if (terminoBusqueda !== undefined) terminoBusquedaActual = terminoBusqueda;
    if (tipoFiltro !== undefined) tipoFiltroActual = tipoFiltro;

    // Aseguramos que siempre arranquemos con un arreglo valido
    let listaFiltrada = todosLosProductos || [];
    
    // Proteccion: forzamos el termino a texto para evitar que .trim() lance error
    const terminoTexto = (terminoBusquedaActual || '').toString().trim();

    // 1. Aplicamos el filtro principal (del Sidebar o clic en una categoria)
    if (terminoTexto !== '') {
        if (tipoFiltroActual === 'categoria') {
            listaFiltrada = filtrarPorCategoriaExacta(listaFiltrada, terminoTexto) || [];
        } else if (tipoFiltroActual === 'etiqueta') {
            listaFiltrada = filtrarPorEtiqueta(listaFiltrada, terminoTexto) || [];
        } else {
            listaFiltrada = busquedaGeneral(listaFiltrada, terminoTexto) || [];
        }
    }
    
    // 2. Filtro simple por texto libre
    if (textoFiltroSimple.trim() !== '') {
        listaFiltrada = busquedaGeneral(listaFiltrada, textoFiltroSimple) || [];
    }
    
    // 3. Filtro simple por etiqueta seleccionada
    if (etiquetaFiltroSimple !== '') {
        listaFiltrada = filtrarPorEtiqueta(listaFiltrada, etiquetaFiltroSimple) || [];
    }
    
    // 4. Aplicamos el orden por precio (Mayor/Menor)
    if (ordenFiltroSimple !== 'relevancia') {
        listaFiltrada = ordenarProductos(listaFiltrada, ordenFiltroSimple) || listaFiltrada;
    }
    
    // 2. Actualizar el titulo para dar feedback visual de que el filtro funciono
    const tituloCatalogo = document.querySelector('.catalogo-header h1');
    if (tituloCatalogo) {
        if (terminoTexto === '') {
            tituloCatalogo.textContent = 'Catálogo de Productos';
        } else {
            // Capitalizamos la primera letra (ej. floristeria -> Floristeria)
            const terminoCapitalizado = terminoTexto.charAt(0).toUpperCase() + terminoTexto.slice(1);
            tituloCatalogo.textContent = `Catálogo - ${terminoCapitalizado}`;
        }
    }

    // Dibujamos en pantalla el resultado final limpio
    dibujarGridCatalogo(listaFiltrada);
}