export async function cargarVistaSolicitudCategoria() {
    // Para simplificar, inyectaremos el modal directamente en lugar de una vista completa
    const htmlModal = `
        <div style="font-family: Arial, sans-serif;">
            <h3 style="margin-top: 0; color: #333; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;">💡 Sugerir Nueva Categoría</h3>
            <p style="font-size: 0.9rem; color: #666; margin-bottom: 20px;">
                ¿No encuentras la categoría adecuada para tus productos? Envía una sugerencia a los administradores.
            </p>
            
            <div style="margin-bottom: 15px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem;">Nombre de la Categoría *</label>
                <input type="text" id="sug-nombre" placeholder="Ej. Velas Aromáticas" style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
            </div>
            
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem;">Justificación *</label>
                <textarea id="sug-justificacion" rows="4" placeholder="¿Por qué es necesaria esta categoría para tus productos?" style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; resize: vertical;"></textarea>
            </div>
            
            <div style="text-align: right; margin-top: 20px;">
                <button onclick="document.getElementById('modal-general').style.display='none'" style="background: #f1f1f1; color: #333; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; margin-right: 10px; font-weight: bold;">Cancelar</button>
                <button id="btn-enviar-sugerencia" style="background: #e7a052; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold;">Enviar Sugerencia</button>
            </div>
        </div>
    `;

    // Usamos el servicio UI existente para mostrar el modal
    if (window.mostrarModal) {
        window.mostrarModal(htmlModal);
        
        // Agregar evento al botón
        document.getElementById('btn-enviar-sugerencia').addEventListener('click', async () => {
            const nombre = document.getElementById('sug-nombre').value.trim();
            const justificacion = document.getElementById('sug-justificacion').value.trim();
            
            if (nombre.length < 3 || justificacion.length < 10) {
                alert('Por favor, ingresa un nombre válido y una justificación detallada (mínimo 10 caracteres).');
                return;
            }
            
            try {
                const parametros = new URLSearchParams();
                parametros.append('accion', 'crear');
                parametros.append('nombre', nombre);
                parametros.append('justificacion', justificacion);
                
                const respuesta = await fetch('solicitudes-categorias', {
                    method: 'POST',
                    body: parametros
                });
                
                if (respuesta.ok) {
                    alert('¡Sugerencia enviada con éxito! Los administradores la revisarán pronto.');
                    document.getElementById('modal-general').style.display = 'none';
                } else {
                    alert('Error al enviar la sugerencia. Por favor intenta de nuevo más tarde.');
                }
            } catch (error) {
                console.error('Error al enviar solicitud:', error);
                alert('Error de conexión.');
            }
        });
    }
}
