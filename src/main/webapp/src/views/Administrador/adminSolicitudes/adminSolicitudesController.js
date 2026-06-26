// Dependencias de UI
import { cargarComponente } from '../../../services/uiService.js';

// Inicializa la vista de gestión de solicitudes
export async function cargarVistaAdminSolicitudes() {
    await cargarComponente('component-main', './src/views/Administrador/adminSolicitudes/adminSolicitudes.html');
    
    cargarListaSolicitudes();
    cargarListaSolicitudesCategorias();
    
    // inicializamos el buscador de solicitudes para identificar artesanos rapidamente
    prepararFiltrosSolicitudes();
}

// Obtiene y renderiza el listado de solicitudes de proveedores
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
                        <button class="btn-aprobar btn-aprobar-prov" data-id="${solicitud.id}" data-nombre="${solicitud.marca}"><i class='bx bx-check'></i> Aprobar</button>
                        <button class="btn-rechazar btn-rechazar-prov" data-id="${solicitud.id}"><i class='bx bx-x'></i> Rechazar</button>
                    ` : '<span>-</span>'}
                </td>
            `;
            tbody.appendChild(tr);
        });

        asignarEventosBotonesProveedor(tbody);

    } catch (error) {
        console.error('Error al cargar la tabla de solicitudes:', error);
        tbody.innerHTML = `<tr><td colspan="7">Error de conexión al cargar solicitudes</td></tr>`;
    }
}

// Obtiene y renderiza el listado de solicitudes de categorias
async function cargarListaSolicitudesCategorias() {
    const tbody = document.getElementById('tabla-solicitudes-cat-body');
    if (!tbody) return;

    try {
        const respuesta = await fetch('solicitudes-categorias');
        if (!respuesta.ok) throw new Error('Error al obtener solicitudes categorias');
        
        const solicitudes = await respuesta.json();
        
        tbody.innerHTML = ''; 

        if (solicitudes.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No hay sugerencias de categorías pendientes</td></tr>';
            return;
        }

        solicitudes.forEach(solicitud => {
            const estadoLower = solicitud.estado_solicitud.toLowerCase();
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${solicitud.id_solicitud_pk}</td>
                <td><strong>${solicitud.nombre_proveedor}</strong></td>
                <td><strong>${solicitud.nombre_sugerido}</strong></td>
                <td>${solicitud.justificacion}</td>
                <td>${solicitud.fecha_creacion.split('.')[0]}</td>
                <td><span class="badge-estado ${estadoLower === 'pendiente' ? 'badge-pendiente' : (estadoLower === 'aprobada' ? 'badge-activo' : 'badge-inactivo')}">${solicitud.estado_solicitud}</span></td>
                <td>
                    ${estadoLower === 'pendiente' ? `
                        <button class="btn-aprobar btn-aprobar-cat" data-id="${solicitud.id_solicitud_pk}" data-nombre="${solicitud.nombre_sugerido}"><i class='bx bx-check'></i> Aprobar</button>
                        <button class="btn-rechazar btn-rechazar-cat" data-id="${solicitud.id_solicitud_pk}"><i class='bx bx-x'></i> Rechazar</button>
                    ` : '<span>-</span>'}
                </td>
            `;
            tbody.appendChild(tr);
        });

        asignarEventosBotonesCategoria(tbody);

    } catch (error) {
        console.error('Error al cargar la tabla de solicitudes de categorias:', error);
        tbody.innerHTML = `<tr><td colspan="7">Error de conexión al cargar solicitudes</td></tr>`;
    }
}

// permite al administrador buscar por nombre de artesano o estado de la peticion
function prepararFiltrosSolicitudes() {
    const inputBusqueda = document.getElementById('busqueda-solicitudes');
    const tbodyProv = document.getElementById('tabla-solicitudes-body');
    const tbodyCat = document.getElementById('tabla-solicitudes-cat-body');
    
    if (inputBusqueda) {
        inputBusqueda.addEventListener('input', (e) => {
            const termino = e.target.value.toLowerCase();
            if (tbodyProv) {
                const filasProv = tbodyProv.querySelectorAll('tr');
                filasProv.forEach(fila => {
                    const texto = fila.innerText.toLowerCase();
                    fila.style.display = texto.includes(termino) ? '' : 'none';
                });
            }
            if (tbodyCat) {
                const filasCat = tbodyCat.querySelectorAll('tr');
                filasCat.forEach(fila => {
                    const texto = fila.innerText.toLowerCase();
                    fila.style.display = texto.includes(termino) ? '' : 'none';
                });
            }
        });
    }
}

// Función que le da vida a los botones de proveedor
function asignarEventosBotonesProveedor(tbody) {
    tbody.addEventListener('click', async (evento) => {
        const btn = evento.target.closest('button');
        if (!btn) return;

        const id = btn.getAttribute('data-id');
        const parametros = new URLSearchParams();
        parametros.append('id', id);

        if (btn.classList.contains('btn-aprobar-prov')) {
            const nombre = btn.getAttribute('data-nombre');
            parametros.append('accion', 'aprobar');
            parametros.append('nombre', nombre);
            
            if (confirm(`¿Aprobar al proveedor "${nombre}"?`)) {
                await procesarSolicitud('solicitudes-proveedor', parametros, cargarListaSolicitudes);
            }
        } else if (btn.classList.contains('btn-rechazar-prov')) {
            parametros.append('accion', 'rechazar');
            
            if (confirm('¿Rechazar esta solicitud de proveedor?')) {
                await procesarSolicitud('solicitudes-proveedor', parametros, cargarListaSolicitudes);
            }
        }
    });
}

// Función que le da vida a los botones de categoria
function asignarEventosBotonesCategoria(tbody) {
    tbody.addEventListener('click', async (evento) => {
        const btn = evento.target.closest('button');
        if (!btn) return;

        const id = btn.getAttribute('data-id');
        const parametros = new URLSearchParams();
        parametros.append('id', id);

        if (btn.classList.contains('btn-aprobar-cat')) {
            const nombre = btn.getAttribute('data-nombre');
            parametros.append('accion', 'aprobar');
            parametros.append('nombre', nombre);
            
            if (confirm(`¿Aprobar y crear la nueva categoría "${nombre}"?`)) {
                await procesarSolicitud('solicitudes-categorias', parametros, cargarListaSolicitudesCategorias);
            }
        } else if (btn.classList.contains('btn-rechazar-cat')) {
            parametros.append('accion', 'rechazar');
            
            if (confirm('¿Rechazar esta solicitud de categoría?')) {
                await procesarSolicitud('solicitudes-categorias', parametros, cargarListaSolicitudesCategorias);
            }
        }
    });
}

async function procesarSolicitud(url, parametros, callbackRecarga) {
    try {
        const respuesta = await fetch(url, {
            method: 'POST',
            body: parametros
        });
        
        if (respuesta.ok) {
            callbackRecarga(); // Recargar tabla
        } else {
            alert('Error al procesar la solicitud en el servidor');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error de conexión');
    }
}