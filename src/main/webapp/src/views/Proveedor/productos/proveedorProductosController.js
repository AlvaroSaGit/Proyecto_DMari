import { cargarComponente } from '../../../services/uiService.js';
import { obtenerProductos, cambiarEstadoProducto } from '../../../services/productoService.js';

// Carga la vista principal del proveedor
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
    
    // Evento para abrir el modal al solicitar nuevo producto
    if (btnNuevaSolicitud) {
        btnNuevaSolicitud.addEventListener('click', () => {
            document.getElementById('modal-solicitud-titulo').innerText = 'Solicitar Nuevo Producto';
            form.reset();
            modal.classList.remove('oculto');
        });
    }

    // Cerrar modal
    const cerrarModal = () => modal.classList.add('oculto');
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);

    // Simulación del envío del formulario (Aquí iría el Fetch a Java)
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            alert('Simulación: Tu solicitud ha sido enviada al Administrador.');
            cerrarModal();
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
            // Adaptamos las variables a como llegan desde el backend (JSON)
            const idProd = prod.idProductoPk || prod.id;
            const nombreProd = prod.nombreProducto || prod.nombre || 'Producto';
            const estadoBadge = prod.estado ? '<span style="background: #d4edda; color: #155724; padding: 2px 6px; border-radius: 10px; font-size: 0.75rem; margin-left: 5px;">Activo</span>' : '<span style="background: #f8d7da; color: #721c24; padding: 2px 6px; border-radius: 10px; font-size: 0.75rem; margin-left: 5px;">Inactivo</span>';
            
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${idProd}</td>
                <td><i class='bx bx-image' style='font-size: 2rem; color: #ccc;'></i></td>
                <td>
                    <div style="font-weight: bold; display: flex; align-items: center;">${nombreProd} ${estadoBadge}</div>
                </td>
                <td>$${prod.precio.toFixed(2)}</td>
                <td>${prod.stock} uds</td>
                <td>
                    <button class="btn-editar" data-id="${idProd}"><i class='bx bx-edit'></i> Editar Info</button>
                    <button class="btn-estado" data-id="${idProd}"><i class='bx bx-refresh'></i> ${prod.estado ? 'Pausar' : 'Activar'}</button>
                    <button class="btn-eliminar" data-id="${idProd}"><i class='bx bx-trash'></i> Solicitar Borrado</button>
                </td>
            `;
            
            // evento dinamico para cambiar el estado (activo/inactivo) al instante
            tr.querySelector('.btn-estado').addEventListener('click', async () => {
                try {
                    await cambiarEstadoProducto(idProd, !prod.estado);
                    cargarMisProductos(); // recargamos para ver el cambio
                } catch (error) {
                    alert('Error al cambiar el estado del producto');
                }
            });

            tbody.appendChild(tr);
        });

        // Eventos a los botones de la tabla
        const modal = document.getElementById('modal-solicitud-proveedor');
        tbody.querySelectorAll('.btn-editar').forEach(btn => btn.addEventListener('click', () => {
            document.getElementById('modal-solicitud-titulo').innerText = 'Solicitar Edición de Producto';
            modal.classList.remove('oculto');
        }));

        tbody.querySelectorAll('.btn-eliminar').forEach(btn => btn.addEventListener('click', () => {
            if(confirm('¿Seguro que deseas enviar una solicitud para eliminar este producto?')) {
                alert('Simulación: Solicitud de borrado enviada al admin.');
            }
        }));
    } catch (error) { console.error(error); }
}