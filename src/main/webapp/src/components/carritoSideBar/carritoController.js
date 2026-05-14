// importamos el servicio de ui
import { cargarComponente } from '../../services/uiService.js';

// arreglo temporal para guardar lo que el usuario compra
let carrito = [];

// funcion para inyectar el carrito en index.html al inicio
export async function inicializarCarrito() {
    await cargarComponente('contenedor-sidebar-carrito', './src/components/carritoSideBar/carritoSidebar.html');
    
    // preparamos los botones de cerrar una vez inyectado el html
    const btnCerrar = document.getElementById('btn-cerrar-carrito');
    const overlay = document.getElementById('overlay-carrito');
    
    // si el usuario da clic en la X o en el fondo oscuro, cerramos
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarCarrito);
    if (overlay) overlay.addEventListener('click', cerrarCarrito);
}

// funcion para mostrar el sidebar agregando clases css
export function abrirCarrito() {
    const sidebar = document.getElementById('sidebar-carrito');
    const overlay = document.getElementById('overlay-carrito');
    
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

// funcion para ocultar el sidebar quitando las clases css
export function cerrarCarrito() {
    const sidebar = document.getElementById('sidebar-carrito');
    const overlay = document.getElementById('overlay-carrito');
    
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}

// funcion para agregar un producto al carrito
export function agregarAlCarrito(id, nombre, precio) {
    // buscamos si el producto ya existe usando un bucle tradicional
    let existe = false;
    for (let i = 0; i < carrito.length; i++) {
        if (carrito[i].id === id) {
            carrito[i].cantidad++;
            existe = true;
            break;
        }
    }
    
    // si no existe lo agregamos como nuevo
    if (!existe) {
        carrito.push({ id: id, nombre: nombre, precio: precio, cantidad: 1 });
    }
    
    // actualizamos la vista del carrito y lo abrimos
    renderizarCarrito();
    abrirCarrito();
}

// funcion para dibujar los productos en el panel lateral
function renderizarCarrito() {
    const contenedor = document.getElementById('items-carrito');
    const txtTotal = document.getElementById('total-carrito');
    
    if (!contenedor || !txtTotal) return;
    
    // limpiamos el contenedor antes de dibujar
    contenedor.innerHTML = '';
    let total = 0;
    
    // recorremos el carrito para inyectar el html de cada item
    for (let i = 0; i < carrito.length; i++) {
        let item = carrito[i];
        let subtotal = item.precio * item.cantidad;
        total += subtotal;
        
        contenedor.innerHTML += `
            <p style="border-bottom: 1px solid #eee; padding: 10px 0;">
                <strong>${item.cantidad}x</strong> ${item.nombre} - $${subtotal}
            </p>`;
    }
    
    txtTotal.innerText = '$' + total;
}