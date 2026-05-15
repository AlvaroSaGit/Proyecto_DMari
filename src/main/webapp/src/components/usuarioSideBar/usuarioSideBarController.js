// importamos el servicio de ui para poder inyectar html
import { cargarComponente } from '../../services/uiService.js';
// importamos las vistas de auth para poder navegar a ellas
import { cargarVistaLogin } from '../../views/Auth/login/loginController.js';
import { cargarVistaRegistro } from '../../views/Auth/registro/registroController.js';
// importamos la vista de configuracion del cliente
import { cargarVistaConfiguracion } from '../../views/Cliente/configuracion/configuracionController.js';

// funcion principal que inyecta el sidebar de usuario oculto en el index al inicio
export async function inicializarUsuario() {
    // cargamos el componente en su contenedor especifico
    await cargarComponente('contenedor-sidebar-usuario', './src/components/usuarioSideBar/usuarioSideBar.html');
    
    // capturamos los elementos para cerrar el menu y la capa oscura
    const btnCerrar = document.getElementById('btn-cerrar-usuario');
    const overlay = document.getElementById('overlay-usuario');
    
    // capturamos los botones de navegacion internos del menu
    const btnLogin = document.getElementById('btn-nav-login');
    const btnRegistro = document.getElementById('btn-nav-registro');
    const btnConfiguracion = document.getElementById('btn-nav-configuracion');
    
    // asignamos eventos para cerrar el sidebar cuando se da clic en la x o afuera
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarUsuario);
    if (overlay) overlay.addEventListener('click', cerrarUsuario);

    // configuramos la navegacion de los botones del menu
    // cada boton cierra primero el sidebar y luego carga la vista solicitada
    if (btnLogin) btnLogin.addEventListener('click', () => { cerrarUsuario(); cargarVistaLogin(); });
    if (btnRegistro) btnRegistro.addEventListener('click', () => { cerrarUsuario(); cargarVistaRegistro(); });
    if (btnConfiguracion) btnConfiguracion.addEventListener('click', () => { cerrarUsuario(); cargarVistaConfiguracion(); });
}

// funcion para mostrar el menu lateral deslizando su contenedor
export function abrirUsuario() {
    const sidebar = document.getElementById('sidebar-usuario');
    const overlay = document.getElementById('overlay-usuario');
    
    // agregamos la clase activo para que css haga la animacion de entrada
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

// funcion para ocultar el menu lateral y el fondo oscuro
export function cerrarUsuario() {
    const sidebar = document.getElementById('sidebar-usuario');
    const overlay = document.getElementById('overlay-usuario');
    
    // quitamos la clase activo para que css lo esconda nuevamente
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}