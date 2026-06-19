import { cargarComponente } from './services/uiService.js';
// importamos el controlador de registro
// importamos el enrutador central
import { inicializarEnrutador, navegarA } from './router/router.js';
// importamos el controlador del carrito
import { inicializarCarrito, abrirCarrito } from './components/carritoSideBar/carritoController.js';
// importamos el controlador de categorias
import { inicializarCategoria, abrirCategoria } from './components/categoriaSideBar/categoriaController.js';
// importamos el controlador del usuario
import { inicializarUsuario, abrirUsuario } from './components/usuarioSideBar/usuarioSideBarController.js';
// importamos el controlador del sidebar de proveedor
import { inicializarProveedor, abrirProveedor } from './views/Proveedor/sidebar/proveedorSideBarController.js';

/* 
    El addEventListener mantiene pendiente cuando ocurra el suceso
    DOMContentLoade - Cuando DOM termine de leer el HTML, hacer
    lo que esta en la funcion {}
*/
document.addEventListener('DOMContentLoaded', async () => {
    /* Busca la id que es el primer parametro y luego la ruta */
    // --- HEADER ---
    // Carga del header
    await cargarComponente('header-container','./src/components/header/header.html');
    
    // Inicializamos nuestro enrutador central para que decida que vista cargar
    inicializarEnrutador();

    // nueva carga del footer
    cargarComponente('footer-container', './src/components/footer/footer.html');


    //      SIDEBAR
    // cargamos el html del carrito oculto en el index
    inicializarCarrito();
    // cargamos el html de categorias oculto en el index
    inicializarCategoria();
    // cargamos el html de usuario oculto en el index
    inicializarUsuario();

    // temporal: cargamos el panel del proveedor para visualizarlo en pantalla
    // inicializarProveedor();

    // BOTONES DEL HEADER
    // activamos los clics de los botones del header
    configurarBotonesHeader();

    // VERIFICAR SESION
    verificarSesion();
});

// funcion para darle accion a los botones del header
function configurarBotonesHeader() {
    // buscamos los contenedores en el html
    const btnLogo = document.getElementById('component-logo');
    const btnCategoria = document.querySelector('.btn-categoria');
    const btnUsuario = document.getElementById('btn-usuario-perfil');
    const btnCarrito = document.getElementById('btn-carrito-header');

    // clic en el logo: carga el inicio
    if (btnLogo) {
        btnLogo.addEventListener('click', function() {
            navegarA('inicio');
        });
    }

    if (btnUsuario) {
        btnUsuario.addEventListener('click', function() {
            // si el usuario es un proveedor, abre su panel. si no, abre el de cliente.
            if (sessionStorage.getItem('rolUsuario') == "4") {
                abrirProveedor();
            } else {
                abrirUsuario();
            }
        });
    }

    if (btnCategoria) {
        btnCategoria.addEventListener('click', function() {
            abrirCategoria();
        });
    }

    if (btnCarrito) {
        btnCarrito.addEventListener('click', function() {
            abrirCarrito();
        });
    }
}

/**
 * Solicita al Servlet `/session` en Java verificar si existe una HttpSession válida.
 * Si responde OK, actualiza visualmente la interfaz y sincroniza la memoria local del navegador.
 */
async function verificarSesion() {
    try {
        const respuesta = await fetch('session');
        
        // ESTADO 200: Existe una sesión válida y logueada en el servidor Tomcat/Java
        if (respuesta.ok) {
            const datos = await respuesta.json();
            
            // PROTECCIÓN CRÍTICA:
            // Si el backend /session no envía explícitamente el 'idRol', evitamos guardar "undefined"
            // para que no se rompa la vista del Administrador al recargar la página.
            if (datos.idRol !== undefined) {
                sessionStorage.setItem('rolUsuario', datos.idRol);
            } else if (datos.rol !== undefined) {
                sessionStorage.setItem('rolUsuario', datos.rol);
            }

            // Si el usuario es un proveedor (Rol 4), inicializamos su panel lateral
            if (sessionStorage.getItem('rolUsuario') == "4") {
                inicializarProveedor();
            }
            
            // Evento Forzado: Disparamos un cambio de Hash falso para obligar al Enrutador a redibujar el header
            window.dispatchEvent(new Event('hashchange'));
            
            // Personalizamos el botón de usuario inyectando un saludo con su nombre
            const btnPerfil = document.getElementById('btn-usuario-perfil');
            if (btnPerfil) {
                btnPerfil.innerHTML = `<span class="material-symbols-outlined">person</span> <span class="btn-text" style="font-size: 0.9rem;">Hola, ${datos.nombre}</span>`;
            }
        } else if (respuesta.status === 401) {
            // ESTADO 401 (No Autorizado): El usuario es un invitado. JS ignora el error silenciosamente.
            console.log("Modo visitante: No hay sesion activa en el sistema.");
        }
    } catch (error) {
        // Error de red: Tomcat está apagado, CORS bloqueado o caída de internet.
        console.error('Error de comunicacion con el servidor:', error);
    }
}