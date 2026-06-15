# 🗄️ Acceso a Datos: Patrón DAO (Data Access Object)

El patrón DAO se utiliza para separar la lógica de negocio (Servlets) de las consultas complejas de SQL. Cada tabla principal de la base de datos de DMari tiene un archivo DAO correspondiente.

## 🛒 ProductoDAO
Es el corazón del inventario.
* **Consultas Complejas (JOINs):** Cuando se solicitan los productos, este DAO no hace un simple `SELECT *`. Realiza un `LEFT JOIN` con las tablas de `categoria`, `etiqueta` e `imagenes`. 
* **Manejo de Etiquetas:** Utiliza funciones de MySQL como `GROUP_CONCAT` para agrupar múltiples etiquetas (Aromaterapia, Relajación) en un solo arreglo (String) por producto, facilitando la conversión a JSON.

## 👤 UsuarioDAO & Credenciales
Maneja la seguridad del sistema.
* **Cifrado Fuerte:** En lugar de guardar las contraseñas como texto plano en la tabla de usuarios, las separa en una tabla satélite llamada `credenciales`.
* **AES_ENCRYPT / DECRYPT:** Utiliza funciones nativas criptográficas de MySQL manipulando datos de tipo `BLOB`. Si la base de datos se ve comprometida, las credenciales seguirán estando seguras.

## 📦 PedidoDAO (Transaccional)
Es el DAO más crítico de la tienda y asegura que no haya pérdida de dinero ni inventario fantasma.
* **Integridad Transaccional:** Cuando un cliente paga su carrito, este DAO desactiva el Auto-Commit (`conn.setAutoCommit(false)`).
* **Ciclo de Vida de los Datos (Carrito ➔ Pedido):** 
  1. Inserta la orden maestra en la tabla `pedido`.
  2. **Snapshot de Precios:** Recupera los items de `detalle_carrito` donde `seleccionado = true` e inserta en `detalle_pedido` guardando el precio unitario del momento.
  3. **Control de Inventario:** Verifica y descuenta las cantidades del stock en `producto`. Si el stock es insuficiente, lanza una excepción para el rollback.
  4. **Sincronizacion de Limpieza:** En lugar de un borrado manual, el sistema delega la limpieza al `carritoDAO`. Al finalizar el pedido, el frontend envia el estado actualizado del carrito (solo con los items no comprados) y el DAO sincroniza la tabla `detalle_carrito` mediante un proceso de reemplazo total (Delete/Insert Batch).
* **Rollback:** Si alguna de las operaciones falla (por ejemplo, si no hay stock de un producto al último segundo), hace un `conn.rollback()`, deshaciendo absolutamente todo y protegiendo el sistema.

## 🏷️ EtiquetaDAO y CategoriaDAO
Manejan los listados satelitales. Permiten que la base de datos sea el único punto de verdad. Si un administrador agrega una categoría desde Java, automáticamente se reflejará en el HTML del frontend sin necesidad de tocar código.