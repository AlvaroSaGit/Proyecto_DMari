/*
   objetivo de este archivo:
   centralizar las reglas de validacion del sistema para reutilizarlas en todos los servlets.
   aplica filtros estrictos para evitar datos malformados en la base de datos.
*/
package com.dmari.helper;

import java.util.regex.Pattern;

public class validacionHelper {

    // expresiones regulares para validar formatos especificos
    private static final String REGEX_SOLO_LETRAS = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$";
    private static final String REGEX_SOLO_NUMEROS = "^[0-9]+$";
    private static final String REGEX_CORREO = "^[A-Za-z0-9+_.-]+@(.+)\\.com$";
    private static final String REGEX_PASSWORD = "^(?=.*[0-9]).{8,}$";

    // verifica que un texto contenga unicamente letras y espacios (util para nombres)
    public static boolean validarNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) return false;
        return Pattern.matches(REGEX_SOLO_LETRAS, texto);
    }

    // verifica que el correo tenga un formato institucional o comercial valido
    public static boolean validarCorreo(String correo) {
        if (correo == null) return false;
        return Pattern.matches(REGEX_CORREO, correo);
    }

    // verifica que la clave tenga al menos 8 caracteres y un numero por seguridad
    public static boolean validarPassword(String password) {
        if (password == null) return false;
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    // verifica que el campo contenga solo digitos (util para telefonos o nit)
    public static boolean validarSoloNumeros(String texto) {
        if (texto == null) return false;
        return Pattern.matches(REGEX_SOLO_NUMEROS, texto);
    }

    // valida que un precio sea un numero positivo mayor a cero
    public static boolean validarPrecio(double precio) {
        return precio > 0;
    }

    // valida que el stock no sea negativo
    public static boolean validarStock(int stock) {
        return stock >= 0;
    }
}
