package com.dmari.modelo;

/*
    objetivo de este archivo:
    representar una etiqueta (tag) de clasificacion de productos en java.
    mapea la tabla 'tag' de mysql para permitir organizar y filtrar productos.
*/
public class etiqueta {
    // identificador unico de la etiqueta en base de datos
    private int idEtiquetaPk;
    // nombre textual del tag para busquedas difusas
    private String nombreEtiqueta;

    // constructor vacio para instanciacion dinamica
    public etiqueta() {}

    // obtiene el identificador primario
    public int getIdEtiquetaPk() {
        return idEtiquetaPk;
    }

    // establece el identificador primario
    public void setIdEtiquetaPk(int idEtiquetaPk) {
        this.idEtiquetaPk = idEtiquetaPk;
    }

    // obtiene el texto de la etiqueta
    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    // establece el texto de la etiqueta
    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
    }
}