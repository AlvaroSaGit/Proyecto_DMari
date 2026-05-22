// importamos el servicio para cargar html
import { cargarComponente } from '../../../services/uiService.js';
// importamos la funcion del carrito para el boton de comprar
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
// importamos nuestro componente de tarjeta unificado y seguro
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';

export async function cargarVistaInicio() {
    // cargamos la vista del inicio en el contenedor principal
    await cargarComponente('component-main', './src/views/Cliente/inicioCliente/inicio.html');
    
    // llamamos al backend para traer los productos reales
    cargarProductosDesdeBD();
}

async function cargarProductosDesdeBD() {
    try {
        // hacemos la peticion al servlet ProductoController en la ruta /listar
        const respuesta = await fetch('listar');
        
        if (respuesta.ok) {
            // convertimos el json que nos mando java a un arreglo de javascript
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
    // buscamos el contenedor de destacados en el html
    const contenedor = document.getElementById('contenedor-producto');
    if (!contenedor) return;

    // limpiamos el contenedor por si tenia html viejo
    contenedor.innerHTML = '';

    // recorremos la lista de productos
    productos.forEach(prod => {
        // creamos la tarjeta usando nuestro componente unificado
        const tarjeta = crearTarjetaHTML(prod);

        // inyectamos la tarjeta en el contenedor de la pagina web
        contenedor.appendChild(tarjeta);
    });

    // damos accion a los botones recien dibujados
    asignarEventosCarrito();
}

/**
 * Busca todos los botones de "Agregar al carrito" generados dinamicamente
 * y les asigna un evento de clic. Extrae los datos del producto (id, nombre, precio, stock)
 * guardados en los atributos 'data-' y los envia al controlador del carrito.
 */
function asignarEventosCarrito() {
    const botones = document.querySelectorAll('.btn-agregar-carrito');
    
    botones.forEach(btn => {
        btn.addEventListener('click', (evento) => {
            // Extraemos la informacion del producto desde el boton que recibio el clic
            const id = parseInt(evento.target.getAttribute('data-id'));
            const nombre = evento.target.getAttribute('data-nombre');
            const precio = parseFloat(evento.target.getAttribute('data-precio'));
            // Validamos que el stock exista y sea un numero, de lo contrario enviamos null
            const stock = evento.target.getAttribute('data-stock') ? parseInt(evento.target.getAttribute('data-stock')) : null;
            
            // enviamos los datos extraidos a la funcion que maneja el arreglo del carrito
            agregarAlCarrito(id, nombre, precio, stock);
        });
    });
}