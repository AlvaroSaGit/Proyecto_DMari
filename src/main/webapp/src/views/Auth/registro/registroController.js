// importamos tu servicio de ui
import { cargarComponente } from '../../../services/uiService.js';
// importamos la vista de login para navegar hacia ella
import { cargarVistaLogin } from '../login/loginController.js';

// funcion principal para mostrar el registro
export async function cargarVistaRegistro() {
    // inyectamos el html de registro en el main
    await cargarComponente('component-main', './src/views/Auth/registro/registro.html');
    
    // iniciamos la logica del formulario ya que el html cargo
    prepararFormularioRegistro();
}

// funcion para capturar los datos
function prepararFormularioRegistro() {
    // buscamos el formulario por su clase
    const formulario = document.querySelector('.formulario-registro');
    const linkLogin = document.getElementById('link-ir-login');
    
    // damos accion al boton de ir a login
    if (linkLogin) {
        linkLogin.addEventListener('click', function(evento) {
            // prevenimos que la pagina salte
            evento.preventDefault();
            cargarVistaLogin();
        });
    }
    
    // si no cargo el formulario salimos para evitar errores
    if (!formulario) return;
    
    // escuchamos cuando envien el formulario
    formulario.addEventListener('submit', async function(evento) {
        // evitamos que la pagina recargue
        evento.preventDefault();
        
        // obtenemos los valores de los inputs por su id
        const nombre = document.getElementById('reg-nombre').value;
        const correo = document.getElementById('reg-correo').value;
        const password = document.getElementById('reg-password').value;
        
        // validacion basica manual para que no haya vacios
        if (nombre === '' || correo === '' || password === '') {
            alert('por favor completa todos los campos');
            // detenemos la ejecucion si hay error
            return; 
        }
        
        // validamos la longitud de la contraseña
        if (password.length < 6) {
            alert('la contraseña debe tener al menos 6 caracteres');
            return;
        }
        
        // preparamos los datos para java
        const datosUsuario = {
            nombre: nombre,
            correo: correo,
            password: password
        };
        
        // usamos trycatch para manejar la futura conexion con java
        try {
            // aqui ira el fetch hacia tu backend en java
            console.log('datos listos para enviar a java:', datosUsuario);
            alert('enviando datos al servidor java...');
            
            // limpiamos el formulario asumiendo exito
            formulario.reset();
        } catch (error) {
            // si java o la red fallan lo atrapamos aqui
            console.error('error al conectar con el servidor:', error);
            alert('hubo un problema de conexion');
        }
    });
}