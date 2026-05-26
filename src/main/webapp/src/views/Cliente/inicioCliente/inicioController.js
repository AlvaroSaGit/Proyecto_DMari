/**
 * objetivo de este archivo:
 * controlador de la vista principal (inicio) del cliente.
 * se encarga de cargar los productos destacados divididos por categorias
 * y de inyectarlos en sus respectivas "vitrinas" (contenedores).
 */

import { cargarComponente } from '../../../services/uiService.js';
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';
import { filtrarPorCategoriaExacta } from '../../../services/filtroService.js';

/**
 * funcion principal (entry point) de la vista de inicio.
 * inyecta el html en la pantalla, pide los productos a java y 
 * activa los escuchadores para el carrito de compras y la actualizacion de stock.
 */
export async function cargarVistaInicio() {
    await cargarComponente('component-main', './src/views/Cliente/inicioCliente/inicio.html');
    
    // llamamos al backend para traer los productos reales
    cargarProductosDesdeBD();

    // asignamos el evento del carrito usando delegacion (se hace una sola vez al cargar la vista)
    asignarEventosCarrito();
    
    // oidos para la actualizacion silenciosa
    if (!window.escuchadorStockInicio) {
        window.addEventListener('inventarioActualizado', () => {
            const vistaActiva = window.location.hash.replace(/^#\/?/, '') || 'inicio';
            // condicional de refresco: solo actualiza si el usuario realmente esta mirando la pantalla de inicio
            if (vistaActiva === 'inicio') cargarProductosDesdeBD();
        });
        window.escuchadorStockInicio = true;
    }
}

/**
 * se conecta al backend de java para pedir el inventario actual.
 * usa cache-busting para evitar que el navegador guarde versiones viejas de la tienda.
 */
async function cargarProductosDesdeBD() {
    try {
        // cache: 'no-store' asegura que le pida a Java el inventario fresco
        const respuesta = await fetch('listar?activos=true&t=' + Date.now(), { cache: 'no-store' });
        
        // condicional de red: evalua si el servidor http respondio con codigo 200 ok
        if (respuesta.ok) {
            // convertimos el json que nos mando java a un arreglo
            const productos = await respuesta.json();
            
            // normalizamos la propiedad categoria por si java la envio con otro nombre
            // iteracion interna (map): recorre el arreglo mutando los nombres de atributos para evitar fallos
            const productosNormalizados = productos.map(p => {
                p.categoria = p.categoria || p.nombre_categoria || p.nombreCategoria || 'Sin categoria';
                return p;
            });
            renderizarProductos(productosNormalizados);
        } else {
            console.error('error al cargar los productos del backend');
        }
    } catch (error) {
        // atrapamos errores de red o servidor caido
        console.error('falla de conexion al intentar traer productos:', error);
    }
}

/**
 * distribuye los productos de la tienda en sus vitrinas correspondientes.
 * usa el filtro exacto para no mezclar donas con flores.
 * 
 * @param {Array} productos - lista completa de inventario.
 */
function renderizarProductos(productos) {
    const listaReposteria = filtrarPorCategoriaExacta(productos, 'Reposteria');
    const listaDecoracion = filtrarPorCategoriaExacta(productos, 'Decoracion');
    const listaFloristeria = filtrarPorCategoriaExacta(productos, 'Floristeria');

    // intentamos inyectar en las vitrinas separadas
    const okReposteria = inyectarEnContenedor('contenedor-reposteria', listaReposteria);
    const okDecoracion = inyectarEnContenedor('contenedor-decoracion', listaDecoracion);
    const okFloristeria = inyectarEnContenedor('contenedor-floristeria', listaFloristeria);

    // condicional de respaldo (fallback): si los tres contenedores especificos fallaron o las listas
    // estaban vacias, inyecta toda la mercancia en un contenedor global de emergencia.
    if (!okReposteria && !okDecoracion && !okFloristeria) {
        inyectarEnContenedor('contenedor-producto', productos);
    }
}

/**
 * funcion auxiliar para inyectar una lista de productos en un contenedor html especifico.
 * 
 * @param {string} idContenedor - el id del div en el dom.
 * @param {Array} lista - los productos que deben ir en ese div.
 * @returns {boolean} - true si logro inyectar al menos una tarjeta, false si fallo.
 */
function inyectarEnContenedor(idContenedor, lista) {
    const contenedor = document.getElementById(idContenedor);
    // condicional de prevencion: si el html fue modificado y no existe el div, aborta sin arrojar error.
    if (!contenedor) return false;

    contenedor.innerHTML = '';
    
    // condicional vacio: si no hay productos para esta categoria, aborta devolviendo falso
    if (!lista || lista.length === 0) return false;

    // iteracion: recorre cada producto de la lista para crear y adjuntar su tarjeta html
    lista.forEach(prod => {
        try {
            const tarjeta = crearTarjetaHTML(prod);
            contenedor.appendChild(tarjeta);
        } catch (e) { console.error("error al crear tarjeta individual", e); }
    });

    return true;
}

/**
 * utiliza "delegacion de eventos" para escuchar los clics en toda la vista.
 * esto es mucho mas eficiente y seguro para elementos inyectados dinamicamente.
 */
function asignarEventosCarrito() {
    const contenedorPrincipal = document.getElementById('component-main');
    if (!contenedorPrincipal) return;

    // condicional logico: previene que se asigne el mismo evento multiple veces (evitando el clic doble en el carrito)
    if (contenedorPrincipal.dataset.eventosCarritoAsignados === 'true') return;
    contenedorPrincipal.dataset.eventosCarritoAsignados = 'true';

    // escuchamos los clics en todo el contenedor principal
    contenedorPrincipal.addEventListener('click', (evento) => {
        // buscamos si el clic provino de un boton de agregar al carrito (o su icono interno)
        const btn = evento.target.closest('.btn-agregar-carrito');
        
        // condicional interactivo: si el clic coincidio exactamente con el boton (o su icono)
        if (btn) {
            const id = parseInt(btn.dataset.id);
            const nombre = btn.dataset.nombre;
            const precio = parseFloat(btn.dataset.precio);
            const stock = btn.dataset.stock ? parseInt(btn.dataset.stock) : null;
            
            agregarAlCarrito(id, nombre, precio, stock);
        }
    });
}