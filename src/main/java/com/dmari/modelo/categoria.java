package com.dmari.modelo;

public class categoria {
    private int idCategoriaPk;
    private String nombre;

    // Constructor vacio
    public categoria() {}

    public int getIdCategoriaPk() {
        return idCategoriaPk;
    }

    public void setIdCategoriaPk(int idCategoriaPk) {
        this.idCategoriaPk = idCategoriaPk;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}