// Dependencias de UI
import { cargarComponente } from '../../../services/uiService.js';

// Inicializa la vista de gestión de productos
export async function cargarVistaAdminProductos() {
    await cargarComponente('component-main', './src/views/Administrador/productos/adminProductos.html');
    
    prepararVistaAdminProductos();
}

// Configura los listeners y lanza las peticiones iniciales
function prepararVistaAdminProductos() {
    const btnAgregar = document.getElementById('btn-nuevo-producto');
    const modal = document.getElementById('modal-producto');
    const btnCerrar = document.getElementById('btn-cerrar-modal');
    const btnCancelar = document.getElementById('btn-cancelar-modal');
    const form = document.getElementById('form-producto');
    
    if (btnAgregar) {
        btnAgregar.addEventListener('click', () => {
            document.getElementById('modal-titulo').innerText = 'Nuevo Producto';
            if (form) form.reset();
            if (modal) modal.classList.remove('oculto');
        });
    }
    
    // Cerrar modal
    const cerrarModal = () => { if (modal) modal.classList.add('oculto'); };
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);
    
    // Simulacion del envio al backend (guardar producto)
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            alert('Simulación: El producto se ha guardado / actualizado en la Base de Datos.');
            cerrarModal();
        });
    }

    cargarListaProductos();
}

// Obtiene y renderiza el listado de productos
async function cargarListaProductos() {
    const tbody = document.getElementById('tabla-productos-body');
    if (!tbody) return;

    try {
        console.log('Modo diseño: Cargando productos simulados');
        
        const productosMock = [
            { id: 1, nombre: 'Vela Aromática Vainilla', precio: 15.50, stock: 20 },
            { id: 2, nombre: 'Set de Regalo Lavanda', precio: 45.00, stock: 5 }
        ];
        
        tbody.innerHTML = '';

        productosMock.forEach(prod => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${prod.id}</td>
                <td><i class='bx bx-image' style='font-size: 2rem; color: #ccc;'></i></td>
                <td>${prod.nombre}</td>
                <td>$${prod.precio.toFixed(2)}</td>
                <td>${prod.stock} uds</td>
                <td>
                    <button class="btn-editar"><i class='bx bx-edit'></i> Editar</button>
                    <button class="btn-eliminar"><i class='bx bx-trash'></i> Borrar</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
        
        // Le damos vida a los nuevos botones que acabamos de crear en la tabla
        const modal = document.getElementById('modal-producto');
        tbody.querySelectorAll('.btn-editar').forEach(btn => btn.addEventListener('click', () => {
            document.getElementById('modal-titulo').innerText = 'Editar Producto';
            if (modal) modal.classList.remove('oculto');
        }));

        tbody.querySelectorAll('.btn-eliminar').forEach(btn => btn.addEventListener('click', (evento) => {
            if(confirm('¿Seguro que deseas eliminar este producto?')) {
                evento.target.closest('tr').remove();
                alert('Simulación: Producto eliminado de la base de datos.');
            }
        }));
    } catch (error) {
        console.error('Error al cargar la tabla de productos:', error);
        tbody.innerHTML = `<tr><td colspan="6">Error de conexión al cargar productos</td></tr>`;
    }
}
