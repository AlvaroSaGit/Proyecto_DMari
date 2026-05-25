/*
    objetivo de este archivo:
    representar la tabla 'detalle_carrito' en java.
    almacena cada producto individual (y su cantidad) que el usuario agrego a su canasta.
*/
package com.dmari.modelo;

public class detalleCarrito {
    
    // ==========================================
    // bloque 1: columnas originales de la tabla
    // ==========================================
    private int idDetalleCarrito;
    private int idCarritoFk;
    private int idProductoFk;
    private int cantidad;
    
    // ==========================================
    // bloque 2: datos extra (joins) para la vista del cliente
    // ==========================================
    private String nombreProducto;
    private double precio;
    private int stock;

    // constructor vacio
    public detalleCarrito() {
    }

    // getters y setters de los datos puros
    public int getIdDetalleCarrito() {
        return idDetalleCarrito;
    }
    public void setIdDetalleCarrito(int idDetalleCarrito) {
        this.idDetalleCarrito = idDetalleCarrito;
    }

    public int getIdCarritoFk() {
        return idCarritoFk;
    }
    public void setIdCarritoFk(int idCarritoFk) {
        this.idCarritoFk = idCarritoFk;
    }

    public int getIdProductoFk() {
        return idProductoFk;
    }
    public void setIdProductoFk(int idProductoFk) {
        this.idProductoFk = idProductoFk;
    }

    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // getters y setters de los datos unificados
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}