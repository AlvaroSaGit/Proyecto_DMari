import { cargarComponente } from '../../../services/uiService.js';
import { obtenerProductos, guardarProducto, eliminarProducto, cambiarEstadoProducto } from '../../../services/productoService.js';
import { crearFilaProducto } from '../../../components/tablas/filaProductoComponent.js';

/** 
 * VARIABLE DE ESTADO GLOBAL:
 * Controla el comportamiento del formulario Modal.
 * - Si es `null`: El formulario se comportará como un creador de nuevos productos.
 * - Si contiene un `ID` (ej: 15): El formulario se comportará como un actualizador para el producto 15.
 */
let productoEditandoId = null;

/**
 * Función de Arranque (Entry Point) exclusiva para la vista del Administrador.
 * Inyecta el HTML base del panel y manda a inicializar todos sus comportamientos.
 */
export async function cargarVistaAdminProductos() {
    await cargarComponente('component-main', './src/views/Administrador/productos/adminProductos.html');
    
    prepararVistaAdminProductos();
}

/**
 * Mapea los elementos clave del DOM del Administrador y les asigna "Escuchadores de Eventos" (Listeners).
 * Aquí se controla cuándo abrir/cerrar modales, crear productos y gestionar la tabla principal.
 */
function prepararVistaAdminProductos() {
    // Extracción de Referencias al DOM (Caché local para rendimiento)
    const btnAgregar = document.getElementById('btn-nuevo-producto');
    const modal = document.getElementById('modal-producto');
    const btnCerrar = document.getElementById('btn-cerrar-modal');
    const btnCancelar = document.getElementById('btn-cancelar-modal');
    const form = document.getElementById('form-producto');
    
    // Elementos para la previsualizacion de imagen
    const inputImagen = document.getElementById('prod-imagen');
    const previewContenedor = document.getElementById('contenedor-preview');
    const previewImg = document.getElementById('prod-imagen-preview');

    // EVENTO: Mostrar la foto cuando el usuario selecciona un archivo de su PC
    if (inputImagen) {
        inputImagen.addEventListener('change', function() {
            const archivo = this.files[0];
            if (archivo) {
                const lector = new FileReader(); // API nativa para leer archivos locales
                lector.onload = function(e) {
                    previewImg.src = e.target.result;
                    previewContenedor.style.display = 'block'; // Mostramos el recuadro
                }
                lector.readAsDataURL(archivo);
            } else {
                previewImg.src = '';
                previewContenedor.style.display = 'none'; // Lo volvemos a esconder si cancela
            }
        });
    }

    // CREAR NUEVO PRODUCTO: Preparar y abrir la ventana flotante (Modal) limpia
    if (btnAgregar) {
        btnAgregar.addEventListener('click', () => {
            productoEditandoId = null; // Reseteamos la variable de estado
            document.getElementById('modal-titulo').innerText = 'Nuevo Producto';
            if (form) form.reset(); // Vaciamos las cajas de texto
            
            // Escondemos la previsualizacion porque es un producto nuevo
            if (previewContenedor) previewContenedor.style.display = 'none';
            if (previewImg) previewImg.src = '';
            if (inputImagen) { inputImagen.value = ''; inputImagen.setAttribute('required', 'required'); } // La imagen es obligatoria
            
            if (modal) modal.classList.remove('oculto'); 
        });
    }
    
    // Función auxiliar anónima para esconder el Modal inyectando la clase CSS `oculto`
    const cerrarModal = () => { if (modal) modal.classList.add('oculto'); };
    
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);
    
    // Evento principal: Cuando el usuario da clic en "Guardar Producto" en el formulario
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault(); // Detenemos el submit tradicional (recarga de página)
            
            // 1. LECTURA DEL FORMULARIO
            const nombre = document.getElementById('prod-nombre').value;
            const descripcion = document.getElementById('prod-descripcion') ? document.getElementById('prod-descripcion').value : '';
            const precio = document.getElementById('prod-precio').value;
            const stock = document.getElementById('prod-stock').value;
            const idCategoria = document.getElementById('prod-categoria').value;
            // Capturamos el texto de las etiquetas
            const etiquetas = document.getElementById('prod-etiquetas') ? document.getElementById('prod-etiquetas').value : '';
            
            /*
             * TECNICA DE ENVIO CON FormData:
             * FormData permite enviar archivos fisicos combinados con texto.
             * Es el estandar de HTML5 para subir imagenes al servidor.
             */
            const formData = new FormData();
            formData.append('nombre', nombre);
            formData.append('descripcion', descripcion);
            formData.append('precio', precio);
            formData.append('stock', stock);
            formData.append('id_categoria', idCategoria);
            formData.append('etiquetas', etiquetas);
            
            // REGLA DE NEGOCIO: Los productos nacen activos en la tienda por defecto.
            formData.append('estado', 'true');
            
            // ¿CREAR O EDITAR? Modificación dinámica de la carga útil
            if (productoEditandoId) {
                formData.append('id', productoEditandoId); // Avisamos a Java que esto es un UPDATE, no un INSERT
            }

            // Atrapamos el archivo fisico de la imagen si el usuario selecciono una
            const inputImagenFile = document.getElementById('prod-imagen');
            if (inputImagenFile && inputImagenFile.files.length > 0) {
                formData.append('imagen', inputImagenFile.files[0]);
            }
            
            try {
                // 2. TRANSMISIÓN AL SERVICIO
                // El booleano `productoEditandoId !== null` dirige la ruta interna hacia el Servlet apropiado
                await guardarProducto(formData, productoEditandoId !== null);
                
                // 3. POST-PROCESAMIENTO: Refresco gráfico sin recargar la web
                alert('¡Producto guardado correctamente!');
                cerrarModal();
                cargarListaProductos(); 
            } catch (error) {
                console.error('Error de conexion:', error);
                alert('Hubo un error al guardar el producto.');
            }
        });
    }

    /*
     * PATRÓN DE DISEÑO: Delegación de Eventos (Event Delegation).
     * En lugar de asignarle un 'EventListener' a cada botón "Editar" y "Eliminar" (lo cual saturaría la RAM 
     * y fallaría si llegan productos nuevos), le asignamos un único escuchador padre a la tabla entera (tbody).
     */
    const tbody = document.getElementById('tabla-productos-body');
    if (tbody) {
        tbody.addEventListener('click', async (evento) => {
            const btnClic = evento.target.closest('button'); // Verificamos si lo que se clicó fue realmente un botón
            if (!btnClic) return; 

            const id = btnClic.getAttribute('data-id');

            if (btnClic.classList.contains('btn-editar')) {
                // MODO EDICIÓN: Extraemos los datos "quemados" en el HTML del botón (`data-nombre`, `data-precio`, etc)
                // y los inyectamos directamente en el formulario Modal
                productoEditandoId = id;
                document.getElementById('prod-nombre').value = btnClic.getAttribute('data-nombre');
                if (document.getElementById('prod-descripcion')) {
                    document.getElementById('prod-descripcion').value = btnClic.getAttribute('data-descripcion') || '';
                }
                document.getElementById('prod-precio').value = btnClic.getAttribute('data-precio');
                document.getElementById('prod-stock').value = btnClic.getAttribute('data-stock');
                
                const catId = btnClic.getAttribute('data-categoria');
                if (catId) document.getElementById('prod-categoria').value = catId;

                if (document.getElementById('prod-etiquetas')) {
                    document.getElementById('prod-etiquetas').value = btnClic.getAttribute('data-etiquetas') || '';
                }
                
                // MODO EDICION - IMAGEN: Mostramos la imagen actual si el producto ya tiene una
                const urlImagen = btnClic.getAttribute('data-imagen');
                
                // Limpiamos el input file por si quedo basura de una edicion anterior
                if (inputImagen) inputImagen.value = '';
                
                if (urlImagen && urlImagen !== 'null' && urlImagen !== 'undefined' && urlImagen.trim() !== '') {
                    if (previewImg) previewImg.src = urlImagen;
                    if (previewContenedor) previewContenedor.style.display = 'block';
                    // Si ya tiene foto, removemos el atributo para NO obligar a subir una nueva
                    if (inputImagen) inputImagen.removeAttribute('required'); 
                } else {
                    if (previewContenedor) previewContenedor.style.display = 'none';
                    if (inputImagen) inputImagen.setAttribute('required', 'required');
                }

                document.getElementById('modal-titulo').innerText = 'Editar Producto';
                if (modal) modal.classList.remove('oculto');
                
            } else if (btnClic.classList.contains('btn-estado')) {
                // CAMBIO RÁPIDO DE ESTADO (Activar / Pausar Producto)
                const estadoActual = btnClic.getAttribute('data-estado') === 'true';
                try {
                    await cambiarEstadoProducto(id, !estadoActual);
                    cargarListaProductos(); // Refrescamos la tabla tras el cambio
                } catch (error) { alert('error al cambiar el estado del producto'); }
                
            } else if (btnClic.classList.contains('btn-eliminar')) {
                // ELIMINACIÓN DE PRODUCTO (Requiere confirmación de seguridad)
                if (confirm('¿seguro que deseas eliminar este producto de forma permanente?')) {
                    try {
                        await eliminarProducto(id);
                        cargarListaProductos();
                    } catch (err) { alert('error al intentar borrar el producto'); }
                }
            }
        });
    }

    // Al entrar por primera vez a la pantalla, pedimos los productos a la base de datos
    cargarListaProductos();
    
    // Llenamos las opciones dinámicas del formulario para que el Admin no tenga que escribirlas
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
            // usamos el componente modularizado para fabricar la fila e inyectarla
            tbody.appendChild(crearFilaProducto(prod));
        });

    } catch (error) {
        console.error('error general al cargar la lista:', error);
    }
}