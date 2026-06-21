// importamos la herramienta para inyectar el html en la pantalla
import { cargarComponente } from '../../../services/uiService.js';
// importamos el servicio de usuarios para centralizar la data y la logica de filtrado
import { obtenerUsuarios, filtrarUsuarios, actualizarPermisosUsuario } from '../../../services/usuarioService.js';

// variable global para recordar a que usuario le estamos cambiando los permisos
let usuarioEditandoId = null;

// cache para los usuarios cargados para filtrar en memoria sin ir a java
let usuariosCache = [];

// funcion de arranque que el enrutador (router.js) llamara al entrar a esta vista
export async function cargarVistaAdminUsuarios() {
    // CORRECCION: Ajustamos la ruta para que apunte correctamente a la carpeta 'usuarios'
    await cargarComponente('component-main', './src/views/Administrador/usuarios/adminUsuarios.html');
    prepararVistaAdminUsuarios();
}

// mapeamos el dom y configuramos los eventos (clics y envios de formulario)
function prepararVistaAdminUsuarios() {
    const modal = document.getElementById('modal-usuario');
    const btnCerrar = document.getElementById('btn-cerrar-modal-usr');
    const btnCancelar = document.getElementById('btn-cancelar-modal-usr');
    const form = document.getElementById('form-usuario');

    // funcion auxiliar para ocultar el modal
    const cerrarModal = () => { if (modal) modal.classList.add('oculto'); };
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);

    // evento: cuando el administrador guarda los nuevos permisos
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault(); // evitamos recargar la pagina

            // atrapamos el rol y el estado seleccionados en los <select>
            const idRol = document.getElementById('usr-rol').value;
            const estado = document.getElementById('usr-estado').value;

            // empaquetamos los datos en formato url (ideal para post simples)
            const parametros = new URLSearchParams();
            parametros.append('id', usuarioEditandoId);
            parametros.append('id_rol', idRol);
            parametros.append('estado', estado);

            try {
                // usamos el servicio para enviar los datos al servidor
                const respuesta = await actualizarPermisosUsuario(parametros);
                
                if (respuesta.ok) {
                    alert('¡permisos del usuario actualizados correctamente!');
                    cerrarModal();
                    cargarListaUsuarios(); // recargamos la tabla para ver los cambios
                } else {
                    alert('error al intentar actualizar los permisos en el servidor.');
                }
            } catch (error) { console.error('error de conexion:', error); }
        });
    }

    // delegacion de eventos: escuchamos los clics en la tabla completa
    const tbody = document.getElementById('tabla-usuarios-body');
    if (tbody) {
        tbody.addEventListener('click', (evento) => {
            const btnClic = evento.target.closest('button');
            if (!btnClic) return;

            if (btnClic.classList.contains('btn-editar')) {
                // modo edicion: extraemos los datos escondidos en el boton y los pasamos al formulario
                usuarioEditandoId = btnClic.getAttribute('data-id');
                document.getElementById('usr-nombre').value = btnClic.getAttribute('data-nombre');
                document.getElementById('usr-rol').value = btnClic.getAttribute('data-rol');
                document.getElementById('usr-estado').value = btnClic.getAttribute('data-estado');

                if (modal) modal.classList.remove('oculto');
            }
        });
    }

    // oidores para los filtros de busqueda
    const inputBusqueda = document.getElementById('busqueda-usuarios');
    const selectRol = document.getElementById('filtro-rol-usuario');
    const selectEstado = document.getElementById('filtro-estado-usuario');

    if (inputBusqueda) inputBusqueda.addEventListener('input', filtrarUsuariosUI);
    if (selectRol) selectRol.addEventListener('change', filtrarUsuariosUI);
    if (selectEstado) selectEstado.addEventListener('change', filtrarUsuariosUI);

    // pedimos los datos a java al momento de abrir la pantalla
    cargarListaUsuarios();
}

// consulta la base de datos y dibuja las filas de la tabla
async function cargarListaUsuarios() {
    const tbody = document.getElementById('tabla-usuarios-body');
    if (!tbody) return;

    try {
        // pedimos los usuarios al servicio
        const usuarios = await obtenerUsuarios();
        dibujarTablaUsuarios(usuarios);

    } catch (error) {
        console.error('error al cargar usuarios:', error);
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red;">error al cargar los datos</td></tr>';
    }
}

// funcion para dibujar las filas basado en un arreglo filtrado
function dibujarTablaUsuarios(lista) {
    const tbody = document.getElementById('tabla-usuarios-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">no se encontraron usuarios</td></tr>';
        return;
    }

    lista.forEach(usr => {
        const tr = document.createElement('tr');
        
        // identificacion visual: colores por rol para saber quien es quien
        let rolTexto = 'cliente';
        let colorBadge = '#3498db'; // azul para clientes
        if (usr.idRol === 1) { rolTexto = 'administrador'; colorBadge = '#2c3e50'; }
        if (usr.idRol === 3) { rolTexto = 'repartidor'; colorBadge = '#2ecc71'; }
        if (usr.idRol === 4) { rolTexto = 'proveedor'; colorBadge = '#e67e22'; }

        tr.innerHTML = `
            <td>#00${usr.idUsuario}</td>
            <td><strong>${usr.nombre} ${usr.apellido}</strong></td>
            <td>${usr.correo || 'Sin correo asociado'}</td>
            <td><span style="font-size: 0.75rem; color: white; background-color: ${colorBadge}; padding: 4px 8px; border-radius: 12px; text-transform: uppercase; font-weight: bold;">${rolTexto}</span></td>
            <td><span class="badge-estado ${usr.estadoCuenta ? 'badge-activo' : 'badge-inactivo'}">${usr.estadoCuenta ? 'ACTIVO' : 'BLOQUEADO'}</span></td>
            <td>
                <button class="btn-editar" data-id="${usr.idUsuario}" data-nombre="${usr.nombre} ${usr.apellido}" data-rol="${usr.idRol}" data-estado="${usr.estadoCuenta}"><i class='bx bx-edit'></i> Editar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// logica de filtrado para el administrador sin tildes
function filtrarUsuariosUI() {
    const termino = document.getElementById('busqueda-usuarios').value.toLowerCase();
    const rolFiltro = document.getElementById('filtro-rol-usuario').value;
    const selectEstado = document.getElementById('filtro-estado-usuario');
    const estadoFiltro = selectEstado ? selectEstado.value : '';

    // le pedimos al servicio que procese el filtro en memoria
    const filtrados = filtrarUsuarios(termino, rolFiltro, estadoFiltro);
    
    // redibujamos la tabla con los resultados
    dibujarTablaUsuarios(filtrados);
}