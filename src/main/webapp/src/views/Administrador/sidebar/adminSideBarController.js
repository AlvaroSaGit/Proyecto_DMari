// importamos el servicio de ui para poder inyectar html
import { cargarComponente } from '../../../services/uiService.js';
// importamos la nueva vista de productos
import { cargarVistaAdminProductos } from '../productos/adminProductosController.js';
// importamos la vista de solicitudes
import { cargarVistaAdminSolicitudes } from '../adminSolicitudes/adminSolicitudesController.js';

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
            console.log('cargar vista de gestion de productos');
            // aqui llamaremos a cargarVistaAdminProductos();
            cargarVistaAdminProductos();
        });
    }
    
    if (btnPedidos) {
        btnPedidos.addEventListener('click', () => {
            console.log('cargar vista de gestion de pedidos');
            // aqui llamaremos a cargarVistaAdminPedidos();
        });
    }
    
    if (btnSolicitudes) {
        btnSolicitudes.addEventListener('click', () => {
            console.log('cargar vista de solicitudes de proveedores');
            cargarVistaAdminSolicitudes();
        });
    }

    if (btnSalir) {
        // simulamos cerrar sesion de admin recargando la pagina hacia la tienda
        btnSalir.addEventListener('click', () => window.location.reload());
    }
}
