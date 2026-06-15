// servicio para gestionar la comunicacion con el backend de usuarios

let usuariosCache = [];

// funcion para obtener la lista de usuarios y guardarla en cache
export async function obtenerUsuarios() {
    try {
        const respuesta = await fetch('usuarios');
        if (!respuesta.ok) throw new Error('no se pudo conectar con el servidor');
        usuariosCache = await respuesta.json();
        return usuariosCache;
    } catch (error) {
        console.error('error en el servicio de usuarios:', error);
        return [];
    }
}

// permite filtrar la lista que ya tenemos en memoria sin volver al servidor
export function filtrarUsuarios(termino, idRol) {
    const busqueda = termino.toLowerCase();
    
    return usuariosCache.filter(usr => {
        const nombreCompleto = (usr.nombre + ' ' + usr.apellido).toLowerCase();
        const correo = (usr.correo || '').toLowerCase();
        
        // verificamos si coincide el texto (nombre o correo)
        const coincideTexto = nombreCompleto.includes(busqueda) || correo.includes(busqueda);
        // verificamos si coincide el rol (si el filtro de rol no esta vacio)
        const coincideRol = idRol === '' || String(usr.idRol) === idRol;

        return coincideTexto && coincideRol;
    });
}

// funcion para actualizar permisos llamando al servlet post
export async function actualizarPermisosUsuario(parametros) {
    const respuesta = await fetch('usuarios', { 
        method: 'POST', 
        body: parametros 
    });
    return respuesta;
}