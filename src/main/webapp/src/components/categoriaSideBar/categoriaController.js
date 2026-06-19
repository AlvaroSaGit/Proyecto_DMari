// importamos el servicio de ui
import { cargarComponente } from '../../services/uiService.js';
// importamos el enrutador para poder viajar entre paginas
import { navegarA } from '../../router/router.js';

/**
 * funcion principal para inyectar y configurar el panel lateral de categorias.
 * inyecta el html dinamicamente y activa los escuchadores de los botones.
 */
export async function inicializarCategoria() {
    // pedimos al servicio ui que cargue el archivo html dentro del div especificado
    // Corregimos la ruta para que sea absoluta desde la raíz del proyecto web.
    await cargarComponente('contenedor-sidebar-categoria', './src/components/categoriaSideBar/categoriaSidebar.html');
    
    // capturamos los elementos visuales de cierre desde el dom
    const btnCerrar = document.getElementById('btn-cerrar-categoria');
    const overlay = document.getElementById('overlay-categoria');
    
    // asignamos el evento click para cerrar el panel si los elementos existen
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarCategoria);
    if (overlay) overlay.addEventListener('click', cerrarCategoria);
    
    // capturamos todos los botones que representan una categoria filtrable
    const botonesCategoria = document.querySelectorAll('.btn-categoria-item');
    
    // recorremos cada boton encontrado en el html para asignarle su respectiva logica
    for (let i = 0; i < botonesCategoria.length; i++) {
        botonesCategoria[i].addEventListener('click', function() {
            // limpieza visual: le quitamos la clase activo a todos los botones primero
            for (let j = 0; j < botonesCategoria.length; j++) {
                botonesCategoria[j].classList.remove('activo');
            }
            
            // iluminacion visual: se la ponemos unicamente al boton recien clickeado
            this.classList.add('activo');
            
            // extraemos la categoria limpiando espacios fantasma y comparamos en minusculas por seguridad
            const categoriaRaw = (this.getAttribute('data-categoria') || '').trim();
            const esBotonTodo = categoriaRaw.toLowerCase() === 'todo' || categoriaRaw === '';
            
            // transformamos la primera letra a mayuscula (ej: reposteria -> Reposteria) 
            // esto es vital para que coincida exactamente con el texto de mysql
            const categoriaFormateada = categoriaRaw.charAt(0).toUpperCase() + categoriaRaw.slice(1);
            
            // revisamos en que vista se encuentra el usuario actualmente leyendo la url
            const hashActual = window.location.hash.replace(/^#\/?/, '');
            
            if (hashActual === 'catalogo') {
                // si ya estamos en el catalogo, aplicamos el filtro al instante modificando el dom
                // usamos la funcion global inyectada en window para evitar errores de modulos circulares
                if (typeof window.aplicarFiltroCatalogo === 'function') {
                    if (esBotonTodo) {
                        window.aplicarFiltroCatalogo('', 'general');
                    } else {
                        window.aplicarFiltroCatalogo(categoriaFormateada, 'categoria');
                    }
                } else {
                    // dow.location.reload();
                }
            } else {
                // si estamos en otra pantalla (inicio, perfil), guardamos el filtro deseado en sessionstorage
                // luego le ordenamos al router que nos redirija forzosamente hacia el catalogo
                sessionStorage.setItem('filtroCategoriaSidebar', esBotonTodo ? '' : categoriaFormateada);
                navegarA('catalogo');
            }
            
            // cerramos el panel lateral automaticamente tras elegir una opcion
            cerrarCategoria();
        });
    }
}

/**
 * muestra visualmente el panel lateral de categorias.
 * le inyecta la clase css activo al menu y al fondo oscuro.
 */
export function abrirCategoria() {
    const sidebar = document.getElementById('sidebar-categoria');
    const overlay = document.getElementById('overlay-categoria');
    
    // validamos que existan en el html antes de modificar sus clases
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

/**
 * oculta el panel lateral de categorias de la pantalla.
 * le quita la clase css activo para esconder los elementos animadamente.
 */
export function cerrarCategoria() {
    const sidebar = document.getElementById('sidebar-categoria');
    const overlay = document.getElementById('overlay-categoria');
    
    // validamos que existan en el html antes de remover sus clases
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}