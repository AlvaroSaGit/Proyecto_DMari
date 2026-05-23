/*
    objetivo de este archivo:
    controlador encargado de la vista de productos del proveedor.
    gestiona la peticion de los productos que le pertenecen al proveedor logueado,
    los dibuja en la tabla usando el componente reutilizable y maneja los eventos 
    de los botones (solicitar edicion, pausar, solicitar borrado).
*/
import { cargarComponente } from '../../../services/uiService.js';
import { obtenerProductos, cambiarEstadoProducto } from '../../../services/productoService.js';
import { crearFilaProducto } from '../../../components/tablas/filaProductoComponent.js';

// carga la vista principal del proveedor
export async function cargarVistaProveedorProductos() {
    await cargarComponente('component-main', './src/views/Proveedor/productos/proveedorProductos.html');
    prepararVistaProveedor();
}

function prepararVistaProveedor() {
    const btnNuevaSolicitud = document.getElementById('btn-nueva-solicitud');
    const modal = document.getElementById('modal-solicitud-proveedor');
    const btnCerrar = document.getElementById('btn-cerrar-modal-prov');
    const btnCancelar = document.getElementById('btn-cancelar-modal-prov');
    const form = document.getElementById('form-solicitud-prov');
    
    // evento para abrir el modal al solicitar nuevo producto
    if (btnNuevaSolicitud) {
        btnNuevaSolicitud.addEventListener('click', () => {
            document.getElementById('modal-solicitud-titulo').innerText = 'Solicitar Nuevo Producto';
            form.reset();
            modal.classList.remove('oculto');
        });
    }

    // cerrar modal
    const cerrarModal = () => modal.classList.add('oculto');
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);

    // simulacion del envio del formulario (aqui iria el fetch a java)
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            alert('simulacion: tu solicitud ha sido enviada al administrador.');
            cerrarModal();
        });
    }

    // aplicamos delegacion de eventos a la tabla entera del proveedor
    const tbody = document.getElementById('tabla-proveedor-productos-body');
    if (tbody) {
        tbody.addEventListener('click', async (evento) => {
            const btnClic = evento.target.closest('button');
            if (!btnClic) return;

            const id = btnClic.getAttribute('data-id');

            if (btnClic.classList.contains('btn-editar')) {
                document.getElementById('modal-solicitud-titulo').innerText = 'Solicitar Edicion de Producto';
                modal.classList.remove('oculto');
                
            } else if (btnClic.classList.contains('btn-estado')) {
                const estadoActual = btnClic.getAttribute('data-estado') === 'true';
                try {
                    await cambiarEstadoProducto(id, !estadoActual);
                    cargarMisProductos(); 
                } catch (error) { alert('error al cambiar el estado del producto'); }
                
            } else if (btnClic.classList.contains('btn-eliminar')) {
                if(confirm('¿seguro que deseas enviar una solicitud para eliminar este producto?')) {
                    alert('simulacion: solicitud de borrado enviada al admin.');
                }
            }
        });
    }

    cargarMisProductos();
}

async function cargarMisProductos() {
    const tbody = document.getElementById('tabla-proveedor-productos-body');
    if (!tbody) return;

    try {
        // pedimos especificamente los productos del proveedor en sesion
        const misProductos = await obtenerProductos('?proveedor=true');
        
        if (!misProductos) throw new Error('No se pudieron obtener los productos');
        
        tbody.innerHTML = '';

        misProductos.forEach(prod => {
            tbody.appendChild(crearFilaProducto(prod));
        });
    } catch (error) { console.error(error); }
}