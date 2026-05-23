// importamos el servicio de ui para la inyeccion de componentes
import { cargarComponente } from '../../services/uiService.js';
// importamos el enrutador para la navegacion
import { navegarA } from '../../router/router.js';
// importamos el servicio encargado de los pedidos
import { enviarPedido } from '../pedido/pedidoService.js';

// intentamos cargar el carrito guardado en el navegador, si no hay, iniciamos vacio
let carrito = JSON.parse(localStorage.getItem('carritoDMari')) || [];

// funcion que inyecta el html del carrito oculto en el index al cargar la pagina
export async function inicializarCarrito() {
    // esperamos a que el componente html se coloque en su contenedor
    await cargarComponente('contenedor-sidebar-carrito', './src/components/carritoSideBar/carritoSidebar.html');
    
    // Agregamos una regla CSS dinamica para quitar las flechitas (spinners) del input en Chrome, Edge y Safari
    if (!document.getElementById('estilo-carrito-input')) {
        const estilo = document.createElement('style');
        estilo.id = 'estilo-carrito-input';
        estilo.textContent = `
            .input-cantidad::-webkit-inner-spin-button,
            .input-cantidad::-webkit-outer-spin-button {
                -webkit-appearance: none;
                margin: 0;
            }
        `;
        document.head.appendChild(estilo);
    }

    // preparamos los elementos para poder cerrar el carrito
    const btnCerrar = document.getElementById('btn-cerrar-carrito');
    const overlay = document.getElementById('overlay-carrito');
    const btnComprar = document.getElementById('btn-comprar-carrito'); // Buscamos el boton de pagar
    
    // si el usuario da clic en la x o en el fondo oscuro, se oculta el menu
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarCarrito);
    if (overlay) overlay.addEventListener('click', cerrarCarrito);

    // capturamos el boton del header para abrir el carrito
    const btnCarritoHeader = document.getElementById('btn-carrito-header');
    if (btnCarritoHeader) btnCarritoHeader.addEventListener('click', abrirCarrito);

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
        alert('Tu carrito esta vacio. ¡Agrega algunos productos primero!');
        return;
    }

    try {
        // Le preguntamos al backend de Java si el usuario tiene la "pulsera" de logueado
        const respuesta = await fetch('session');

        if (respuesta.ok) {
            // respondio 200 ok: esta logueado
            
            // usamos el nuevo servicio modularizado para enviar el carrito a java
            const exito = await enviarPedido(carrito);
            
            if (exito) {
                alert('¡compra realizada con exito! el pedido se ha guardado en tu historial.');
                
                // vaciamos el carrito tras la compra real
                carrito = [];
                localStorage.setItem('carritoDMari', JSON.stringify(carrito));
                renderizarCarrito();
                cerrarCarrito();
            } else {
                alert('hubo un problema al registrar tu pedido en el sistema.');
            }
        } else {
            // Respondio 401: No esta logueado
            alert('Por favor, inicia sesion o registrate para poder finalizar tu compra.');
            cerrarCarrito(); // Ocultamos el carrito
            navegarA('login'); // Lo llevamos a la pantalla de login mediante el enrutador
        }
    } catch (error) {
        console.error('Error al verificar la sesion:', error);
        alert('Hubo un error de conexion con el servidor.');
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

/**
 * Agrega un producto al carrito de compras o incrementa su cantidad si ya existe.
 * Tambien valida que la cantidad no supere el stock disponible (si se proporciona uno).
 * 
 * @param {number} id - Identificador unico del producto en la base de datos.
 * @param {string} nombre - Nombre descriptivo del producto.
 * @param {number} precio - Precio unitario del producto.
 * @param {number|null} stock - Inventario actual del producto. Es opcional, por defecto es null.
 */
export function agregarAlCarrito(id, nombre, precio, stock = null) {
    // buscamos si el producto ya existe en nuestro arreglo en memoria
    const productoExistente = carrito.find(item => item.id === id);
    
    if (productoExistente) {
        // ACTUALIZACION: Si el producto ya existia, le refrescamos el stock con el dato mas reciente
        if (stock != null) {
            productoExistente.stock = stock;
        }

            // incrementamos la cantidad, pero la topamos al maximo del stock disponible silenciosamente
            productoExistente.cantidad++;
            if (productoExistente.stock != null && productoExistente.cantidad > productoExistente.stock) {
                productoExistente.cantidad = productoExistente.stock;
            }
    } else {
        // validamos que si es un producto nuevo, tenga al menos 1 unidad de stock
        if (stock != null && stock <= 0) {
            alert('Este producto se encuentra agotado por el momento.');
            return;
        }
        // si es un producto nuevo, insertamos el objeto completo al arreglo guardando su stock
        carrito.push({ id: id, nombre: nombre, precio: precio, cantidad: 1, stock: stock });
    }
    
    // Guardamos la informacion actualizada en el navegador
    localStorage.setItem('carritoDMari', JSON.stringify(carrito));

    // tras modificar los datos, redibujamos el html y forzamos a abrir el panel
    renderizarCarrito();
    abrirCarrito();
}

/**
 * Actualiza directamente la cantidad de un producto (usado por los inputs de texto y botones +/-).
 * Fuerza un tope de stock, evitando que el usuario introduzca un numero mayor a lo permitido.
 * @param {number} id - Identificador del producto a modificar.
 * @param {number} nuevaCantidad - La cantidad requerida por el usuario.
 */
export function actualizarCantidad(id, nuevaCantidad) {
    const productoExistente = carrito.find(item => item.id === id);
    if (productoExistente && nuevaCantidad > 0) {
        // si existe un limite de stock y lo superamos, lo topamos al maximo disponible
        if (productoExistente.stock != null && nuevaCantidad > productoExistente.stock) {
            productoExistente.cantidad = productoExistente.stock;
        } else {
            productoExistente.cantidad = nuevaCantidad;
        }
        
        localStorage.setItem('carritoDMari', JSON.stringify(carrito));
        renderizarCarrito(); // redibujamos con el nuevo precio subtotal
    }
}

/**
 * Remueve completamente un articulo del carrito basandose en su ID.
 * @param {number} id - Identificador del producto a eliminar.
 */
export function eliminarDelCarrito(id) {
    carrito = carrito.filter(item => item.id !== id);
    localStorage.setItem('carritoDMari', JSON.stringify(carrito));
    renderizarCarrito();
}

/**
 * Construye el HTML de los elementos del carrito leyendo los datos en memoria.
 * Inyecta este HTML en el contenedor lateral, calcula el total 
 * y vuelve a enlazar los eventos de los botones recien creados (+, -, X, input).
 */
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
        
        // si el producto tiene stock, preparamos el atributo max para el input
        const maxAttr = item.stock ? `max="${item.stock}"` : '';
        // preparamos un texto visual para que el usuario sepa cuanto stock le queda disponible
        const textoStock = item.stock != null ? `<span class="badge-stock">Stock: ${item.stock}</span>` : '';

        // concatenamos el texto con el bloque html de este articulo, ahora mucho mas estilizado
        htmlCarrito += `
            <div class="item-carrito-producto" style="display: flex; flex-direction: column; padding: 10px; border-bottom: 1px solid #eee; margin-bottom: 5px;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                        <span style="font-weight: 600; color: #333;">${item.nombre}</span>
                        ${textoStock}
                    </div>
                    <button class="btn-eliminar-item" data-id="${item.id}" style="color: #ff4d4d; background: none; border: none; cursor: pointer; font-weight: bold; font-size: 1.2rem; line-height: 1; padding: 0 5px;" title="Eliminar">✕</button>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span style="color: #666; font-size: 0.95rem;">$${subtotal.toFixed(2)}</span>
                    <div style="display: flex; align-items: center; border: 1px solid #ccc; border-radius: 5px; overflow: hidden;">
                        <button class="btn-restar" data-id="${item.id}" style="background: #f4f4f4; border: none; padding: 5px 12px; cursor: pointer; font-size: 1rem; color: #333;">-</button>
                        <input type="number" min="1" ${maxAttr} value="${item.cantidad}" class="input-cantidad" data-id="${item.id}" style="width: 40px; text-align: center; border: none; border-left: 1px solid #ccc; border-right: 1px solid #ccc; outline: none; padding: 5px 0; -moz-appearance: textfield;">
                        <button class="btn-sumar" data-id="${item.id}" style="background: #f4f4f4; border: none; padding: 5px 12px; cursor: pointer; font-size: 1rem; color: #333;">+</button>
                    </div>
                </div>
            </div>`;
    });
    
    // inyectamos de golpe todo el texto armado en el contenedor principal (mejor rendimiento)
    contenedor.innerHTML = htmlCarrito;
    
    // actualizamos el texto del total a pagar
    txtTotal.innerText = '$' + total.toFixed(2); // Aseguramos que solo muestre 2 decimales

    // Comportamiento del boton de restar "-"
    document.querySelectorAll('.btn-restar').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.target.dataset.id);
            const item = carrito.find(i => i.id === id);
            if (item && item.cantidad > 1) actualizarCantidad(id, item.cantidad - 1);
        });
    });

    // Comportamiento del boton de sumar "+"
    document.querySelectorAll('.btn-sumar').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = parseInt(e.target.dataset.id);
            const item = carrito.find(i => i.id === id);
            if (item) actualizarCantidad(id, item.cantidad + 1);
        });
    });

    // Agregamos el comportamiento a los nuevos inputs de cantidad
    document.querySelectorAll('.input-cantidad').forEach(input => {
        input.addEventListener('change', (e) => {
            actualizarCantidad(parseInt(e.target.dataset.id), parseInt(e.target.value));
        });
    });

    // Agregamos el comportamiento a los botones de eliminar "X"
    document.querySelectorAll('.btn-eliminar-item').forEach(btn => {
        btn.addEventListener('click', (e) => {
            eliminarDelCarrito(parseInt(e.target.dataset.id));
        });
    });
}