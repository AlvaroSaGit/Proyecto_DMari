// importamos el servicio de ui para poder inyectar html dinamicamente en la pagina
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador para hacer la navegacion dinamica tipo spa (single page application) sin recargar
import { navegarA } from '../../../router/router.js';

// funcion principal que inyecta el menu lateral (sidebar) exclusivo del administrador.
// esta funcion es llamada desde el router general cuando detecta que el usuario logueado es admin.
export async function inicializarAdmin() {
    // cargamos el html del sidebar desde la carpeta de vistas del administrador.
    // agregamos '?t=' + new Date().getTime() para crear una url unica en cada recarga.
    // esto destruye la cache del navegador y nos asegura que siempre cargue la version mas reciente del menu.
    await cargarComponente('contenedor-sidebar-admin', './src/views/Administrador/sidebar/adminSideBar.html?t=' + new Date().getTime());
    
    // extraemos y guardamos en variables los botones de navegacion que acabamos de inyectar en el html
    const btnDashboard = document.getElementById('btn-nav-dashboard'); // asumiendo que el id es este en tu html
    const btnProductos = document.getElementById('btn-nav-productos');
    const btnCategorias = document.getElementById('btn-nav-categorias');
    const btnPedidos = document.getElementById('btn-nav-pedidos');
    const btnSolicitudes = document.getElementById('btn-nav-solicitudes');
    const btnDevoluciones = document.getElementById('btn-nav-devoluciones');
    const btnUsuarios = document.getElementById('btn-nav-usuarios');
    const btnSalir = document.getElementById('btn-nav-salir');
    
    // configuramos la navegacion para cada boton.
    // si el boton existe en el html, le agregamos un escuchador de eventos 'click'.
    // cuando hagan clic, le ordenamos al router que cambie la url hacia esa vista especifica.
    if (btnDashboard) {
        btnDashboard.addEventListener('click', () => {
            navegarA('dashboard');
        });
    }

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

    if (btnDevoluciones) {
        btnDevoluciones.addEventListener('click', () => {
            navegarA('admin-devoluciones');
        });
    }

    if (btnUsuarios) {
        btnUsuarios.addEventListener('click', () => {
            navegarA('admin-usuarios');
        });
    }

    // logica para mantener iluminado (resaltado) el boton en el que estamos actualmente.
    // ejecutamos la funcion una vez al arrancar para iluminar el boton inicial.
    actualizarEstadoActivo();
    // le pedimos a la ventana del navegador que vuelva a ejecutar esta funcion cada vez que la url cambie.
    window.addEventListener('hashchange', actualizarEstadoActivo);

    if (btnSalir) {
        // cierre de sesion seguro y reinicio grafico del sistema
        btnSalir.addEventListener('click', async () => {
            // 1. avisamos al backend java que destruya la sesion en el servidor
            try { await fetch('logout'); } catch(e) {}
            
            // 2. borramos la memoria local (sessionstorage) del navegador
            sessionStorage.removeItem('rolUsuario');
            
            // 3. redirigimos la url a la pagina de inicio publica
            window.location.hash = 'inicio';
            
            // 4. forzamos una recarga completa de la pestana para limpiar cualquier dato residual en memoria
            window.location.reload();
        });
    }

    // funcion interna y automatica para leer la url actual y aplicar la clase css "activo" al boton correcto
    function actualizarEstadoActivo() {
        // leemos en que pagina estamos (ej: '#admin-productos'). si no hay hash, asumimos la de productos.
        const hashActual = window.location.hash || '#admin-productos';
        
        // apagamos todos los botones primero removiendo la clase 'activo'
        document.querySelectorAll('.admin-nav-item').forEach(btn => btn.classList.remove('activo'));

        // encendemos (iluminamos) unicamente el boton que coincide con la url actual
        if (hashActual.includes('dashboard')) {
            if (btnDashboard) btnDashboard.classList.add('activo');
        } else if (hashActual.includes('admin-productos') && btnProductos) {
            btnProductos.classList.add('activo');
        } else if (hashActual.includes('admin-categorias') && btnCategorias) {
            btnCategorias.classList.add('activo');
        } else if (hashActual.includes('admin-pedidos') && btnPedidos) {
            btnPedidos.classList.add('activo');
        } else if (hashActual.includes('admin-devoluciones') && btnDevoluciones) {
            btnDevoluciones.classList.add('activo');
        } else if (hashActual.includes('admin-solicitudes') && btnSolicitudes) {
            btnSolicitudes.classList.add('activo');
        } else if (hashActual.includes('admin-usuarios') && btnUsuarios) {
            btnUsuarios.classList.add('activo');
        }
    }
}
