// importamos el servicio de ui para poder inyectar html
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador para navegación dinámica SPA
import { navegarA } from '../../../router/router.js';

// funcion principal que inyecta el sidebar de administrador
export async function inicializarAdmin() {
    // cargamos el html del sidebar desde la misma carpeta del administrador
    await cargarComponente('contenedor-sidebar-admin', './src/views/Administrador/sidebar/adminSideBar.html');
    
    // capturamos los botones de navegacion internos del menu
    const btnProductos = document.getElementById('btn-nav-productos');
    const btnPedidos = document.getElementById('btn-nav-pedidos');
    const btnSolicitudes = document.getElementById('btn-nav-solicitudes');
    const btnSalir = document.getElementById('btn-nav-salir');
    
    // configuramos la navegacion
    if (btnProductos) {
        btnProductos.addEventListener('click', () => {
            navegarA('admin-productos');
        });
    }
    
    if (btnPedidos) {
        btnPedidos.addEventListener('click', () => {
            navegarA('admin-pedidos');
        });
    }
    
    if (btnSolicitudes) {
        btnSolicitudes.addEventListener('click', () => {
            navegarA('admin-solicitudes');
        });
    }

    if (btnSalir) {
        // Cierre de sesión seguro y reinicio gráfico del sistema
        btnSalir.addEventListener('click', async () => {
            try { await fetch('logout'); } catch(e) {}
            sessionStorage.removeItem('rolUsuario');
            window.location.hash = 'inicio';
            window.location.reload();
        });
    }
}
