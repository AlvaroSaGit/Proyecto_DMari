import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador para la navegacion
import { navegarA } from '../../../router/router.js';

// funcion principal que carga la vista de login en el contenedor principal
export async function cargarVistaLogin() {
    // inyectamos el html de login en el main, esperando a que termine
    await cargarComponente('component-main', './src/views/Auth/login/login.html');
    
    // iniciamos la logica del formulario una vez que el dom esta listo
    prepararFormularioLogin();
}

// funcion interna para manejar los eventos del formulario de login
function prepararFormularioLogin() {
    // capturamos los elementos clave del dom
    const formulario = document.getElementById('form-login');
    const linkRegistro = document.getElementById('link-ir-registro');
    
    // damos accion al boton de ir a registro por si el usuario no tiene cuenta
    if (linkRegistro) {
        linkRegistro.addEventListener('click', function(evento) {
            // evitamos que el enlace recargue la pagina (comportamiento por defecto)
            evento.preventDefault();
            // usamos el enrutador para ir a la vista de registro
            navegarA('registro');
        });
    }
    
    // si por alguna razon no se cargo el formulario, detenemos la ejecucion
    if (!formulario) return;
    
    // escuchamos el evento submit cuando el usuario intenta enviar sus datos
    formulario.addEventListener('submit', async function(evento) {
        // evitamos que la pagina recargue al enviar el formulario
        evento.preventDefault();
        
        // extraemos los valores que el usuario escribio en los inputs
        const correo = document.getElementById('login-correo').value;
        const password = document.getElementById('login-password').value;
        
        // validacion basica para asegurar que no envien campos vacios
        if (correo === '' || password === '') {
            alert('por favor completa todos los campos');
            return; // cortamos la funcion aqui si faltan datos
        }
        
        // validacion para que la contrasena no contenga espacios en blanco
        if (password.includes(' ')) {
            alert('La contraseña no puede contener espacios en blanco');
            return;
        }
        
        // Empaquetamos los datos con URLSearchParams para que Java los lea facil con request.getParameter
        const parametros = new URLSearchParams();
        parametros.append('correo', correo);
        parametros.append('password', password);
        
        // bloque try-catch para manejar errores de conexion con el servidor
        try {
            const respuesta = await fetch('login', {
                method: 'POST',
                body: parametros
            });
            
            if (respuesta.ok) {
                // extraemos el json que nos mando java con el id del rol
                const datos = await respuesta.json();
                
                // Guardamos el rol en la memoria del navegador para que el enrutador sepa quién está navegando
                sessionStorage.setItem('rolUsuario', datos.idRol);
                
                alert('¡inicio de sesion exitoso!');
                formulario.reset();
                
                // redirigimos a la vista correspondiente segun el rol asignado en la base de datos.
                // Se corrigen los IDs segun los INSERTs reales en la BD (Admin = 1, Proveedor = 4, Cliente = 2)
                if (datos.idRol === 1) { 
                    window.location.hash = 'admin-productos';
                } else if (datos.idRol === 4) { 
                    window.location.hash = 'proveedor-productos';
                } else { // Cliente (ID 2) u otros roles
                    window.location.hash = 'inicio';
                }
                
                window.location.reload();
            } else {
                alert('correo o contrasena incorrectos');
            }
        } catch (error) {
            console.error('error al iniciar sesion:', error);
            alert('hubo un problema de conexion al intentar entrar.');
        }
    });
}