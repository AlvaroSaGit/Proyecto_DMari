package com.dmari.modelo;

/**
 * dto para transportar datos de graficas.
 * agrupa el total de ventas por un periodo de tiempo.
 */
public class VentaEstadisticaDTO {
    private String etiqueta;
    private double total;

    // constructor vacio para inicializacion
    public VentaEstadisticaDTO() {}

    // constructor con parametros para carga rapida
    public VentaEstadisticaDTO(String etiqueta, double total) {
        this.etiqueta = etiqueta;
        this.total = total;
    }

    // obtiene el nombre del periodo o categoria
    public String getEtiqueta() {
        return etiqueta;
    }

    // establece el nombre del periodo o categoria
    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}