/*
    objetivo de este archivo:
    este componente visual aisla la logica de construccion html para el historial de compras.
    recibe un objeto de pedido agrupado y retorna un elemento div listo para ser inyectado 
    en la interfaz, mejorando la legibilidad y el mantenimiento del controlador principal.
*/

export function crearBloquePedido(pedido) {
    // se define un color por defecto (gris oscuro) para estados desconocidos
    let colorEstado = '#333';
    
    // se asigna un color semantico dependiendo de la fase actual del pedido.
    // tolowercase garantiza que la comparacion no falle por diferencias de mayusculas.
    if (pedido.estado.toLowerCase() === 'entregado') colorEstado = 'green';
    if (pedido.estado.toLowerCase() === 'pendiente') colorEstado = 'orange';

    // variable para acumular el codigo html de cada producto individual dentro de la factura
    let htmlProductos = '';
    
    // se itera sobre el sub-arreglo de productos que pertenecen exclusivamente a este pedido
    pedido.productos.forEach(prod => {
        // se concatena una fila limpia con la cantidad, el nombre y el subtotal formateado a 2 decimales
        htmlProductos += `
            <div style="display:flex; justify-content:space-between; border-bottom:1px solid #eee; padding:5px 0; font-size:0.9rem;">
                <span>${prod.cantidad}x ${prod.producto}</span>
                <span style="color:#666;">$${prod.subtotal.toFixed(2)}</span>
            </div>
        `;
    });

    // se instancia un nuevo elemento div en la memoria del navegador (dom virtual)
    const div = document.createElement('div');
    
    // se asigna una clase css por si en el futuro se desea aplicar estilos desde una hoja externa
    div.className = 'item-historial';
    
    // se inyectan los estilos en linea para asegurar que la tarjeta se vea como un bloque separado
    div.style.cssText = 'display:block; margin-bottom:15px; border:1px solid #ddd; padding:15px; border-radius:8px; background:#fff; box-shadow: 0 2px 4px rgba(0,0,0,0.02);';
    
    // se ensambla el esqueleto principal de la tarjeta inyectando las variables del pedido y el acumulado de productos
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
    
    // se retorna el nodo html completo y listo para ser adjuntado al contenedor principal
    return div;
}