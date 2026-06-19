// importamos todos los controladores de vistas del sistema
import { cargarVistaInicio } from '../views/Cliente/inicioCliente/inicioController.js';
import { cargarVistaLogin } from '../views/Auth/login/loginController.js';
import { cargarVistaRegistro } from '../views/Auth/registro/registroController.js';
import { cargarVistaHistorialPedidos } from '../views/Cliente/historialPedidos/historialPedidosController.js';
import { cargarVistaCatalogo } from '../views/Cliente/catalogo/productosController.js';
import { cargarVistaAdminProductos } from '../views/Administrador/productos/adminProductosController.js';
import { cargarVistaProveedorProductos } from '../views/Proveedor/productos/proveedorProductosController.js';
import { inicializarAdmin } from '../views/Administrador/sidebar/adminSideBarController.js';
import { conectarBotonHeaderProveedor, inicializarProveedor } from '../views/Proveedor/sidebar/proveedorSideBarController.js';
import { cargarVistaAdminCategorias } from '../views/Administrador/categorias/adminCategoriasController.js';
import { cargarVistaAdminUsuarios } from '../views/Administrador/usuarios/adminusuariosController.js';
import { cargarVistaAdminPedidos } from '../views/Administrador/adminPedido/adminPedidosController.js';
import { cargarVistaAdminDevoluciones } from '../views/Administrador/devoluciones/adminDevolucionesController.js';
import { cargarVistaAdminSolicitudes } from '../views/Administrador/adminSolicitudes/adminSolicitudesController.js';
import { cargarVistaPerfil } from '../views/Cliente/perfil/perfilController.js';

// importamos las nuevas vistas de estadistica para administrador y proveedor
import { cargarVistaAdminDashboard } from '../views/Administrador/estadistica/adminDashboardController.js';
import { cargarVistaProveedorEstadistica } from '../views/Proveedor/estadistica/proveedorEstadisticaController.js';
// importamos el controlador de solicitud de categoria del proveedor
import { cargarVistaSolicitudCategoria } from '../views/Proveedor/productos/solicitudProveedorController.js';

// Importamos el servicio de interfaz para poder inyectar la sidebar
import { cargarComponente } from '../services/uiService.js';

/**
 * Navega hacia una nueva vista cambiando el ancla (hash) de la URL actual.
 * Al modificar el `window.location.hash`, el navegador dispara de forma nativa 
 * el evento 'hashchange', el cual es escuchado por este mismo archivo para procesar el cambio.
 * @param {string} vista - Nombre de la vista destino (ej: 'login', 'catalogo').
 */
export function navegarA(vista) {
    window.location.hash = vista;
}

/**
 * Funcion central del enrutador (El "Guardia de Trafico"). 
 * Analiza la URL actual, verifica quien es el usuario conectado y decide que archivo JS ejecutar.
 */
async function manejarRuta() {
    // Leemos el hash de la URL, le quitamos el '#' (ej: de '#login' a 'login')
    // Si no hay hash, usamos 'inicio' por defecto
    let vista = window.location.hash.replace('#', '') || 'inicio';

    // Verificamos en la memoria temporal (SessionStorage) el rol con el que el usuario
    // inicio sesion (1: Admin, 2: Cliente, 4: Proveedor). Si es null, es un Visitante.
    const rolUsuario = sessionStorage.getItem('rolUsuario');

    // Modificamos la cabecera (Header) dinamicamente antes de renderizar la vista.
    // Esto garantiza que el Admin no vea el carrito ni las categorias de cliente.
    await adaptarHeaderSegunRol(rolUsuario);

    // PROTECCION DE RUTAS: 
    // Impedimos que el Administrador o Proveedor naveguen por la tienda publica (inicio/catalogo).
    // Si lo intentan, los rebotamos instantaneamente hacia sus respectivos paneles de gestion (Dashboard).
    if (vista === 'inicio' || vista === 'catalogo') {
        if (rolUsuario === '1') {
            window.location.hash = 'admin-productos';
            return; // Cortamos aquí para que el navegador vuelva a disparar el evento con la nueva URL
        } else if (rolUsuario === '4') {
            window.location.hash = 'proveedor-productos';
            return;
        }
    }

    // SWITCH DE RUTAS: Ejecutamos el controlador específico según la ruta solicitada
    if (vista === 'login') {
        cargarVistaLogin();
    } else if (vista === 'registro') {
        cargarVistaRegistro();
    } else if (vista === 'historial') {
        cargarVistaHistorialPedidos();
    } else if (vista === 'perfil') {
        cargarVistaPerfil();
    } else if (vista === 'catalogo') {
        cargarVistaCatalogo();
    } else if (vista === 'admin-productos') {
        cargarVistaAdminProductos();
    } else if (vista === 'admin-categorias') {
        cargarVistaAdminCategorias();
    } else if (vista === 'admin-usuarios') {
        cargarVistaAdminUsuarios();
    } else if (vista === 'proveedor-categorias') {
        cargarVistaAdminCategorias(); // reutilizamos la vista de gestion de categorias
    } else if (vista === 'admin-pedidos') {
        cargarVistaAdminPedidos();
    } else if (vista === 'admin-devoluciones') {
        cargarVistaAdminDevoluciones();
    } else if (vista === 'dashboard') {
        // decidimos que controlador de estadistica cargar segun el rol del usuario
        if (rolUsuario === '1') {
            cargarVistaAdminDashboard();
        } else if (rolUsuario === '4') {
            cargarVistaProveedorEstadistica();
        }
    } else if (vista === 'admin-solicitudes') {
        cargarVistaAdminSolicitudes();
    } else if (vista === 'proveedor-productos') {
        cargarVistaProveedorProductos();
    } else if (vista === 'proveedor-pedidos') {
        cargarVistaAdminPedidos(); // reutilizamos la misma vista porque la base de datos se encarga de filtrar
    } else if (vista === 'proveedor-estadistica') {
        // ruta directa para que el proveedor pueda ver sus estadisticas de ventas desde el sidebar
        cargarVistaProveedorEstadistica();
    } else {
        cargarVistaInicio(); // por defecto carga el inicio
    }
}

/**
 * Adapta el diseno de la cabecera principal e inyecta sidebars especiales segun el rol del usuario.
 * Esta funcion es clave para mantener un unico archivo `header.html` y evitar la duplicacion de codigo.
 * @param {string|null} rol - ID del rol del usuario ('1', '2', '4' o null).
 */
async function adaptarHeaderSegunRol(rol) {
    const btnCarrito = document.getElementById('btn-carrito-header');
    const btnPerfil = document.getElementById('btn-usuario-perfil');

    // convertimos el rol a string seguro para evitar fallos de comparacion
    const rolActivo = rol ? String(rol).trim() : null;

    if (rolActivo === '1') {
        // --- MODO ADMINISTRADOR: sidebar tipo panel fijo de dos columnas ---
        // movemos el boton de perfil al nav del header para tenerlo accesible
        const headerNav = document.querySelector('.header-navegacion');
        if (btnPerfil && headerNav) {
            headerNav.appendChild(btnPerfil);
        }

        // activamos el modo dashboard que convierte el layout en dos columnas
        document.body.classList.add('layout-dashboard');

        // buscamos el contenedor del sidebar del admin en el index.html
        let sidebarAdminContainer = document.getElementById('contenedor-sidebar-admin');

        // si el contenedor no existe en el dom, lo creamos y lo anclamos al body
        if (!sidebarAdminContainer) {
            sidebarAdminContainer = document.createElement('div');
            sidebarAdminContainer.id = 'contenedor-sidebar-admin';
            // la clase le aplica los estilos del panel fijo de dos columnas
            sidebarAdminContainer.classList.add('dashboard-sidebar-container');
            // lo insertamos antes del layout-container para que quede a la izquierda
            const layoutContainer = document.querySelector('.layout-container');
            if (layoutContainer) {
                document.body.insertBefore(sidebarAdminContainer, layoutContainer);
            } else {
                document.body.prepend(sidebarAdminContainer);
            }
        }

        // solo cargamos el html del sidebar si el contenedor esta vacio
        if (sidebarAdminContainer.innerHTML.trim() === '') {
            await inicializarAdmin();
        }

    } else if (rolActivo === '4') {
        // --- MODO PROVEEDOR: sidebar tipo drawer deslizante (igual que el carrito) ---
        // el proveedor NO usa el layout de dos columnas. su sidebar es un panel
        // que se desliza desde la izquierda con position:fixed, igual que el carrito.
        // por eso NO agregamos la clase layout-dashboard al body.

        // movemos el boton de perfil al nav para que el proveedor lo vea facilmente
        const headerNav = document.querySelector('.header-navegacion');
        if (btnPerfil && headerNav) {
            headerNav.appendChild(btnPerfil);
        }

        // buscamos el contenedor del drawer del proveedor (ya existe en el index.html)
        const sidebarProvContainer = document.getElementById('contenedor-sidebar-proveedor');

        // solo inicializamos el drawer si el contenedor esta vacio (evita duplicados)
        if (sidebarProvContainer && sidebarProvContainer.innerHTML.trim() === '') {
            await inicializarProveedor();
            // conectamos el boton del header para que al hacer clic abra el drawer
            if (typeof conectarBotonHeaderProveedor === 'function') {
                conectarBotonHeaderProveedor();
            }
        }

    } else {
        // --- MODO CLIENTE O VISITANTE: layout normal de tienda ---
        const headerAction = document.querySelector('.header-accion');
        if (btnPerfil && headerAction && btnCarrito) {
            // regresamos el boton de perfil a su lugar original en el header de cliente
            headerAction.insertBefore(btnPerfil, btnCarrito);
        }

        // desmontamos el modo dashboard si el admin cerro sesion
        document.body.classList.remove('layout-dashboard');

        // eliminamos el contenedor dinamico de la sidebar del admin si existe
        const sidebarAdminContainer = document.getElementById('contenedor-sidebar-admin');
        if (sidebarAdminContainer && sidebarAdminContainer.classList.contains('dashboard-sidebar-container')) {
            // solo eliminamos el que fue creado dinamicamente, no el del index.html
            sidebarAdminContainer.remove();
        }
    }
}

/**
 * Inicializa el enrutador al cargar la aplicacion (Single Page Application).
 * Activa los "oidos" del navegador para detectar cambios en la URL.
 */
export function inicializarEnrutador() {
    // EventListener nativo: Escucha cuando la URL cambia y ejecuta `manejarRuta`
    window.addEventListener('hashchange', manejarRuta);

    // Forzamos una primera ejecución al entrar a la página o al presionar F5
    manejarRuta();
    
    // DELEGACION DE EVENTOS PARA CIERRE DE SESION:
    // Como el boton de salir puede ser inyectado despues de que carga JS, escuchamos el 'click'
    // en todo el documento y filtramos solo cuando provenga de ese boton en especifico.
    document.addEventListener('click', async (e) => {
        const btnCerrar = e.target.closest('#btn-cerrar-sesion');
        if (btnCerrar) {
            // 1. Nos comunicamos con el Servidor Java para destruir la sesion real (HttpSession)
            try {
                await fetch('logout');
            } catch (error) { console.error('Error al cerrar sesion en el servidor', error); }
            
            // 2. Limpiamos las credenciales locales y forzamos el reinicio grafico
            sessionStorage.removeItem('rolUsuario'); 
            window.location.hash = 'inicio'; 
            window.location.reload(); 
        }
    });
}