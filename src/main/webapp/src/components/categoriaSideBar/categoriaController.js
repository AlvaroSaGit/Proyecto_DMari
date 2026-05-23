// importamos el servicio de ui
import { cargarComponente } from '../../services/uiService.js';
// importamos el enrutador para poder viajar entre paginas
import { navegarA } from '../../router/router.js';

// funcion para inyectar las categorias al inicio
export async function inicializarCategoria() {
    await cargarComponente('contenedor-sidebar-categoria', './src/components/categoriaSideBar/categoriaSidebar.html');
    
    // preparamos los botones de cerrar
    const btnCerrar = document.getElementById('btn-cerrar-categoria');
    const overlay = document.getElementById('overlay-categoria');
    
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarCategoria);
    if (overlay) overlay.addEventListener('click', cerrarCategoria);
    
    // preparamos los botones de cada categoria
    const botonesCategoria = document.querySelectorAll('.btn-categoria-item');
    
    // recorremos los botones con un bucle clasico
    for (let i = 0; i < botonesCategoria.length; i++) {
        botonesCategoria[i].addEventListener('click', function() {
            // le quitamos la clase activo a todos primero
            for (let j = 0; j < botonesCategoria.length; j++) {
                botonesCategoria[j].classList.remove('activo');
            }
            
            // se la ponemos solo al que acabamos de clickear
            this.classList.add('activo');
            
            // Extraemos la categoria limpiando espacios fantasma y comparamos en minusculas por seguridad
            const categoriaRaw = (this.getAttribute('data-categoria') || '').trim();
            const esBotonTodo = categoriaRaw.toLowerCase() === 'todo' || categoriaRaw === '';
            
            // Transformamos "reposteria" a "Reposteria" para que coincida exactamente con MySQL
            const categoriaFormateada = categoriaRaw.charAt(0).toUpperCase() + categoriaRaw.slice(1);
            
            // revisamos si el usuario ya se encuentra en la pantalla del catalogo
            const hashActual = window.location.hash.replace(/^#\/?/, '');
            
            if (hashActual === 'catalogo') {
                // si ya estamos en el catalogo, aplicamos el filtro al instante sin recargar la pagina
                // Usamos la funcion global para evitar el error de doble instancia de modulos ES6
                if (typeof window.aplicarFiltroCatalogo === 'function') {
                    if (esBotonTodo) {
                        window.aplicarFiltroCatalogo('', 'general');
                    } else {
                        window.aplicarFiltroCatalogo(categoriaFormateada, 'categoria');
                    }
                } else {
                    // Respaldo por si la funcion no cargo a tiempo
                    window.location.reload();
                }
            } else {
                // si estamos en otra pantalla (como el inicio), guardamos el filtro en memoria
                // y le ordenamos al router que nos lleve al catalogo
                sessionStorage.setItem('filtroCategoriaSidebar', esBotonTodo ? '' : categoriaFormateada);
                navegarA('catalogo');
            }
            
            // cerramos el panel automaticamente
            cerrarCategoria();
        });
    }
}

// funcion para mostrar el sidebar agregando clases css
export function abrirCategoria() {
    const sidebar = document.getElementById('sidebar-categoria');
    const overlay = document.getElementById('overlay-categoria');
    
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

// funcion para ocultar el sidebar
export function cerrarCategoria() {
    const sidebar = document.getElementById('sidebar-categoria');
    const overlay = document.getElementById('overlay-categoria');
    
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}