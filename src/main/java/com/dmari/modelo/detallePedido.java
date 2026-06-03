/*
    objetivo de este archivo:
    molde para representar los productos individuales dentro de una compra.
    tambien contiene campos extra para facilitar las vistas cruzadas,
    como el nombre del cliente y el estado del pedido.
    
    nota de arquitectura:
    este modelo funciona como un dto (data transfer object). 
    para no crear clases separadas para 'pedido', 'cliente' y 'usuario', 
    fusionamos todas esas columnas de mysql en este solo archivo. 
    asi es mas facil enviar un solo paquete json al frontend javascript.
*/
package com.dmari.modelo;

public class detallePedido {
    
    // ==========================================
    // bloque 1: datos puros de la tabla detalle_pedido
    // ==========================================
    private int idDetallePedido;
    private int idPedidoFk;
    private int idProductoFk;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    
    // ==========================================
    // bloque 2: datos fusionados (joins) de las tablas pedido, usuario y cliente
    // ==========================================
    private String nombreProducto;
    private String nombreCliente;
    private String fechaPedido;
    private String estadoPedido;
    private String nombreProveedor;
    private String metodoPago;
    private String referenciaPago;

    // constructores vacios por defecto
    public detallePedido() {
    }

    // getters y setters
    public int getIdDetallePedido() {
        return idDetallePedido;
    }
    public void setIdDetallePedido(int idDetallePedido) {
        this.idDetallePedido = idDetallePedido;
    }

    public int getIdPedidoFk() {
        return idPedidoFk;
    }
    public void setIdPedidoFk(int idPedidoFk) {
        this.idPedidoFk = idPedidoFk;
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

    public double getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // getters y setters de los datos extra
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(String fechaPedido) { this.fechaPedido = fechaPedido; }

    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String estadoPedido) { this.estadoPedido = estadoPedido; }

    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
}