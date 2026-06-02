// importamos el servicio de ui para la inyeccion de componentes
import { cargarComponente } from '../../services/uiService.js';
// importamos el enrutador para la navegacion
import { navegarA } from '../../router/router.js';
// importamos el servicio encargado de los pedidos
import { enviarPedido } from '../pedido/pedidoService.js';

// intentamos cargar el carrito guardado en el navegador, si no hay, iniciamos vacio
let carrito = JSON.parse(localStorage.getItem('carritoDMari')) || [];

// variable para evitar saturar el servidor (tecnica de "debounce")
let temporizadorSincronizacion = null;

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

    // =========================================================================
    // INYECCION DINAMICA DEL MODAL DE PAGOS
    // =========================================================================
    // Creamos un modal de pagos usando JavaScript puro en lugar de tenerlo fijo en el HTML.
    // Esto mantiene el DOM (la estructura de la pagina) ligero y limpio hasta que realmente
    // el usuario este preparado para hacer una compra.
    let modalPago = document.getElementById('modal-pago-simulado');
    if (!modalPago) {
        // Si el modal no existe, creamos un contenedor <div> en la memoria
        modalPago = document.createElement('div');
        modalPago.id = 'modal-pago-simulado';
        // Inyectamos la estructura interna del modal (titulos, inputs, selectores y botones)
        modalPago.innerHTML = `
            <div style="background: #fff; padding: 25px; border-radius: 10px; width: 90%; max-width: 400px; text-align: center; box-shadow: 0 5px 15px rgba(0,0,0,0.3);">
                <h3 style="margin-bottom: 15px; color: #212529; font-size: 1.3rem;">Pasarela de pago</h3>
                <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">Selecciona tu metodo de pago simulado para finalizar la orden.</p>
                
                <div style="display: flex; flex-direction: column; gap: 15px; text-align: left;">
                    <div style="display: flex; flex-direction: column; gap: 5px;">
                        <label style="font-weight: 600; font-size: 0.9rem;">Metodo de pago</label>
                        <select id="select-metodo-pago" style="padding: 10px; border: 1px solid #ccc; border-radius: 5px; outline: none;">
                            <option value="" disabled selected>Cargando opciones...</option>
                        </select>
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 5px;">
                        <label style="font-weight: 600; font-size: 0.9rem;">Numero de cuenta / celular</label>
                        <input type="text" id="input-cuenta-pago" placeholder="Ej. 3100000000" style="padding: 10px; border: 1px solid #ccc; border-radius: 5px; outline: none;">
                    </div>
                </div>
                
                <div style="display: flex; justify-content: space-between; margin-top: 25px;">
                    <button id="btn-cancelar-pago" style="background: #f8f9fa; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: bold; color: #333;">Cancelar</button>
                    <button id="btn-confirmar-pago" style="background: #212529; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: bold;">Pagar ahora</button>
                </div>
            </div>
        `;
        // Aplicamos estilos directamente usando Object.assign (Es mas seguro que usar texto en cssText).
        // 'fixed', 'top:0', 'left:0', 'width:100%', 'height:100%' garantizan que tape toda la pantalla.
        // 'z-index: 10000' asegura que quede por encima de todo (navbars, banners, etc).
        Object.assign(modalPago.style, { position: 'fixed', top: '0', left: '0', width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'none', justifyContent: 'center', alignItems: 'center', zIndex: '10000' });
        
        // Empujamos el modal de la memoria hacia la pagina visible
        document.body.appendChild(modalPago);
        
        // Enganchamos el comportamiento de los dos botones recien creados
        document.getElementById('btn-cancelar-pago').addEventListener('click', () => modalPago.style.display = 'none');
        document.getElementById('btn-confirmar-pago').addEventListener('click', confirmarPagoSimulado);

        // =========================================================================
        // VALIDACION EN TIEMPO REAL (UX)
        // =========================================================================
        // este bloque impide que el usuario escriba letras en el campo de cuenta/telefono
        const inputCuenta = document.getElementById('input-cuenta-pago');
        if (inputCuenta) {
            inputCuenta.addEventListener('input', (e) => {
                // reemplazamos cualquier caracter que no sea un numero (0-9) por nada
                e.target.value = e.target.value.replace(/[^0-9]/g, '');
            });
        }
    }
    
    // al cargar la pagina, verificamos si el usuario esta logueado para descargar su carrito de mysql.
    // asi, si se paso del celular a la computadora, recupera sus donas al instante.
    try {
        const resSess = await fetch('session');
        if (resSess.ok) {
            const resBD = await fetch('carrito-db');
            if (resBD.ok) {
                const carritoBD = await resBD.json();
                if (carritoBD.length > 0) { 
                    let huboCambiosStock = false;
                    
                    // Mapeamos el carrito para normalizar llaves (idProducto -> id) y verificar recortes de inventario
                    carrito = carritoBD.map(item => {
                        let cantidadReal = item.cantidad;
                        
                        // Si el stock en BD es menor a la cantidad guardada por el cliente, lo topamos al máximo disponible
                        if (item.stock != null && cantidadReal > item.stock) {
                            cantidadReal = item.stock;
                            huboCambiosStock = true;
                        }
                        
                        return {
                            id: item.idProducto || item.id, // Normalizamos el ID para que los botones de sumar/restar funcionen
                            nombre: item.nombre,
                            precio: item.precio,
                            cantidad: cantidadReal,
                            stock: item.stock,
                            imagen: item.imagen
                        };
                    }).filter(item => item.cantidad > 0); // Quitamos los artículos que se agotaron por completo (stock 0)
                    
                    if (carritoBD.length !== carrito.length) huboCambiosStock = true;

                    localStorage.setItem('carritoDMari', JSON.stringify(carrito)); 
                    renderizarCarrito(); 
                    
                    if (huboCambiosStock) {
                        // Retrasamos la alerta medio segundo para no trabar el renderizado visual
                        setTimeout(() => alert('Atencion: Algunos productos de tu carrito fueron ajustados o removidos porque el inventario disponible cambio.'), 500);
                        programarSincronizacion(); // Obligamos a MySQL a registrar el nuevo límite
                    }
                }
            }
        }
    } catch (e) { console.error('Error al sincronizar carrito inicial', e); }
}

/**
 * Consulta al Backend los metodos de pago disponibles (Nequi, Efectivo, etc) 
 * y llena la etiqueta <select> del modal dinamicamente.
 */
async function cargarMetodosPago() {
    const select = document.getElementById('select-metodo-pago');
    if (!select) return;
    
    try {
        select.innerHTML = '<option value="" disabled selected>Cargando opciones...</option>';
        const respuesta = await fetch('metodos-pago');
        if (respuesta.ok) {
            // Desempaquetamos el JSON que trae {id, descripcion}
            const metodos = await respuesta.json();
            
            if (metodos.length === 0) {
                select.innerHTML = '<option value="" disabled selected>No hay metodos (revisa mysql)</option>';
                return;
            }
            
            select.innerHTML = '<option value="" disabled selected>Elige una opcion...</option>';
            // Recorremos el JSON inyectando una etiqueta <option> por cada metodo habilitado
            metodos.forEach(m => {
                const opt = document.createElement('option');
                opt.value = m.id;
                opt.textContent = m.descripcion;
                select.appendChild(opt);
            });
        } else {
            select.innerHTML = '<option value="" disabled selected>Error en java (revisa netbeans)</option>';
        }
    } catch (error) { 
        console.error('Error al cargar metodos de pago:', error); 
        select.innerHTML = '<option value="" disabled selected>Falla de red</option>';
    }
}

/**
 * Reacciona cuando el cliente presiona el boton principal de "Comprar".
 * Verifica la sesion y valida que su perfil logistico este completo 
 * (tenga direccion y telefono) antes de dejarlo pagar.
 */
async function procesarCompra() {
    // Regla 1: No procesar aire.
    if (carrito.length === 0) {
        alert('Tu carrito esta vacio. ¡Agrega algunos productos primero!');
        return;
    }

    try {
        // Le preguntamos al backend de Java si el usuario tiene la "pulsera" de logueado
        const respuesta = await fetch('session');

        if (respuesta.ok) {
            // Respondio 200 (OK): El servidor de Java confirmo que tiene sesion iniciada.
            
            // Validamos logisticamente al cliente: consultamos su perfil
            const resPerfil = await fetch('perfil-cliente');
            if (resPerfil.ok) {
                const perfil = await resPerfil.json();
                // Si los datos llegan nulos o como un string de espacios vacios, bloqueamos el proceso.
                if (!perfil.direccion || perfil.direccion.trim() === '' || !perfil.telefono || perfil.telefono.trim() === '') {
                    alert('Para poder entregar tu pedido, es obligatorio que completes tus datos de envio (direccion y telefono principal). Te llevaremos a tu perfil.');
                    cerrarCarrito();    // Ocultamos el menu lateral
                    navegarA('perfil'); // Forzamos una redireccion SPA hacia el formulario
                    return;             // Abortamos la ejecucion para que no se abra el modal de pago
                }
                
                // automatizacion: traemos el numero de telefono configurado por el usuario al campo de pago
                const inputCuenta = document.getElementById('input-cuenta-pago');
                if (inputCuenta) {
                    inputCuenta.value = perfil.telefono || '';
                }
            }
            
            // Si tiene cuenta y perfil completo, mostramos la pasarela.
            const modalPago = document.getElementById('modal-pago-simulado');
            if (modalPago) {
                await cargarMetodosPago(); // cargamos las opciones frescas justo antes de abrir el modal
                modalPago.style.display = 'flex';
                cerrarCarrito(); // ocultamos el carrito para que no estorbe la vista del pago
            }
        } else {
            // Respondio 401 (UNAUTHORIZED): El visitante no se ha identificado.
            alert('Por favor, inicia sesion o registrate para poder finalizar tu compra.');
            cerrarCarrito(); 
            navegarA('login'); // Interceptamos y lo enviamos al login
        }
    } catch (error) {
        console.error('Error al verificar la sesion:', error);
        alert('Hubo un error de conexion con el servidor.');
    }
}

/**
 * Se dispara cuando el cliente llena el modal de pago y le da a "Pagar Ahora".
 * Recolecta el estado del carrito local, el ID del pago, y delega todo al
 * servicio de pedidos (pedidoService.js) para comunicarse con Java.
 */
async function confirmarPagoSimulado() {
    // Extraemos los datos del formulario flotante
    const cuenta = document.getElementById('input-cuenta-pago').value;
    const idMetodo = document.getElementById('select-metodo-pago').value;
    
    // validacion estricta: el campo de cuenta/celular no debe estar vacio
    if (!cuenta || cuenta.trim() === '') {
        alert('Por favor ingresa un numero de cuenta o telefono valido para continuar.');
        return;
    }

    // validacion de formato: solo permitimos numeros en la cuenta de pago
    const regexSoloNumeros = /^[0-9]+$/;
    if (!regexSoloNumeros.test(cuenta)) {
        alert('error: el numero de cuenta o celular solo debe contener digitos numericos.');
        return;
    }

    // Quitamos el modal de la pantalla para evitar dobles envios
    document.getElementById('modal-pago-simulado').style.display = 'none';
    
    // Cambiamos el boton a estado "cargando" por si la BD es lenta
    const botonConfirmar = document.getElementById('btn-confirmar-pago');
    botonConfirmar.innerText = "Procesando pago...";
    botonConfirmar.disabled = true;
    
    // LLAMADO CENTRAL: Enviamos la informacion al controlador de Pedidos en Java
    const exito = await enviarPedido(carrito, idMetodo, cuenta);
    
    if (exito) {
        // Si Java devolvio exito, vaciamos el arreglo local de JS
        carrito = [];
        // Destruimos las cookies / almacenamiento local para que no reaparezcan donas fantasma
        localStorage.setItem('carritoDMari', JSON.stringify(carrito));
        
        // Le avisamos a la BD que sincronice nuestro carrito (mandara un carrito vacio que ejecutara el DELETE)
        programarSincronizacion();
        
        renderizarCarrito(); // Redibujamos la canasta (quedara vacia en pantalla)
        
        window.dispatchEvent(new CustomEvent('inventarioActualizado'));
        mostrarNotificacion('¡Pago aprobado y compra realizada con exito!', 'exito');
    } else {
        window.dispatchEvent(new CustomEvent('inventarioActualizado'));
        mostrarNotificacion('El pedido fallo. Revisa si algun producto se agoto.', 'error');
    }
    
    botonConfirmar.innerText = "Pagar ahora";
    botonConfirmar.disabled = false;
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
    
    // avisamos a la base de datos de forma silenciosa y sin saturar
    programarSincronizacion();
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
        
        // avisamos a la base de datos de forma silenciosa y sin saturar
        programarSincronizacion();
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
    
    // avisamos a la base de datos de forma silenciosa y sin saturar
    programarSincronizacion();
}

/**
 * sincroniza el carrito con mysql utilizando un "temporizador de retardo" (debounce).
 * esto evita saturar el servidor si el usuario da 20 clics rapidos al boton de '+'.
 */
function programarSincronizacion() {
    // si ya habia un guardado programado en la recamara, lo cancelamos
    if (temporizadorSincronizacion) clearTimeout(temporizadorSincronizacion);
    
    // programamos un nuevo envio al servidor para dentro de 1.5 segundos
    temporizadorSincronizacion = setTimeout(async () => {
        // empaquetamos el carrito tal cual lo hacemos para las facturas
        const parametros = new URLSearchParams();
        carrito.forEach(item => {
            parametros.append('id_producto', item.id);
            parametros.append('cantidad', item.cantidad);
        });
        
        try {
            // enviamos la peticion en la sombra sin bloquear la pantalla.
            // si el usuario es visitante (no esta logueado), java respondera un error 401, 
            // pero como estamos en un setTimeout en la sombra y omitimos los alerts, 
            // el visitante jamas se dara cuenta y su pagina seguira perfecta con el localstorage.
            await fetch('carrito-db', { method: 'POST', body: parametros });
        } catch (e) {
            console.warn('Fallo la sincronizacion silenciosa del carrito. Esto es normal si el usuario no ha iniciado sesion.', e.message);
        } 
    }, 1500);
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