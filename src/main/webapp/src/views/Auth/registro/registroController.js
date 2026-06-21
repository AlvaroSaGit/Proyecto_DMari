import { cargarComponente } from '../../../services/uiService.js';
import { navegarA } from '../../../router/router.js';
import { validarNombre, validarCorreo, validarPassword, mostrarErrorCampo, limpiarErrorCampo } from '../../../services/validacionHelper.js';

export async function cargarVistaRegistro() {
    // inyectamos el html de registro en la caja fuerte del main
    await cargarComponente('component-main', './src/views/Auth/registro/registro.html');
    
    // iniciamos la logica de capturas y botones ya que el html se ha cargado
    prepararFormularioRegistro();
}

function prepararFormularioRegistro() {
    // buscamos el formulario (soporta busqueda por id o por clase para evitar fallos silenciosos)
    const formulario = document.getElementById('form-registro') || document.querySelector('.formulario-registro');
    const linkLogin = document.getElementById('link-ir-login');
    const selectRol = document.getElementById('reg-rol');
    const contenedorProveedor = document.getElementById('campos-proveedor');

    if (selectRol && contenedorProveedor) {
        selectRol.addEventListener('change', function() {
            if (selectRol.value === '4') {
                contenedorProveedor.style.display = 'block';
            } else {
                contenedorProveedor.style.display = 'none';
            }
        });
    }
    
    // damos accion al boton inferior por si el usuario ya tenia cuenta
    if (linkLogin) {
        linkLogin.addEventListener('click', function(evento) {
            // prevenimos que la pagina salte a arriba
            evento.preventDefault();
            // llamamos al enrutador para reemplazar la vista
            navegarA('login');
        });
    }
    
    // validacion de seguridad por si falla la carga html
    if (!formulario) {
        console.error('Critico: no se encontro el formulario de registro en el dom. Verifica que exista el id "form-registro" en el html.');
        return;
    }
    
    // escuchamos el submit cuando apretan el boton ingresar
    formulario.addEventListener('submit', async function(evento) {
        // evitamos que la pagina recargue borrando todos los datos
        evento.preventDefault();
        
        // extraemos los valores exactos que ingreso el usuario en ese momento
        const nombre = document.getElementById('reg-nombre').value;
        const apellido = document.getElementById('reg-apellido').value;
        const correo = document.getElementById('reg-correo').value;
        const password = document.getElementById('reg-password').value;
        const confirmPassword = document.getElementById('reg-confirm-password').value;
        
        const campoRol = document.getElementById('reg-rol');
        const rol = campoRol ? campoRol.value : '2';

        const parametros = new URLSearchParams();
        parametros.append('nombre', nombre);
        parametros.append('apellido', apellido);
        parametros.append('correo', correo);
        parametros.append('password', password);
        parametros.append('rol', rol);

        if (rol === '4') {
            const nit = document.getElementById('reg-nit').value;
            const marca = document.getElementById('reg-marca').value;
            const cuenta = document.getElementById('reg-cuenta').value;
            const banco = document.getElementById('reg-banco').value;
            const tipoCuentaSelect = document.getElementById('reg-tipo-cuenta');
            const tipoCuenta = tipoCuentaSelect ? tipoCuentaSelect.value : 'ahorros';

            parametros.append('nit', nit);
            parametros.append('marca', marca);
            parametros.append('cuenta', cuenta);
            parametros.append('banco', banco);
            parametros.append('tipoCuenta', tipoCuenta);
        }

        // --- Bloque de Validación ---
        limpiarErrorCampo('reg-nombre');
        limpiarErrorCampo('reg-apellido');
        limpiarErrorCampo('reg-correo');
        limpiarErrorCampo('reg-password');
        limpiarErrorCampo('reg-confirm-password');

        let esValido = true;

        if (!validarNombre(nombre)) {
            mostrarErrorCampo('reg-nombre', 'El nombre es inválido o muy corto.');
            esValido = false;
        }
        if (!validarNombre(apellido)) {
            mostrarErrorCampo('reg-apellido', 'El apellido es inválido o muy corto.');
            esValido = false;
        }
        if (!validarCorreo(correo)) {
            mostrarErrorCampo('reg-correo', 'El formato del correo es inválido.');
            esValido = false;
        }
        if (!validarPassword(password)) {
            mostrarErrorCampo('reg-password', 'Debe tener 8+ caracteres, 1 mayúscula y 1 número.');
            esValido = false;
        }
        if (password !== confirmPassword) {
            mostrarErrorCampo('reg-confirm-password', 'Las contraseñas no coinciden.');
            esValido = false;
        }

        if (rol === '4') {
            const nitVal = parametros.get('nit');
            const marcaVal = parametros.get('marca');
            const cuentaVal = parametros.get('cuenta');
            
            if (!nitVal || !/^\d{8,20}$/.test(nitVal.trim())) {
                mostrarErrorCampo('reg-nit', 'El NIT debe ser estrictamente numérico (entre 8 y 20 dígitos).');
                esValido = false;
            } else {
                limpiarErrorCampo('reg-nit');
            }
            
            if (!marcaVal || marcaVal.trim().length < 3) {
                mostrarErrorCampo('reg-marca', 'El nombre de la marca debe tener al menos 3 caracteres.');
                esValido = false;
            } else {
                limpiarErrorCampo('reg-marca');
            }
            
            if (!cuentaVal || !/^\d{5,30}$/.test(cuentaVal)) {
                mostrarErrorCampo('reg-cuenta', 'La cuenta debe contener entre 5 y 30 dígitos.');
                esValido = false;
            } else {
                limpiarErrorCampo('reg-cuenta');
            }
        }

        if (!esValido) return;
        
        // envolvemos en un trycatch para atajar problemas de internet o de base de datos
        try {
            // peticion fetch hacia el controlador de autenticacion en java en la ruta /registro
            const respuesta = await fetch('registro', {
                method: 'POST',
                body: parametros
            });
            
            if (respuesta.ok) {
                const msj = rol === '4' ? 'Registro enviado con exito. Espere la aprobacion del administrador.' : '¡Registro exitoso! Ahora inicia sesion.';
                // limpiamos el local storage para evitar arrastrar el carrito anterior
                localStorage.removeItem('carritoDMari');
                alert(msj);
                formulario.reset();
                navegarA('login');
            } else {
                // capturamos el mensaje de error que viene desde el validacionhelper de java
                const mensajeError = await respuesta.text();
                // Mostramos el error (ej: "correo ya en uso") debajo del campo de correo
                mostrarErrorCampo('reg-correo', mensajeError || 'Error al registrarse. Intenta de nuevo.');
            }
        } catch (error) {
            // si falla la promesa de java caera aqui sin crashear la pagina
            console.error('Error al conectar con el servidor:', error);
            alert('Hubo un problema de conexion');
        }
    });
}