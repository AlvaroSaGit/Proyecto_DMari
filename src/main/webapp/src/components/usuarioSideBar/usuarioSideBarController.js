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

            // 1.5 creamos dinamicamente el boton de historial de pedidos
            let btnPedidos = document.getElementById('btn-nav-historial');
            if (!btnPedidos) {
                btnPedidos = document.createElement('button');
                btnPedidos.className = 'btn-usuario-item'; 
                btnPedidos.id = 'btn-nav-historial';
                // icono de bolsa de compras para diferenciarlo de la tuerca de configuracion
                btnPedidos.innerHTML = "<i class='bx bx-shopping-bag'></i> Mis pedidos";
                
                // lo insertamos en el menu, justo antes de configuracion
                if (btnConfiguracion && btnConfiguracion.parentNode) {
                    btnConfiguracion.parentNode.insertBefore(btnPedidos, btnConfiguracion);
                }
                
                // le damos la orden para navegar al historial al hacer clic
                btnPedidos.addEventListener('click', () => { cerrarUsuario(); navegarA('historial'); });
            }

            // 1.6 creamos dinamicamente el boton de mi perfil
            let btnPerfilUsuario = document.getElementById('btn-nav-perfil-usuario');
            if (!btnPerfilUsuario) {
                btnPerfilUsuario = document.createElement('button');
                btnPerfilUsuario.className = 'btn-usuario-item'; 
                btnPerfilUsuario.id = 'btn-nav-perfil-usuario';
                // icono de usuario para mantener el diseno visual (boxicons)
                btnPerfilUsuario.innerHTML = "<i class='bx bx-user'></i> mi perfil";
                
                // lo insertamos en el menu, justo antes de configuracion
                if (btnConfiguracion && btnConfiguracion.parentNode) {
                    btnConfiguracion.parentNode.insertBefore(btnPerfilUsuario, btnConfiguracion);
                }
                
                // le damos la orden para navegar al perfil al hacer clic
                btnPerfilUsuario.addEventListener('click', () => { cerrarUsuario(); navegarA('perfil'); });
            }

            // 2. creamos y agregamos dinamicamente el boton de cerrar sesion
            // asi no tienes que modificar el html manualmente
            let btnSalir = document.getElementById('btn-nav-salir');
            if (!btnSalir) {
                btnSalir = document.createElement('button'); // ahora es un boton
                btnSalir.className = 'btn-usuario-item'; // misma clase que configuracion
                btnSalir.id = 'btn-nav-salir';
                btnSalir.style.color = '#ff4d4d'; // color rojo

                // usamos el icono boxicons para que coincida exactamente con los otros
                btnSalir.innerHTML = "<i class='bx bx-log-out'></i> salir de sesion";

                // lo insertamos al fondo del menu, debajo de todas las demas opciones
                if (btnConfiguracion && btnConfiguracion.parentNode) {
                    // movemos la linea separadora original para que quede justo encima del boton de salir
                    const separador = document.querySelector('.sidebar-body .separador');
                    if (separador) {
                        btnConfiguracion.parentNode.appendChild(separador);
                    }
                    
                    btnConfiguracion.parentNode.appendChild(btnSalir);
                }
            }

            // 3. le damos la orden para destruir la sesion en el servidor al hacerle clic
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