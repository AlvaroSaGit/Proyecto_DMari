/*
    objetivo de este archivo:
    controlar el comportamiento de la vista de "perfil" del cliente.
    se encarga de descargar la direccion y telefono guardados desde mysql
    y enviarlos de vuelta cuando el usuario los actualice.
*/
import { cargarComponente } from '../../../services/uiService.js';
import { validarSoloNumeros, validarTelefono, mostrarErrorCampo, limpiarErrorCampo, validarPassword } from '../../../services/validacionHelper.js';

// funcion de arranque, llamada por el enrutador
export async function cargarVistaPerfil() {
    // inyeccion del html de la vista de perfil en el contenedor spa
    await cargarComponente('component-main', './src/views/Cliente/perfil/perfilCliente.html');
    // configuracion de eventos y carga de datos iniciales
    prepararFormularioPerfil();
}

async function prepararFormularioPerfil() {
    const formulario = document.getElementById('form-perfil-cliente');
    const formPassword = document.getElementById('form-cambiar-password');
    
    // verificacion de integridad de la vista cargada
    if (!formulario) {
        console.warn('Advertencia: no se encontro el <form id="form-perfil-cliente"> en el html. Revisa el nombre.');
    }

    // --- VALIDACIÓN EN TIEMPO REAL PARA CAMPOS DE TELÉFONO ---
    const inTelefono = document.getElementById('perfil-telefono');
    const inTelSec = document.getElementById('perfil-telefono-sec');

    if (inTelefono) inTelefono.addEventListener('input', validarSoloNumeros);
    if (inTelSec) inTelSec.addEventListener('input', validarSoloNumeros);
    // --- FIN DE LA VALIDACIÓN ---


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
    
            // --- BLOQUE DE VALIDACIÓN CON MENSAJES DE ERROR ---
            let esValido = true;
            const telefono = document.getElementById('perfil-telefono')?.value || '';
            const telefonoSec = document.getElementById('perfil-telefono-sec')?.value || '';

            // Limpiamos los errores previos antes de volver a validar
            limpiarErrorCampo('perfil-telefono');
            limpiarErrorCampo('perfil-telefono-sec');

            // Validamos el teléfono principal
            if (!validarTelefono(telefono)) {
                mostrarErrorCampo('perfil-telefono', 'El teléfono principal debe tener entre 7 y 15 dígitos.');
                esValido = false;
            }

            // El teléfono secundario es opcional, pero si se ingresa, debe ser válido.
            // Usamos telefonoSec (la variable con el valor) para no validar un campo vacío.
            if (telefonoSec && !validarTelefono(telefonoSec)) {
                mostrarErrorCampo('perfil-telefono-sec', 'El teléfono alternativo debe tener entre 7 y 15 dígitos.');
                esValido = false;
            }

            if (!esValido) {
                // Si hay errores, restauramos el botón y detenemos el envío.
                if(btnGuardar) { btnGuardar.innerText = 'Guardar Perfil'; btnGuardar.disabled = false; }
                return;
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
                    // confirmacion al usuario en pantalla sin expulsarlo de la vista
                    mostrarMensaje('¡Tus datos de envio se han guardado con exito!', 'exito');
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

    // 3. escuchar cuando el usuario intente cambiar su contraseña
    if (formPassword) {
        formPassword.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const passActual = document.getElementById('pass-actual').value;
            const passNueva = document.getElementById('pass-nueva').value;
            const passConfirm = document.getElementById('pass-confirm').value;

            // --- Bloque de Validación de Contraseña ---
            limpiarErrorCampo('pass-nueva');
            limpiarErrorCampo('pass-confirm');
            let esValido = true;

            if (!validarPassword(passNueva)) {
                mostrarErrorCampo('pass-nueva', 'Debe tener 8+ caracteres, 1 mayúscula y 1 número.');
                esValido = false;
            }

            if (passNueva !== passConfirm) {
                mostrarErrorCampo('pass-confirm', 'Las contraseñas nuevas no coinciden.');
                esValido = false;
            }

            if (!esValido) {
                return;
            }
            const parametros = new URLSearchParams();
            parametros.append('passActual', passActual);
            parametros.append('passNueva', passNueva);

            try {
                const respuesta = await fetch('cambiar-password', { method: 'POST', body: parametros });
                if (respuesta.ok) {
                    alert('¡Tu contraseña ha sido cambiada con éxito!');
                    formPassword.reset();
                } else {
                    alert('La contraseña actual es incorrecta o hubo un error.');
                }
            } catch (error) { console.error('error al cambiar pass:', error); }
        });
    }
}

/**
 * Crea una notificación minimalista acorde a los colores de la tienda (Oscuros).
 * Reemplaza los molestos alert() y evita el uso de colores rojos agresivos.
 */
function mostrarMensaje(mensaje, tipo) {
    const toast = document.createElement('div');
    const icono = tipo === 'exito' ? '✓ ' : '⚠ ';
    toast.innerText = icono + mensaje;
    
    // Usamos colores grises/oscuros elegantes (naturaleza de la pagina) en lugar de rojos
    const colorFondo = tipo === 'exito' ? '#212529' : '#343a40'; 
    toast.style.cssText = `position: fixed; bottom: 30px; right: 30px; background: ${colorFondo}; color: white; padding: 15px 25px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.2); font-weight: 500; font-family: sans-serif; z-index: 10000; transition: opacity 0.5s ease;`;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 500);
    }, 3500);
}
