// importamos el servicio de ui para poder inyectar html
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador para navegacion dinamica SPA
import { navegarA } from '../../../router/router.js';

// funcion principal que inyecta el sidebar de administrador
export async function inicializarAdmin() {
    // cargamos el html del sidebar desde la misma carpeta del administrador
    // Agregamos un timestamp dinamico para destruir la cache del navegador y forzar la inyeccion
    await cargarComponente('contenedor-sidebar-admin', './src/views/Administrador/sidebar/adminSideBar.html?t=' + new Date().getTime());
    
    // capturamos los botones de navegacion internos del menu
    const btnProductos = document.getElementById('btn-nav-productos');
    const btnCategorias = document.getElementById('btn-nav-categorias');
    const btnPedidos = document.getElementById('btn-nav-pedidos');
    const btnSolicitudes = document.getElementById('btn-nav-solicitudes');
    const btnSalir = document.getElementById('btn-nav-salir');
    
    // configuramos la navegacion
    if (btnProductos) {
        btnProductos.addEventListener('click', () => {
            navegarA('admin-productos');
        });
    }
    
    if (btnCategorias) {
        btnCategorias.addEventListener('click', () => {
            navegarA('admin-categorias');
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

    // Logica para mantener iluminado (resaltado) el boton en el que estamos actualmente
    actualizarEstadoActivo();
    window.addEventListener('hashchange', actualizarEstadoActivo);

    if (btnSalir) {
        // Cierre de sesion seguro y reinicio grafico del sistema
        btnSalir.addEventListener('click', async () => {
            try { await fetch('logout'); } catch(e) {}
            sessionStorage.removeItem('rolUsuario');
            window.location.hash = 'inicio';
            window.location.reload();
        });
    }

    // Funcion interna para leer la URL y aplicar la clase CSS "activo" al boton correcto
    function actualizarEstadoActivo() {
        const hashActual = window.location.hash || '#admin-productos';
        
        // Apagamos todos los botones primero
        document.querySelectorAll('.admin-nav-item').forEach(btn => btn.classList.remove('activo'));

        // Encendemos el correspondiente
        if (hashActual.includes('admin-productos') && btnProductos) {
            btnProductos.classList.add('activo');
        } else if (hashActual.includes('admin-categorias') && btnCategorias) {
            btnCategorias.classList.add('activo');
        } else if (hashActual.includes('admin-pedidos') && btnPedidos) {
            btnPedidos.classList.add('activo');
        } else if (hashActual.includes('admin-solicitudes') && btnSolicitudes) {
            btnSolicitudes.classList.add('activo');
        }
    }
}
