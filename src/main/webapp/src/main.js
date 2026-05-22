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
// import { inicializarProveedor } from './views/Proveedor/sidebar/proveedorSideBarController.js';

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
    const btnUsuario = document.querySelector('.btn-usuario');
    const btnCarrito = document.querySelector('.btn-carrito');

    // clic en el logo: carga el inicio
    if (btnLogo) {
        btnLogo.addEventListener('click', function() {
            // usamos el enrutador para volver al inicio
            navegarA('inicio');
        });
    }

    // clic en usuario: preparamos el espacio para el sidebar
    if (btnUsuario) {
        btnUsuario.addEventListener('click', function() {
            // abrimos la barra lateral del usuario
            abrirUsuario();
        });
    }

    // clic en categoria: preparamos el espacio para el sidebar
    if (btnCategoria) {
        btnCategoria.addEventListener('click', function() {
            // abrimos la barra lateral de categorias
            abrirCategoria();
        });
    }

    // clic en carrito: preparamos el espacio
    if (btnCarrito) {
        btnCarrito.addEventListener('click', function() {
            // llamamos a la funcion para deslizar el carrito
            abrirCarrito();
        });
    }
}

// funcion para consultar a Java si el usuario ya inicio sesion
async function verificarSesion() {
    try {
        const respuesta = await fetch('session');
        
        // Si responde 200 OK, es porque hay un usuario logueado en Java (HttpSession)
        if (respuesta.ok) {
            const datos = await respuesta.json();
            
            // Cambiamos el contenido del boton del header para que muestre el nombre del usuario
            const btnUsuario = document.querySelector('.btn-usuario');
            if (btnUsuario) {
                btnUsuario.innerHTML = `<span class="material-symbols-outlined">person</span> <span class="btn-text" style="font-size: 0.9rem;">Hola, ${datos.nombre}</span>`;
            }
        } else if (respuesta.status === 401) {
            // Atrapamos el 401 especificamente para que la app sepa que estamos en modo visitante
            console.log("Modo visitante: No hay sesion activa en el sistema.");
        }
    } catch (error) {
        // Esto solo saltara si el backend esta apagado o no hay conexion
        console.error('Error de comunicacion con el servidor:', error);
    }
}