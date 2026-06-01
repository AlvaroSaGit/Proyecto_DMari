// importamos el servicio de ui para inyectar componentes
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador
import { navegarA } from '../../../router/router.js';

// funcion principal encargada de mostrar el formulario de registro en pantalla
export async function cargarVistaRegistro() {
    // inyectamos el html de registro en la caja fuerte del main
    await cargarComponente('component-main', './src/views/Auth/registro/registro.html');
    
    // iniciamos la logica de capturas y botones ya que el html se ha cargado
    prepararFormularioRegistro();
}

// funcion interna para configurar eventos y capturar los datos ingresados
function prepararFormularioRegistro() {
    // buscamos el formulario (soporta busqueda por id o por clase para evitar fallos silenciosos)
    const formulario = document.getElementById('form-registro') || document.querySelector('.formulario-registro');
    const linkLogin = document.getElementById('link-ir-login');
    
    // damos accion al boton inferior por si el usuario ya tenia cuenta
    if (linkLogin) {
        linkLogin.addEventListener('click', function(evento) {
            // prevenimos que la pagina salte a arriba
            evento.preventDefault();
            // llamamos al enrutador para reemplazar la vista
            navegarA('login');
        });
    }
    
    // validacion de seguridad por si falla la carga html
    if (!formulario) {
        console.error('Critico: no se encontro el formulario de registro en el dom. Verifica que exista el id "form-registro" en el html.');
        return;
    }
    
    // escuchamos el submit cuando apretan el boton ingresar
    formulario.addEventListener('submit', async function(evento) {
        // evitamos que la pagina recargue borrando todos los datos
        evento.preventDefault();
        
        // extraemos los valores exactos que ingreso el usuario en ese momento
        const nombre = document.getElementById('reg-nombre').value;
        const correo = document.getElementById('reg-correo').value;
        const password = document.getElementById('reg-password').value;
        
        // Usamos URLSearchParams para enviar los datos como formulario (facilita la lectura en Java sin librerías extra)
        const parametros = new URLSearchParams();
        parametros.append('nombre', nombre);
        parametros.append('correo', correo);
        parametros.append('password', password);
        
        // envolvemos en un trycatch para atajar problemas de internet o de base de datos
        try {
            // peticion fetch hacia el controlador de autenticacion en java en la ruta /registro
            const respuesta = await fetch('registro', {
                method: 'POST',
                body: parametros
            });
            
            if (respuesta.ok) {
                alert('¡Registro exitoso! Ahora inicia sesion.');
                formulario.reset();
                navegarA('login'); // Enviamos al usuario a la vista de login
            } else {
                // capturamos el mensaje de error que viene desde el validacionhelper de java
                const mensajeError = await respuesta.text();
                alert(mensajeError || 'error al registrarse');
            }
        } catch (error) {
            // si falla la promesa de java caera aqui sin crashear la pagina
            console.error('Error al conectar con el servidor:', error);
            alert('Hubo un problema de conexion');
        }
    });
}