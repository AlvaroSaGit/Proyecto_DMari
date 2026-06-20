// Dependencias de UI
import { cargarComponente } from '../../../services/uiService.js';

// Inicializa la vista de gestión de solicitudes
export async function cargarVistaAdminSolicitudes() {
    await cargarComponente('component-main', './src/views/Administrador/adminSolicitudes/adminSolicitudes.html');
    
    cargarListaSolicitudes();
    // inicializamos el buscador de solicitudes para identificar artesanos rapidamente
    prepararFiltrosSolicitudes();
}

// Obtiene y renderiza el listado de solicitudes
async function cargarListaSolicitudes() {
    const tbody = document.getElementById('tabla-solicitudes-body');
    if (!tbody) return;

    try {
        const respuesta = await fetch('solicitudes-proveedor');
        if (!respuesta.ok) throw new Error('Error al obtener solicitudes');
        
        const solicitudes = await respuesta.json();
        
        tbody.innerHTML = ''; 

        if (solicitudes.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No hay solicitudes pendientes</td></tr>';
            return;
        }

        solicitudes.forEach(solicitud => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${solicitud.id}</td>
                <td>
                    <strong>${solicitud.usuarioNombre}</strong><br>
                    <small style="color: #666;">${solicitud.usuarioCorreo}</small>
                </td>
                <td>${solicitud.nit}</td>
                <td><strong>${solicitud.marca}</strong></td>
                <td>
                    ${solicitud.banco}<br>
                    <small style="color: #666;">Cta ${solicitud.tipoCuenta}: ${solicitud.cuenta}</small>
                </td>
                <td><span class="badge-estado ${solicitud.estado === 'pendiente' ? 'badge-pendiente' : (solicitud.estado === 'aprobada' ? 'badge-activo' : 'badge-inactivo')}">${solicitud.estado}</span></td>
                <td>
                    ${solicitud.estado === 'pendiente' ? `
                        <button class="btn-aprobar" data-id="${solicitud.id}" data-nombre="${solicitud.marca}"><i class='bx bx-check'></i> Aprobar</button>
                        <button class="btn-rechazar" data-id="${solicitud.id}"><i class='bx bx-x'></i> Rechazar</button>
                    ` : '<span>-</span>'}
                </td>
            `;
            tbody.appendChild(tr);
        });

        asignarEventosBotones(tbody);

    } catch (error) {
        console.error('Error al cargar la tabla de solicitudes:', error);
        tbody.innerHTML = `<tr><td colspan="6">Error de conexión al cargar solicitudes</td></tr>`;
    }
}

// permite al administrador buscar por nombre de artesano o estado de la peticion
function prepararFiltrosSolicitudes() {
    const inputBusqueda = document.getElementById('busqueda-solicitudes');
    const tbody = document.getElementById('tabla-solicitudes-body');
    
    if (inputBusqueda && tbody) {
        inputBusqueda.addEventListener('input', (e) => {
            const termino = e.target.value.toLowerCase();
            const filas = tbody.querySelectorAll('tr');
            filas.forEach(fila => {
                const texto = fila.innerText.toLowerCase();
                fila.style.display = texto.includes(termino) ? '' : 'none';
            });
        });
    }
}

// Función que le da vida a los botones
function asignarEventosBotones(tbody) {
    tbody.addEventListener('click', async (evento) => {
        const btn = evento.target.closest('button');
        if (!btn) return;

        const id = btn.getAttribute('data-id');
        const parametros = new URLSearchParams();
        parametros.append('id', id);

        if (btn.classList.contains('btn-aprobar')) {
            const nombre = btn.getAttribute('data-nombre');
            parametros.append('accion', 'aprobar');
            parametros.append('nombre', nombre);
            
            if (confirm(`¿Aprobar y crear la categoría "${nombre}"?`)) {
                await procesarSolicitud(parametros);
            }
        } else if (btn.classList.contains('btn-rechazar')) {
            parametros.append('accion', 'rechazar');
            
            if (confirm('¿Rechazar esta solicitud?')) {
                await procesarSolicitud(parametros);
            }
        }
    });
}

async function procesarSolicitud(parametros) {
    try {
        const respuesta = await fetch('solicitudes-proveedor', {
            method: 'POST',
            body: parametros
        });
        
        if (respuesta.ok) {
            cargarListaSolicitudes(); // Recargar tabla
        } else {
            alert('Error al procesar la solicitud en el servidor');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error de conexión');
    }
}