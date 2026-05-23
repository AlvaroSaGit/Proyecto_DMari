// importamos el servicio de ui
import { cargarComponente } from '../../services/uiService.js';
// importamos la funcion de filtros del catalogo
import { aplicarFiltroInteligente } from '../../views/Cliente/catalogo/productosController.js';
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
            
            const categoriaSeleccionada = this.getAttribute('data-categoria');
            
            // revisamos si el usuario ya se encuentra en la pantalla del catalogo
            const hashActual = window.location.hash.replace('#', '');
            
            if (hashActual === 'catalogo') {
                // si ya estamos en el catalogo, aplicamos el filtro al instante sin recargar la pagina
                if (categoriaSeleccionada === 'todo') {
                    aplicarFiltroInteligente('', 'general');
                } else {
                    aplicarFiltroInteligente(categoriaSeleccionada, 'categoria');
                }
            } else {
                // si estamos en otra pantalla (como el inicio), guardamos el filtro en memoria
                // y le ordenamos al router que nos lleve al catalogo
                sessionStorage.setItem('filtroCategoriaSidebar', categoriaSeleccionada === 'todo' ? '' : categoriaSeleccionada);
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