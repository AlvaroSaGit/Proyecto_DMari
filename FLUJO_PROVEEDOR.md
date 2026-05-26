# 📦 Flujo de Datos en DMari - Modulo de Proveedor

Este documento explica como funciona el modulo exclusivo para los proveedores (Rol 4), desde que inician sesion hasta que administran su inventario personal y como el sistema protege sus datos.

---

## 1. 🔐 Inicio de Sesion y Enrutamiento Protegido
**Objetivo:** Identificar al proveedor y restringir su acceso a sus vistas correspondientes.

1. **Frontend:** El proveedor ingresa sus credenciales en el login. El `loginController.js` envia los datos a Java.
2. **Backend:** El `usuarioDAO` verifica las credenciales y devuelve un JSON con el `id_rol_fk = 4`.
3. **Enrutador (`router.js`):** Al detectar el rol 4 en el `sessionStorage`, el sistema bloquea el acceso a la tienda publica de clientes y lo redirige automaticamente a la ruta privada `#proveedor-productos`.

---

## 2. 📋 Carga del Catalogo Privado
**Objetivo:** Mostrarle al proveedor unica y exclusivamente los productos que le pertenecen a su marca.

1. **Frontend (`proveedorProductosController.js`):** Al cargar la vista de la tabla, JavaScript hace una peticion GET especial anadiendo un parametro a la URL: `fetch('listar?proveedor=true')`.
2. **Backend (`ProductoController.java`):** El Servlet detecta el parametro `proveedor=true`. En lugar de traer todo el inventario de la tienda, extrae el ID del usuario de la sesion actual y llama a un metodo especifico del DAO.
3. **DAO (`productoDAO.java` - `listarProductosPorProveedor`):** Ejecuta una consulta SQL con un `INNER JOIN` hacia la tabla intermedia `proveedor_producto`, filtrando por el ID del proveedor logueado. Devuelve un JSON seguro asegurando que no vea mercancia de su competencia.

---

## 3. ➕ Creacion y Asignacion Automatica de Productos
**Objetivo:** Guardar un nuevo articulo (dona, vela, flor) y asociarlo automaticamente al proveedor de forma invisible.

1. **Frontend:** El proveedor llena el modal (nombre, precio, stock, y adjunta una foto). JS empaqueta todo usando `FormData` (vital para soportar el envio de imagenes fisicas) y hace un POST a `/insertar`.
2. **Backend (`ProductoController.java`):** 
   * Recibe los textos y el archivo (Part) de la imagen.
   * El DAO inserta el producto general en la tabla `producto` y recupera el `ID` autogenerado por MySQL.
   * Guarda la imagen fisicamente en el disco duro del servidor y guarda su ruta en la tabla `imagenes`.
   * **La Magia (Enlace Inteligente):** Java revisa la sesion activa. Al ver que el usuario es de Rol 4, ejecuta silenciosamente `dao.asignarProductoAProveedor(idGenerado, idUsuario)`. Esto inyecta el registro en la tabla `proveedor_producto` sin que el proveedor tuviera que hacer nada extra.
3. **Respuesta:** Se devuelve un codigo `200 OK`. El frontend cierra el modal y recarga la tabla dinamicamente.

---

## 4. ⚙️ Ciclo de Vida: Edicion, Pausado y Eliminacion
* **Pausar (`/cambiar-estado`):** Si un proveedor se queda sin insumos para hacer una dona, le da a "Pausar". El sistema no borra el producto, solo cambia la columna `estado` a `false`. Esto hace que el producto desaparezca inmediatamente del catalogo publico de los clientes (porque ellos filtran con `?activos=true`), pero el proveedor lo sigue viendo en su panel.
* **Eliminar (`/eliminar`):** Intenta ejecutar un `DELETE` fisico en la BD. **Integridad Referencial:** Si un cliente ya habia comprado este producto alguna vez (existe en `detalle_pedido`), MySQL bloqueara la eliminacion para no destruir el historial de facturas. Java captura este error y le sugiere al proveedor que simplemente lo "Pause".
* **Actualizar (`/actualizar`):** Funciona igual que crear. Si el proveedor sube una foto nueva, Java primero borra la ruta vieja en `imagenesDAO` y luego inserta la nueva.