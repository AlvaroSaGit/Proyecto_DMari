import { cargarComponente } from '../../../services/uiService.js';
import { navegarA } from '../../../router/router.js';

/**
 * Inicializa el panel lateral administrativo para el Proveedor.
 */
export async function inicializarProveedor() {
    // Cargamos el HTML del sidebar en el contenedor específico (debe existir en el index.html)
    // ¡CORRECCIÓN DEFINITIVA! Apuntamos a la ruta completa desde la raíz del proyecto web.
    await cargarComponente('contenedor-sidebar-proveedor', './src/views/Proveedor/sidebar/proveedorSidebar.html');
    
    const btnCerrar = document.getElementById('btn-cerrar-prov');
    const overlay = document.getElementById('overlay-prov');
    
    if (btnCerrar) btnCerrar.onclick = cerrarProveedor;
    if (overlay) overlay.onclick = cerrarProveedor;

    // Configurar navegación de los items del menú
    const items = document.querySelectorAll('.prov-item');
    items.forEach(item => {
        item.onclick = (e) => {
            const destino = e.currentTarget.getAttribute('data-go');
            if (destino) {
                navegarA(destino);
                cerrarProveedor();
            }
        };
    });
}

/**
 * Engancha el evento de apertura al botón del header principal.
 * Debe ser llamado por el enrutador DESPUÉS de que el header se haya adaptado.
 */
export function conectarBotonHeaderProveedor() {
    // El botón 'btn-usuario-perfil' es reutilizado en el modo Dashboard para abrir los paneles.
    const btnAbrirPanel = document.getElementById('btn-usuario-perfil');
    if (btnAbrirPanel) btnAbrirPanel.onclick = abrirProveedor;
}

export function abrirProveedor() {
    const sidebar = document.getElementById('sidebar-proveedor');
    const overlay = document.getElementById('overlay-prov');
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

export function cerrarProveedor() {
    const sidebar = document.getElementById('sidebar-proveedor');
    const overlay = document.getElementById('overlay-prov');
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}