// importamos el servicio de ui para poder inyectar html
import { cargarComponente } from '../../../services/uiService.js';
// importamos la nueva vista de productos
import { cargarVistaAdminProductos } from './productos/adminProductosController.js';

// funcion principal que inyecta el sidebar de administrador
export async function inicializarAdmin() {
    // cargamos el componente en su contenedor especifico (deberemos crear este id en index.html o layout)
    await cargarComponente('contenedor-sidebar-admin', './src/components/adminSideBar/adminSideBar.html');
    
    // capturamos los botones de navegacion internos del menu
    const btnProductos = document.getElementById('btn-nav-productos');
    const btnPedidos = document.getElementById('btn-nav-pedidos');
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

    if (btnSalir) {
        // simulamos cerrar sesion de admin recargando la pagina hacia la tienda
        btnSalir.addEventListener('click', () => window.location.reload());
    }
}
