// importamos mostrarModal directamente del modulo uiService para que funcione con ES modules
// window.mostrarModal no existe porque los modulos ES no exponen nada al objeto window globalmente
import { mostrarModal, cerrarModalGeneral, mostrarMensaje } from '../../../services/uiService.js';

export async function cargarVistaSolicitudCategoria() {
    // construimos el html del formulario de solicitud de categoria
    const htmlModal = `
        <div style="font-family: 'Poppins', sans-serif;">
            <h3 style="margin-top: 0; color: #1a1a2e; border-bottom: 2px solid #e91e63; padding-bottom: 10px;">
                💡 Sugerir Nueva Categoría
            </h3>
            <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">
                ¿No encuentras la categoría adecuada para tus productos? Envía una sugerencia a los administradores.
            </p>
            
            <div style="margin-bottom: 15px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem; color: #333;">
                    Nombre de la Categoría *
                </label>
                <input 
                    type="text" 
                    id="sug-nombre" 
                    placeholder="Ej. Velas Aromáticas" 
                    style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; font-size: 0.95rem;"
                >
            </div>
            
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem; color: #333;">
                    Justificación *
                </label>
                <textarea 
                    id="sug-justificacion" 
                    rows="4" 
                    placeholder="¿Por qué es necesaria esta categoría para tus productos?" 
                    style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; resize: vertical; font-size: 0.95rem; font-family: inherit;"
                ></textarea>
            </div>
            
            <div style="text-align: right; margin-top: 20px; display: flex; gap: 10px; justify-content: flex-end;">
                <button 
                    id="btn-cancelar-sugerencia"
                    style="background: #f1f1f1; color: #333; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold;">
                    Cancelar
                </button>
                <button 
                    id="btn-enviar-sugerencia" 
                    style="background: #e91e63; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold;">
                    Enviar Sugerencia
                </button>
            </div>
        </div>
    `;

    // usamos la funcion importada directamente, NO window.mostrarModal
    mostrarModal(htmlModal);

    // configuramos el boton de cancelar para cerrar el modal
    const btnCancelar = document.getElementById('btn-cancelar-sugerencia');
    if (btnCancelar) {
        btnCancelar.addEventListener('click', () => cerrarModalGeneral());
    }

    // configuramos el boton de envio de la sugerencia
    const btnEnviar = document.getElementById('btn-enviar-sugerencia');
    if (btnEnviar) {
        btnEnviar.addEventListener('click', async () => {
            // leemos los valores escritos por el proveedor
            const nombre = document.getElementById('sug-nombre').value.trim();
            const justificacion = document.getElementById('sug-justificacion').value.trim();
            
            // validacion minima: nombre y justificacion no pueden estar vacios
            if (nombre.length < 3) {
                mostrarMensaje('el nombre debe tener al menos 3 caracteres.', 'error');
                return;
            }
            if (justificacion.length < 10) {
                mostrarMensaje('la justificacion debe tener al menos 10 caracteres.', 'error');
                return;
            }
            
            try {
                // empaquetamos los datos como parametros de url (igual que un form normal)
                const parametros = new URLSearchParams();
                parametros.append('accion', 'crear');
                parametros.append('nombre', nombre);
                parametros.append('justificacion', justificacion);
                
                // enviamos la solicitud al servlet de java
                const respuesta = await fetch('solicitudes-categorias', {
                    method: 'POST',
                    body: parametros
                });
                
                if (respuesta.ok) {
                    // cerramos el modal y mostramos notificacion de exito
                    cerrarModalGeneral();
                    mostrarMensaje('sugerencia enviada con exito. los administradores la revisaran pronto.', 'exito');
                } else {
                    // si java devolvio un error (400, 403, 500), lo mostramos
                    const mensajeError = await respuesta.text();
                    mostrarMensaje(mensajeError || 'error al enviar la sugerencia.', 'error');
                }
            } catch (error) {
                // error de red: tomcat apagado o sin conexion
                console.error('error al enviar solicitud de categoria:', error);
                mostrarMensaje('error de conexion con el servidor.', 'error');
            }
        });
    }
}
