// importamos la herramienta para inyectar el html en la pantalla
import { cargarComponente } from '../../../services/uiService.js';

// variable global para recordar a que usuario le estamos cambiando los permisos
let usuarioEditandoId = null;

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
                // hacemos la peticion al nuevo servlet 'usuarios' que programaremos en java
                const respuesta = await fetch('usuarios', { method: 'POST', body: parametros });
                
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

    // pedimos los datos a java al momento de abrir la pantalla
    cargarListaUsuarios();
}

// consulta la base de datos y dibuja las filas de la tabla
async function cargarListaUsuarios() {
    const tbody = document.getElementById('tabla-usuarios-body');
    if (!tbody) return;

    try {
        // peticion get al backend para traer a todo el personal registrado
        const respuesta = await fetch('usuarios');
        if (!respuesta.ok) throw new Error('fallo la peticion al servidor');

        const usuarios = await respuesta.json();
        tbody.innerHTML = '';

        if (usuarios.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">no hay usuarios registrados</td></tr>';
            return;
        }

        // recorremos el arreglo y dibujamos a cada persona
        usuarios.forEach(usr => {
            const tr = document.createElement('tr');
            
            // traduccion de roles numericos a texto amigable
            let rolTexto = 'cliente';
            if (usr.idRol === 1) rolTexto = 'administrador';
            if (usr.idRol === 3) rolTexto = 'repartidor';
            if (usr.idRol === 4) rolTexto = 'proveedor';

            tr.innerHTML = `
                <td>#00${usr.idUsuario}</td>
                <td><strong>${usr.nombre} ${usr.apellido}</strong></td>
                <td>${usr.correo || 'Sin correo asociado'}</td>
                <td><span style="font-size: 0.75rem; color: #666; background-color: #f0f0f0; padding: 2px 6px; border-radius: 10px; text-transform: uppercase; font-weight: bold;">${rolTexto}</span></td>
                <td><span class="badge-estado ${usr.estadoCuenta ? 'badge-activo' : 'badge-inactivo'}">${usr.estadoCuenta ? 'ACTIVO' : 'BLOQUEADO'}</span></td>
                <td>
                    <button class="btn-editar" data-id="${usr.idUsuario}" data-nombre="${usr.nombre} ${usr.apellido}" data-rol="${usr.idRol}" data-estado="${usr.estadoCuenta}"><i class='bx bx-edit'></i> Editar Permisos</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('error al cargar usuarios:', error);
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red;">error al cargar los datos</td></tr>';
    }
}