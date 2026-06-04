// servicio para la gestion de visualizacion de facturas modulo 4

/**
 * solicita los datos del pedido al servidor y despliega el modal de factura.
 * @param {number} idPedido - identificador unico del pedido a consultar.
 */
export async function verFactura(idPedido) {
    try {
        // peticion al servlet encargado de la facturacion
        const respuesta = await fetch(`facturacion?id=${idPedido}`);
        if (!respuesta.ok) throw new Error('no se pudo obtener la informacion de la factura');
        
        const factura = await respuesta.json();
        
        // construccion de la estructura html para el modal (pruebas)
        const html = `
            <div style="font-family: sans-serif; color: #333;">
                <div style="border-bottom: 2px solid #d4a373; padding-bottom: 10px; margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
                    <h3 style="margin: 0; color: #d4a373;">DETALLE DE PEDIDO #00${factura.idPedido}</h3>
                    <span style="font-size: 0.85rem; color: #666;">${factura.fecha}</span>
                </div>
                
                <div style="margin-bottom: 20px; font-size: 0.9rem;">
                    <p><strong>Cliente:</strong> ${factura.nombreCliente}</p>
                    <p><strong>Envio:</strong> ${factura.direccionEnvio}</p>
                    <p><strong>Pago:</strong> ${factura.metodoPago}</p>
                </div>

                <table style="width: 100%; border-collapse: collapse; font-size: 0.9rem;">
                    <thead>
                        <tr style="background: #f8f9fa;">
                            <th style="padding: 10px; text-align: left; border-bottom: 1px solid #dee2e6;">Producto</th>
                            <th style="padding: 10px; text-align: center; border-bottom: 1px solid #dee2e6;">Cant.</th>
                            <th style="padding: 10px; text-align: right; border-bottom: 1px solid #dee2e6;">Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${factura.detalles.map(d => `
                            <tr>
                                <td style="padding: 10px; border-bottom: 1px solid #eee;">${d.nombreProducto}</td>
                                <td style="padding: 10px; text-align: center; border-bottom: 1px solid #eee;">${d.cantidad}</td>
                                <td style="padding: 10px; text-align: right; border-bottom: 1px solid #eee;">$${d.subtotal.toFixed(2)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>

                <div style="text-align: right; margin-top: 20px; font-size: 1.1rem;">
                    <strong>Total Pagado: <span style="color: #28a745;">$${factura.total.toFixed(2)}</span></strong>
                </div>
            </div>
        `;

        // uso del sistema de modal global definido en uiservice.js
        window.mostrarModal(html);

    } catch (error) { console.error('error al cargar la factura:', error); }
}