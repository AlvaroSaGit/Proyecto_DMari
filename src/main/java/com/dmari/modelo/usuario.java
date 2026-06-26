package com.dmari.modelo;

public class usuario {
    // Atributos que corresponden a la tabla 'usuario' en MySQL
    // identificador correlativo del usuario
    private int idUsuario;
    // primer nombre del usuario
    private String nombre;
    // primer apellido del usuario
    private String apellido;
    // llave foranea al rol del sistema
    private int idRol;

    // Atributo que corresponde a la tabla 'correo' en MySQL
    private String correo;

    // Atributo que corresponde a la tabla 'credenciales' en MySQL (BLOB en BD,
    // String en Java)
    private String password;

    // Atributo para saber si la cuenta esta activa o bloqueada
    private boolean estadoCuenta;

    // Constructor vacio obligatorio para Java Web / Servlets
    public usuario() {
    }

    // ==========================================================
    // GETTERS Y SETTERS (Metodos de acceso)
    // ==========================================================

    // obtiene el id del usuario
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Establece el identificador unico del usuario.
     *
     * @param idUsuario {@code int} con el valor de la llave primaria.
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtiene el primer nombre del usuario.
     *
     * @return {@code String} con el nombre registrado, o {@code null} si no fue
     *         asignado.
     */
    public String getNombre() {
        return nombre;
    }

    // asigna el nombre
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // obtiene el apellido
    public String getApellido() {
        return apellido;
    }

    // asigna el apellido
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // obtiene el id del rol
    public int getIdRol() {
        return idRol;
    }

    // asigna el id del rol
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    // obtiene el correo electronico
    public String getCorreo() {
        return correo;
    }

    // asigna el correo electronico
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    // obtiene la contrasena encriptada
    public String getPassword() {
        return password;
    }

    // asigna la contrasena
    public void setPassword(String password) {
        this.password = password;
    }

    // verifica el estado de la cuenta
    public boolean isEstadoCuenta() {
        return estadoCuenta;
    }

    // asigna el estado de la cuenta
    public void setEstadoCuenta(boolean estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }
}