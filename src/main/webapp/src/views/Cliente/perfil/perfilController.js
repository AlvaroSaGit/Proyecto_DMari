/*
    objetivo de este archivo:
    controlar el comportamiento de la vista de "perfil" del cliente.
    se encarga de descargar la direccion y telefono guardados desde mysql
    y enviarlos de vuelta cuando el usuario los actualice.
*/
import { cargarComponente } from '../../../services/uiService.js';

// funcion de arranque, llamada por el enrutador
export async function cargarVistaPerfil() {
    await cargarComponente('component-main', './src/views/Cliente/perfil/perfil.html');
    prepararFormularioPerfil();
}

async function prepararFormularioPerfil() {
    const formulario = document.getElementById('form-perfil-cliente');
    if (!formulario) return;

    // 1. cargar datos actuales del cliente al abrir la pagina
    try {
        // hacemos una peticion get al servlet perfilcontroller (java)
        const respuesta = await fetch('perfil-cliente');
        
        if (respuesta.ok) {
            const perfil = await respuesta.json();
            
            // inyectamos los datos en los inputs solo si vienen con informacion valida
            if (perfil.telefono) document.getElementById('perfil-telefono').value = perfil.telefono;
            if (perfil.direccion) document.getElementById('perfil-direccion').value = perfil.direccion;
            if (perfil.direccion_detallada) document.getElementById('perfil-detalle').value = perfil.direccion_detallada;
            if (perfil.referencia_ubicacion) document.getElementById('perfil-referencia').value = perfil.referencia_ubicacion;
            if (perfil.telefono_secundario) document.getElementById('perfil-telefono-sec').value = perfil.telefono_secundario;
        }
    } catch (error) {
        console.error('Error al cargar el perfil actual', error);
    }

    // 2. escuchar cuando el usuario intente guardar los datos nuevos
    formulario.addEventListener('submit', async (e) => {
        e.preventDefault(); // evitamos que la pagina parpadee o recargue
        
        const btnGuardar = document.getElementById('btn-guardar-perfil');
        btnGuardar.innerText = 'Guardando...';
        btnGuardar.disabled = true; // previene doble clic accidental

        // empaquetamos los datos usando urlsearchparams para que java los pueda
        // atrapar facilmente usando request.getparameter("nombre_campo");
        const params = new URLSearchParams();
        params.append('numeroTelefono', document.getElementById('perfil-telefono').value);
        params.append('direccionPrimaria', document.getElementById('perfil-direccion').value);
        params.append('direccionDetalle', document.getElementById('perfil-detalle').value);
        params.append('referencia', document.getElementById('perfil-referencia').value);
        params.append('telefonoSecundario', document.getElementById('perfil-telefono-sec').value);

        try {
            // hacemos un post al mismo servlet para decirle a java que guarde
            const res = await fetch('perfil-cliente', {
                method: 'POST',
                body: params
            });
            
            if (res.ok) {
                // si todo salio bien, le avisamos al cliente.
                // en este punto, el clientedao hizo su magia y distribuyo la info en las 3 tablas.
                alert('¡Tus datos de envío se han guardado con éxito!');
                
                // (opcional) si el cliente vino aqui bloqueado desde el carrito, 
                // podria querer volver al catalogo a seguir comprando.
                window.location.hash = 'catalogo';
            } else {
                alert('Hubo un error al intentar guardar tu perfil. Revisa tu conexión.');
            }
        } catch (error) {
            console.error('Fallo de red al guardar perfil', error);
        } finally {
            // restauramos el boton a la normalidad
            btnGuardar.innerText = 'Guardar Perfil';
            btnGuardar.disabled = false;
        }
    });
}
