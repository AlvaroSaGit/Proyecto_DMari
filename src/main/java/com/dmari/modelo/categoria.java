package com.dmari.modelo;

/**
 * Entidad que representa una categoria de productos en DMari.
 *
 * Mapea la tabla #categoria de MySQL. El campo #estado_activo
 * es la bandera que controla si la categoria es visible en el catalogo
 * publico de la tienda o si esta pausada por el administrador.
 */
public class categoria {
    // identificador primario de la categoria en bd
    private int idCategoriaPk;
    // nombre visible de la familia de productos
    private String nombre;
    // texto descriptivo sobre que productos incluye
    private String descripcion;
    // bandera de visibilidad en el menu principal
    private boolean estado_activo;

    // recupera el id de la categoria
    public int getIdCategoriaPk() {
        return idCategoriaPk;
    }

    // asigna el id de la categoria
    public void setIdCategoriaPk(int idCategoriaPk) {
        this.idCategoriaPk = idCategoriaPk;
    }

    // recupera el nombre de la categoria
    public String getNombre() {
        return nombre;
    }

    // asigna el nombre de la categoria
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // recupera la descripcion de la categoria
    public String getDescripcion() {
        return descripcion;
    }

    // asigna la descripcion de la categoria
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // verifica si la categoria esta habilitada
    public boolean isEstado_activo() {
        return estado_activo;
    }

    // cambia el estado de habilitacion
    public void setEstado_activo(boolean estado_activo) {
        this.estado_activo = estado_activo;
    }

}