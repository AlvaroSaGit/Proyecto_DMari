import { cargarComponente } from '../../../services/uiService.js';
import { navegarA } from '../../../router/router.js';
import { cargarVistaSolicitudCategoria } from '../productos/solicitudProveedorController.js';

export async function inicializarProveedor() {
    await cargarComponente('contenedor-sidebar-dashboard', './src/views/Proveedor/sidebar/proveedorSideBar.html?t=' + new Date().getTime());
    
    const btnProductos = document.getElementById('btn-prov-productos');
    const btnPedidos = document.getElementById('btn-prov-pedidos');
    const btnEstadistica = document.getElementById('btn-prov-estadistica');
    const btnCategoria = document.getElementById('btn-prov-categoria');
    const btnSalir = document.getElementById('btn-nav-salir');
    
    if (btnProductos) btnProductos.onclick = () => navegarA('proveedor-productos');
    if (btnPedidos) btnPedidos.onclick = () => navegarA('proveedor-pedidos');
    if (btnEstadistica) btnEstadistica.onclick = () => navegarA('proveedor-estadistica');
    if (btnCategoria) btnCategoria.onclick = () => cargarVistaSolicitudCategoria();
    
    actualizarEstadoActivo();
    window.addEventListener('hashchange', actualizarEstadoActivo);

    if (btnSalir) {
        btnSalir.addEventListener('click', async () => {
            try { await fetch('logout'); } catch(e) {}
            sessionStorage.removeItem('rolUsuario');
            window.location.hash = 'inicio';
            window.location.reload();
        });
    }

    function actualizarEstadoActivo() {
        const hashActual = window.location.hash || '#proveedor-productos';
        document.querySelectorAll('.admin-nav-item').forEach(btn => btn.classList.remove('activo'));

        if (hashActual.includes('proveedor-productos') && btnProductos) btnProductos.classList.add('activo');
        else if (hashActual.includes('proveedor-pedidos') && btnPedidos) btnPedidos.classList.add('activo');
        else if (hashActual.includes('proveedor-estadistica') && btnEstadistica) btnEstadistica.classList.add('activo');
    }
}

// Ya no necesitamos abrirProveedor ni cerrarProveedor en desktop, el panel ahora es fijo.
export function conectarBotonHeaderProveedor() {
    // Ya no se usa en modo desktop de layout fijo.
}

export function abrirProveedor() {
    // Funcion vacia solicitada para evitar que main.js tire Uncaught SyntaxError
    // En un futuro aqui puede ir logica para desplegar la sidebar en moviles
}