// importamos el servicio necesario para cargar la vista html
import { cargarComponente } from '../../../services/uiService.js';
// importamos el enrutador
import { navegarA } from '../../../router/router.js';

// funcion principal que renderiza la vista de configuracion del cliente
export async function cargarVistaConfiguracion() {
    // Verificamos primero si el usuario tiene sesion activa
    try {
        const respuesta = await fetch('session');
        if (!respuesta.ok) {
            alert('Debes iniciar sesion para acceder a tu configuracion.');
            navegarA('login'); // Lo mandamos a la pantalla de login
            return; // Detenemos la carga de la vista de configuracion
        }
    } catch (error) {
        console.error('Error al verificar sesion:', error);
        return;
    }

    // inyectamos la estructura html de la configuracion en el main.
    // agregamos '?t=' + timestamp para destruir la cache del navegador y obligarlo a mostrar los campos nuevos.
    await cargarComponente('component-main', './src/views/Cliente/configuracion/configuracion.html?t=' + new Date().getTime());
    
    // inicializamos la logica del formulario
    prepararFormularioPerfil();
}

async function prepararFormularioPerfil() {
    const formPerfil = document.getElementById('form-config-perfil');
    const formPassword = document.getElementById('form-cambiar-password');

    // 1. logica para el formulario de edicion de perfil
    if (formPerfil) {
        try {
            const respuesta = await fetch('perfil-cliente');
            if (respuesta.ok) {
                const datos = await respuesta.json();
                if (datos.telefono) document.getElementById('conf-telefono').value = datos.telefono;
                if (datos.telefonoSecundario) document.getElementById('conf-telefono-sec').value = datos.telefonoSecundario;
                if (datos.direccion) document.getElementById('conf-direccion').value = datos.direccion;
                if (datos.direccionDetalle) document.getElementById('conf-detalle').value = datos.direccionDetalle;
                if (datos.referencia) document.getElementById('conf-referencia').value = datos.referencia;
            }
        } catch (error) { console.error('error al cargar perfil:', error); }

        formPerfil.addEventListener('submit', async (e) => {
            e.preventDefault();
            const parametros = new URLSearchParams();
            parametros.append('telefono', document.getElementById('conf-telefono').value);
            parametros.append('telefonoSecundario', document.getElementById('conf-telefono-sec').value);
            parametros.append('direccion', document.getElementById('conf-direccion').value);
            parametros.append('direccionDetalle', document.getElementById('conf-detalle').value);
            parametros.append('referencia', document.getElementById('conf-referencia').value);

            try {
                const respuesta = await fetch('perfil-cliente', { method: 'POST', body: parametros });
                if (respuesta.ok) alert('¡tu informacion de envio ha sido actualizada correctamente!');
                else alert('hubo un error al actualizar la informacion.');
            } catch (error) { console.error('error al enviar perfil:', error); }
        });
    }

    // 2. logica para el formulario de cambio de contrasena
    if (formPassword) {
        formPassword.addEventListener('submit', async (e) => {
            e.preventDefault();
            const passActual = document.getElementById('pass-actual').value;
            const passNueva = document.getElementById('pass-nueva').value;
            const passConfirm = document.getElementById('pass-confirm').value;

            if (passNueva !== passConfirm) {
                alert('las contrasenas nuevas no coinciden');
                return;
            }

            const parametros = new URLSearchParams();
            parametros.append('passActual', passActual);
            parametros.append('passNueva', passNueva);

            try {
                const respuesta = await fetch('cambiar-password', { method: 'POST', body: parametros });
                if (respuesta.ok) {
                    alert('¡tu contrasena ha sido cambiada con exito!');
                    formPassword.reset();
                } else {
                    alert('la contrasena actual es incorrecta o hubo un error.');
                }
            } catch (error) { console.error('error al cambiar pass:', error); }
        });
    }
}