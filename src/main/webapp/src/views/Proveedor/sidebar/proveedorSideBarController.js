// importamos el servicio de ui para inyectar el html del sidebar
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador para navegar entre vistas sin recargar la pagina
import { navegarA } from '../../../router/router.js';
// importamos el controlador de solicitud de categoria para abrir el modal directamente
import { cargarVistaSolicitudCategoria } from '../productos/solicitudProveedorController.js';

/**
 * inicializa el panel lateral del proveedor.
 * carga el html del sidebar e inyecta los eventos de navegacion.
 */
export async function inicializarProveedor() {
    // cargamos el html del sidebar en el contenedor que ya existe en el index.html
    await cargarComponente('contenedor-sidebar-proveedor', './src/views/Proveedor/sidebar/proveedorSideBar.html');
    
    // buscamos el boton de cerrar y el overlay de fondo oscuro
    const btnCerrar = document.getElementById('btn-cerrar-prov');
    const overlay = document.getElementById('overlay-prov');
    
    // al hacer clic en cerrar o en el fondo oscuro, cerramos el panel
    if (btnCerrar) btnCerrar.onclick = cerrarProveedor;
    if (overlay) overlay.onclick = cerrarProveedor;

    // configuramos la navegacion de cada item del menu del proveedor
    // usamos querySelectorAll para agarrar todos los botones de una sola vez
    const items = document.querySelectorAll('.prov-item[data-go]');
    items.forEach(item => {
        item.onclick = (e) => {
            // leemos el destino del atributo data-go del elemento clickeado
            const destino = e.currentTarget.getAttribute('data-go');
            
            if (destino === 'solicitar-categoria') {
                // caso especial: solicitar categoria abre un modal flotante, no navega
                // cerramos el sidebar primero para que no tape el modal
                cerrarProveedor();
                // abrimos el formulario de solicitud de categoria como ventana emergente
                cargarVistaSolicitudCategoria();
            } else if (destino) {
                // para las demas rutas, le decimos al router que cambie la vista
                navegarA(destino);
                // cerramos el panel despues de seleccionar una opcion
                cerrarProveedor();
            }
        };
    });

    // configuramos el boton de cerrar sesion dentro del sidebar
    const btnCerrarSesion = document.getElementById('btn-cerrar-sesion');
    if (btnCerrarSesion) {
        btnCerrarSesion.addEventListener('click', async () => {
            // 1. avisamos al backend que destruya la sesion en el servidor java
            try { await fetch('logout'); } catch(e) {}
            
            // 2. borramos el rol guardado en la memoria temporal del navegador
            sessionStorage.removeItem('rolUsuario');
            
            // 3. enviamos al usuario a la pagina de inicio publica
            window.location.hash = 'inicio';
            
            // 4. forzamos una recarga completa para limpiar cualquier dato residual en memoria
            window.location.reload();
        });
    }
}

/**
 * engancha el evento de apertura al boton del header principal.
 * debe ser llamado por el enrutador despues de que el header se haya adaptado.
 */
export function conectarBotonHeaderProveedor() {
    // buscamos el boton de usuario del header para asignarle la accion de abrir el drawer
    const btnAbrirPanel = document.getElementById('btn-usuario-perfil');
    if (btnAbrirPanel) btnAbrirPanel.onclick = abrirProveedor;
}

// abre el panel deslizante del proveedor agregando la clase 'activo'
export function abrirProveedor() {
    const sidebar = document.getElementById('sidebar-proveedor');
    const overlay = document.getElementById('overlay-prov');
    // la clase 'activo' hace que el css mueva el sidebar de left:-350px a left:0
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

// cierra el panel deslizante del proveedor removiendo la clase 'activo'
export function cerrarProveedor() {
    const sidebar = document.getElementById('sidebar-proveedor');
    const overlay = document.getElementById('overlay-prov');
    // removemos la clase para que el css devuelva el sidebar fuera de pantalla
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}