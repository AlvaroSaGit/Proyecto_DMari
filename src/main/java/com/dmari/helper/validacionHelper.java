/*
   objetivo de este archivo:
   centralizar las reglas de validacion del sistema para reutilizarlas en todos los servlets.
   aplica filtros estrictos para evitar datos malformados en la base de datos.
*/
package com.dmari.helper;

import java.util.regex.Pattern;

public class validacionHelper {

    // expresiones regulares para validar formatos especificos y garantizar la integridad de la base de datos
    // permite letras mayusculas, minusculas, espacios y caracteres extendidos como la enye o acentos
    private static final String REGEX_SOLO_LETRAS = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$";
    // valida cadenas que contengan unicamente digitos del 0 al 9
    private static final String REGEX_SOLO_NUMEROS = "^[0-9]+$";
    // se actualiza para permitir dominios de mas de 2 letras (como .co, .org, .net) y subdominios
    private static final String REGEX_CORREO = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    // exige al menos 8 caracteres, una mayuscula y un numero para coincidir con la seguridad del frontend
    private static final String REGEX_PASSWORD = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
    // exige entre 7 y 15 digitos numericos para telefonos
    private static final String REGEX_TELEFONO = "^[0-9]{7,15}$";

    // verifica que un texto contenga unicamente letras y espacios (util para nombres y apellidos).
    // @param texto cadena recibida desde el formulario de registro o perfil.
    // @return true si cumple el patron alfabetico y no esta vacio ni es nulo.
    public static boolean validarNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) return false;
        return Pattern.matches(REGEX_SOLO_LETRAS, texto);
    }

    // verifica que el correo tenga un formato institucional o comercial valido (acepta .co, .net, etc).
    // @param correo direccion de email a evaluar.
    // @return true si el formato es aceptado por la expresion regular.
    public static boolean validarCorreo(String correo) {
        if (correo == null) return false;
        return Pattern.matches(REGEX_CORREO, correo);
    }

    // verifica que la clave tenga al menos 8 caracteres, una mayuscula y un numero por seguridad.
    // ayuda a prevenir ataques de fuerza bruta simples.
    // @param password contrasena en texto plano.
    // @return true si cumple con la politica de seguridad minima.
    public static boolean validarPassword(String password) {
        if (password == null) return false;
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    // verifica que el campo contenga solo digitos (util para telefonos o nit).
    // util para campos que se procesaran como cadenas pero representan numeros.
    // @param texto cadena de caracteres numericos.
    // @return true si no contiene letras ni simbolos especiales.
    public static boolean validarSoloNumeros(String texto) {
        if (texto == null) return false;
        return Pattern.matches(REGEX_SOLO_NUMEROS, texto);
    }

    /**
     * Verifica que un teléfono contenga solo números y tenga una longitud válida.
     * @param telefono Cadena que representa el número de teléfono.
     * @return true si cumple con el formato (7-15 dígitos).
     */
    public static boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) return false;
        return Pattern.matches(REGEX_TELEFONO, telefono.trim());
    }

    // valida que un precio sea un numero positivo mayor a cero.
    // evita errores de logica donde un producto podria registrarse sin costo.
    // @param precio valor monetario del producto.
    // @return true si el valor es estrictamente mayor a cero.
    public static boolean validarPrecio(double precio) {
        return precio > 0;
    }

    // valida que el stock no sea negativo.
    // permite valor cero para productos agotados pero bloquea inconsistencias negativas.
    // @param stock cantidad fisica en inventario.
    // @return true si es cero o positivo.
    public static boolean validarStock(int stock) {
        return stock >= 0;
    }
}
