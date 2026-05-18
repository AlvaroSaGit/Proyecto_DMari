import { cargarComponente } from './services/uiService.js';
// importamos el controlador de registro
import { cargarVistaRegistro } from './views/Auth/registro/registroController.js';
// importamos el controlador de inicio
import { cargarVistaInicio } from './views/Cliente/inicioCliente/inicioController.js';
// importamos el controlador del carrito
import { inicializarCarrito, abrirCarrito } from './components/carritoSideBar/carritoController.js';
// importamos el controlador de categorias
import { inicializarCategoria, abrirCategoria } from './components/categoriaSideBar/categoriaController.js';
// importamos el controlador del usuario
import { inicializarUsuario, abrirUsuario } from './components/usuarioSideBar/usuarioSideBarController.js';
// importamos el controlador del sidebar de proveedor
import { inicializarProveedor } from './views/Proveedor/sidebar/proveedorSideBarController.js';

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
    // cargamos el inicio usando su controlador para que inyecte los productos dinamicos
    cargarVistaInicio();
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
    inicializarProveedor();

    // BOTONES DEL HEADER
    // activamos los clics de los botones del header
    configurarBotonesHeader();
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
            // usamos el controlador en vez de solo cargar el html
            cargarVistaInicio();
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
