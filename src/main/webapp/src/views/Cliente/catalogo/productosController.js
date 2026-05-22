import { cargarComponente } from '../../../services/uiService.js';
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
// importamos el servicio que maneja los datos
import { obtenerProductos } from '../../../services/productoService.js';
// importamos nuestro nuevo componente ui de tarjeta
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';
// importamos nuestro nuevo servicio de filtrado y ordenamiento logico
import { filtrarProductos, ordenarProductos } from '../../../services/filtroService.js';

// Variables globales para manejar el estado del catalogo sin recargar la BD
let todosLosProductos = [];
let eventosAsignados = false;
// Variables de estado para recordar que estamos buscando y como ordenarlo
let terminoBusquedaActual = ''; 
let criterioOrdenActual = 'relevancia';
let precioMinActual = 0;
let precioMaxActual = Infinity;

/**
 * funcion principal para cargar la vista del catalogo.
 * inyecta el html base y luego renderiza los productos.
 */
export async function cargarVistaCatalogo() {
    // 1. inyectamos el contenedor principal para el catalogo
    await cargarComponente('component-main', 'src/views/Cliente/catalogo/catalogo.html');
    
    // 1.5 Inyectamos el componente de filtros avanzado que armamos
    await cargarComponente('component-filtros', './src/assets/components/filtro/filtro.html');

    // 2. obtenemos y mostramos los productos desde el backend
    renderizarProductosCatalogo();
}

/**
 * obtiene los productos del backend y los muestra en el dom.
 */
async function renderizarProductosCatalogo() {
    const contenedor = document.getElementById('catalogo-productos-grid');
    if (!contenedor) return;

    // mostramos un estado de carga mientras esperamos los datos
    contenedor.innerHTML = '<p>Cargando productos...</p>';

    // 3. obtenemos los datos limpios desde nuestro servicio
    todosLosProductos = await obtenerProductos();

    // si el servicio devuelve null, hubo un error de conexion
    if (!todosLosProductos) {
        contenedor.innerHTML = '<p>Hubo un problema al cargar los productos. Por favor, intenta más tarde.</p>';
        return;
    }

    // Dibujamos todos los productos inicialmente
    dibujarGridCatalogo(todosLosProductos);

    // 4. Asignamos los eventos de clics al contenedor principal (solo una vez)
    if (!eventosAsignados) {
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
                aplicarFiltroInteligente(etiqueta.dataset.filtro);
            }
        });
        
        // Activamos la escucha del componente de filtros (por si escribes en un input o haces clic en un boton alli)
        configurarBuscadorFiltros();
        
        eventosAsignados = true;
    }
}

/**
 * Dibuja las tarjetas en pantalla basandose en el arreglo que le pasen (todos o filtrados)
 */
function dibujarGridCatalogo(listaProductos) {
    const contenedor = document.getElementById('catalogo-productos-grid');
    if (!contenedor) return;
    
    contenedor.innerHTML = '';
    if (listaProductos.length === 0) {
        contenedor.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #666;">No se encontraron productos con esa búsqueda.</p>';
        return;
    }

    listaProductos.forEach(producto => {
        const tarjetaNodo = crearTarjetaHTML(producto);
        contenedor.appendChild(tarjetaNodo);
    });
}

/**
 * Busca entradas de texto o botones dentro del componente filtro.html
 */
function configurarBuscadorFiltros() {
    const contenedorFiltros = document.getElementById('component-filtros');
    if (!contenedorFiltros) return;

    // Si tienes un input (caja de texto) en tu filtro.html para buscar, atrapara cuando escribas
    contenedorFiltros.addEventListener('input', (evento) => {
        if (evento.target.tagName === 'INPUT' && evento.target.type === 'text') {
            aplicarFiltroInteligente(evento.target.value);
        }
    });
    
    // Si tienes botones de filtro rapido (ej. <button class="btn-macro-filtro" data-filtro="reposteria">)
    contenedorFiltros.addEventListener('click', (evento) => {
        const btnFiltro = evento.target.closest('.btn-macro-filtro');
        if (btnFiltro && btnFiltro.dataset.filtro) {
            // Actualizamos la vista visual de los botones (marcamos cual esta activo)
            document.querySelectorAll('.btn-macro-filtro').forEach(btn => btn.classList.remove('activo'));
            btnFiltro.classList.add('activo');
            
            // Limpiamos los campos de precio para evitar choques visuales
            document.getElementById('precio-min').value = '';
            document.getElementById('precio-max').value = '';
            precioMinActual = 0; precioMaxActual = Infinity;

            aplicarFiltroInteligente(btnFiltro.dataset.filtro);
        }
    });
    
    // Escuchar boton de "Filtrar" por rango de precios
    const btnBuscarPrecio = document.querySelector('.btn-buscar-precio');
    if (btnBuscarPrecio) {
        btnBuscarPrecio.addEventListener('click', () => {
            // Leemos las cajas de texto y si estan vacias asignamos 0 o Infinito
            const inputMin = parseFloat(document.getElementById('precio-min').value) || 0;
            const inputMax = parseFloat(document.getElementById('precio-max').value) || Infinity;
            
            precioMinActual = inputMin;
            precioMaxActual = inputMax;
            
            // Re-filtramos usando el mismo termino que teniamos antes + los nuevos precios
            aplicarFiltroInteligente(terminoBusquedaActual);
        });
    }

    // Escuchar el cambio en la lista desplegable de "Ordenar por"
    contenedorFiltros.addEventListener('change', (evento) => {
        if (evento.target.id === 'select-ordenar') {
            criterioOrdenActual = evento.target.value;
            aplicarFiltroInteligente(terminoBusquedaActual); // Re-filtramos aplicando el nuevo orden
        }
    });

    // Cargar los botones de etiquetas dinamicamente desde la base de datos
    cargarBotonesFiltroDinamicos();
}

/**
 * El cerebro del sistema: Mapea palabras maestras a palabras reales de la BD y filtra.
 */
export function aplicarFiltroInteligente(terminoBusqueda) {
    // Guardamos el termino actual en memoria por si el usuario cambia el orden luego
    terminoBusquedaActual = terminoBusqueda || '';

    // 1. Delegamos el filtrado puro a nuestro servicio
    const listaFiltrada = filtrarProductos(todosLosProductos, terminoBusquedaActual, precioMinActual, precioMaxActual);
    
    // 2. Delegamos el ordenamiento a nuestro servicio
    const listaOrdenada = ordenarProductos(listaFiltrada, criterioOrdenActual);
    
    // 3. Dibujamos en pantalla el resultado final
    dibujarGridCatalogo(listaOrdenada);
}

/**
 * Llama al backend para obtener categorias y etiquetas y crear los botones visuales en sus secciones.
 */
async function cargarBotonesFiltroDinamicos() {
    // 1. Cargar las categorias maestras en el bloque 1
    try {
        const resCat = await fetch('categorias');
        if (resCat.ok) {
            const categoriasBD = await resCat.json();
            const contenedorCat = document.getElementById('contenedor-categorias-filtro');
            
            if (contenedorCat) {
                contenedorCat.innerHTML = '<button class="btn-filtro btn-macro-filtro activo" data-filtro="">Todas</button>';
                categoriasBD.forEach(cat => {
                    contenedorCat.innerHTML += `<button class="btn-filtro btn-macro-filtro" data-filtro="${cat.nombre}">${cat.nombre}</button>`;
                });
            }
        }
    } catch (error) { console.error('Error al cargar categorias en filtros:', error); }

    // 2. Cargar las etiquetas secundarias en el bloque 2
    try {
        const resTag = await fetch('etiquetas');
        if (resTag.ok) {
            const etiquetasBD = await resTag.json();
            const contenedorTag = document.getElementById('contenedor-etiquetas-filtro');
            
            if (contenedorTag) {
                contenedorTag.innerHTML = ''; // borramos el texto de carga
                etiquetasBD.forEach(tag => {
                    contenedorTag.innerHTML += `<button class="btn-filtro btn-macro-filtro" data-filtro="${tag.nombre}">${tag.nombre}</button>`;
                });
            }
        }
    } catch (error) {
        console.error('Error al cargar etiquetas en filtros:', error);
    }
}