import { cargarComponente } from '../../../services/uiService.js';
import { navegarA } from '../../../router/router.js';

/**
 * Inicializa el menu lateral (Sidebar) exclusivo para los proveedores.
 * Carga la interfaz visual y enlaza los botones con el sistema de enrutamiento.
 */
export async function inicializarProveedor() {
    // Cargamos el HTML de la sidebar del proveedor (forzando destruccion de cache con ?t=)
    await cargarComponente('contenedor-sidebar-admin', './src/views/Proveedor/sidebar/proveedorSideBar.html?t=' + new Date().getTime());
    
    // Capturamos las referencias a los botones recien inyectados en el DOM
    const btnProductos = document.getElementById('btn-prov-productos');
    const btnPedidos = document.getElementById('btn-prov-pedidos');
    const btnSalir = document.getElementById('btn-nav-salir');
    
    // Asignamos navegacion SPA a los botones usando el enrutador
    if (btnProductos) btnProductos.addEventListener('click', () => navegarA('proveedor-productos'));
    if (btnPedidos) btnPedidos.addEventListener('click', () => navegarA('proveedor-pedidos'));

    // Logica de iluminado de botones
    actualizarEstadoActivo();
    window.addEventListener('hashchange', actualizarEstadoActivo);

    // Logica de cierre de sesion segura
    if (btnSalir) {
        btnSalir.addEventListener('click', async () => {
            try { await fetch('logout'); } catch(e) {}
            sessionStorage.removeItem('rolUsuario');
            window.location.hash = 'inicio';
            window.location.reload();
        });
    }

    /**
     * Lee la URL actual y le aplica la clase CSS 'activo' al boton correspondiente
     * para que el usuario sepa visualmente en que pantalla se encuentra.
     */
    function actualizarEstadoActivo() {
        const hashActual = window.location.hash || '#proveedor-productos';
        
        // Limpiamos todos los botones
        document.querySelectorAll('.admin-nav-item').forEach(btn => btn.classList.remove('activo'));

        // Encendemos el boton correcto segun la ruta
        if (hashActual.includes('proveedor-productos') && btnProductos) {
            btnProductos.classList.add('activo');
        } else if (hashActual.includes('proveedor-pedidos') && btnPedidos) {
            btnPedidos.classList.add('activo');
        }
    }
}