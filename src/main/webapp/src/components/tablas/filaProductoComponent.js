/*
    objetivo de este archivo:
    componente reutilizable que fabrica el html de una fila (tr) de producto.
    al separar esto, limpiamos los controladores y evitamos repetir codigo 
    entre las distintas tablas del proyecto.
*/

export function crearFilaProducto(prod) {
    const idProd = prod.idProductoPk || prod.id;
    const nombreProd = prod.nombreProducto || prod.nombre || 'Producto';
    const nombreCategoria = prod.categoria || prod.nombreCategoria || 'Sin categoria';
    const catFk = prod.idCategoriaFk || '';
    
    const estadoBadge = prod.estado 
        ? '<span style="background: #d4edda; color: #155724; padding: 2px 6px; border-radius: 10px; font-size: 0.75rem; margin-left: 5px;">Activo</span>' 
        : '<span style="background: #f8d7da; color: #721c24; padding: 2px 6px; border-radius: 10px; font-size: 0.75rem; margin-left: 5px;">Inactivo</span>';
    
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>#00${idProd}</td>
        <td><i class='bx bx-image' style='font-size: 2rem; color: #ccc;'></i></td>
        <td>
            <div style="font-weight: bold; display: flex; align-items: center;">${nombreProd} ${estadoBadge}</div>
            <span style="font-size: 0.75rem; color: #666; background-color: #f0f0f0; padding: 2px 6px; border-radius: 10px;">${nombreCategoria}</span>
        </td>
        <td>$${prod.precio.toFixed(2)}</td>
        <td>${prod.stock} uds</td>
        <td>
            <button class="btn-editar" data-id="${idProd}" data-nombre="${nombreProd}" data-precio="${prod.precio}" data-stock="${prod.stock}" data-categoria="${catFk}"><i class='bx bx-edit'></i> Editar</button>
            <button class="btn-estado" data-id="${idProd}" data-estado="${prod.estado}"><i class='bx bx-refresh'></i> ${prod.estado ? 'Pausar' : 'Activar'}</button>
            <button class="btn-eliminar" data-id="${idProd}"><i class='bx bx-trash'></i> Borrar</button>
        </td>
    `;
    return tr;
}