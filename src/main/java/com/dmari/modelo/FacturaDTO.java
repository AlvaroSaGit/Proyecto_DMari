// dto maestro para facturacion. corregimos el paquete para que el compilador lo encuentre.
package com.dmari.modelo;

import java.util.List;

public class FacturaDTO {
    // identificador unico del pedido para la cabecera
    private int idPedido;
    // fecha de emision formateada para la vista
    private String fecha;
    // nombre completo del cliente que realizo la compra
    private String nombreCliente;
    // ubicacion fisica para el envio de los productos
    private String direccionEnvio;
    // medio por el cual se proceso el pago
    private String metodoPago;
    // monto total de la transaccion
    private double total;
    // listado de items individuales que componen la factura
    private List<FacturaDetalleDTO> detalles;

    // constructor vacio para inicializacion
    public FacturaDTO() {
    }

    // getters y setters para mapeo de joins complejos en el dao
    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<FacturaDetalleDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<FacturaDetalleDTO> detalles) {
        this.detalles = detalles;
    }
}
