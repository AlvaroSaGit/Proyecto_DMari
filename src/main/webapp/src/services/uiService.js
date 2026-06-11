// este servicio se encarga de cosas de la carga de interfaz

/*
    los 2 parametros para permitir cargar la id y la ruta
    de services
*/
export async function cargarComponente(id, ruta) {
    /*
        trycatch, intentara el bloque de codigo
        en caso de no tener el id del html bien
        o la ruta, mandara el error
    */
    try {
        const contenedor = document.getElementById(id);
        // se comprueba 
        if (!contenedor) return; // si no existe el hueco, no hacemos nada

        // se extrae la ruta en una constante de respuesta
        const respuesta = await fetch(ruta);
        // verificamos que la peticion http haya sido exitosa (ej. que no sea un error 404)
        if (!respuesta.ok) {
            throw new Error(`Error HTTP: ${respuesta.status}`);
        }
        // html esperara hasta que tenga la url de la ruta y lo volvera texto
        const html = await respuesta.text();
        /* 
            actualiza lo que haya en el contenedor y su id
            por ejemplo en el html id de header, en el html tendra "hola"
            pero al asignarle un nuevo valor que es el de la ruta se reescribe
        */
        contenedor.innerHTML = html;

        // imprime el error
    } catch (error) {
        console.error("Error al traer el componente de: " + ruta, error);
    }
}

/**
 * modulo 4: gestion dinamica de modales para facturacion y auditoria.
 * inyecta contenido html en un contenedor global y lo hace visible.
 * @param {string} html - contenido estructurado de la factura o formulario.
 */
export function mostrarModal(html) {
    // buscamos el contenedor del modal en el index.html
    let contenedorModal = document.getElementById('modal-general');
    
    // si no existe, lo creamos dinamicamente para evitar errores de null
    if (!contenedorModal) {
        // creacion del contenedor principal para ventanas emergentes
        contenedorModal = document.createElement('div');
        contenedorModal.id = 'modal-general';
        // estilos basicos para centrar el contenido y oscurecer el fondo
        Object.assign(contenedorModal.style, {
            position: 'fixed', top: '0', left: '0', width: '100%', height: '100%',
            backgroundColor: 'rgba(0,0,0,0.7)', display: 'none', justifyContent: 'center',
            alignItems: 'center', zIndex: '20000'
        });
        
        contenedorModal.innerHTML = `
            <div id="modal-contenido" style="background: white; padding: 20px; border-radius: 8px; max-width: 600px; width: 90%; position: relative;">
                <button onclick="this.parentElement.parentElement.style.display='none'" style="position: absolute; top: 10px; right: 10px; border: none; background: none; cursor: pointer; font-size: 20px;">&times;</button>
                <div id="modal-body"></div>
            </div>
        `;
        document.body.appendChild(contenedorModal);
    }

    const cuerpo = document.getElementById('modal-body');
    if (cuerpo) {
        // inyectamos el html (puede ser la factura del modulo 4 o el motivo de cancelacion del modulo 2)
        cuerpo.innerHTML = html;
        contenedorModal.style.display = 'flex';
    }
};

// funcion auxiliar para cerrar modales desde cualquier controlador
export function cerrarModalGeneral() {
    const modal = document.getElementById('modal-general');
    if (modal) modal.style.display = 'none';
}