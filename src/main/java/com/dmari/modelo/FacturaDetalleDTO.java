// dto para representar cada linea de producto en la factura modulo 4
package com.dmari.dto;

public class FacturaDetalleDTO {
    // nombre descriptivo del producto artesanal
    private String nombreProducto;
    // numero de unidades adquiridas
    private int cantidad;
    // costo por unidad al momento de la compra
    private double precioUnitario;
    // calculo de cantidad por precio unitario
    private double subtotal;

    // constructor para serializacion json
    public FacturaDetalleDTO() {}

    // getters y setters para transferencia de datos
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
