// importamos todos los controladores de vistas del sistema
import { cargarVistaInicio } from '../views/Cliente/inicioCliente/inicioController.js';
import { cargarVistaConfiguracion } from '../views/Cliente/configuracion/configuracionController.js';
import { cargarVistaLogin } from '../views/Auth/login/loginController.js';
import { cargarVistaRegistro } from '../views/Auth/registro/registroController.js';
import { cargarVistaHistorialPedidos } from '../views/Cliente/historialPedidos/historialPedidosController.js';
import { cargarVistaCatalogo } from '../views/Cliente/catalogo/productosController.js';
import { cargarVistaAdminProductos } from '../views/Administrador/productos/adminProductosController.js';
import { cargarVistaProveedorProductos } from '../views/Proveedor/productos/proveedorProductosController.js';
import { inicializarAdmin } from '../views/Administrador/sidebar/adminSideBarController.js';
import { cargarVistaAdminCategorias } from '../views/Administrador/categorias/adminCategoriasController.js';
import { inicializarProveedor } from '../views/Proveedor/sidebar/proveedorSideBarController.js';

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
    if (vista === 'configuracion') {
        cargarVistaConfiguracion();
    } else if (vista === 'login') {
        cargarVistaLogin();
    } else if (vista === 'registro') {
        cargarVistaRegistro();
    } else if (vista === 'historial') {
        cargarVistaHistorialPedidos();
    } else if (vista === 'catalogo') {
        cargarVistaCatalogo();
    } else if (vista === 'admin-productos') {
        cargarVistaAdminProductos();
    } else if (vista === 'admin-categorias') {
        cargarVistaAdminCategorias();
    } else if (vista === 'admin-pedidos') {
        // Vista temporal para Pedidos hasta que crees su Controller
        const main = document.getElementById('component-main');
        if (main) main.innerHTML = '<section class="admin-vista-productos"><div class="admin-header-seccion"><h2>Gestion de Pedidos</h2></div><div class="admin-tabla-contenedor" style="padding:20px;">Modulo de despacho de pedidos pendiente de construccion...</div></section>';
    } else if (vista === 'admin-solicitudes') {
        // Vista temporal para Solicitudes hasta que crees su Controller
        const main = document.getElementById('component-main');
        if (main) main.innerHTML = '<section class="admin-vista-productos"><div class="admin-header-seccion"><h2>Solicitudes de Proveedores</h2></div><div class="admin-tabla-contenedor" style="padding:20px;">Modulo para aceptar o rechazar solicitudes en construccion...</div></section>';
    } else if (vista === 'proveedor-productos') {
        cargarVistaProveedorProductos();
    } else if (vista === 'proveedor-pedidos') {
        // Vista temporal para Pedidos del Proveedor
        const main = document.getElementById('component-main');
        if (main) main.innerHTML = '<section class="admin-vista-productos"><div class="admin-header-seccion"><h2>Mis Pedidos</h2></div><div class="admin-tabla-contenedor" style="padding:20px;">Modulo de pedidos del proveedor en construccion...</div></section>';
    } else {
        cargarVistaInicio(); // Por defecto carga el inicio
    }
}

/**
 * Adapta el diseno de la cabecera principal e inyecta sidebars especiales segun el rol del usuario.
 * Esta funcion es clave para mantener un unico archivo `header.html` y evitar la duplicacion de codigo.
 * @param {string|null} rol - ID del rol del usuario ('1', '2', '4' o null).
 */
async function adaptarHeaderSegunRol(rol) {
    const bloqueCategoria = document.getElementById('header-bloque-categoria');
    const btnCarrito = document.getElementById('btn-carrito-header');
    const btnCerrarSesion = document.getElementById('btn-cerrar-sesion');
    const btnPerfil = document.getElementById('btn-usuario-perfil');
    const logoHeader = document.getElementById('component-logo');
    const footerContainer = document.getElementById('footer-container');

    // Convertimos el rol a string seguro para evitar fallos de comprobación
    const rolActivo = rol ? String(rol).trim() : null;

    if (rolActivo === '1' || rolActivo === '4') {
        const headerNav = document.querySelector('.header-navegacion');
        if (btnPerfil && headerNav) {
            headerNav.appendChild(btnPerfil); // Movemos tu perfil a la extrema izquierda
        }
        

        // --- LOGICA DE LA SIDEBAR TIPO DASHBOARD ---
        // Le aplicamos una clase al body que convierte toda la pagina en una estructura de Panel de Control
        document.body.classList.add('layout-dashboard'); 
        
        // Creamos dinamicamente el "hueco" (div) donde vivira el menu lateral negro del Administrador
        let sidebarAdmin = document.getElementById('contenedor-sidebar-admin');
        if (!sidebarAdmin) {
            sidebarAdmin = document.createElement('div');
            sidebarAdmin.id = 'contenedor-sidebar-admin';
            const main = document.getElementById('component-main');
            // Insertamos la nueva sidebar en el arbol de HTML, justo antes del contenedor principal
            if (main) main.parentNode.insertBefore(sidebarAdmin, main); 
        }
        
        // Si el hueco de la sidebar esta vacio, inicializamos el controlador del Dashboard
        // Usamos trim() para asegurar que no nos enganen espacios en blanco fantasmas
        if (sidebarAdmin.innerHTML.trim() === '') {
            if (rolActivo === '1') {
                await inicializarAdmin();
            } else if (rolActivo === '4') {
                await inicializarProveedor();
            }
        }
    } else {
        const headerAction = document.querySelector('.header-accion');
        if (btnPerfil && headerAction && btnCarrito) {
            headerAction.insertBefore(btnPerfil, btnCarrito); // Regresamos el perfil a la derecha
        }

        // DESMONTAJE DEL DASHBOARD: Si el admin cerro sesion, destruimos toda su estructura especial
        // para que la tienda vuelva a verse limpia para el cliente
        document.body.classList.remove('layout-dashboard');
        const sidebarAdmin = document.getElementById('contenedor-sidebar-admin');
        if (sidebarAdmin) {
            sidebarAdmin.remove(); 
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