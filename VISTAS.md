# 🖥️ Lógica de las Vistas y Componentes (Frontend)

Este documento explica cómo funciona la interfaz de usuario en la aplicación Single Page Application (SPA) de DMari.

## 🧭 El Enrutador (`router.js`)
Es el "Guardia de Tráfico" del sistema. Escucha los cambios en la URL (evento `hashchange`).
* **Mutación Dinámica:** Inyecta diferentes archivos HTML en el contenedor central según la ruta (ej. `#catalogo` carga `catalogo.html`).
* **Bloqueo por Roles:** Revisa el `sessionStorage`. Si un Administrador intenta entrar a `#inicio`, lo intercepta y lo expulsa hacia su panel (`#admin-productos`).

## 🎭 Adaptación Dinámica del Header
En lugar de crear varios archivos de cabecera, `header.html` es único e inteligente.
* **Modo Cliente:** Muestra el icono de Categorías, el Perfil y el Carrito de compras.
* **Modo Administrador:** El enrutador oculta (vía `display: none`) el carrito y las categorías (un admin no compra en su propia tienda). Convierte el ícono de perfil en una etiqueta estática (ej. "Hola, Álvaro") y muestra el botón directo de "Salir".

## 📦 Vista del Catálogo (`catalogo.html` / `productosController.js`)
* **Filtros Avanzados:** Posee un sistema de filtrado cruzado en memoria (`filtroService.js`) que permite buscar por texto, categorías (Repostería, Decoración) y rango de precios sin necesidad de hacer múltiples consultas a la base de datos.
* **Optimización de Caché:** Al solicitar los datos a Java, usa un "Cache-Buster" (`&t=timestamp`) para forzar a navegadores tercos a mostrar los productos más recientes.

## 🛠️ Vista del Administrador (`adminProductos.html`)
Utiliza el estilo de diseño **Dashboard** (Panel de Control).
* **Layout Dashboard:** El enrutador inyecta una barra lateral oscura (`adminSideBar.html`) fijada a la izquierda y empuja la tabla principal a la derecha.
* **Delegación de Eventos:** En lugar de asignarle un clic a cada botón "Editar" o "Borrar", se le asigna un único escuchador a la tabla (`tbody`). Esto ahorra memoria RAM y evita fallos al inyectar nuevos productos.
* **Modal Dinámico:** Usa una ventana flotante (Modal) reutilizable. Si el admin da clic en "Nuevo Producto", la ventana nace limpia. Si da clic en "Editar", la ventana se pre-llena con los atributos HTML del botón clickeado (data-attributes).

## 🛒 Componente del Carrito (`carritoController.js`)
* **Persistencia Local:** Los artículos seleccionados se guardan en el `localStorage` del navegador. Si el cliente cierra la pestaña y vuelve, su carrito sigue intacto.
* **Control de Stock:** Frena silenciosamente los intentos del usuario por agregar más cantidades de las que existen en la base de datos (tope de input).