/*
    objetivo de este archivo:
    controlador principal de la vista de configuracion de cuenta.
    se encarga de gestionar dos procesos criticos del usuario:
    1. la actualizacion de sus datos personales y direcciones de entrega.
    2. la seguridad de su cuenta mediante el cambio encriptado de su contrasena.
*/

// importamos el servicio necesario para inyectar la vista html en la pantalla
import { cargarComponente, mostrarMensaje } from '../../../services/uiService.js';
// importamos el enrutador para redireccionar al usuario si no tiene permisos
import { navegarA } from '../../../router/router.js';

export async function cargarVistaConfiguracion() {
    // 1. barrera de seguridad: verificamos con el servidor de java si el usuario realmente inicio sesion
    try {
        const respuesta = await fetch('session');
        if (!respuesta.ok) {
            mostrarMensaje('Debes iniciar sesión para acceder a tu configuración.', 'error');
            navegarA('login'); // lo rebotamos a la pantalla de login
            return; // cortamos la ejecucion para que no cargue la vista
        }
    } catch (error) {
        console.error('error al verificar sesion:', error);
        return;
    }

    // 2. inyeccion del html
    // el truco de "?t=" mas el tiempo actual evita que el navegador use una version vieja (cacheada) del html
    await cargarComponente('component-main', './src/views/Cliente/configuracion/configuracion.html?t=' + new Date().getTime());
    
    // 3. activamos todos los escuchadores de los botones y cajas de texto
    prepararFormularioPerfil();
}

/*
    funcion interna que conecta los formularios de la pantalla con las apis de java.
*/
async function prepararFormularioPerfil() {
    // extraemos las referencias de ambos formularios
    const formPerfil = document.getElementById('form-config-perfil');
    const formPassword = document.getElementById('form-cambiar-password');

    // ====================================================================
    // bloque 1: logica para el formulario de edicion de datos de envio
    // ====================================================================
    if (formPerfil) {
        // peticion get: le pedimos a java los datos actuales del cliente para pre-llenar las cajas
        try {
            // usamos cache-busting estricto para evitar que cruce datos entre usuarios nuevos
            const respuesta = await fetch('perfil-cliente?t=' + Date.now(), { cache: 'no-store' });
            if (respuesta.ok) {
                const datos = await respuesta.json();
                // si la base de datos devolvio informacion, la inyectamos en cada input respectivo
                if (datos.telefono) document.getElementById('conf-telefono').value = datos.telefono;
                if (datos.telefono_secundario) document.getElementById('conf-telefono-sec').value = datos.telefono_secundario;
                if (datos.direccion) document.getElementById('conf-direccion').value = datos.direccion;
                if (datos.direccion_detallada) document.getElementById('conf-detalle').value = datos.direccion_detallada;
                if (datos.referencia_ubicacion) document.getElementById('conf-referencia').value = datos.referencia_ubicacion;
                
                // inyectamos los nuevos datos fijos de la cuenta
                if (datos.nombre !== undefined) {
                    const inNombre = document.getElementById('conf-nombre');
                    if (inNombre) inNombre.value = datos.nombre;
                }
                if (datos.apellido !== undefined) {
                    const inApellido = document.getElementById('conf-apellido');
                    if (inApellido) inApellido.value = datos.apellido;
                }
                // validacion robusta para que inyecte el correo aun si llega vacio
                if (datos.correo !== undefined) {
                    const inputCorreo = document.getElementById('conf-correo');
                    if (inputCorreo) inputCorreo.value = datos.correo;
                }
            }
        } catch (error) { console.error('error al cargar perfil:', error); }

        // evento submit: cuando el usuario le da clic al boton de guardar informacion
        formPerfil.addEventListener('submit', async (e) => {
            e.preventDefault(); // prevenimos la recarga molesta de la pagina
            
            // usamos urlsearchparams para empaquetar los datos de una forma que java entienda facilmente
            const parametros = new URLSearchParams();
            parametros.append('numeroTelefono', document.getElementById('conf-telefono').value);
            parametros.append('telefonoSecundario', document.getElementById('conf-telefono-sec').value);
            parametros.append('direccionPrimaria', document.getElementById('conf-direccion').value);
            parametros.append('direccionDetalle', document.getElementById('conf-detalle').value);
            parametros.append('referencia', document.getElementById('conf-referencia').value);

            // peticion post: mandamos el paquete al servlet de clientecontroller
            try {
                const respuesta = await fetch('perfil-cliente', { method: 'POST', body: parametros });
                if (respuesta.ok) mostrarMensaje('¡Tu información de envío ha sido actualizada correctamente!', 'exito');
                else mostrarMensaje('Hubo un error al actualizar la información.', 'error');
            } catch (error) { console.error('error al enviar perfil:', error); }
        });
    }

    // ====================================================================
    // bloque 2: logica de seguridad (cambio de contrasena)
    // ====================================================================
    if (formPassword) {
        formPassword.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // capturamos las 3 contrasenas digitadas
            const passActual = document.getElementById('pass-actual').value;
            const passNueva = document.getElementById('pass-nueva').value;
            const passConfirm = document.getElementById('pass-confirm').value;

            // validacion del lado del cliente: comprobamos que no se haya equivocado al repetir la clave
            if (passNueva !== passConfirm) {
                mostrarMensaje('Las contraseñas nuevas no coinciden.', 'error');
                return;
            }

            // preparamos el paquete seguro para java
            const parametros = new URLSearchParams();
            parametros.append('passActual', passActual);
            parametros.append('passNueva', passNueva);

            // enviamos al passwordcontroller para que ejecute la funcion aes_encrypt en mysql
            try {
                const respuesta = await fetch('cambiar-password', { method: 'POST', body: parametros });
                if (respuesta.ok) {
                    mostrarMensaje('¡Tu contraseña ha sido cambiada con éxito!', 'exito');
                    formPassword.reset(); // vaciamos las cajas por seguridad
                } else {
                    mostrarMensaje('La contraseña actual es incorrecta o hubo un error.', 'error');
                }
            } catch (error) { console.error('error al cambiar pass:', error); }
        });
    }
}