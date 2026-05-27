// importamos los servicios necesarios para pintar la interfaz y comunicarnos con la base de datos
import { cargarComponente } from '../../../services/uiService.js';
import { obtenerProductos, guardarProducto, eliminarProducto, cambiarEstadoProducto } from '../../../services/productoService.js';

/** 
 * variable de estado global:
 * controla el comportamiento del formulario modal.
 * - si es null: el formulario se comportara como un creador de nuevos productos.
 * - si contiene un id (ej: 15): el formulario se comportara como un actualizador para el producto 15.
 */
let productoEditandoId = null;

/**
 * bandera de seguridad (candado logico):
 * evita que envios simultaneos en microsegundos manden peticiones dobles al servidor java.
 */
let guardandoProducto = false;

/**
 * funcion de arranque (entry point) exclusiva para la vista del administrador.
 * inyecta el html base del panel y manda a inicializar todos sus comportamientos.
 */
export async function cargarVistaAdminProductos() {
    await cargarComponente('component-main', './src/views/Administrador/productos/adminProductos.html');
    
    prepararVistaAdminProductos();
}

/**
 * mapea los elementos clave del dom del administrador y les asigna escuchadores de eventos.
 * aqui se controla cuando abrir o cerrar modales, crear productos y gestionar la tabla principal.
 */
function prepararVistaAdminProductos() {
    // extraccion de referencias al dom (cache local para mejorar el rendimiento)
    const btnAgregar = document.getElementById('btn-nuevo-producto');
    const modal = document.getElementById('modal-producto');
    const btnCerrar = document.getElementById('btn-cerrar-modal');
    const btnCancelar = document.getElementById('btn-cancelar-modal');
    const form = document.getElementById('form-producto');
    
    // elementos visuales para la previsualizacion de la imagen
    const inputImagen = document.getElementById('prod-imagen');
    const previewContenedor = document.getElementById('contenedor-preview');
    const previewImg = document.getElementById('prod-imagen-preview');

    // evento: mostrar la foto temporalmente cuando el usuario selecciona un archivo de su pc
    if (inputImagen) {
        inputImagen.onchange = function() {
            const archivo = this.files[0];
            if (archivo) {
                const lector = new FileReader(); // api nativa de javascript para leer archivos locales
                lector.onload = function(e) {
                    previewImg.src = e.target.result;
                    previewContenedor.style.display = 'block'; // mostramos el recuadro
                }
                lector.readAsDataURL(archivo);
            } else {
                previewImg.src = '';
                previewContenedor.style.display = 'none'; // lo volvemos a esconder si el usuario cancela
            }
        };
    }

    // crear nuevo producto: preparar y abrir la ventana flotante (modal) totalmente limpia
    if (btnAgregar) {
        btnAgregar.onclick = () => {
            productoEditandoId = null; // reseteamos la variable de estado
            document.getElementById('modal-titulo').innerText = 'Nuevo Producto';
            if (form) form.reset(); // vaciamos todas las cajas de texto
            
            // escondemos la previsualizacion porque es un producto nuevo
            if (previewContenedor) previewContenedor.style.display = 'none';
            if (previewImg) previewImg.src = '';
            if (inputImagen) { inputImagen.value = ''; inputImagen.setAttribute('required', 'required'); } // la imagen es obligatoria al crear
            
            if (modal) modal.classList.remove('oculto'); 
        };
    }
    
    // funcion auxiliar anonima para esconder el modal inyectandole la clase css 'oculto'
    const cerrarModal = () => { if (modal) modal.classList.add('oculto'); };
    
    if (btnCerrar) btnCerrar.onclick = cerrarModal;
    if (btnCancelar) btnCancelar.onclick = cerrarModal;
    
    // evento principal: cuando el usuario da clic en "guardar producto" en el formulario
    if (form) {
        form.onsubmit = async (e) => {
            e.preventDefault(); // detenemos el submit tradicional que recargaria la pagina entera
            
            // candado de seguridad: si ya se esta guardando un producto, ignoramos cualquier intento adicional de inmediato
            if (guardandoProducto) return;
            
            guardandoProducto = true; // cerramos el candado
            
            // 1. lectura de todas las cajas del formulario
            const nombre = document.getElementById('prod-nombre').value;
            const descripcion = document.getElementById('prod-descripcion') ? document.getElementById('prod-descripcion').value : '';
            const precio = document.getElementById('prod-precio').value;
            const stock = document.getElementById('prod-stock').value;
            const idCategoria = document.getElementById('prod-categoria').value;
            // capturamos el proveedor seleccionado por el administrador
            const idProveedor = document.getElementById('prod-proveedor') ? document.getElementById('prod-proveedor').value : '';
            // capturamos el texto de las etiquetas separadas por coma
            const etiquetas = document.getElementById('prod-etiquetas') ? document.getElementById('prod-etiquetas').value : '';
            
            // VALIDACION SENA FRONTEND: Evitar textos en blanco, valores negativos y campos vacios
            if (nombre.trim() === '') {
                alert('Error: El nombre del producto no puede estar vacio ni contener solo espacios.');
                guardandoProducto = false;
                return;
            }
            if (precio <= 0) {
                alert('Error: El precio debe ser mayor a 0.');
                guardandoProducto = false;
                return;
            }
            if (stock < 0) {
                alert('Error: El stock no puede ser un numero negativo.');
                guardandoProducto = false;
                return;
            }
            if (!idCategoria) {
                alert('Error: Debes seleccionar una categoria obligatoriamente.');
                guardandoProducto = false;
                return;
            }
            
            /*
             * tecnica de envio con formdata:
             * formdata permite enviar archivos fisicos combinados con texto normal.
             * es el estandar absoluto de html5 para subir imagenes al servidor de java.
             */
            const formData = new FormData();
            formData.append('nombre', nombre);
            formData.append('descripcion', descripcion);
            formData.append('precio', precio);
            formData.append('stock', stock);
            formData.append('id_categoria', idCategoria);
            formData.append('etiquetas', etiquetas);
            // si eligio un proveedor, lo empacamos en la maleta para mandarlo a java
            if (idProveedor) formData.append('id_proveedor', idProveedor);
            
            // regla de negocio: todos los productos nacen activos en la tienda por defecto
            formData.append('estado', 'true');
            
            // decidir si es crear o editar: modificacion dinamica de la carga util
            if (productoEditandoId) {
                formData.append('id', productoEditandoId); // avisamos a java que esto es un update, no un insert
            }

            // atrapamos el archivo fisico de la imagen si el administrador selecciono una
            const inputImagenFile = document.getElementById('prod-imagen');
            if (inputImagenFile && inputImagenFile.files.length > 0) {
                formData.append('imagen', inputImagenFile.files[0]);
            }
            
            // Bloqueamos el boton para evitar que el usuario de multiples clics por accidente
            const btnSubmit = form.querySelector('button[type="submit"]');
            if (btnSubmit) {
                btnSubmit.disabled = true;
                btnSubmit.innerText = 'Guardando...';
            }

            try {
                // 2. transmision al servicio de backend
                // el booleano 'productoeditandoid !== null' dirige la ruta interna hacia el servlet apropiado (insertar o actualizar)
                await guardarProducto(formData, productoEditandoId !== null);
                
                // 3. post-procesamiento: refresco grafico sin recargar la web
                alert('¡producto guardado correctamente!');
                cerrarModal();
                cargarListaProductos(); 
            } catch (error) {
                console.error('error de conexion con el servidor:', error);
                alert('hubo un error al guardar el producto.');
            } finally {
                // abrimos el candado logico nuevamente, pase lo que pase (exito o error)
                guardandoProducto = false;
                
                // Desbloqueamos el boton cuando termine el proceso, pase lo que pase
                if (btnSubmit) {
                    btnSubmit.disabled = false;
                    btnSubmit.innerText = 'Guardar Producto';
                }
            }
        };
    }

    /*
     * patron de diseno: delegacion de eventos (event delegation).
     * en lugar de asignarle un 'eventlistener' a cada boton de "editar" y "eliminar" (lo cual saturaria la ram 
     * y fallaria si llegan productos nuevos despues), le asignamos un unico escuchador padre a la tabla entera (tbody).
     */
    const tbody = document.getElementById('tabla-productos-body');
    if (tbody) {
        tbody.onclick = async (evento) => {
            const btnClic = evento.target.closest('button'); // verificamos si lo que se presiono fue realmente un boton
            if (!btnClic) return; 

            const id = btnClic.getAttribute('data-id');

            if (btnClic.classList.contains('btn-editar')) {
                // modo edicion: extraemos los datos que escondimos en el html del boton (data-nombre, data-precio, etc)
                // y los inyectamos directamente en las cajas del formulario modal
                productoEditandoId = id;
                document.getElementById('prod-nombre').value = btnClic.getAttribute('data-nombre');
                if (document.getElementById('prod-descripcion')) {
                    document.getElementById('prod-descripcion').value = btnClic.getAttribute('data-descripcion') || '';
                }
                document.getElementById('prod-precio').value = btnClic.getAttribute('data-precio');
                document.getElementById('prod-stock').value = btnClic.getAttribute('data-stock');
                
                const catId = btnClic.getAttribute('data-categoria');
                const selectCat = document.getElementById('prod-categoria');
                // Validamos estrictamente que el ID exista para que no seleccione 'undefined'
                if (catId && catId !== 'undefined' && catId !== 'null' && selectCat) selectCat.value = catId;

                if (document.getElementById('prod-etiquetas')) {
                    document.getElementById('prod-etiquetas').value = btnClic.getAttribute('data-etiquetas') || '';
                }
                
                // modo edicion - imagen: mostramos la imagen actual si el producto ya tiene una guardada
                const urlImagen = btnClic.getAttribute('data-imagen');
                
                // limpiamos el input file por si quedo basura de una edicion anterior
                if (inputImagen) inputImagen.value = '';
                
                if (urlImagen && urlImagen !== 'null' && urlImagen !== 'undefined' && urlImagen.trim() !== '') {
                    if (previewImg) previewImg.src = urlImagen;
                    if (previewContenedor) previewContenedor.style.display = 'block';
                    // si el producto ya tiene foto en la base de datos, quitamos el 'required' para no obligar a subir una nueva
                    if (inputImagen) inputImagen.removeAttribute('required'); 
                } else {
                    if (previewContenedor) previewContenedor.style.display = 'none';
                    if (inputImagen) inputImagen.setAttribute('required', 'required');
                }

                document.getElementById('modal-titulo').innerText = 'Editar Producto';
                if (modal) modal.classList.remove('oculto');
                
            } else if (btnClic.classList.contains('btn-estado')) {
                // cambio rapido de estado (activar / pausar producto directamente desde la tabla)
                const estadoActual = btnClic.getAttribute('data-estado') === 'true';
                try {
                    await cambiarEstadoProducto(id, !estadoActual);
                    cargarListaProductos(); // refrescamos la tabla tras el cambio de estado
                } catch (error) { alert('error al cambiar el estado del producto'); }
                
            } else if (btnClic.classList.contains('btn-eliminar')) {
                // eliminacion de producto (siempre requiere confirmacion de seguridad)
                if (confirm('¿seguro que deseas eliminar este producto de forma permanente?')) {
                    try {
                        await eliminarProducto(id);
                        cargarListaProductos();
                    } catch (err) { 
                        alert('no se pudo borrar.\n\nes probable que un cliente ya haya comprado este producto y se encuentre registrado en un pedido historico.\n\nsi ya no deseas venderlo, te sugerimos usar el boton de "pausar/activar".'); 
                    }
                }
            }
        };
    }

    // al entrar por primera vez a la pantalla, pedimos los productos a la base de datos
    cargarListaProductos();
    
    // llenamos las opciones dinamicas del formulario para que el administrador no tenga que escribirlas a mano
    cargarCategoriasFormulario();
    cargarProveedoresFormulario();
}

/**
 * llama al backend para obtener las categorias maestras y llenar el <select> del modal.
 */
async function cargarCategoriasFormulario() {
    try {
        const respuesta = await fetch('categorias');
        if (respuesta.ok) {
            const categorias = await respuesta.json();
            const select = document.getElementById('prod-categoria');
            if (!select) return;
            
            // vaciamos el select y dejamos una opcion inicial deshabilitada como guia
            select.innerHTML = '<option value="" disabled selected>seleccione una categoria</option>';
            
            // iteramos (recorremos) una por una las categorias que llegaron de la base de datos.
            // el foreach es como un ciclo que dice: "por cada categoria que exista, haz lo siguiente:"
            categorias.forEach(cat => {
                // innerhtml += significa "manten lo que ya tenias y agregale esto nuevo al final".
                // aqui estamos fabricando visualmente las etiquetas <option> para que el administrador pueda seleccionarlas en el formulario.
                select.innerHTML += `<option value="${cat.id_categoria_pk || cat.id}">${cat.nombre}</option>`;
            });
        }
    } catch (error) {
        console.error('error al cargar categorias en el formulario:', error);
    }
}

/**
 * llama al backend para obtener la lista de proveedores registrados y llenar el <select> correspondiente.
 */
async function cargarProveedoresFormulario() {
    try {
        const respuesta = await fetch('proveedores');
        if (respuesta.ok) {
            const proveedores = await respuesta.json();
            const select = document.getElementById('prod-proveedor');
            if (!select) return;
            
            select.innerHTML = '<option value="">sin proveedor (producto propio de dmari)</option>';
            
            // recorremos el arreglo de proveedores que nos entrego java.
            proveedores.forEach(prov => {
                // por cada proveedor, agregamos una opcion al menu desplegable del formulario modal.
                // el 'value' oculto es el id numerico, y el texto visible es el nombre del proveedor.
                select.innerHTML += `<option value="${prov.id_usuario_pk}">${prov.nombre}</option>`;
            });
        } 
    } catch (error) { console.error('error al cargar proveedores:', error); }
}

/**
 * se comunica con el servicio web para traer todos los productos de la tienda
 * y construye dinamicamente una por una las filas visuales de la tabla del administrador.
 */
async function cargarListaProductos() {
    const tbody = document.getElementById('tabla-productos-body');
    if (!tbody) return; // proteccion: si no existe la tabla en el html, abortamos para no causar errores

    try {
        // pedimos la lista al backend usando el servicio dedicado
        const productosBD = await obtenerProductos();
        
        // si el servicio nos devuelve null (por fallo de red), disparamos un error intencional para caer en el catch
        if (!productosBD) throw new Error('no se pudieron obtener los productos desde java');
        
        // vaciamos el texto temporal de "cargando..."
        tbody.innerHTML = '';
 
        // recorremos el arreglo de productos que llego de mysql y armamos la interfaz.
        // el foreach iterara y ejecutara este bloque de codigo la misma cantidad de veces que productos existan.
        productosBD.forEach(prod => {
            // document.createelement('tr') fabrica una fila de tabla en blanco directamente en la memoria de javascript.
            const tr = document.createElement('tr');
            
            /* 
             * extraccion segura de variables usando el operador logico || (o)
             * ¿por que hay varios nombres para una misma cosa como urlruta o url_ruta?
             * porque dependiendo de como java convierta el objeto a json (con la libreria gson o con tu jsonhelper manual), 
             * la variable puede llegar escrita en formato camelcase (urlRuta), en formato base de datos (url_ruta) o muy corta (imagen).
             * javascript evaluara una por una: si la primera no existe, lee la segunda, luego la tercera...
             * si la base de datos no arroja ninguna, colocara la ruta del gato por defecto.
             */
            const id = prod.idProductoPk || prod.id_producto_pk || prod.id;
            const nombre = prod.nombreProducto || prod.nombre_producto || prod.nombre || 'Producto sin nombre';
            const rutaImg = prod.urlRuta || prod.url_ruta || prod.imagen || 'src/img/productos/default/gato_programador.jpg';
            // Agregamos todas las combinaciones posibles de JSON para atrapar el ID de la categoria si o si
            const catId = prod.idCategoriaFk || prod.id_categoria_fk || prod.id_categoria || prod.idCategoria || prod.categoriaId || '';
            const nombreCategoria = prod.categoria || prod.nombre_categoria || prod.nombreCategoria || 'Sin categoria';
            
            // verificamos si las etiquetas vienen como array o como texto simple
            let etiquetasTxt = '';
            if (Array.isArray(prod.etiquetas)) etiquetasTxt = prod.etiquetas.join(', ');
            else if (prod.etiquetas) etiquetasTxt = prod.etiquetas;
            
            // inyectamos el codigo html (las columnas td) adentro de la fila vacia que creamos arriba.
            // las comillas invertidas ` ` nos permiten mezclar texto html con variables dinamicas ${}.
            tr.innerHTML = `
                <td>#00${id}</td>
                <td>
                    <img src="${rutaImg}" alt="${nombre}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 6px; border: 1px solid #ddd;">
                </td>
                <td>
                    <div style="font-weight: bold; margin-bottom: 4px;">${nombre}</div>
                    <span style="font-size: 0.75rem; color: #666; background-color: #f0f0f0; padding: 2px 6px; border-radius: 10px;">${nombreCategoria}</span>
                </td>
                <td>
                    <div>$${prod.precio}</div>
                    <div style="font-size: 0.8rem; margin-top: 4px; font-weight: 600; color: ${prod.stock > 5 ? '#28a745' : (prod.stock > 0 ? '#f39c12' : '#dc3545')};">
                        stock: ${prod.stock} un.
                    </div>
                </td>
                <td><span class="badge-estado ${prod.estado !== false ? 'badge-activo' : 'badge-inactivo'}">${prod.estado !== false ? 'ACTIVO' : 'PAUSADO'}</span></td>
                <td>
                    <button class="btn-editar" 
                        data-id="${id}" 
                        data-nombre="${nombre}" 
                        data-descripcion="${prod.descripcion || ''}"
                        data-precio="${prod.precio}"
                        data-stock="${prod.stock}"
                        data-categoria="${catId}"
                        data-etiquetas="${etiquetasTxt}"
                        data-imagen="${rutaImg}">
                        <i class='bx bx-edit'></i> Editar
                    </button>
                    <button class="btn-estado" data-id="${id}" data-estado="${prod.estado !== false}">
                        <i class='bx bx-refresh'></i> ${prod.estado !== false ? 'Pausar' : 'Activar'}
                    </button>
                    <button class="btn-eliminar" data-id="${id}">
                        <i class='bx bx-trash'></i> Borrar
                    </button>
                </td>
            `;
            // appendchild es la instruccion final: toma esa fila ya armada en memoria y la incrusta
            // fisicamente en la tabla (tbody) de la pagina web para que el ojo humano la pueda ver.
            tbody.appendChild(tr);
        });

    } catch (error) {
        console.error('error general al cargar la lista:', error);
    }
}