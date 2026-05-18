// Dependencias de UI
import { cargarComponente } from '../../../services/uiService.js';

// Inicializa la vista de gestión de solicitudes
export async function cargarVistaAdminSolicitudes() {
    await cargarComponente('component-main', './src/views/Administrador/adminSolicitudes/adminSolicitudes.html');
    
    cargarListaSolicitudes();
}

// Obtiene y renderiza el listado de solicitudes
async function cargarListaSolicitudes() {
    const tbody = document.getElementById('tabla-solicitudes-body');
    if (!tbody) return;

    try {
        // TODO: Implementar fetch al backend cuando se cree la tabla de solicitudes
        console.log('Modo de diseño: Cargando datos simulados en la vista');
        
        // Lista de ejemplos para ver cómo se comporta el diseño
        const solicitudesMock = [
            { id: 1, proveedor: 'Proveedor Aromas S.A', producto: 'Vela de Lavanda 500g', accion: 'NUEVO', claseCSS: 'badge-nuevo' },
            { id: 2, proveedor: 'Ceras y Esencias', producto: 'Set Relax Menta', accion: 'EDITAR', claseCSS: 'badge-editar' },
            { id: 3, proveedor: 'Velas del Bosque', producto: 'Vela Pino 200g', accion: 'BORRAR', claseCSS: 'badge-borrar' }
        ];
        
        tbody.innerHTML = ''; // Limpiamos el texto de "Cargando..."

        // Recorremos la lista y creamos las filas dinámicamente
        solicitudesMock.forEach(solicitud => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${solicitud.id}</td>
                <td>${solicitud.proveedor}</td>
                <td>${solicitud.producto}</td>
                <td><span class="badge-accion ${solicitud.claseCSS}">${solicitud.accion}</span></td>
                <td>PENDIENTE</td>
                <td>
                    <button class="btn-aprobar"><i class='bx bx-check'></i> Aprobar</button>
                    <button class="btn-rechazar"><i class='bx bx-x'></i> Rechazar</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Añadimos la interactividad visual (eliminar la fila al hacer clic)
        asignarEventosBotones(tbody);

    } catch (error) {
        console.error('Error al cargar la tabla de solicitudes:', error);
        tbody.innerHTML = `<tr><td colspan="6">Error de conexión al cargar solicitudes</td></tr>`;
    }
}

// Función que le da vida a los botones temporalmente
function asignarEventosBotones(tbody) {
    const botonesAprobar = tbody.querySelectorAll('.btn-aprobar');
    const botonesRechazar = tbody.querySelectorAll('.btn-rechazar');

    botonesAprobar.forEach(btn => btn.addEventListener('click', (evento) => {
        const fila = evento.target.closest('tr');
        fila.remove(); // Borra la fila simulando que ya se aprobó
    }));

    botonesRechazar.forEach(btn => btn.addEventListener('click', (evento) => {
        const fila = evento.target.closest('tr');
        fila.remove(); // Borra la fila simulando que ya se rechazó
    }));
}