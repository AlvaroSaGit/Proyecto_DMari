# 🛡️ Flujo de Datos en DMari - Modulo de Administrador

Este documento explica el comportamiento del panel de control principal, disenado para el Administrador de la tienda (Rol 1), quien tiene privilegios absolutos sobre el inventario, los usuarios y la logistica.

---

## 1. 🏗️ Enrutamiento y Mutacion de la Interfaz (Dashboard)
**Objetivo:** Transformar la tienda de clientes en un panel de administracion seguro.

1. **Deteccion:** Al hacer login, el servidor devuelve el `id_rol_fk = 1`.
2. **Mutacion Visual (`router.js`):** El enrutador intercepta la navegacion.
   * Le inyecta la clase `layout-dashboard` a toda la pagina para cambiar la estructura CSS.
   * Oculta botones innecesarios en el Header (como el carrito de compras y las categorias publicas).
   * Inyecta dinamicamente un panel lateral oscuro (`adminSideBar.html`) exclusivo para el control del negocio.
3. **Bloqueo:** Si el administrador intenta escribir la ruta `#carrito` o `#inicio` en la URL, el sistema lo rebota de inmediato a `#admin-productos`.

---

## 2. 📦 Gestion Maestra del Inventario (CRUD Total)
**Objetivo:** Controlar todo el catalogo, sin importar de que proveedor sea.

1. **Carga Global (`adminProductosController.js`):** A diferencia del proveedor, el administrador hace un `fetch('listar')` sin restricciones, trayendo toda la mercancia de la base de datos.
2. **Creacion con Delegacion:** Al crear un producto, el administrador usa un modal similar al del proveedor (`FormData` para la imagen), pero con un "Poder Extra": **El Select de Proveedores**. 
3. **Asignacion Manual:** Si el administrador selecciona un proveedor en la lista, Java inserta el producto y luego inyecta el registro en `proveedor_producto` asignandole el dueno elegido. Si lo deja en blanco, el producto se considera "Propio de DMari".

---

## 3. 🚚 Control de Logistica y Pedidos
**Objetivo:** Monitorear las ventas y cambiar los estados de entrega.

1. **Visualizacion (`adminPedidosController.js`):** El administrador ve todos los pedidos del sistema agrupados por numero de factura (usando la misma logica de agrupacion que el cliente, pero a nivel global).
2. **Actualizacion de Estado:** El administrador usa un `<select>` para cambiar el paquete de `Pendiente` ➔ `Preparando` ➔ `En Camino` ➔ `Entregado`.
3. **Backend:** Al cambiar el estado, JavaScript lanza un POST a `/pedido` con el nuevo estado. Java actualiza unicamente la columna `estado_pedido` en la tabla `pedido`.

---

## 4. 👥 Gestion de Usuarios y Ascensos (Seguridad)
**Objetivo:** Administrar quien entra al sistema y con que permisos.

1. **Panel de Usuarios:** El administrador puede ver a todos los clientes registrados (`usuarioDAO.listarUsuarios`).
2. **Buzon de Solicitudes:** El administrador cuenta con una vista para revisar peticiones de `solicitud_proveedor`.
3. **Aprobacion Transaccional:** Al aprobar una solicitud:
   * El `id_rol_fk` del usuario cambia de 2 a 4.
   * Los datos de la solicitud (NIT, Marca, Banco) se mueven permanentemente a la tabla `proveedor`.
   * La solicitud se marca como `APROBADA`.
4. **Rechazo:** Si el administrador rechaza, la solicitud cambia a `RECHAZADA` y el usuario permanece como cliente.