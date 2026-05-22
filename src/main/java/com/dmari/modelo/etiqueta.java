package com.dmari.modelo;

public class etiqueta {
    private int idEtiquetaPk;
    private String nombreEtiqueta;

    // Constructor vacio
    public etiqueta() {}

    public int getIdEtiquetaPk() {
        return idEtiquetaPk;
    }

    public void setIdEtiquetaPk(int idEtiquetaPk) {
        this.idEtiquetaPk = idEtiquetaPk;
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
    }
}