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