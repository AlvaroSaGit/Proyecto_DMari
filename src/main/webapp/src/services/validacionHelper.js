/**
 * Helper para centralizar las validaciones del frontend
 */

// Mínimo 3 letras, sin caracteres especiales
export function validarNombre(texto) {
    if (!texto) return false;
    const regex = /^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s]{3,}$/;
    return regex.test(texto.trim());
}

// Mínimo 8 caracteres, al menos 1 número y 1 mayúscula
export function validarPassword(clave) {
    if (!clave) return false;
    const regex = /^(?=.*[0-9])(?=.*[A-Z]).{8,}$/;
    return regex.test(clave);
}

// Validación de numéricos (precio, stock)
export function validarNumerico(valor) {
    if (valor === null || valor === undefined || valor === '') return false;
    const num = Number(valor);
    return !isNaN(num) && num >= 0;
}

// Validación básica de correo
export function validarCorreo(email) {
    if (!email) return false;
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email.trim());
}

/**
 * Valida un campo de input en tiempo real para permitir solo dígitos numéricos.
 * @param {Event} e - El evento 'input' del campo de texto.
 */
export function validarSoloNumeros(e) {
    // Reemplaza cualquier caracter que no sea un número por una cadena vacía
    e.target.value = e.target.value.replace(/[^0-9]/g, '');
}

/**
 * Valida que un número de teléfono tenga entre 7 y 15 dígitos.
 * @param {string} telefono - El número de teléfono a validar.
 * @returns {boolean} - True si es válido, false en caso contrario.
 */
export function validarTelefono(telefono) {
    if (!telefono) return false;
    const regex = /^[0-9]{7,15}$/;
    return regex.test(telefono.trim());
}
/**
 * Muestra un mensaje de error debajo del input especificado
 * @param {string} inputId - ID del input HTML
 * @param {string} mensaje - Mensaje de error a mostrar
 */
export function mostrarErrorCampo(inputId, mensaje) {
    const input = document.getElementById(inputId);
    if (!input) return;

    // Remover error previo si existe
    limpiarErrorCampo(inputId);

    // Crear el mensaje de error
    const spanError = document.createElement('span');
    spanError.className = 'error-validacion';
    spanError.id = `error-${inputId}`;
    spanError.style.color = '#dc3545';
    spanError.style.fontSize = '0.8rem';
    spanError.style.display = 'block';
    spanError.style.marginTop = '4px';
    spanError.innerText = mensaje;

    // Borde rojo al input
    input.style.borderColor = '#dc3545';

    // Insertar después del input
    input.parentNode.insertBefore(spanError, input.nextSibling);
}

/**
 * Remueve el mensaje de error de un input
 * @param {string} inputId - ID del input HTML
 */
export function limpiarErrorCampo(inputId) {
    const input = document.getElementById(inputId);
    if (input) {
        input.style.borderColor = ''; // Restaurar borde original
    }
    const errorExistente = document.getElementById(`error-${inputId}`);
    if (errorExistente) {
        errorExistente.remove();
    }
}
