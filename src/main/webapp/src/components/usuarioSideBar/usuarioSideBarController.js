// importamos el servicio de ui para poder inyectar html
import { cargarComponente } from '../../services/uiService.js';
// importamos el enrutador
import { navegarA } from '../../router/router.js';

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

    // capturamos el boton del header para abrir el panel de usuario
    const btnPerfilHeader = document.getElementById('btn-usuario-perfil');
    if (btnPerfilHeader) btnPerfilHeader.addEventListener('click', abrirUsuario);

    // configuramos la navegacion de los botones del menu
    // cada boton cierra primero el sidebar y luego carga la vista solicitada
    if (btnLogin) btnLogin.addEventListener('click', () => { cerrarUsuario(); navegarA('login'); });
    if (btnRegistro) btnRegistro.addEventListener('click', () => { cerrarUsuario(); navegarA('registro'); });
    if (btnConfiguracion) btnConfiguracion.addEventListener('click', () => { cerrarUsuario(); navegarA('configuracion'); });

    // Validamos la sesion especificamente para configurar el menu del sidebar
    try {
        const respuesta = await fetch('session');
        if (respuesta.ok) {
            // 1. Ocultamos las opciones que solo son para invitados
            if (btnLogin) btnLogin.style.display = 'none';
            if (btnRegistro) btnRegistro.style.display = 'none';

            // 1.5 Creamos dinamicamente el boton de Historial de Pedidos
            let btnPedidos = document.getElementById('btn-nav-historial');
            if (!btnPedidos) {
                btnPedidos = document.createElement('button');
                btnPedidos.className = 'btn-usuario-item'; 
                btnPedidos.id = 'btn-nav-historial';
                // Icono de bolsa de compras para diferenciarlo de la tuerca de configuracion
                btnPedidos.innerHTML = "<i class='bx bx-shopping-bag'></i> Mis Pedidos";
                
                // Lo insertamos en el menu, justo ANTES de Configuracion
                if (btnConfiguracion && btnConfiguracion.parentNode) {
                    btnConfiguracion.parentNode.insertBefore(btnPedidos, btnConfiguracion);
                }
                
                // Le damos la orden para navegar al historial al hacer clic
                btnPedidos.addEventListener('click', () => { cerrarUsuario(); navegarA('historial'); });
            }

            // 2. Creamos y agregamos dinamicamente el boton de Cerrar Sesion
            // Asi no tienes que modificar el HTML manualmente
            let btnSalir = document.getElementById('btn-nav-salir');
            if (!btnSalir) {
                btnSalir = document.createElement('button'); // Ahora es un boton
                btnSalir.className = 'btn-usuario-item'; // Misma clase que Configuracion
                btnSalir.id = 'btn-nav-salir';
                btnSalir.style.color = '#ff4d4d'; // Color rojo

                // Usamos el icono Boxicons para que coincida exactamente con los otros
                btnSalir.innerHTML = "<i class='bx bx-log-out'></i> Salir de sesion";

                // Lo insertamos en el menu, justo ANTES del separador gris
                if (btnConfiguracion && btnConfiguracion.parentNode) {
                    const separador = document.querySelector('.sidebar-body .separador');
                    if (separador) {
                        btnConfiguracion.parentNode.insertBefore(btnSalir, separador);
                    } else {
                        btnConfiguracion.parentNode.insertBefore(btnSalir, btnConfiguracion);
                    }
                }
            }

            // 3. Le damos la orden para destruir la sesion en el servidor al hacerle clic
            btnSalir.addEventListener('click', async (e) => {
                e.preventDefault();
                await fetch('logout');
                sessionStorage.setItem('vistaActual', 'inicio'); // Volvemos al inicio tras salir
                window.location.reload();
            });
        }
    } catch (error) {
        console.error('Error comprobando sesion en el sidebar:', error);
    }
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