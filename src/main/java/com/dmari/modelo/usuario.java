package com.dmari.modelo;

/**
 * Entidad que representa a un usuario registrado en el sistema DMari.
 *
 * <p>Mapea la tabla {@code usuario} de la base de datos MySQL y es el objeto
 * central de seguridad: se almacena en la {@code HttpSession} al iniciar sesion
 * para que los Servlets puedan identificar quien esta haciendo cada peticion
 * y que permisos tiene segun su rol.</p>
 *
 * <p>Esta clase funciona tambien como DTO en algunos contextos, dado que
 * agrupa datos de las tablas {@code usuario} y {@code correo}.</p>
 *
 * @author Alvaro Andres Salazar Herrera
 * @version 1.0
 */
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

    /**
     * Obtiene el identificador unico del usuario en la base de datos.
     *
     * @return {@code int} con la llave primaria de la tabla {@code usuario}.
     */
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
     * @return {@code String} con el nombre registrado, o {@code null} si no fue asignado.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el primer nombre del usuario.
     *
     * @param nombre {@code String} con el nombre a registrar.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el apellido del usuario.
     *
     * @return {@code String} con el apellido registrado, o {@code null} si no fue asignado.
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Establece el apellido del usuario.
     *
     * @param apellido {@code String} con el apellido a registrar.
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Obtiene el identificador del rol asignado al usuario.
     * Los roles son: 1=Administrador, 2=Cliente, 4=Proveedor.
     *
     * @return {@code int} con el id del rol (llave foranea a la tabla {@code rol}).
     */
    public int getIdRol() {
        return idRol;
    }

    /**
     * Establece el identificador del rol del usuario.
     *
     * @param idRol {@code int} con el id del rol a asignar.
     */
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    /**
     * Obtiene la direccion de correo electronico del usuario.
     *
     * @return {@code String} con el correo electronico registrado.
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Establece la direccion de correo electronico del usuario.
     *
     * @param correo {@code String} con el correo a registrar.
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Obtiene la contrasena del usuario. En la base de datos se almacena como
     * un hash generado por BCrypt; este campo transporta el valor en texto plano
     * solo durante el proceso de registro y comparacion de login.
     *
     * @return {@code String} con la contrasena (hash o texto plano segun el contexto).
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contrasena del usuario.
     *
     * @param password {@code String} con la contrasena en texto plano recibida del formulario.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Verifica si la cuenta del usuario esta activa.
     *
     * @return {@code true} si la cuenta esta activa y puede iniciar sesion;
     *         {@code false} si el administrador la ha bloqueado.
     */
    public boolean isEstadoCuenta() {
        return estadoCuenta;
    }

    /**
     * Establece el estado de activacion de la cuenta del usuario.
     *
     * @param estadoCuenta {@code true} para activar la cuenta, {@code false} para bloquearla.
     */
    public void setEstadoCuenta(boolean estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }
}