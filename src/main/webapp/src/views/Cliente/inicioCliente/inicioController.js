// importamos el servicio para cargar html
import { cargarComponente } from '../../../services/uiService.js';
// importamos la funcion del carrito para el boton de comprar
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
// importamos nuestro componente de tarjeta unificado y seguro
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';
// importamos el servicio de filtrado exacto para sentar bases solidas en las secciones
import { filtrarPorCategoriaExacta } from '../../../services/filtroService.js';

export async function cargarVistaInicio() {
    // cargamos la vista del inicio en el contenedor principal
    await cargarComponente('component-main', './src/views/Cliente/inicioCliente/inicio.html');
    
    // llamamos al backend para traer los productos reales
    cargarProductosDesdeBD();

    // Asignamos el evento del carrito usando delegacion (se hace una sola vez al cargar la vista)
    asignarEventosCarrito();
}

async function cargarProductosDesdeBD() {
    try {
        // hacemos la peticion al servlet productocontroller pidiendo solo los productos activos
        const respuesta = await fetch('listar?activos=true');
        
        if (respuesta.ok) {
            // convertimos el json que nos mando java a un arreglo
            const productos = await respuesta.json();
            renderizarProductos(productos);
        } else {
            console.error('error al cargar los productos del backend');
        }
    } catch (error) {
        // atrapamos errores de red o servidor caido
        console.error('falla de conexion al intentar traer productos:', error);
    }
}

function renderizarProductos(productos) {
    // usamos la nueva funcion exacta para no confundir categorias.
    // asi, si un producto se llama "vela de flor", no aparecera jamas en la seccion de floristeria.
    const listaDonas = filtrarPorCategoriaExacta(productos, 'dona');
    const listaVelas = filtrarPorCategoriaExacta(productos, 'vela');
    
    // en el futuro, cuando se agregue la seccion html de floristeria, solo se requerira esta linea:
    // const listaFloristeria = filtrarPorCategoriaExacta(productos, 'floristeria');

    // Inyectamos las donas en su seccion exclusiva
    inyectarEnContenedor('contenedor-donas', listaDonas);
    
    // Inyectamos las velas en su seccion exclusiva
    inyectarEnContenedor('contenedor-velas', listaVelas);

    // Mantenemos todos los productos en la seccion principal de destacados
    inyectarEnContenedor('contenedor-producto', productos);
}

/**
 * Funcion auxiliar para inyectar una lista de productos en un contenedor especifico
 */
function inyectarEnContenedor(idContenedor, lista) {
    const contenedor = document.getElementById(idContenedor);
    if (!contenedor) return;

    contenedor.innerHTML = '';

    lista.forEach(prod => {
        const tarjeta = crearTarjetaHTML(prod);
        contenedor.appendChild(tarjeta);
    });
}

/**
 * Utiliza "Delegacion de Eventos" para escuchar los clics en toda la vista.
 * Esto es mucho mas eficiente y seguro para elementos inyectados dinamicamente.
 */
function asignarEventosCarrito() {
    const contenedorPrincipal = document.getElementById('component-main');
    if (!contenedorPrincipal) return;

    // Escuchamos los clics en todo el contenedor principal
    contenedorPrincipal.addEventListener('click', (evento) => {
        // Buscamos si el clic provino de un boton de agregar al carrito (o su icono interno)
        const btn = evento.target.closest('.btn-agregar-carrito');
        
        if (btn) {
            const id = parseInt(btn.dataset.id);
            const nombre = btn.dataset.nombre;
            const precio = parseFloat(btn.dataset.precio);
            const stock = btn.dataset.stock ? parseInt(btn.dataset.stock) : null;
            
            agregarAlCarrito(id, nombre, precio, stock);
        }
    });
}