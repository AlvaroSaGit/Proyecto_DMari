package com.dmari.modelo;

import java.sql.Timestamp;

public class SolicitudCategoria {
    private int idSolicitudPk;
    // vinculo con el usuario de rol proveedor
    private int idProveedorFk;
    private String nombreProveedor; // Para facilitar el despliegue en el Admin
    // nombre que el proveedor desea para la nueva categoria
    private String nombreSugerido;
    // explicacion de la necesidad de esta nueva categoria
    private String justificacion;
    // estado del flujo: pendiente, aprobada o rechazada
    private String estadoSolicitud;
    // marca de tiempo del registro
    private Timestamp fechaCreacion;

    // constructor por defecto
    public SolicitudCategoria() {}

    // Getters y Setters
    // obtiene el id de la solicitud
    public int getIdSolicitudPk() { return idSolicitudPk; }
    // establece el id de la solicitud
    public void setIdSolicitudPk(int idSolicitudPk) { this.idSolicitudPk = idSolicitudPk; }

    // obtiene el id del proveedor solicitante
    public int getIdProveedorFk() { return idProveedorFk; }
    // establece el id del proveedor solicitante
    public void setIdProveedorFk(int idProveedorFk) { this.idProveedorFk = idProveedorFk; }

    // obtiene el nombre del proveedor
    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }

    // obtiene el nombre de categoria sugerido
    public String getNombreSugerido() { return nombreSugerido; }
    public void setNombreSugerido(String nombreSugerido) { this.nombreSugerido = nombreSugerido; }

    // obtiene la explicacion de la solicitud
    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }

    // obtiene el estado actual del tramite
    public String getEstadoSolicitud() { return estadoSolicitud; }
    public void setEstadoSolicitud(String estadoSolicitud) { this.estadoSolicitud = estadoSolicitud; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
