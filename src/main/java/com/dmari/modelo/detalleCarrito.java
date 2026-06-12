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
    // identificador del item en el carrito
    private int idDetalleCarrito;
    // referencia a la cabecera del carrito
    private int idCarritoFk;
    // referencia al producto seleccionado
    private int idProductoFk;
    // unidades que el cliente desea comprar
    private int cantidad;
    
    // ==========================================
    // bloque 2: datos extra (joins) para la vista del cliente
    // ==========================================
    // nombre para mostrar en la lista del carrito
    private String nombreProducto;
    // precio unitario actual del producto
    private double precio;
    // disponibilidad real para validaciones
    private int stock;
    // flag booleano para el flujo de compra parcial
    private boolean seleccionado;

    // constructor vacio
    public detalleCarrito() {
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public boolean isSeleccionado() { return seleccionado; }
    public void setSeleccionado(boolean seleccionado) { this.seleccionado = seleccionado; }

    // getters y setters de los datos puros
    // obtiene el id del detalle
    public int getIdDetalleCarrito() {
        return idDetalleCarrito;
    }
    // establece el id del detalle
    public void setIdDetalleCarrito(int idDetalleCarrito) {
        this.idDetalleCarrito = idDetalleCarrito;
    }

    // obtiene el id del carrito
    public int getIdCarritoFk() {
        return idCarritoFk;
    }
    // establece el id del carrito

    public void setIdCarritoFk(int idCarritoFk) {
        this.idCarritoFk = idCarritoFk;
    }

    // obtiene el id del producto
    public int getIdProductoFk() {
        return idProductoFk;
    }
    // establece el id del producto

    public void setIdProductoFk(int idProductoFk) {
        this.idProductoFk = idProductoFk;
    }

    // obtiene la cantidad pedida
    public int getCantidad() {
        return cantidad;
    }
    // establece la cantidad pedida

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // getters y setters de los datos unificados
    // obtiene el nombre del producto
    public String getNombreProducto() { return nombreProducto; }
    // establece el nombre del producto
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}