import { cargarComponente } from '../../../services/uiService.js';

// Variable global para saber si estamos creando o editando
let categoriaEditandoId = null;

/**
 * Funcion principal de inicializacion para la vista de "Gestion de Categorias".
 * Actua como el punto de entrada (Entry Point) cuando el enrutador detecta la URL '#admin-categorias'.
 * Se encarga de:
 * 1. Inyectar el HTML en el espacio principal.
 * 2. Solicitar al backend la lista de categorias.
 * 3. Preparar la ventana emergente (Modal) para escuchar los clics.
 */
export async function cargarVistaAdminCategorias() {
    // Usamos '?t=' + getTime() para destruir la cache del navegador. 
    // Esto obliga al navegador a descargar la ultima version del HTML, resolviendo el problema de que no te cargue.
    await cargarComponente('component-main', './src/views/Administrador/categorias/adminCategorias.html?t=' + new Date().getTime());
    
    cargarListaCategorias();
    configurarModalCategorias();
}

/**
 * Se comunica con el backend de Java (Servlet 'categorias') para obtener la lista
 * de categorias maestras y las dibuja una por una en la tabla HTML dinamicamente.
 */
async function cargarListaCategorias() {
    const tbody = document.getElementById('tabla-categorias-body');
    // Proteccion: Si la tabla no existe en el DOM, abortamos la ejecucion para no generar errores.
    if (!tbody) return;

    try {
        // Hacemos una peticion GET al servidor pidiendo TODAS las categorias (incluso pausadas)
        const respuesta = await fetch('categorias?todas=true');
        const categorias = await respuesta.json();
        
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

    // EVENTO: Mostrar la ventana Modal vacia al darle "Nueva Categoria"
    if (btnNueva) {
        btnNueva.addEventListener('click', () => { 
            categoriaEditandoId = null; // Reseteamos la variable
            document.getElementById('modal-titulo-cat').innerText = 'Nueva Categoria';
            if (formCategoria) formCategoria.reset(); // Limpiamos la basura de usos anteriores
            modal.classList.remove('oculto'); 
        });
    }
    
    // FUNCION AUXILIAR: Agrega la clase CSS que vuelve invisible la ventana
    const cerrarModal = () => modal.classList.add('oculto');
    // Asignamos esta funcion a los botones de Salir (La "X" y "Cancelar")
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener('click', cerrarModal);

    // DELEGACION DE EVENTOS: Escuchar clics en los botones "Editar" de la tabla
    if (tbody) {
        tbody.addEventListener('click', async (evento) => {
            const btnClic = evento.target.closest('button');
            if (!btnClic) return;

            if (btnClic.classList.contains('btn-editar')) {
                categoriaEditandoId = btnClic.getAttribute('data-id');
                
                // Rellenamos el formulario con los datos guardados en el boton
                document.getElementById('cat-nombre').value = btnClic.getAttribute('data-nombre');
                document.getElementById('cat-descripcion').value = btnClic.getAttribute('data-descripcion');
                
                document.getElementById('modal-titulo-cat').innerText = 'Editar Categoria';
                modal.classList.remove('oculto');
                
            } else if (btnClic.classList.contains('btn-estado')) {
                // LOGICA PARA CAMBIAR EL ESTADO (ACTIVAR / INACTIVAR)
                const id = btnClic.getAttribute('data-id');
                const estadoActual = btnClic.getAttribute('data-estado') === 'true';
                
                if (confirm(`¿Seguro que deseas ${estadoActual ? 'inactivar' : 'activar'} esta categoria?`)) {
                    const parametros = new URLSearchParams();
                    parametros.append('accion', 'cambiar_estado'); // Bandera para que Java lo identifique
                    parametros.append('id', id);
                    parametros.append('estado', (!estadoActual).toString()); // Invertimos el estado actual

                    try {
                        const respuesta = await fetch('categorias', { method: 'POST', body: parametros });
                        if (respuesta.ok) {
                            cargarListaCategorias(); // Recargamos la tabla visualmente
                        } else {
                            alert('Error al intentar cambiar el estado en el servidor.');
                        }
                    } catch (error) { console.error('Error de red:', error); }
                }
            }
        });
    }

    // EVENTO PRINCIPAL: Que pasa cuando el admin presiona el boton verde de "Guardar Categoria"
    if (formCategoria) {
        formCategoria.addEventListener('submit', async (e) => {
            e.preventDefault(); // Detenemos la recarga automatica de la pagina

            // 1. Extraemos los textos que escribio el usuario
            const nombre = document.getElementById('cat-nombre').value;
            const descripcion = document.getElementById('cat-descripcion').value;

            // 2. Empacamos los datos en formato estandar de formulario
            const parametros = new URLSearchParams();
            parametros.append('nombre', nombre);
            parametros.append('descripcion', descripcion);
            
            // Si estamos editando, mandamos el ID para que Java sepa que debe hacer un UPDATE
            if (categoriaEditandoId) {
                parametros.append('id', categoriaEditandoId);
            }

            try {
                // 3. Enviamos los datos al mismo Servlet pero usando POST (crear)
                const respuesta = await fetch('categorias', { method: 'POST', body: parametros });

                if (respuesta.ok) {
                    alert('¡Categoria creada con exito!');
                    cerrarModal(); // Ocultamos la ventana
                    cargarListaCategorias(); // Volvemos a consultar la Base de Datos para ver la nueva fila
                } else { alert('Error del servidor al intentar guardar.'); }
            } catch (error) { console.error('Error de peticion POST:', error); }
        });
    }
}