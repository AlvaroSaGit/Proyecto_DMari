/*
    objetivo de este archivo:
    componente reutilizable que fabrica el html de una tarjeta o bloque de pedido
    para el historial del cliente. al separar esto, limpiamos el controlador y 
    mantenemos el diseno visual centralizado.
*/

export function crearBloquePedido(pedido) {
    // se asigna un color semantico dependiendo de en que fase se encuentra el pedido
    let colorEstado = '#333';
    if (pedido.estado.toLowerCase() === 'entregado') colorEstado = 'green';
    if (pedido.estado.toLowerCase() === 'pendiente') colorEstado = 'orange';

    let htmlProductos = '';
    // se genera una fila por cada producto comprado dentro de esta factura
    pedido.productos.forEach(prod => {
        htmlProductos += `
            <div style="display:flex; justify-content:space-between; border-bottom:1px solid #eee; padding:5px 0; font-size:0.9rem;">
                <span>${prod.cantidad}x ${prod.producto}</span>
                <span style="color:#666;">$${prod.subtotal.toFixed(2)}</span>
            </div>
        `;
    });

    // se crea el contenedor principal de la factura
    const div = document.createElement('div');
    div.className = 'item-historial';
    div.style.cssText = 'display:block; margin-bottom:15px; border:1px solid #ddd; padding:15px; border-radius:8px; background:#fff; box-shadow: 0 2px 4px rgba(0,0,0,0.02);';
    
    // se inyecta la informacion agrupada
    div.innerHTML = `
        <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom:2px solid #f5f5f5; padding-bottom:10px;">
            <div>
                <span style="font-weight:bold; font-size:1.1rem; color:#000;">pedido #00${pedido.id}</span><br>
                <span style="font-size:0.85rem; color:#888;">realizado el: ${pedido.fecha}</span>
            </div>
            <div style="text-align:right;">
                <span style="font-weight:bold; color:${colorEstado}; text-transform:uppercase; font-size:0.85rem;">${pedido.estado}</span><br>
                <span style="font-weight:bold; font-size:1.1rem; color:#000;">total: $${pedido.total.toFixed(2)}</span>
            </div>
        </div>
        <div>
            <p style="margin:0 0 5px 0; font-size:0.85rem; color:#444; font-weight:bold;">resumen de compra</p>
            ${htmlProductos}
        </div>
    `;
    return div;
}