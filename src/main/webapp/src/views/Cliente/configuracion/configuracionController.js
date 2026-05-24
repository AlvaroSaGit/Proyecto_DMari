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

    // inyectamos la estructura html de la configuracion en el main
    await cargarComponente('component-main', './src/views/Cliente/configuracion/configuracion.html');
    
    // capturamos el nuevo boton para ir al historial
    const btnIrHistorial = document.getElementById('btn-ir-historial');
    if (btnIrHistorial) {
        btnIrHistorial.addEventListener('click', () => {
            navegarA('historial'); // Llama al router para cambiar de pantalla
        });
    }
    
    // inicializamos la logica del formulario
    prepararFormularioPerfil();
}

async function prepararFormularioPerfil() {
    const form = document.getElementById('form-perfil-cliente');
    if (!form) return;

    // 1. cargar los datos actuales del cliente si ya tenia perfil guardado
    try {
        const respuesta = await fetch('perfil-cliente');
        if (respuesta.ok) {
            const datos = await respuesta.json();
            // si hay direccion, llenamos los campos automaticamente
            if (datos.direccion) {
                document.getElementById('cli-direccion').value = datos.direccion;
                document.getElementById('cli-telefono').value = datos.telefono;
                if(document.getElementById('cli-referencia')) document.getElementById('cli-referencia').value = datos.referencia || '';
            }
        }
    } catch (error) { console.error('error al cargar perfil:', error); }

    // 2. enviar los datos nuevos o editados a la base de datos
    form.addEventListener('submit', async (e) => {
        e.preventDefault(); // prevenimos recarga

        const direccion = document.getElementById('cli-direccion').value;
        const telefono = document.getElementById('cli-telefono').value;
        const referencia = document.getElementById('cli-referencia') ? document.getElementById('cli-referencia').value : '';

        const parametros = new URLSearchParams();
        parametros.append('direccion', direccion);
        parametros.append('telefono', telefono);
        parametros.append('referencia', referencia);

        try {
            const respuesta = await fetch('perfil-cliente', { method: 'POST', body: parametros });

            if (respuesta.ok) {
                alert('¡tu informacion de envio ha sido guardada correctamente!');
            } else {
                alert('hubo un error al guardar la informacion.');
            }
        } catch (error) { console.error('error al enviar perfil:', error); }
    });
}