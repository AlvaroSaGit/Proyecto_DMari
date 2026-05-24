package com.dmari.modelo;

public class categoria {
    private int idCategoriaPk;
    private String nombre;
    private String descripcion;
    private boolean estado_activo;

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEstado_activo() {
        return estado_activo;
    }

    public void setEstado_activo(boolean estado_activo) {
        this.estado_activo = estado_activo;
    }

    
}