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

    // inyectamos dinamicamente el html de la pasarela de pagos
    let modalPago = document.getElementById('modal-pago-simulado');
    if (!modalPago) {
        modalPago = document.createElement('div');
        modalPago.id = 'modal-pago-simulado';
        modalPago.innerHTML = `
            <div style="background: #fff; padding: 25px; border-radius: 10px; width: 90%; max-width: 400px; text-align: center; box-shadow: 0 5px 15px rgba(0,0,0,0.3);">
                <h3 style="margin-bottom: 15px; color: #212529; font-size: 1.3rem;">pasarela de pago</h3>
                <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">selecciona tu metodo de pago simulado para finalizar la orden.</p>
                
                <div style="display: flex; flex-direction: column; gap: 15px; text-align: left;">
                    <div style="display: flex; flex-direction: column; gap: 5px;">
                        <label style="font-weight: 600; font-size: 0.9rem;">metodo de pago</label>
                        <select id="select-metodo-pago" style="padding: 10px; border: 1px solid #ccc; border-radius: 5px; outline: none;">
                            <!-- estos seran dinamicos despues, por ahora estan fijos para visualizar -->
                            <option value="1">nequi</option>
                            <option value="2">tarjeta de credito / debito</option>
                            <option value="3">efectivo (contra entrega)</option>
                        </select>
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 5px;">
                        <label style="font-weight: 600; font-size: 0.9rem;">numero de cuenta / celular</label>
                        <input type="text" id="input-cuenta-pago" placeholder="ej. 3100000000" style="padding: 10px; border: 1px solid #ccc; border-radius: 5px; outline: none;">
                    </div>
                </div>
                
                <div style="display: flex; justify-content: space-between; margin-top: 25px;">
                    <button id="btn-cancelar-pago" style="background: #f8f9fa; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: bold; color: #333;">cancelar</button>
                    <button id="btn-confirmar-pago" style="background: #212529; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: bold;">pagar ahora</button>
                </div>
            </div>
        `;
        // estilos css para que flote sobre toda la pagina
        Object.assign(modalPago.style, { position: 'fixed', top: '0', left: '0', width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'none', justifyContent: 'center', alignItems: 'center', zIndex: '10000' });
        
        document.body.appendChild(modalPago);
        
        // eventos de los botones del modal
        document.getElementById('btn-cancelar-pago').addEventListener('click', () => modalPago.style.display = 'none');
        document.getElementById('btn-confirmar-pago').addEventListener('click', confirmarPagoSimulado);
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
            
            // validamos si el cliente ya configuro su perfil de envio
            const resPerfil = await fetch('perfil-cliente');
            if (resPerfil.ok) {
                const perfil = await resPerfil.json();
                // verificamos si las cajas de texto de direccion o telefono estan vacias/inexistentes
                if (!perfil.direccion || perfil.direccion.trim() === '' || !perfil.telefono || perfil.telefono.trim() === '') {
                    alert('para poder entregar tu pedido, es obligatorio que completes tus datos de envio (direccion y telefono principal). te llevaremos a configuracion.');
                    cerrarCarrito();
                    navegarA('configuracion'); // lo mandamos a llenar sus datos
                    return; // detenemos la apertura del pago
                }
            }
            
            // interrumpimos el envio directo y mejor abrimos el modal de pagos
            const modalPago = document.getElementById('modal-pago-simulado');
            if (modalPago) {
                modalPago.style.display = 'flex';
                cerrarCarrito(); // ocultamos el carrito para que no estorbe la vista del pago
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

// funcion que recolecta los datos de la pasarela y envia la orden final
async function confirmarPagoSimulado() {
    const cuenta = document.getElementById('input-cuenta-pago').value;
    const idMetodo = document.getElementById('select-metodo-pago').value;
    
    if (!cuenta || cuenta.trim() === '') {
        alert('por favor ingresa un numero de cuenta o telefono valido para continuar.');
        return;
    }
    
    // ocultamos el modal
    document.getElementById('modal-pago-simulado').style.display = 'none';
    
    // NOTA: aqui estamos utilizando la funcion vieja de enviar pedido temporalmente.
    // en el siguiente paso modificaremos esa peticion para que lleve el idMetodo y la cuenta
    const exito = await enviarPedido(carrito);
    
    if (exito) {
        // vaciamos el carrito local
        carrito = [];
        localStorage.setItem('carritoDMari', JSON.stringify(carrito));
        renderizarCarrito();
        
        window.dispatchEvent(new CustomEvent('inventarioActualizado'));
        mostrarNotificacion('¡pago aprobado y compra realizada con exito!', 'exito');
    } else {
        window.dispatchEvent(new CustomEvent('inventarioActualizado'));
        mostrarNotificacion('el pedido fallo. revisa si algun producto se agoto.', 'error');
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

        // topamos al maximo del stock disponible y alertamos al usuario
        if (productoExistente.stock != null && productoExistente.cantidad >= productoExistente.stock) {
            alert(`¡Lo sentimos! Solo nos quedan ${productoExistente.stock} unidades de este producto.`);
            productoExistente.cantidad = productoExistente.stock;
        } else {
            productoExistente.cantidad++;
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
            alert(`¡Lo sentimos! Solo nos quedan ${productoExistente.stock} unidades disponibles.`);
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

/**
 * Crea una alerta flotante moderna (Toast) no bloqueante.
 * Esto permite que el navegador siga trabajando y actualice el catalogo visualmente de fondo.
 */
function mostrarNotificacion(mensaje, tipo) {
    const toast = document.createElement('div');
    toast.innerText = mensaje;
    
    const colorFondo = tipo === 'exito' ? '#4caf50' : '#f44336';
    toast.style.cssText = `position: fixed; bottom: 30px; right: 30px; background: ${colorFondo}; color: white; padding: 15px 25px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.2); font-weight: bold; z-index: 10000; transition: opacity 0.5s ease;`;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 500); // Lo borramos del HTML tras la animacion
    }, 3500);
}