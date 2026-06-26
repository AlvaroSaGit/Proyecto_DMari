package com.dmari.modelo;

/**
 * DTO para transportar datos de graficas de ventas por periodo de tiempo.
 *
 * Cada instancia de este objeto representa un punto en una grafica de barras
 * o de lineas: la etiqueta es el nombre del eje X (ej. "Enero",
 * "Categoria 1") y el total es el valor del eje Y (suma de ventas).
 *
 * Es usado por el EstadisticaController para
 * serializar las estadisticas de ventas antes de enviarlas al frontend.
 */
public class VentaEstadisticaDTO {
    private String etiqueta;
    private double total;

    // constructor vacio para inicializacion
    public VentaEstadisticaDTO() {
    }

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