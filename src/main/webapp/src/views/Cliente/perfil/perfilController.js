/*
    objetivo de este archivo:
    controlar el comportamiento de la vista de "perfil" del cliente.
    se encarga de descargar la direccion y telefono guardados desde mysql
    y enviarlos de vuelta cuando el usuario los actualice.
*/
import { cargarComponente } from '../../../services/uiService.js';

// funcion de arranque, llamada por el enrutador
export async function cargarVistaPerfil() {
    // inyeccion del html de la vista de perfil en el contenedor spa
    await cargarComponente('component-main', './src/views/Cliente/perfil/perfilCliente.html');
    // configuracion de eventos y carga de datos iniciales
    prepararFormularioPerfil();
}

async function prepararFormularioPerfil() {
    const formulario = document.getElementById('form-perfil-cliente');
    
    // verificacion de integridad de la vista cargada
    if (!formulario) {
        console.warn('Advertencia: no se encontro el <form id="form-perfil-cliente"> en el html. Revisa el nombre.');
    }

    // 1. cargar datos actuales del cliente al abrir la pagina
    try {
        // hacemos una peticion get al servlet perfilcontroller (java)
        // usamos cache-busting estricto para evitar datos de usuarios viejos
        const respuesta = await fetch('perfil-cliente?t=' + Date.now(), { cache: 'no-store' });
        
        // procesamiento de la respuesta exitosa del servidor
        if (respuesta.ok) {
            const perfil = await respuesta.json();
            
            // atrapamos las referencias del html de forma segura
            const inTelefono = document.getElementById('perfil-telefono');
            const inDireccion = document.getElementById('perfil-direccion');
            const inDetalle = document.getElementById('perfil-detalle');
            const inReferencia = document.getElementById('perfil-referencia');
            const inTelSec = document.getElementById('perfil-telefono-sec');
            const inCorreo = document.getElementById('perfil-correo');
            const inNombre = document.getElementById('perfil-nombre');
            const inApellido = document.getElementById('perfil-apellido');
            
            // inyectamos los datos solo si el dato existe Y la caja de texto existe en el html (evita crash)
            if (perfil.telefono && inTelefono) inTelefono.value = perfil.telefono;
            if (perfil.direccion && inDireccion) inDireccion.value = perfil.direccion;
            if (perfil.direccion_detallada && inDetalle) inDetalle.value = perfil.direccion_detallada;
            if (perfil.referencia_ubicacion && inReferencia) inReferencia.value = perfil.referencia_ubicacion;
            if (perfil.telefono_secundario && inTelSec) inTelSec.value = perfil.telefono_secundario;
            
            // agregamos el correo por si el cliente tambien quiere visualizarlo aqui
            if (perfil.correo !== undefined && inCorreo) inCorreo.value = perfil.correo;
            if (perfil.nombre !== undefined && inNombre) inNombre.value = perfil.nombre;
            if (perfil.apellido !== undefined && inApellido) inApellido.value = perfil.apellido;
        }
    } catch (error) {
        console.error('Error al cargar el perfil actual', error);
    }

    // 2. escuchar cuando el usuario intente guardar los datos nuevos
    if (formulario) {
        // interceptor del evento de guardado
        formulario.addEventListener('submit', async (e) => {
            e.preventDefault(); // evitamos que la pagina parpadee o recargue
            
            // bloqueo de interfaz para evitar peticiones concurrentes
            const btnGuardar = document.getElementById('btn-guardar-perfil');
            if(btnGuardar) {
                btnGuardar.innerText = 'Guardando...';
                btnGuardar.disabled = true; // previene doble clic accidental
            }
    
            // empaquetamos los datos usando urlsearchparams para que java los pueda
            // atrapar facilmente usando request.getparameter("nombre_campo");
            const params = new URLSearchParams();
            params.append('numeroTelefono', document.getElementById('perfil-telefono')?.value || '');
            params.append('direccionPrimaria', document.getElementById('perfil-direccion')?.value || '');
            params.append('direccionDetalle', document.getElementById('perfil-detalle')?.value || '');
            params.append('referencia', document.getElementById('perfil-referencia')?.value || '');
            params.append('telefonoSecundario', document.getElementById('perfil-telefono-sec')?.value || '');
    
            try {
                // hacemos un post al mismo servlet para decirle a java que guarde
                const res = await fetch('perfil-cliente', {
                    method: 'POST',
                    body: params
                });
                
                if (res.ok) {
                    // confirmacion al usuario y redireccion al catalogo
                    alert('¡Tus datos de envio se han guardado con exito!');
                    window.location.hash = 'catalogo';
                } else {
                    alert('Hubo un error al intentar guardar tu perfil. Revisa tu conexion.');
                }
            } catch (error) {
                // captura de errores de comunicacion
                console.error('Fallo de red al guardar perfil', error);
            } finally {
                if(btnGuardar) {
                    btnGuardar.innerText = 'Guardar Perfil';
                    btnGuardar.disabled = false;
                }
            }
        });
    }
}
