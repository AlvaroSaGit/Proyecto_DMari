package com.dmari.modelo;

public class usuario {
    // Atributos que corresponden a la tabla 'usuario' en MySQL
    private int idUsuario;
    private String nombre;
    private String apellido;
    private int idRol;
    
    // Atributo que corresponde a la tabla 'correo' en MySQL
    private String correo;
    
    // Atributo que corresponde a la tabla 'credenciales' en MySQL (BLOB en BD, String en Java)
    private String password;

    // Atributo para saber si la cuenta esta activa o bloqueada
    private boolean estadoCuenta;

    // Constructor vacio obligatorio para Java Web / Servlets
    public usuario() {
    }

    // ==========================================================
    // GETTERS Y SETTERS (Metodos de acceso)
    // ==========================================================

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(boolean estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }
}