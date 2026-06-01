package com.dmari.helper;

// objetivo: centralizar las reglas de validacion para asegurar datos limpios
public class validacionHelper {
    
    // verifica que el nombre solo contenga letras y espacios, rechazando numeros
    public static boolean validarNombre(String nombre) {
        // validamos que no sea nulo ni este vacio
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        // la expresion regular permite letras minusculas, mayusculas, enes y espacios
        return nombre.matches("^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$");
    }

    // comprueba que la clave tenga al menos 8 caracteres y contenga numeros
    public static boolean validarPassword(String password) {
        // validamos longitud minima de seguridad
        if (password == null || password.length() < 8) {
            return false;
        }
        // verifica que exista al menos un digito numerico en la cadena
        return password.matches(".*\\d.*");
    }

    // valida la logica del correo verificando el arroba y la terminacion punto com
    public static boolean validarCorreo(String correo) {
        // validamos que el objeto no sea nulo
        if (correo == null) {
            return false;
        }
        // convertimos a minusculas para una comparacion mas segura
        String correoMin = correo.toLowerCase().trim();
        
        // verificamos que contenga el simbolo arroba
        boolean tieneArroba = correoMin.contains("@");
        // verificamos que termine estrictamente en punto com
        boolean terminaEnCom = correoMin.endsWith(".com");
        
        return tieneArroba && terminaEnCom;
    }
}
