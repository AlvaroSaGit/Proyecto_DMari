// Importamos la funcion para inyectar el HTML en la pantalla principal
import { cargarComponente } from '../../../services/uiService.js';
// Importamos nuestro servicio que hace el trabajo sucio de comunicarse con Java
import { obtenerProductos, guardarProducto, eliminarProducto } from '../../../services/productoService.js';

// Variable global en este archivo para saber el estado del formulario.
// Si es 'null', significa que estamos creando un producto nuevo.
// Si tiene un numero (ej: 5), significa que estamos editando el producto con ID 5.
let productoEditandoId = null;

/**
 * Funcion de arranque de la vista de administrador de productos.
 * Primero inyecta el HTML y luego configura todos los botones.
 */
export async function cargarVistaAdminProductos() {
    await cargarComponente('component-main', './src/views/Administrador/productos/adminProductos.html');
    
    prepararVistaAdminProductos();
}

/**
 * Busca todos los botones y formularios en el HTML que recien cargamos
 * y les asigna eventos (click, submit) para darles vida.
 */
function prepararVistaAdminProductos() {
    // Capturamos los elementos del DOM (HTML)
    const btnAgregar = document.getElementById('btn-nuevo-producto');
    const modal = document.getElementById('modal-producto');
    const btnCerrar = document.getElementById('btn-cerrar-modal');
    const btnCancelar = document.getElementById('btn-cancelar-modal');
    const form = document.getElementById('form-producto');
    
    // Evento para el boton "+ Nuevo Producto"
    if (btnAgregar) {
        btnAgregar.addEventListener('click', () => {
            // Como es nuevo, nos aseguramos de que el ID este vacio
            productoEditandoId = null; 
            document.getElementById('modal-titulo').innerText = 'Nuevo Producto';
            if (form) form.reset(); // Vaciamos las cajas de texto
            if (modal) modal.classList.remove('oculto'); // Mostramos la ventana popup
        });
    }
    
    // Funcion cortita para ocultar la ventana popup
    const cerrarModal = () => { if (modal) modal.classList.add('oculto'); };
    // Se la asignamos a la X de la esquina y al boton Cancelar
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);
    
    // Evento principal: Cuando el usuario da clic en "Guardar Producto" en el formulario
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault(); // Evita que la pagina parpadee o se recargue al enviar
            
            // 1. Extraemos lo que el usuario escribio en las cajitas de texto
            const nombre = document.getElementById('prod-nombre').value;
            const precio = document.getElementById('prod-precio').value;
            const stock = document.getElementById('prod-stock').value;
            // Extraemos el ID de la categoria elegida por el administrador
            const idCategoria = document.getElementById('prod-categoria').value;
            
            /*
             * EXPLICACION DE URLSearchParams:
             * Normalmente, enviar JSON a Java requiere librerias complejas en el backend.
             * URLSearchParams emula un formulario HTML clasico. Transforma nuestros datos en algo como:
             * "nombre=Vela&precio=15.50&stock=20"
             * Esto hace que en Java podamos leerlos super facil usando request.getParameter("nombre");
             */
            const parametros = new URLSearchParams();
            parametros.append('nombre', nombre);
            parametros.append('precio', precio);
            parametros.append('stock', stock);
            parametros.append('id_categoria', idCategoria);
            
            // Si la variable no es nula, significa que el usuario abrio un producto para editarlo
            if (productoEditandoId) {
                // Agregamos el ID a la mochila de parametros para que Java sepa cual actualizar
                parametros.append('id', productoEditandoId);
            }
            
            try {
                // 2. Le pasamos el paquete de datos a nuestro servicio.
                // El segundo parametro (true/false) le avisa al servicio si debe usar la ruta de actualizar o insertar
                await guardarProducto(parametros, productoEditandoId !== null);
                
                // 3. Si no hubo errores, avisamos al usuario, cerramos la ventana y recargamos la tabla
                alert('¡Producto guardado correctamente!');
                cerrarModal();
                cargarListaProductos(); 
            } catch (error) {
                console.error('Error de conexion:', error);
                alert('Hubo un error al guardar el producto.');
            }
        });
    }

    // Al entrar por primera vez a la pantalla, pedimos los productos a la base de datos
    cargarListaProductos();
    
    // Tambien llenamos las opciones dinamicas del <select> de categorias
    cargarCategoriasFormulario();
}

/**
 * Llama al backend para obtener las categorias y llenar el <select> del modal.
 */
async function cargarCategoriasFormulario() {
    try {
        const respuesta = await fetch('categorias');
        if (respuesta.ok) {
            const categorias = await respuesta.json();
            const select = document.getElementById('prod-categoria');
            if (!select) return;
            
            // Vaciamos el select y dejamos una opcion inicial deshabilitada
            select.innerHTML = '<option value="" disabled selected>Seleccione una categoría</option>';
            
            // Iteramos sobre las categorias activas extraidas de la Base de Datos
            categorias.forEach(cat => {
                select.innerHTML += `<option value="${cat.id}">${cat.nombre}</option>`;
            });
        }
    } catch (error) {
        console.error('Error al cargar categorias en el formulario:', error);
    }
}

/**
 * Se comunica con la base de datos para traer los productos
 * y construye una por una las filas visuales de la tabla.
 */
async function cargarListaProductos() {
    const tbody = document.getElementById('tabla-productos-body');
    if (!tbody) return; // Proteccion: si no existe la tabla en el HTML, no hacemos nada

    try {
        // Pedimos la lista al backend usando el servicio
        const productosBD = await obtenerProductos();
        
        // Si el servicio nos devuelve null, disparamos un error intencional para caer en el catch
        if (!productosBD) throw new Error('No se pudieron obtener los productos');
        
        // Vaciamos el texto de "Cargando..."
        tbody.innerHTML = '';
 
        // Recorremos el arreglo de productos que llego de MySQL
        productosBD.forEach(prod => {
            // Extraemos los datos adaptandonos a los posibles nombres que arroja Java (JSON)
            const idProd = prod.idProductoPk || prod.id;
            const nombreProd = prod.nombreProducto || prod.nombre || 'Producto';
            // Extraemos el nombre de la categoria que manda el backend (o ponemos un texto por defecto)
            const nombreCategoria = prod.categoria || prod.nombreCategoria || 'Sin categoría';
            const catFk = prod.idCategoriaFk || '';
            
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${idProd}</td>
                <td><i class='bx bx-image' style='font-size: 2rem; color: #ccc;'></i></td>
                <td>
                    <div style="font-weight: bold;">${nombreProd}</div>
                    <span style="font-size: 0.75rem; color: #666; background-color: #f0f0f0; padding: 2px 6px; border-radius: 10px;">${nombreCategoria}</span>
                </td>
                <td>$${prod.precio.toFixed(2)}</td>
                <td>${prod.stock} uds</td>
                <td>
                    <!-- Guardamos la informacion del producto directamente en el boton usando "data-" -->
                    <!-- Asì, cuando hagan clic en Editar, sabemos exactamente que datos poner en el formulario -->
                    <button class="btn-editar" data-id="${idProd}" data-nombre="${nombreProd}" data-precio="${prod.precio}" data-stock="${prod.stock}" data-categoria="${catFk}"><i class='bx bx-edit'></i> Editar</button>
                    <button class="btn-eliminar" data-id="${idProd}"><i class='bx bx-trash'></i> Borrar</button>
                </td>
            `;
            // Metemos la fila recien creada en el cuerpo de la tabla
            tbody.appendChild(tr);
        });
        
        // Le damos vida a los botones "Editar" de cada fila
        const modal = document.getElementById('modal-producto');
        
        tbody.querySelectorAll('.btn-editar').forEach(btn => btn.addEventListener('click', (evento) => {
            // Extraemos los datos que escondimos en el boton especifico que recibio el clic
            const btnClic = evento.currentTarget;
            productoEditandoId = btnClic.getAttribute('data-id');
            
            // Pre-llenamos las cajas de texto del formulario con los datos viejos
            document.getElementById('prod-nombre').value = btnClic.getAttribute('data-nombre');
            document.getElementById('prod-precio').value = btnClic.getAttribute('data-precio');
            document.getElementById('prod-stock').value = btnClic.getAttribute('data-stock');
            
            // Si el producto ya tiene categoria asignada, pre-seleccionamos ese valor en el formulario
            const catId = btnClic.getAttribute('data-categoria');
            if (catId) document.getElementById('prod-categoria').value = catId;
            
            // Cambiamos el titulo de la ventana y la mostramos
            document.getElementById('modal-titulo').innerText = 'Editar Producto';
            if (modal) modal.classList.remove('oculto'); 
        }));
        
        // Le damos vida a los botones "Borrar" de cada fila
        tbody.querySelectorAll('.btn-eliminar').forEach(btn => btn.addEventListener('click', async (evento) => {
            // confirm() muestra una alerta del navegador. Si el usuario da a Aceptar, devuelve true
            if(confirm('¿Seguro que deseas eliminar este producto?')) {
                // Extraemos el id del producto de este boton
                const idEliminar = evento.currentTarget.getAttribute('data-id');
                
                try {
                    // Delegamos la orden de eliminar a nuestro archivo de servicio
                    await eliminarProducto(idEliminar);
                    alert('Producto eliminado correctamente.');
                    
                    // Recargamos los productos para que la fila desaparezca visualmente
                    cargarListaProductos(); 
                } catch (error) {
                    console.error('Fallo al borrar:', error);
                    alert('No se pudo eliminar. Quizas tenga imagenes o dependencias asociadas.');
                }
            }
        }));
    } catch (error) {
        // Si se cayo el internet, fallo Java o MySQL esta apagado, mostramos el error sin crashear todo
        console.error('Error al cargar la tabla de productos:', error);
        tbody.innerHTML = `<tr><td colspan="6">Error de conexión al cargar productos</td></tr>`;
    }
}
