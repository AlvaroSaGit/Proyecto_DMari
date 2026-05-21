import { cargarComponente } from '../../../services/uiService.js';
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
// importamos el servicio que maneja los datos
import { obtenerProductos } from '../../../services/productoService.js';
// importamos nuestro nuevo componente ui de tarjeta
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';

/**
 * funcion principal para cargar la vista del catalogo.
 * inyecta el html base y luego renderiza los productos.
 */
export async function cargarVistaCatalogo() {
    // 1. inyectamos el contenedor principal para el catalogo
    await cargarComponente('component-main', 'src/views/Cliente/catalogo/catalogo.html');
    
    // 1.5 Inyectamos el componente de filtros avanzado que armamos
    await cargarComponente('component-filtros', './src/assets/components/filtro/filtro.html');

    // 2. obtenemos y mostramos los productos desde el backend
    renderizarProductosCatalogo();
}

/**
 * obtiene los productos del backend y los muestra en el dom.
 */
async function renderizarProductosCatalogo() {
    const contenedor = document.getElementById('catalogo-productos-grid');
    if (!contenedor) return;

    // mostramos un estado de carga mientras esperamos los datos
    contenedor.innerHTML = '<p>Cargando productos...</p>';

    // 3. obtenemos los datos limpios desde nuestro servicio
    const productos = await obtenerProductos();

    // si el servicio devuelve null, hubo un error de conexion
    if (!productos) {
        contenedor.innerHTML = '<p>Hubo un problema al cargar los productos. Por favor, intenta más tarde.</p>';
        return;
    }

    // si no hay productos, mostramos un mensaje
    if (productos.length === 0) {
        contenedor.innerHTML = '<p>No hay productos disponibles en este momento.</p>';
        return;
    }

    // 4. limpiamos el contenedor y agregamos los nodos html de forma segura
    contenedor.innerHTML = '';
    productos.forEach(producto => {
        const tarjetaNodo = crearTarjetaHTML(producto);
        contenedor.appendChild(tarjetaNodo);
    });

    // 5. delegacion de eventos para los botones "agregar"
    contenedor.addEventListener('click', (evento) => {
        const boton = evento.target.closest('.btn-agregar-catalogo');
        if (boton) {
            const id = parseInt(boton.dataset.id);
            const nombre = boton.dataset.nombre;
            const precio = parseFloat(boton.dataset.precio);
            // Extraemos el stock asegurandonos de que sea un numero. Si por alguna razon no viene, enviamos null.
            const stock = boton.dataset.stock ? parseInt(boton.dataset.stock) : null;
            
            agregarAlCarrito(id, nombre, precio, stock);
        }
    });
}