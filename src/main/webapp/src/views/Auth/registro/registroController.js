// importamos tu servicio de ui para inyectar componentes
import { cargarComponente } from '../../../services/uiService.js';
// importamos la vista de login para navegar hacia ella cuando le den al enlace
import { cargarVistaLogin } from '../login/loginController.js';

// funcion principal encargada de mostrar el formulario de registro en pantalla
export async function cargarVistaRegistro() {
    // inyectamos el html de registro en la caja fuerte del main
    await cargarComponente('component-main', './src/views/Auth/registro/registro.html');
    
    // iniciamos la logica de capturas y botones ya que el html se ha cargado
    prepararFormularioRegistro();
}

// funcion interna para configurar eventos y capturar los datos ingresados
function prepararFormularioRegistro() {
    // buscamos el formulario entero por su clase css
    const formulario = document.querySelector('.formulario-registro');
    const linkLogin = document.getElementById('link-ir-login');
    
    // damos accion al boton inferior por si el usuario ya tenia cuenta
    if (linkLogin) {
        linkLogin.addEventListener('click', function(evento) {
            // prevenimos que la pagina salte a arriba
            evento.preventDefault();
            // llamamos al login para reemplazar la vista
            cargarVistaLogin();
        });
    }
    
    // validacion de seguridad por si falla la carga html
    if (!formulario) return;
    
    // escuchamos el submit cuando apretan el boton ingresar
    formulario.addEventListener('submit', async function(evento) {
        // evitamos que la pagina recargue borrando todos los datos
        evento.preventDefault();
        
        // extraemos los valores exactos que ingreso el usuario en ese momento
        const nombre = document.getElementById('reg-nombre').value;
        const correo = document.getElementById('reg-correo').value;
        const password = document.getElementById('reg-password').value;
        
        // validacion manual para obligarlos a llenar las 3 cajas
        if (nombre === '' || correo === '' || password === '') {
            alert('por favor completa todos los campos');
            // detenemos la ejecucion si hay error cortando la funcion
            return; 
        }
        
        // validamos que la contrasena cumpla el minimo de seguridad
        if (password.length < 6) {
            alert('la contrasena debe tener al menos 6 caracteres');
            return;
        }
        
        // Usamos URLSearchParams para enviar los datos como formulario (facilita la lectura en Java sin librerías extra)
        const parametros = new URLSearchParams();
        parametros.append('nombre', nombre);
        parametros.append('correo', correo);
        parametros.append('password', password);
        
        // envolvemos en un trycatch para atajar problemas de internet o de base de datos
        try {
            // peticion fetch hacia tu nuevo AuthController en Java en la ruta /registro
            const respuesta = await fetch('registro', {
                method: 'POST',
                body: parametros
            });
            
            if (respuesta.ok) {
                alert('¡Registro exitoso! Ahora inicia sesion.');
                formulario.reset();
                cargarVistaLogin(); // Enviamos al usuario a la vista de login
            } else {
                alert('Hubo un error en el registro.');
            }
        } catch (error) {
            // si falla la promesa de java caera aqui sin crashear la pagina
            console.error('error al conectar con el servidor:', error);
            alert('hubo un problema de conexion');
        }
    });
}