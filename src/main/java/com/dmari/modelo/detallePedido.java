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
    // identificador unico de la linea de detalle
    private int idDetallePedido;
    // referencia al pedido maestro
    private int idPedidoFk;
    // referencia al producto comprado
    private int idProductoFk;
    // unidades adquiridas
    private int cantidad;
    // precio capturado al momento de la venta
    private double precioUnitario;
    // calculo de cantidad por precio unitario
    private double subtotal;
    
    // ==========================================
    // bloque 2: datos fusionados (joins) de las tablas pedido, usuario y cliente
    // ==========================================
    // nombre descriptivo para la vista
    private String nombreProducto;
    // nombre de quien realiza la compra
    private String nombreCliente;
    // marca de tiempo de la orden
    private String fechaPedido;
    // situacion logistica actual del pedido
    private String estadoPedido;
    // motivo por el cual se canceló, si aplica
    private String motivoCancelacion;
    // nombre de la marca que vende el producto
    private String nombreProveedor;
    // forma en la que se liquido el importe
    private String metodoPago;
    // codigo o comprobante de la transaccion
    private String referenciaPago;

    // constructores vacios por defecto
    public detallePedido() {
    }

    // getters y setters
    // obtiene el id del detalle
    public int getIdDetallePedido() {
        return idDetallePedido;
    }
    // establece el id del detalle
    public void setIdDetallePedido(int idDetallePedido) {
        this.idDetallePedido = idDetallePedido;
    }

    // obtiene el id del pedido padre
    public int getIdPedidoFk() {
        return idPedidoFk;
    }
    // establece el id del pedido padre
    public void setIdPedidoFk(int idPedidoFk) {
        this.idPedidoFk = idPedidoFk;
    }

    // obtiene el id del producto
    public int getIdProductoFk() {
        return idProductoFk;
    }
    // establece el id del producto
    public void setIdProductoFk(int idProductoFk) {
        this.idProductoFk = idProductoFk;
    }

    // obtiene la cantidad
    public int getCantidad() {
        return cantidad;
    }
    // establece la cantidad
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // obtiene el precio por unidad
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    // establece el precio por unidad
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    // obtiene el subtotal de la linea
    public double getSubtotal() {
        return subtotal;
    }
    // establece el subtotal de la linea
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

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
}