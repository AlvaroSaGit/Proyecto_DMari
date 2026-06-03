package com.dmari.modelo;

/**
 * dto para transportar datos de graficas.
 * agrupa el total de ventas por un periodo de tiempo.
 */
public class VentaEstadisticaDTO {
    private String etiqueta; // nombre del mes o categoria
    private double total;    // suma de dinero recaudado

    // constructor vacio para inicializacion
    public VentaEstadisticaDTO() {}

    // constructor con parametros para carga rapida
    public VentaEstadisticaDTO(String etiqueta, double total) {
        this.etiqueta = etiqueta;
        this.total = total;
    }

    // metodos de acceso para etiqueta
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    // metodos de acceso para el valor total
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}