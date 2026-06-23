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

        // --- bloque de validacion ---
        limpiarErrorCampo('reg-nombre');
        limpiarErrorCampo('reg-apellido');
        limpiarErrorCampo('reg-correo');
        limpiarErrorCampo('reg-password');
        limpiarErrorCampo('reg-confirm-password');
        limpiarErrorCampo('reg-nit');
        limpiarErrorCampo('reg-marca');
        limpiarErrorCampo('reg-cuenta');

        let esValido = true;

        // validacion detallada para el nombre
        if (!nombre || nombre.trim() === '') {
            mostrarErrorCampo('reg-nombre', 'El nombre es obligatorio.');
            esValido = false;
        } else if (nombre.trim().length < 3) {
            mostrarErrorCampo('reg-nombre', 'El nombre debe tener al menos 3 caracteres.');
            esValido = false;
        } else if (!/^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s]+$/.test(nombre.trim())) {
            mostrarErrorCampo('reg-nombre', 'El nombre solo puede contener letras.');
            esValido = false;
        }

        // validacion detallada para el apellido
        if (!apellido || apellido.trim() === '') {
            mostrarErrorCampo('reg-apellido', 'El apellido es obligatorio.');
            esValido = false;
        } else if (apellido.trim().length < 3) {
            mostrarErrorCampo('reg-apellido', 'El apellido debe tener al menos 3 caracteres.');
            esValido = false;
        } else if (!/^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s]+$/.test(apellido.trim())) {
            mostrarErrorCampo('reg-apellido', 'El apellido solo puede contener letras.');
            esValido = false;
        }

        // validacion detallada para el correo
        if (!correo || correo.trim() === '') {
            mostrarErrorCampo('reg-correo', 'El correo electronico es obligatorio.');
            esValido = false;
        } else if (!validarCorreo(correo)) {
            mostrarErrorCampo('reg-correo', 'El formato del correo es invalido (ejemplo: usuario@correo.com).');
            esValido = false;
        }

        // validacion detallada para la contrasena
        if (!password) {
            mostrarErrorCampo('reg-password', 'La contraseña es obligatoria.');
            esValido = false;
        } else if (password.length < 8) {
            mostrarErrorCampo('reg-password', 'La contraseña debe tener al menos 8 caracteres.');
            esValido = false;
        } else if (!/[A-Z]/.test(password)) {
            mostrarErrorCampo('reg-password', 'La contraseña debe incluir al menos una letra mayuscula.');
            esValido = false;
        } else if (!/[0-9]/.test(password)) {
            mostrarErrorCampo('reg-password', 'La contraseña debe incluir al menos un numero.');
            esValido = false;
        }

        // validacion para la confirmacion de contrasena
        if (!confirmPassword) {
            mostrarErrorCampo('reg-confirm-password', 'Debe confirmar su contraseña.');
            esValido = false;
        } else if (password !== confirmPassword) {
            mostrarErrorCampo('reg-confirm-password', 'Las contraseñas no coinciden.');
            esValido = false;
        }

        // validacion especifica para proveedores
        if (rol === '4') {
            const nitVal = parametros.get('nit');
            const marcaVal = parametros.get('marca');
            const cuentaVal = parametros.get('cuenta');
            
            if (!nitVal || nitVal.trim() === '') {
                mostrarErrorCampo('reg-nit', 'El NIT es obligatorio para proveedores.');
                esValido = false;
            } else if (!/^\d+$/.test(nitVal.trim())) {
                mostrarErrorCampo('reg-nit', 'El NIT debe ser estrictamente numerico.');
                esValido = false;
            } else if (nitVal.trim().length < 8 || nitVal.trim().length > 20) {
                mostrarErrorCampo('reg-nit', 'El NIT debe tener entre 8 y 20 digitos.');
                esValido = false;
            }
            
            if (!marcaVal || marcaVal.trim() === '') {
                mostrarErrorCampo('reg-marca', 'El nombre de la marca es obligatorio para proveedores.');
                esValido = false;
            } else if (marcaVal.trim().length < 3) {
                mostrarErrorCampo('reg-marca', 'El nombre de la marca debe tener al menos 3 caracteres.');
                esValido = false;
            }
            
            if (!cuentaVal || cuentaVal.trim() === '') {
                mostrarErrorCampo('reg-cuenta', 'El numero de cuenta es obligatorio para proveedores.');
                esValido = false;
            } else if (!/^\d+$/.test(cuentaVal.trim())) {
                mostrarErrorCampo('reg-cuenta', 'El numero de cuenta debe ser estrictamente numerico.');
                esValido = false;
            } else if (cuentaVal.trim().length < 5 || cuentaVal.trim().length > 30) {
                mostrarErrorCampo('reg-cuenta', 'El numero de cuenta debe tener entre 5 y 30 digitos.');
                esValido = false;
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