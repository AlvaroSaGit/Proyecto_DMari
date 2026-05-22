package com.dmari.modelo;

import java.util.ArrayList;

public class producto {
    /*
        Los mismos atributos que la tabla de la base de datos
        producto
    
        ARCHIVO GETTER Y SETTER
    */
    
    private int idProductoPk;
    private int idCategoriaFk;
    /*Informacion general del prodcuto*/
    private String nombreProducto;
    private String descripcion;
    /*Precio del producto*/
    private double precio;
    /*Cantidad de unidades del producto*/
    private int stock;
    /*Estado del producto*/
    private boolean estado;
    /*Fecha creacion temporal*/
    private String fechaCreacion;
    /* Nueva variable para la ruta de la tabla imagenes */
    private String urlRuta;
    /* Nombre de la categoria proveniente del JOIN */
    private String categoria;
    /* Lista de etiquetas de la tabla intermedia */
    private ArrayList<String> etiquetas = new ArrayList<>();
    
    /*Constructor vacio para poder armar objetos en blanco*/
    public producto(){}

    public int getIdProductoPk() {
        return idProductoPk;
    }

    public void setIdProductoPk(int idProductoPk) {
        this.idProductoPk = idProductoPk;
    }

    public int getIdCategoriaFk() {
        return idCategoriaFk;
    }

    public void setIdCategoriaFk(int idCategoriaFk) {
        this.idCategoriaFk = idCategoriaFk;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUrlRuta() {
        return urlRuta;
    }

    public void setUrlRuta(String urlRuta) {
        this.urlRuta = urlRuta;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public ArrayList<String> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(ArrayList<String> etiquetas) {
        this.etiquetas = etiquetas;
    }
}
