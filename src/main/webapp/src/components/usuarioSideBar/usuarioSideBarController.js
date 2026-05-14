// importamos el servicio de ui
import { cargarComponente } from '../../services/uiService.js';
// importamos las vistas de auth
import { cargarVistaLogin } from '../../views/Auth/login/loginController.js';
import { cargarVistaRegistro } from '../../views/Auth/registro/registroController.js';

// inyecta el sidebar en index.html al inicio
export async function inicializarUsuario() {
    await cargarComponente('contenedor-sidebar-usuario', './src/components/usuarioSideBar/usuarioSideBar.html');
    
    const btnCerrar = document.getElementById('btn-cerrar-usuario');
    const overlay = document.getElementById('overlay-usuario');
    const btnLogin = document.getElementById('btn-nav-login');
    const btnRegistro = document.getElementById('btn-nav-registro');
    
    // cerrar sidebar
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarUsuario);
    if (overlay) overlay.addEventListener('click', cerrarUsuario);

    // Navegación (Cargamos la vista y cerramos el menú)
    if (btnLogin) btnLogin.addEventListener('click', () => { cerrarUsuario(); cargarVistaLogin(); });
    if (btnRegistro) btnRegistro.addEventListener('click', () => { cerrarUsuario(); cargarVistaRegistro(); });
}

// muestra el sidebar agregando clases css
export function abrirUsuario() {
    const sidebar = document.getElementById('sidebar-usuario');
    const overlay = document.getElementById('overlay-usuario');
    
    if (sidebar) sidebar.classList.add('activo');
    if (overlay) overlay.classList.add('activo');
}

// oculta el sidebar quitando las clases css
export function cerrarUsuario() {
    const sidebar = document.getElementById('sidebar-usuario');
    const overlay = document.getElementById('overlay-usuario');
    
    if (sidebar) sidebar.classList.remove('activo');
    if (overlay) overlay.classList.remove('activo');
}