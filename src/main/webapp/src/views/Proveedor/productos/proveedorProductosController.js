import { cargarComponente } from '../../../services/uiService.js';

/**
 * Funcion de arranque para la vista del proveedor.
 * 1. Inyecta el HTML en el contenedor principal.
 * 2. Llama a la base de datos para cargar los productos.
 * 3. Prepara la logica de la ventana emergente (Modal).
 */
export async function cargarVistaProveedorProductos() {
    await cargarComponente('component-main', './src/views/Proveedor/productos/proveedorProductos.html');
    cargarMisProductos();
    configurarModalProveedor();
}

/**
 * Se comunica con el backend de Java (Servlet) para traer unicamente
 * los productos que le pertenecen al proveedor logueado y construye la tabla visual.
 */
async function cargarMisProductos() {
    const tbody = document.getElementById('tabla-prov-productos-body');
    // Proteccion: Si la tabla no existe en el HTML, detenemos la funcion
    if (!tbody) return;

    try {
        // Peticion real al Servlet de Java
        const respuesta = await fetch('productosProveedor');
        if (!respuesta.ok) throw new Error('No se pudo conectar con el servidor');
        
        const misProductos = await respuesta.json();
        
        tbody.innerHTML = ''; // Limpiar mensaje de carga

        // Recorremos el arreglo de productos y creamos una fila <tr> por cada uno
        misProductos.forEach(prod => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${prod.idProductoPk}</td>
                <td><img src="${prod.urlRuta || 'src/img/productos/default/gato_programador.jpg'}" alt="imagen"></td>
                <td>${prod.nombreProducto}</td>
                <td>$${prod.precio}</td>
                <td>${prod.stock} uds</td>
                <td><span class="badge-estado ${prod.estado ? 'badge-activo' : 'badge-inactivo'}">${prod.estado ? 'ACTIVO' : 'INACTIVO'}</span></td>
                <td>
                    <button class="btn-editar"><i class='bx bx-edit'></i> Editar</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('Error al cargar mis productos:', error);
    }
}

/**
 * Mapea los botones del HTML y les asigna los eventos (clics) necesarios
 * para abrir, limpiar y cerrar el formulario de agregar/editar productos.
 */
function configurarModalProveedor() {
    const btnNuevo = document.getElementById('btn-nuevo-producto');
    const modal = document.getElementById('modal-producto');
    const btnCerrar = document.getElementById('btn-cerrar-modal');
    const btnCancelar = document.getElementById('btn-cancelar-modal');

    // EVENTO ABRIR: Resetea el formulario, carga las categorias y muestra la ventana
    if (btnNuevo) {
        btnNuevo.addEventListener('click', () => {
            document.getElementById('modal-titulo').innerText = 'Nuevo Producto';
            document.getElementById('form-producto').reset();
            document.getElementById('contenedor-preview').style.display = 'none';
            cargarCategorias(); // Llenamos el <select>
            modal.classList.remove('oculto');
        });
    }

    // EVENTO CERRAR: Le agrega la clase 'oculto' al modal para que desaparezca
    const cerrarModal = () => modal.classList.add('oculto');
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);
}

/**
 * Hace una peticion al servidor para obtener las categorias maestras 
 * (Reposteria, Decoracion, etc.) y llena el <select> del formulario.
 */
async function cargarCategorias() {
    const select = document.getElementById('prod-categoria');
    if (!select) return;

    try {
        const respuesta = await fetch('categorias');
        const categorias = await respuesta.json();
        
        // Vaciamos el select y le ponemos una opcion por defecto
        select.innerHTML = '<option value="">Seleccione una categoria...</option>';
        categorias.forEach(cat => {
            select.innerHTML += `<option value="${cat.id_categoria_pk}">${cat.nombre}</option>`;
        });
    } catch (error) {
        console.error('Error al cargar categorias:', error);
    }
}