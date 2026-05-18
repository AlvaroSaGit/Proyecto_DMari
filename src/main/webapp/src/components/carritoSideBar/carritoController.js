// importamos el servicio de ui para la inyeccion de componentes
import { cargarComponente } from '../../services/uiService.js';
// importamos la funcion para cargar el login en caso de que no este registrado
import { cargarVistaLogin } from '../../views/Auth/login/loginController.js';

// intentamos cargar el carrito guardado en el navegador, si no hay, iniciamos vacio
let carrito = JSON.parse(localStorage.getItem('carritoDMari')) || [];

// funcion que inyecta el html del carrito oculto en el index al cargar la pagina
export async function inicializarCarrito() {
    // esperamos a que el componente html se coloque en su contenedor
    await cargarComponente('contenedor-sidebar-carrito', './src/components/carritoSideBar/carritoSidebar.html');
    
    // preparamos los elementos para poder cerrar el carrito
    const btnCerrar = document.getElementById('btn-cerrar-carrito');
    const overlay = document.getElementById('overlay-carrito');
    const btnComprar = document.getElementById('btn-comprar-carrito'); // Buscamos el boton de pagar
    
    // si el usuario da clic en la x o en el fondo oscuro, se oculta el menu
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarCarrito);
    if (overlay) overlay.addEventListener('click', cerrarCarrito);

    // si el usuario da clic en comprar, verificamos su sesion
    if (btnComprar) {
        btnComprar.addEventListener('click', procesarCompra);
    }

    // Si hay items recuperados del localstorage, renderizarlos visualmente de una vez
    if(carrito.length > 0) {
        renderizarCarrito();
    }
}

// funcion que se ejecuta al intentar pagar
async function procesarCompra() {
    // evitamos que proceda si el carrito esta vacio
    if (carrito.length === 0) {
        alert('Tu carrito está vacío. ¡Agrega algunos productos primero!');
        return;
    }

    try {
        // Le preguntamos al backend de Java si el usuario tiene la "pulsera" de logueado
        const respuesta = await fetch('session');

        if (respuesta.ok) {
            // Respondió 200 OK: Está logueado
            alert('¡Excelente! Procesando tu compra...');
            // A futuro: Aquí enviaremos los datos del carrito a Java para crear el Pedido en BD.
        } else {
            // Respondió 401: No está logueado
            alert('Por favor, inicia sesión o regístrate para poder finalizar tu compra.');
            cerrarCarrito(); // Ocultamos el carrito
            cargarVistaLogin(); // Lo llevamos a la pantalla de login
        }
    } catch (error) {
        console.error('Error al verificar la sesión:', error);
        alert('Hubo un error de conexión con el servidor.');
    }
}

// funcion para desplegar visualmente el carrito usando css
export function abrirCarrito() {
    const sidebar = document.getElementById('sidebar-carrito');
    const overlay = document.getElementById('overlay-carrito');
    
    // agregamos la clase que lo desliza a la pantalla
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

// funcion para esconder el carrito retirando las clases de css
export function cerrarCarrito() {
    const sidebar = document.getElementById('sidebar-carrito');
    const overlay = document.getElementById('overlay-carrito');
    
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}

// funcion encargada de recibir un producto nuevo o sumar su cantidad
export function agregarAlCarrito(id, nombre, precio) {
    // buscamos si el producto ya existe en nuestro arreglo en memoria
    const productoExistente = carrito.find(item => item.id === id);
    
    if (productoExistente) {
        // si el producto ya estaba, solo incrementamos cuantas unidades quiere
        productoExistente.cantidad++;
    } else {
        // si es un producto nuevo, insertamos el objeto completo al arreglo
        carrito.push({ id: id, nombre: nombre, precio: precio, cantidad: 1 });
    }
    
    // Guardamos la informacion actualizada en el navegador
    localStorage.setItem('carritoDMari', JSON.stringify(carrito));

    // tras modificar los datos, redibujamos el html y forzamos a abrir el panel
    renderizarCarrito();
    abrirCarrito();
}

// funcion que recorre el arreglo de productos y arma el html visual
function renderizarCarrito() {
    const contenedor = document.getElementById('items-carrito');
    const txtTotal = document.getElementById('total-carrito');
    
    // proteccion en caso de que el html aun no este listo
    if (!contenedor || !txtTotal) return;
    
    let total = 0;
    // variable tipo texto (string) para acumular el html sin tocar el dom repetidas veces
    let htmlCarrito = ''; 
    
    // recorremos los elementos del carrito para ir calculando precios y armar etiquetas
    carrito.forEach(item => {
        // calculamos cuanto cuesta en total este articulo por su cantidad
        let subtotal = item.precio * item.cantidad;
        // sumamos al monto total de la compra
        total += subtotal;
        
        // concatenamos el texto con el bloque html de este articulo
        htmlCarrito += `
            <p class="item-carrito-producto">
                <strong>${item.cantidad}x</strong> ${item.nombre} - $${subtotal}
            </p>`;
    });
    
    // inyectamos de golpe todo el texto armado en el contenedor principal (mejor rendimiento)
    contenedor.innerHTML = htmlCarrito;
    
    // actualizamos el texto del total a pagar
    txtTotal.innerText = '$' + total;
}