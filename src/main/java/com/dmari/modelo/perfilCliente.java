package com.dmari.modelo;

/**
 * clase modelo orientada a objetos para el perfil del cliente.
 * actua como un dto (data transfer object) encapsulando los datos 
 * esparcidos de las tablas 'cliente', 'direccion' y 'telefono' 
 * en una sola caja facil de mover.
 */
public class perfilCliente {
    private String direccion;
    private String direccionDetalle;
    private String telefono;
    private String telefonoSecundario;
    private String referencia;

    // metodos de extraccion (getters)
    /**
     * @return string: direccion principal guardada
     */
    public String getDireccion() {
        return direccion;
    }
    /**
     * @return string: detalles adicionales del domicilio
     */
    public String getDireccionDetalle() {
        return direccionDetalle;
    }
    /**
     * @return string: numero celular principal
     */
    public String getTelefono() {
        return telefono;
    }
    /**
     * @return string: numero de respaldo opcional
     */
    public String getTelefonoSecundario() {
        return telefonoSecundario;
    }
    /**
     * @return string: puntos de referencia (ej: al lado de la panaderia)
     */
    public String getReferencia() {
        return referencia;
    }

    // metodos de inyeccion (setters)
    /**
     * @param direccion string: establece la direccion primaria
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    /**
     * @param direccionDetalle string: establece datos especificos como el timbre
     */
    public void setDireccionDetalle(String direccionDetalle) {
        this.direccionDetalle = direccionDetalle;
    }
    /**
     * @param telefono string: establece el telefono que no puede ser nulo
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    /**
     * @param telefonoSecundario string: guarda el numero extra si fue otorgado
     */
    public void setTelefonoSecundario(String telefonoSecundario) {
        this.telefonoSecundario = telefonoSecundario;
    }
    /**
     * @param referencia string: guarda instrucciones textuales de ubicacion
     */
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}