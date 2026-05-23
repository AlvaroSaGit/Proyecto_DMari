# 🍩 Tienda DMari - E-commerce & Sistema de Catálogo

## 📌 Naturaleza del Proyecto
DMari es una plataforma de comercio electrónico orientada a la venta de repostería, decoración y arreglos florales. El sistema no solo funciona como una tienda pública (B2C), sino como un **Sistema de Gestión Integral** que maneja múltiples roles (Administradores, Clientes, Proveedores y Repartidores).

## 🏗️ Arquitectura del Sistema
El proyecto está construido bajo el patrón de arquitectura **Cliente-Servidor**, implementando una **Single Page Application (SPA)** en el Frontend y una **API RESTful** en el Backend.

### 💻 Frontend (El Cliente)
- **Tecnologías:** HTML5, CSS3, JavaScript (Vanilla ES6+).
- **Enfoque:** Arquitectura modular basada en componentes y controladores.
- **Navegación:** Enrutador propio (`router.js`) que simula la navegación entre páginas inyectando vistas dinámicamente en el `<div id="component-main">` sin recargar el navegador.

### ⚙️ Backend (El Servidor)
- **Tecnologías:** Java (Jakarta EE / Servlets), JDBC.
- **Patrón de Diseño:** MVC (Modelo-Vista-Controlador) adaptado a servicios REST.
- **Base de Datos:** MySQL Relacional (Esquema altamente normalizado).

## 🔐 Gestión de Roles y Seguridad
El sistema adapta su interfaz gráfica y restringe el acceso a las rutas (URL) dependiendo del rol de la sesión activa:

1. **Visitante (Sin sesión):** Puede ver el inicio y el catálogo, y agregar al carrito (pero no puede pagar).
2. **Cliente (Rol 2):** Tiene acceso al catálogo, carrito de compras sincronizado y a su historial de pedidos.
3. **Administrador (Rol 1):** Interfaz transformada en **Dashboard** (Panel de Control). Se le ocultan las vistas de compras y se le habilita la gestión CRUD (Crear, Leer, Actualizar, Borrar) del inventario.
4. **Proveedor (Rol 4):** Acceso restringido a sus propios productos.

## 📂 Estructura de la Documentación
Para entender la lógica a fondo, consulta los siguientes manuales técnicos ubicados en la carpeta `/docs`:

* 📄 Lógica de Vistas y UI (Frontend)
* 📄 Controladores y Servlets (Backend)
* 📄 Acceso a Datos y DAOs (Base de Datos)