// importamos el servicio para inyectar html y el controlador de registro
import { cargarComponente } from '../../../services/uiService.js';
import { cargarVistaRegistro } from '../registro/registroController.js';

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
            // llamamos al controlador de registro para cambiar de vista
            cargarVistaRegistro();
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
        
        // empaquetamos los datos en un objeto para enviarlos al backend
        const datosLogin = {
            correo: correo,
            password: password
        };
        
        // bloque try-catch para manejar errores de conexion con el servidor
        try {
            // simulamos el envio de datos (aqui ira el fetch a java)
            console.log('intentando iniciar sesion:', datosLogin);
            alert('enviando datos de login al servidor java...');
            // limpiamos el formulario despues de enviar
            formulario.reset();
        } catch (error) {
            // si falla la red o el servidor, lo mostramos en consola
            console.error('error al conectar con el servidor:', error);
            alert('hubo un problema de conexion');
        }
    });
}
