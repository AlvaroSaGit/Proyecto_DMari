/*
    objetivo de este archivo:
    representar un articulo de la tienda (dona, vela, etc).
    
    nota de arquitectura:
    al igual que detallepedido, este modelo esta "aplanado". 
    esto significa que absorbe datos de las tablas 'categoria', 'imagenes' 
    y 'etiqueta' para no tener que crear multiples archivos java.
    el producto dao se encarga de llenar todo esto con una sola consulta sql.
*/
package com.dmari.modelo;

import java.util.ArrayList;

public class producto {

    // ==========================================
    // bloque 1: columnas originales de la tabla producto
    // ==========================================
    private int idProductoPk;
    private int idCategoriaFk;
    private String nombreProducto;
    private String descripcion;
    private double precio;
    private int stock;
    private boolean estado;
    private String fechaCreacion;
    
    // ==========================================
    // bloque 2: datos fusionados de tablas hijas (joins)
    // ==========================================
    
    // absorbe la columna url_ruta de la tabla 'imagenes'
    // nota: si a futuro un producto tiene galeria de 5 fotos, se debera cambiar a arraylist<string>
    private String urlRuta;
    
    // absorbe la columna nombre de la tabla 'categoria'
    private String categoria;
    
    // absorbe los cruces de la tabla intermedia 'producto_etiqueta'
    private ArrayList<String> etiquetas = new ArrayList<>();
    
    // absorbe la marca del proveedor desde la base de datos
    private String proveedorMarca;
    
    // constructor vacio para poder instanciar objetos en blanco desde los daos
    public producto(){}

    // metodos de acceso (getters y setters)
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

    public String getProveedorMarca() {
        return proveedorMarca;
    }

    public void setProveedorMarca(String proveedorMarca) {
        this.proveedorMarca = proveedorMarca;
    }
}
