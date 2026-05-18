import { cargarComponente } from '../../../services/uiService.js';

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
        const misProductosMock = [
            { id: 101, nombre: 'Esencia de Vainilla 100ml', precio: 5.50, stock: 100 },
            { id: 102, nombre: 'Cera de Soya 1kg', precio: 12.00, stock: 50 }
        ];
        
        tbody.innerHTML = '';

        misProductosMock.forEach(prod => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${prod.id}</td>
                <td><i class='bx bx-image' style='font-size: 2rem; color: #ccc;'></i></td>
                <td>${prod.nombre}</td>
                <td>$${prod.precio.toFixed(2)}</td>
                <td>${prod.stock} uds</td>
                <td>
                    <button class="btn-editar"><i class='bx bx-edit'></i> Solicitar Edición</button>
                    <button class="btn-eliminar"><i class='bx bx-trash'></i> Solicitar Borrado</button>
                </td>
            `;
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