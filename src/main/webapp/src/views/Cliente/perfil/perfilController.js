/*
    objetivo de este archivo:
    controlar la vista de solo lectura del perfil del cliente.
*/
import { cargarComponente } from '../../../services/uiService.js';

export async function cargarVistaPerfil() {
    // 1. inyectamos el html en el contenedor principal
    await cargarComponente('component-main', './src/views/Cliente/perfil/perfilCliente.html');
    
    // 2. capturamos los elementos del dom
    const txtTelefono = document.getElementById('txt-telefono');
    const txtTelefonoSec = document.getElementById('txt-telefono-sec');
    const txtDireccion = document.getElementById('txt-direccion');
    const txtDetalle = document.getElementById('txt-detalle');
    const txtReferencia = document.getElementById('txt-referencia');
    
    try {
        const respuesta = await fetch('perfil-cliente');
        if (respuesta.ok) {
            const datos = await respuesta.json();
            // llenamos los textos si el usuario ya tenia informacion guardada
            if (datos.telefono) txtTelefono.innerText = datos.telefono;
            if (datos.telefonoSecundario) txtTelefonoSec.innerText = datos.telefonoSecundario;
            if (datos.direccion) txtDireccion.innerText = datos.direccion;
            if (datos.direccionDetalle) txtDetalle.innerText = datos.direccionDetalle;
            if (datos.referencia) txtReferencia.innerText = datos.referencia;
        }
    } catch (error) {
        console.error('error al cargar el perfil:', error);
    }
}
