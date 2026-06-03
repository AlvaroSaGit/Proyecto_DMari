import { cargarComponente } from '../../../services/uiService.js';
import { crearCategoria, actualizarCategoria, obtenerCategorias, cambiarEstadoCategoria } from '../../../services/categoriaService.js';

// variable global para saber si estamos creando o editando
let categoriaEditandoId = null;

/**
 * funcion principal de inicializacion para la vista de gestion de categorias.
 * actua como el punto de entrada (entry point) cuando el enrutador detecta la url '#admin-categorias'.
 * se encarga de:
 * 1. inyectar el html en el espacio principal.
 * 2. solicitar al backend la lista de categorias.
 * 3. preparar la ventana emergente (modal) para escuchar los clics.
 */
export async function cargarVistaAdminCategorias() {
    // usamos '?t=' + gettime() para destruir la cache del navegador. 
    // esto obliga al navegador a descargar la ultima version del html, resolviendo el problema de carga.
    await cargarComponente('component-main', './src/views/Administrador/categorias/adminCategorias.html?t=' + new Date().getTime());
    
    cargarListaCategorias();
    configurarModalCategorias();
}

/**
 * se comunica con el backend de java (servlet 'categorias') para obtener la lista
 * de categorias maestras y las dibuja una por una en la tabla html dinamicamente.
 */
async function cargarListaCategorias() {
    const tbody = document.getElementById('tabla-categorias-body');
    // Proteccion: Si la tabla no existe en el DOM, abortamos la ejecucion para no generar errores.
    if (!tbody) return;

    try {
        // usamos el servicio para traer los datos limpios
        const categorias = await obtenerCategorias(true);

        // Vaciamos el mensaje temporal de "cargando..."
        tbody.innerHTML = '';
        
        // Validacion: Si la base de datos responde pero esta vacia, mostramos un aviso amigable
        if(categorias.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No hay categorias registradas en el sistema</td></tr>';
            return;
        }

        // Recorremos el arreglo (Array) que nos mando Java y creamos una fila (tr) por cada categoria
        categorias.forEach(cat => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#00${cat.id || cat.id_categoria_pk}</td>
                <td><strong>${cat.nombre}</strong></td>
                <td>${cat.descripcion || 'Sin descripcion'}</td>
                <td><span class="badge-estado ${cat.estado_activo !== false ? 'badge-activo' : 'badge-inactivo'}">${cat.estado_activo !== false ? 'ACTIVO' : 'INACTIVO'}</span></td>
                <td>
                    <!-- Guardamos los atributos en el boton (data-) para facilitar una futura edicion -->
                    <button class="btn-editar" data-id="${cat.id || cat.id_categoria_pk}" data-nombre="${cat.nombre}" data-descripcion="${cat.descripcion || ''}"><i class='bx bx-edit'></i> Editar</button>
                    <button class="btn-estado" data-id="${cat.id || cat.id_categoria_pk}" data-estado="${cat.estado_activo !== false}"><i class='bx bx-refresh'></i> ${cat.estado_activo !== false ? 'Pausar' : 'Activar'}</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        // En caso de que el Tomcat este apagado o haya un error de red
        console.error('Error al cargar categorias:', error);
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color: red;">Error de conexion con la Base de Datos</td></tr>';
    }
}

/**
 * Mapea el DOM (Document Object Model) para enlazar los botones fisicos del HTML
 * con funciones de JavaScript, otorgandoles el poder de abrir, cerrar y guardar en el formulario (Modal).
 */
function configurarModalCategorias() {
    const btnNueva = document.getElementById('btn-nueva-categoria');
    const modal = document.getElementById('modal-categoria');
    const btnCerrar = document.getElementById('btn-cerrar-modal-cat');
    const btnCancelar = document.getElementById('btn-cancelar-modal-cat');
    const formCategoria = document.getElementById('form-categoria');
    const tbody = document.getElementById('tabla-categorias-body');

    // evento: mostrar la ventana modal vacia al darle nueva categoria
    if (btnNueva) {
        btnNueva.addEventListener('click', () => { 
            categoriaEditandoId = null; // reseteamos la variable
            document.getElementById('modal-titulo-cat').innerText = 'Nueva Categoria';
            if (formCategoria) formCategoria.reset(); // limpiamos la basura de usos anteriores
            modal.classList.remove('oculto'); 
        });
    }
    
    // funcion auxiliar: agrega la clase css que vuelve invisible la ventana
    const cerrarModal = () => modal.classList.add('oculto');
    // asignamos esta funcion a los botones de salir (la x y cancelar)
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);

    // delegacion de eventos: escuchar clics en los botones editar y estado de la tabla
    if (tbody) {
        tbody.addEventListener('click', async (evento) => {
            const btnClic = evento.target.closest('button');
            if (!btnClic) return;

            if (btnClic.classList.contains('btn-editar')) {
                categoriaEditandoId = btnClic.getAttribute('data-id');
                
                // rellenamos el formulario con los datos guardados en el boton
                document.getElementById('cat-nombre').value = btnClic.getAttribute('data-nombre');
                document.getElementById('cat-descripcion').value = btnClic.getAttribute('data-descripcion');
                
                document.getElementById('modal-titulo-cat').innerText = 'Editar Categoria';
                modal.classList.remove('oculto');
                
            } else if (btnClic.classList.contains('btn-estado')) {
                // logica para cambiar el estado (activar / inactivar)
                const id = btnClic.getAttribute('data-id');
                const estadoActual = btnClic.getAttribute('data-estado') === 'true';
                
                if (confirm(`¿seguro que deseas ${estadoActual ? 'inactivar' : 'activar'} esta categoria?`)) {
                    // ejecutamos el cambio mediante el servicio
                    const exito = await cambiarEstadoCategoria(id, !estadoActual);
                    if (exito) {
                        cargarListaCategorias(); // recargamos la tabla
                    } else {
                        alert('error al intentar cambiar el estado en el servidor.');
                    }
                }
            }
        });
    }

    // evento principal: que pasa cuando el admin presiona el boton verde de guardar categoria
    if (formCategoria) {
        formCategoria.addEventListener('submit', async (e) => {
            e.preventDefault(); // detenemos la recarga automatica de la pagina

            // 1. capturamos los textos que escribio el usuario
            const nombre = document.getElementById('cat-nombre').value;
            const descripcion = document.getElementById('cat-descripcion').value;

            // validacion de seguridad modulo 5: regex para texto sin simbolos
            const regexTexto = /^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s]{3,40}$/;
            if (!regexTexto.test(nombre)) {
                alert('error: el nombre de la categoria debe tener entre 3 y 40 caracteres (solo letras).');
                return;
            }

            try {
                let exito = false;

                // 2. usamos los servicios inyectados segun el modo
                if (categoriaEditandoId) {
                    exito = await actualizarCategoria(categoriaEditandoId, nombre, descripcion);
                } else {
                    exito = await crearCategoria(nombre, descripcion);
                }

                if (exito) {
                    alert('¡categoria guardada con exito!');
                    cerrarModal(); // ocultamos la ventana
                    cargarListaCategorias(); // refrescamos la tabla
                } else { 
                    alert('error del servidor al intentar guardar.'); 
                }
            } catch (error) { 
                console.error('error en la operacion de categorias:', error); 
            }
        });
    }
}