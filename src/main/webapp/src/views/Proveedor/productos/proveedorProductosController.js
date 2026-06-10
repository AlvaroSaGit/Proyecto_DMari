// importamos los servicios necesarios para manipular la interfaz y los datos
import { cargarComponente } from '../../../services/uiService.js';
import { cargarVistaSolicitudCategoria } from './solicitudProveedorController.js';
import { obtenerProductos, guardarProducto, eliminarProducto, cambiarEstadoProducto } from '../../../services/productoService.js';

// variable global que nos indica si estamos creando un producto nuevo (null) o editando uno existente (id)
let productoEditandoId = null;

// funcion de arranque para la vista del proveedor.
// inyecta el esqueleto html en la pantalla y luego prepara los botones.
export async function cargarVistaProveedorProductos() {
    await cargarComponente('component-main', './src/views/Proveedor/productos/proveedorProductos.html');
    prepararVistaProveedorProductos();
}

// mapea todos los elementos del dom y les asigna eventos (clics, envios de formulario, etc)
function prepararVistaProveedorProductos() {
    // extraemos los botones y contenedores principales
    const btnAgregar = document.getElementById('btn-nuevo-producto-prov');
    const modal = document.getElementById('modal-producto-prov');
    const btnCerrar = document.getElementById('btn-cerrar-modal-prov');
    const btnCancelar = document.getElementById('btn-cancelar-modal-prov');
    const form = document.getElementById('form-producto-prov');
    
    // extraemos los elementos visuales para la previsualizacion de la foto
    const inputImagen = document.getElementById('prod-imagen-prov');
    const previewContenedor = document.getElementById('contenedor-preview-prov');
    const previewImg = document.getElementById('prod-imagen-preview-prov');

    // evento: previsualizacion de la foto local antes de subirla al servidor
    if (inputImagen) {
        inputImagen.addEventListener('change', function() {
            const archivo = this.files[0];
            if (archivo) {
                // usamos filereader para leer la foto directamente desde el disco duro del usuario
                const lector = new FileReader();
                lector.onload = function(e) {
                    previewImg.src = e.target.result;
                    previewContenedor.style.display = 'block';
                }
                lector.readAsDataURL(archivo);
            } else {
                // si cancela la seleccion, ocultamos el cuadro
                previewImg.src = '';
                previewContenedor.style.display = 'none';
            }
        });
    }

    // boton de nuevo producto: limpia todo el formulario y muestra la ventana flotante
    if (btnAgregar) {
        btnAgregar.addEventListener('click', () => {
            productoEditandoId = null;
            document.getElementById('modal-titulo-prov').innerText = 'Nuevo Producto';
            if (form) form.reset();
            if (previewContenedor) previewContenedor.style.display = 'none';
            if (previewImg) previewImg.src = '';
            if (inputImagen) { inputImagen.value = ''; inputImagen.setAttribute('required', 'required'); }
            if (modal) modal.classList.remove('oculto'); 
        });
    }
    
    // funcion auxiliar para cerrar la ventana emergente agregando la clase 'oculto'
    const cerrarModal = () => { if (modal) modal.classList.add('oculto'); };
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);
    
    // evento principal de guardado o edicion: se dispara al darle 'submit' al formulario
    if (form) {
        form.addEventListener('submit', async (e) => {
            // evitamos que la pagina entera se recargue
            e.preventDefault();
            
            // capturamos todos los textos escritos por el proveedor
            const nombre = document.getElementById('prod-nombre-prov').value;
            const descripcion = document.getElementById('prod-descripcion-prov') ? document.getElementById('prod-descripcion-prov').value : '';
            const precio = document.getElementById('prod-precio-prov').value;
            const stock = document.getElementById('prod-stock-prov').value;
            const idCategoria = document.getElementById('prod-categoria-prov').value;
            const etiquetas = document.getElementById('prod-etiquetas-prov') ? document.getElementById('prod-etiquetas-prov').value : '';
            
            // usamos formdata para empaquetar los textos y la imagen en un solo envio compatible con java
            const formData = new FormData();
            formData.append('nombre', nombre);
            formData.append('descripcion', descripcion);
            formData.append('precio', precio);
            formData.append('stock', stock);
            formData.append('id_categoria', idCategoria);
            formData.append('etiquetas', etiquetas);
            formData.append('estado', 'true');
            
            // si estamos editando, le enviamos el id para que java haga un update
            if (productoEditandoId) {
                formData.append('id', productoEditandoId);
            }

            // si el proveedor eligio una foto nueva, la empacamos
            if (inputImagen && inputImagen.files.length > 0) {
                formData.append('imagen', inputImagen.files[0]);
            }
            
            try {
                // enviamos los datos al servidor y recargamos la tabla visualmente
                await guardarProducto(formData, productoEditandoId !== null);
                alert('¡producto guardado correctamente!');
                cerrarModal();
                cargarMisProductos(); 
            } catch (error) {
                console.error('error al guardar:', error);
                alert('hubo un error al guardar el producto.');
            }
        });
    }

    // delegacion de eventos para la tabla: un solo escuchador para todos los botones de la tabla
    const tbody = document.getElementById('tabla-productos-prov-body');
    if (tbody) {
        tbody.addEventListener('click', async (evento) => {
            // verificamos si lo que se presiono fue realmente un boton
            const btnClic = evento.target.closest('button');
            if (!btnClic) return; 

            const id = btnClic.getAttribute('data-id');

            if (btnClic.classList.contains('btn-editar')) {
                // modo edicion: sacamos los datos ocultos en el boton y los ponemos en las cajas de texto
                productoEditandoId = id;
                document.getElementById('prod-nombre-prov').value = btnClic.getAttribute('data-nombre');
                if (document.getElementById('prod-descripcion-prov')) {
                    document.getElementById('prod-descripcion-prov').value = btnClic.getAttribute('data-descripcion') || '';
                }
                document.getElementById('prod-precio-prov').value = btnClic.getAttribute('data-precio');
                document.getElementById('prod-stock-prov').value = btnClic.getAttribute('data-stock');
                
                const catId = btnClic.getAttribute('data-categoria');
                const selectCat = document.getElementById('prod-categoria-prov');
                // proteccion para asegurar que se seleccione la categoria correcta sin arrojar undefined
                if (catId && catId !== 'undefined' && catId !== 'null' && selectCat) selectCat.value = catId;

                if (document.getElementById('prod-etiquetas-prov')) {
                    document.getElementById('prod-etiquetas-prov').value = btnClic.getAttribute('data-etiquetas') || '';
                }
                
                // logica para mostrar la imagen actual del producto si es que tiene una
                const urlImagen = btnClic.getAttribute('data-imagen');
                if (inputImagen) inputImagen.value = '';
                
                if (urlImagen && urlImagen !== 'null' && urlImagen !== 'undefined' && urlImagen.trim() !== '') {
                    if (previewImg) previewImg.src = urlImagen;
                    if (previewContenedor) previewContenedor.style.display = 'block';
                    if (inputImagen) inputImagen.removeAttribute('required'); 
                } else {
                    if (previewContenedor) previewContenedor.style.display = 'none';
                    // si no tiene imagen previa, lo obligamos a subir una
                    if (inputImagen) inputImagen.setAttribute('required', 'required');
                }

                document.getElementById('modal-titulo-prov').innerText = 'Editar Producto';
                if (modal) modal.classList.remove('oculto');
                
            } else if (btnClic.classList.contains('btn-estado')) {
                // logica para pausar o activar un producto rapidamente
                const estadoActual = btnClic.getAttribute('data-estado') === 'true';
                try {
                    await cambiarEstadoProducto(id, !estadoActual);
                    cargarMisProductos();
                } catch (error) { alert('error al cambiar el estado del producto'); }
                
            } else if (btnClic.classList.contains('btn-eliminar')) {
                // eliminacion con confirmacion de seguridad
                if (confirm('¿seguro que deseas eliminar este producto de forma permanente?')) {
                    try {
                        await eliminarProducto(id);
                        cargarMisProductos();
                    } catch (err) { 
                        alert('no se pudo borrar. quiza este en un pedido historico. puedes pausarlo.'); 
                    }
                }
            }
        });
    }

    // ejecuciones iniciales al cargar la pantalla
    cargarMisProductos();
    cargarCategoriasFormularioProv();
}

// solicita a java la lista de categorias maestras para llenar el selector del formulario
async function cargarCategoriasFormularioProv() {
    try {
        const respuesta = await fetch('categorias');
        if (respuesta.ok) {
            const categorias = await respuesta.json();
            const select = document.getElementById('prod-categoria-prov');
            if (!select) return;
            
            select.innerHTML = '<option value="" disabled selected>seleccione una categoria</option>';
            // por cada categoria, creamos una opcion html para el menu desplegable
            categorias.forEach(cat => {
                // usamos la extraccion segura de variables
                select.innerHTML += `<option value="${cat.id_categoria_pk || cat.id}">${cat.nombre}</option>`;
            });
        }
    } catch (error) { console.error('error al cargar categorias:', error); }
}

// solicita al backend unicamente los productos de este proveedor y arma la tabla
async function cargarMisProductos() {
    const tbody = document.getElementById('tabla-productos-prov-body');
    if (!tbody) return; 

    try {
        // aqui esta la magia: le pedimos al backend solo los del proveedor usando el parametro url "?proveedor=true"
        const productosBD = await obtenerProductos('?proveedor=true');
        
        // forzamos el error si no hay respuesta
        if (!productosBD) throw new Error('no se pudieron obtener los productos');
        
        // limpiamos la tabla
        tbody.innerHTML = '';
 
        // si el servidor nos devuelve una lista vacia, mostramos un mensaje amigable
        if(productosBD.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Aun no has creado ningun producto</td></tr>';
            return;
        }

        // recorremos los productos uno por uno para armar las filas
        productosBD.forEach(prod => {
            // creamos una fila vacia en la memoria del navegador
            const tr = document.createElement('tr');
            
            // extraccion segura de variables por si jsonhelper cambia los nombres
            const id = prod.idProductoPk || prod.id_producto_pk || prod.id;
            const nombre = prod.nombreProducto || prod.nombre_producto || prod.nombre || 'Producto sin nombre';
            const rutaImg = prod.urlRuta || prod.url_ruta || prod.imagen || 'src/img/productos/default/gato_programador.jpg';
            // fallback robusto para atrapar el id de la categoria
            const catId = prod.idCategoriaFk || prod.id_categoria_fk || prod.id_categoria || prod.idCategoria || prod.categoriaId || '';
            const nombreCategoria = prod.categoria || prod.nombre_categoria || prod.nombreCategoria || 'Sin categoria';
            
            let etiquetasTxt = '';
            if (Array.isArray(prod.etiquetas)) etiquetasTxt = prod.etiquetas.join(', ');
            else if (prod.etiquetas) etiquetasTxt = prod.etiquetas;
            
            // inyectamos el html interno de las columnas (td) con sus datos correspondientes
            tr.innerHTML = `
                <td>#00${id}</td>
                <td><img src="${rutaImg}" alt="${nombre}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 6px; border: 1px solid #ddd;"></td>
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
                    <button class="btn-editar" data-id="${id}" data-nombre="${nombre}" data-descripcion="${prod.descripcion || ''}" data-precio="${prod.precio}" data-stock="${prod.stock}" data-categoria="${catId}" data-etiquetas="${etiquetasTxt}" data-imagen="${rutaImg}"><i class='bx bx-edit'></i> Editar</button>
                    <button class="btn-estado" data-id="${id}" data-estado="${prod.estado !== false}"><i class='bx bx-refresh'></i> ${prod.estado !== false ? 'Pausar' : 'Activar'}</button>
                    <button class="btn-eliminar" data-id="${id}"><i class='bx bx-trash'></i> Borrar</button>
                </td>
            `;
            // pegamos la fila terminada en el cuerpo de la tabla para que el usuario la vea
            tbody.appendChild(tr);
        });

    } catch (error) {
        console.error('error al cargar mis productos:', error);
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red;">error al cargar los datos</td></tr>';
    }
}