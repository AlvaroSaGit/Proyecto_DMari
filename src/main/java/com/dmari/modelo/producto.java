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
    // identificador primario del producto
    private int idProductoPk;
    // referencia a la categoria asignada
    private int idCategoriaFk;
    // nombre comercial del articulo
    private String nombreProducto;
    // texto informativo sobre el producto
    private String descripcion;
    // valor monetario
    private double precio;
    // unidades disponibles en almacen
    private int stock;
    // bandera de disponibilidad (activo/pausado)
    private boolean estado;
    // fecha de registro en el sistema
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
    
    // datos de contacto del proveedor para visibilidad del cliente
    private String proveedorTelefono;
    private String proveedorCorreo;
    
    // constructor vacio para poder instanciar objetos en blanco desde los daos
    public producto(){}

    // metodos de acceso (getters y setters)
    // obtiene el id del producto
    public int getIdProductoPk() {
        return idProductoPk;
    }

    // establece el id del producto
    public void setIdProductoPk(int idProductoPk) {
        this.idProductoPk = idProductoPk;
    }

    // obtiene el id de la categoria
    public int getIdCategoriaFk() {
        return idCategoriaFk;
    }

    // establece el id de la categoria
    public void setIdCategoriaFk(int idCategoriaFk) {
        this.idCategoriaFk = idCategoriaFk;
    }

    // obtiene el nombre del producto
    public String getNombreProducto() {
        return nombreProducto;
    }

    // establece el nombre del producto
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    // obtiene la descripcion
    public String getDescripcion() {
        return descripcion;
    }

    // establece la descripcion
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // obtiene el precio
    public double getPrecio() {
        return precio;
    }

    // establece el precio
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    // obtiene el stock
    public int getStock() {
        return stock;
    }

    // establece el stock
    public void setStock(int stock) {
        this.stock = stock;
    }

    // verifica si el producto esta activo
    public boolean isEstado() {
        return estado;
    }

    // cambia el estado del producto
    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    // obtiene la fecha de creacion
    public String getFechaCreacion() {
        return fechaCreacion;
    }

    // establece la fecha de creacion
    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // obtiene la ruta de la imagen
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

    public String getProveedorTelefono() { return proveedorTelefono; }
    public void setProveedorTelefono(String proveedorTelefono) { this.proveedorTelefono = proveedorTelefono; }

    public String getProveedorCorreo() { return proveedorCorreo; }
    public void setProveedorCorreo(String proveedorCorreo) { this.proveedorCorreo = proveedorCorreo; }
}
