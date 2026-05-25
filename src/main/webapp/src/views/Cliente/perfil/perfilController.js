/*
    objetivo de este archivo:
    controlar la administracion y edicion de los datos del cliente.
*/
import { cargarComponente } from '../../../services/uiService.js';

export async function cargarVistaPerfil() {
    // 1. inyectamos el html en el contenedor principal
    await cargarComponente('component-main', './src/views/Cliente/perfil/perfilCliente.html');
    
    // 2. capturamos los elementos del dom
    const form = document.getElementById('form-perfil-cliente');
    const inputTelefono = document.getElementById('perfil-telefono');
    const inputTelefonoSec = document.getElementById('perfil-telefono-sec');
    const inputDireccion = document.getElementById('perfil-direccion');
    const inputDetalle = document.getElementById('perfil-detalle');
    const inputReferencia = document.getElementById('perfil-referencia');
    
    if (!form) return;

    // 3. traemos la data
    try {
        const respuesta = await fetch('perfil-cliente');
        if (respuesta.ok) {
            const datos = await respuesta.json();
            // llenamos los inputs si el usuario ya tenia informacion guardada
            if (datos.telefono) inputTelefono.value = datos.telefono;
            if (datos.telefonoSecundario) inputTelefonoSec.value = datos.telefonoSecundario;
            if (datos.direccion) inputDireccion.value = datos.direccion;
            if (datos.direccionDetalle) inputDetalle.value = datos.direccionDetalle;
            if (datos.referencia) inputReferencia.value = datos.referencia;
        }
    } catch (error) {
        console.error('error al cargar el perfil:', error);
    }

    // 4. manejamos el evento de guardar (metodo post)
    form.addEventListener('submit', async (e) => {
        e.preventDefault(); 
        
        const btnGuardar = form.querySelector('button[type="submit"]');
        btnGuardar.disabled = true;
        btnGuardar.innerText = 'guardando...';

        const parametros = new URLSearchParams();
        parametros.append('telefono', inputTelefono.value);
        parametros.append('telefonoSecundario', inputTelefonoSec.value);
        parametros.append('direccion', inputDireccion.value);
        parametros.append('direccionDetalle', inputDetalle.value);
        parametros.append('referencia', inputReferencia.value);

        try {
            const respuesta = await fetch('perfil-cliente', { method: 'POST', body: parametros });
            if (respuesta.ok) {
                alert('¡tu informacion se ha guardado correctamente!');
            } else {
                alert('hubo un error al guardar tu perfil. revisa tu conexion.');
            }
        } catch (error) { console.error('error al enviar perfil:', error); } 
        finally { btnGuardar.disabled = false; btnGuardar.innerText = 'guardar cambios'; }
    });
}
