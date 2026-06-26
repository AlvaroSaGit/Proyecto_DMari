package com.dmari.helper;

import java.util.regex.Pattern;

/*
    objetivo de este archivo:
    centralizar las reglas de validacion del sistema para reutilizarlas en todos los servlets.
    aplica filtros estrictos para evitar datos malformados en la base de datos.
    todos los metodos son estaticos, no es necesario instanciar la clase.
*/
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

    /**
     * Verifica que un texto contenga unicamente letras y espacios.
     * Util para validar nombres y apellidos. Rechaza numeros y caracteres especiales.
     *
     * @param texto cadena recibida desde el formulario.
     * @return true si el texto es valido, false si es nulo, vacio o contiene caracteres no permitidos.
     */
    public static boolean validarNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) return false;
        return Pattern.matches(REGEX_SOLO_LETRAS, texto);
    }

    /**
     * Verifica que el correo tenga un formato valido.
     * Acepta dominios comunes como .co, .net, .org, .com.co.
     *
     * @param correo direccion de email a evaluar.
     * @return true si el formato es correcto, false si no lo es.
     */
    public static boolean validarCorreo(String correo) {
        if (correo == null) return false;
        return Pattern.matches(REGEX_CORREO, correo);
    }

    /**
     * Verifica que la contrasena cumpla la politica minima de seguridad:
     * al menos 8 caracteres, una letra mayuscula y un numero.
     *
     * @param password contrasena en texto plano.
     * @return true si cumple la politica, false si no.
     */
    public static boolean validarPassword(String password) {
        if (password == null) return false;
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    /**
     * Verifica que el campo contenga unicamente digitos numericos.
     * Util para campos como NIT o numero de cuenta bancaria.
     *
     * @param texto cadena de caracteres a evaluar.
     * @return true si solo contiene digitos, false si tiene letras o simbolos.
     */
    public static boolean validarSoloNumeros(String texto) {
        if (texto == null) return false;
        return Pattern.matches(REGEX_SOLO_NUMEROS, texto);
    }

    /**
     * Verifica que un numero de telefono contenga solo digitos y tenga entre 7 y 15 caracteres.
     *
     * @param telefono cadena que representa el telefono.
     * @return true si el formato es valido, false si no.
     */
    public static boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) return false;
        return Pattern.matches(REGEX_TELEFONO, telefono.trim());
    }

    /**
     * Valida que un precio sea mayor a cero.
     * Evita que un producto se registre sin valor monetario.
     *
     * @param precio valor monetario del producto.
     * @return true si el precio es positivo, false si es cero o negativo.
     */
    public static boolean validarPrecio(double precio) {
        return precio > 0;
    }

    /**
     * Valida que el stock no sea un valor negativo.
     * Permite el valor cero para productos agotados.
     *
     * @param stock cantidad de unidades en inventario.
     * @return true si el stock es cero o mayor, false si es negativo.
     */
    public static boolean validarStock(int stock) {
        return stock >= 0;
    }
}
