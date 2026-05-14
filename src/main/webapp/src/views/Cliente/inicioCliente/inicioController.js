// importamos el servicio ui
import { cargarComponente } from '../../../services/uiService.js';
// importamos la funcion de agregar al carrito
import { agregarAlCarrito } from '../../../components/carritoSideBar/carritoController.js';
// importamos el servicio de base de datos
import { obtenerProductos } from '../../../services/productoService.js';
// importamos el componente de la tarjeta
import { crearTarjetaHTML } from '../../../components/tarjeta/tarjetaComponent.js';

// funcion principal para cargar la vista de inicio
export async function cargarVistaInicio() {
    // inyectamos el html vacio
    await cargarComponente('component-main', 'src/views/Cliente/inicioCliente/inicio.html');
    
    // una vez cargado el html llamamos la logica de las tarjetas
    renderizarProductos();
}

// funcion para inyectar las tarjetas
async function renderizarProductos() {
    const contenedorDestacado = document.getElementById('contenedor-producto');
    const contenedorDonas = document.getElementById('contenedor-donas');
    const contenedorVelas = document.getElementById('contenedor-velas');
    
    if (!contenedorDestacado || !contenedorDonas || !contenedorVelas) return;
    
    // obtenemos los datos desde el servicio
    const productos = await obtenerProductos();
    if (!productos) return;

    // limpiamos contenedores
    contenedorDestacado.innerHTML = '';
    contenedorDonas.innerHTML = '';
    contenedorVelas.innerHTML = '';

    // construimos los nodos de forma segura
    productos.forEach(producto => {
        contenedorDestacado.appendChild(crearTarjetaHTML(producto));

        if (producto.categoria === 'donas') {
            contenedorDonas.appendChild(crearTarjetaHTML(producto));
        } else if (producto.categoria === 'velas') {
            contenedorVelas.appendChild(crearTarjetaHTML(producto));
        }
    });
    
    // buscamos todos los botones recien creados (con la nueva clase de la tarjeta)
    const botonesAgregar = document.querySelectorAll('.btn-agregar-catalogo');
    
    botonesAgregar.forEach(boton => {
        boton.addEventListener('click', function() {
            const id = parseInt(this.getAttribute('data-id'));
            const nombre = this.getAttribute('data-nombre');
            const precio = parseFloat(this.getAttribute('data-precio'));
            agregarAlCarrito(id, nombre, precio);
        });
    });
}