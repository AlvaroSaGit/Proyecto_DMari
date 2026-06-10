package com.dmari.modelo;

import java.sql.Timestamp;

public class Devolucion {
    private int idDevolucionPk;
    private int idPedidoFk;
    private int idClienteFk;
    private String motivo;
    private String estadoDevolucion;
    private Timestamp fechaSolicitud;
    private Timestamp fechaResolucion;
    
    // Campo adicional para la vista
    private String nombreCliente;

    public Devolucion() {
    }

    public int getIdDevolucionPk() {
        return idDevolucionPk;
    }

    public void setIdDevolucionPk(int idDevolucionPk) {
        this.idDevolucionPk = idDevolucionPk;
    }

    public int getIdPedidoFk() {
        return idPedidoFk;
    }

    public void setIdPedidoFk(int idPedidoFk) {
        this.idPedidoFk = idPedidoFk;
    }

    public int getIdClienteFk() {
        return idClienteFk;
    }

    public void setIdClienteFk(int idClienteFk) {
        this.idClienteFk = idClienteFk;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstadoDevolucion() {
        return estadoDevolucion;
    }

    public void setEstadoDevolucion(String estadoDevolucion) {
        this.estadoDevolucion = estadoDevolucion;
    }

    public Timestamp getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Timestamp fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Timestamp getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(Timestamp fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
}
