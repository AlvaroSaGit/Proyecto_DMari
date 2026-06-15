# 🌊 Flujo de Datos en DMari - Arquitectura Cliente/Servidor

Este documento explica el recorrido paso a paso de la informacion a traves de las diferentes capas del sistema: **Frontend (Vista) ➔ Backend (Servlet) ➔ DAO (Logica de Datos) ➔ MySQL (Base de Datos)**.

---

## 1. 📝 Flujo de Registro de Usuario
**Objetivo:** Crear una nueva cuenta protegiendo la contrasena y dividiendo los datos.

1. **Frontend (`registro.html` + `registroController.js`):** 
   El visitante llena el formulario. Al presionar "Registrarse", el archivo `registroController.js` intercepta el evento `submit`, empaqueta el nombre, correo y contrasena usando `URLSearchParams`, y hace una peticion `fetch('registro', { method: 'POST' })` hacia Java.
2. **Backend (Servlet Controlador):**
   El Servlet recibe la peticion POST. Extrae los textos, crea un objeto del modelo `usuario` y se lo envia al `usuarioDAO`.
3. **DAO (`usuarioDAO.java` - `registrarUsuario`):**
   Este es el punto critico. El DAO apaga el autoguardado (`setAutoCommit(false)`) para iniciar una **transaccion segura** y ejecuta tres pasos:
   * Inserta en la tabla `usuario` (asignandole rol de cliente 2 por defecto) y captura el `ID` generado.
   * Usa ese mismo `ID` para insertar en la tabla `correo`.
   * Inserta en la tabla `credenciales`, usando la funcion nativa **`AES_ENCRYPT(password, llave_secreta)`** para que la contrasena quede ilegible (BLOB).
4. **Respuesta:** Si las tres tablas se llenan con exito, se hace un `commit()` y se responde con un codigo `200 OK` al Frontend para que redirija al login.

---

## 2. 🔐 Flujo de Inicio de Sesion (Login)
**Objetivo:** Validar credenciales, crear una sesion segura y direccionar al usuario segun su rol.

1. **Frontend (`login.html` + `loginController.js`):** 
   El usuario digita su correo y contrasena. El controlador hace un `fetch('login')` enviando ambos datos.
2. **Backend (Servlet Controlador):**
   El Servlet recibe los datos y le pide al DAO que verifique si el usuario existe y si la clave coincide.
3. **DAO (`usuarioDAO.java` - `verificarLogin`):**
   El DAO ejecuta un `INNER JOIN` entre las tablas `usuario`, `correo` y `credenciales`.
   * Usa **`AES_DECRYPT(passwd_encript, llave_secreta)`** para destrabar la contrasena de la BD temporalmente y compararla con la que escribio el usuario.
   * Si coinciden, empaqueta los datos del usuario (incluyendo su `id_rol_fk`) y lo devuelve al Servlet.
4. **Manejo de Sesion (Backend):**
   El Servlet crea una sesion oficial en el servidor (`request.getSession(true)`), guarda el objeto usuario alli y le devuelve al Frontend un JSON con el numero de rol.
5. **Enrutamiento (`router.js`):**
   JavaScript lee el JSON, guarda el rol en `sessionStorage` y decide: si es Rol 1 (Admin) lo manda al dashboard de productos; si es Rol 2 (Cliente) lo manda a la tienda (`inicio`).

---

## 3. ⚙️ Flujo de Configuracion de Cliente (Guardar Perfil)
**Objetivo:** Guardar o actualizar la direccion de entrega y el telefono del cliente de forma segura.

1. **Frontend (`configuracion.html` + `configuracionController.js`):** 
   El cliente digita su direccion y telefono. JavaScript atrapa estos datos y hace un `fetch('perfil-cliente', { method: 'POST' })`.
2. **Backend (`PerfilClienteController.java`):**
   El Servlet tiene una **barrera de seguridad**: verifica primero si hay una sesion activa (`request.getSession(false)`). Si la hay, extrae el `idUsuario`, recibe los textos del formulario y llama al DAO.
3. **DAO (`clienteDAO.java` - `guardarOActualizarPerfil`):**
   El DAO inicia una transaccion (`setAutoCommit(false)`) y actualiza 3 tablas diferentes de forma orquestada:
   * **Tabla `cliente`:** Inserta o actualiza referencias generales usando la clausula inteligente `ON DUPLICATE KEY UPDATE` (crea si no existe, actualiza si ya existe).
   * **Tabla `direccion`:** Guarda la calle y detalle forzando el estado `direccion_primario = 1`.
   * **Tabla `telefono`:** Inserta el numero telefonico, y si el usuario ya tenia uno viejo, usa un `UPDATE` para reemplazarlo.
4. **Respuesta:**
   Si nada falla, se hace un `commit()` y se envia un `200 OK`. JavaScript muestra la alerta verde de "Informacion actualizada".

---

## 4. 🛒 Flujo del Carrito de Compras (Sincronizacion Hibrida)
**Objetivo:** Mantener la pagina rapida usando el navegador del cliente, pero sin perder los datos si cierra la pestana.

1. **Frontend (LocalStorage + Debounce):** 
   Cuando el usuario da clic en "Agregar", JavaScript guarda el producto instantaneamente en la memoria del navegador (`localStorage`). La interfaz se actualiza sin recargar. Para no saturar a MySQL si el usuario da 20 clics rapidos, se usa una tecnica llamada **"Debounce"** (`setTimeout` de 1.5s): el sistema espera a que el usuario deje de dar clics y luego hace una sola peticion HTTP en la sombra hacia `/carrito-db`.
2. **Backend (`CarritoDBController.java`):**
   Recibe la lista de productos y cantidades.
3. **DAO (`carritoDAO.java` - `sincronizarCarrito`):**
   Usa procesamiento por lotes (Batch).
   * Desactiva el autocommit.
   * Ejecuta un `DELETE` de todos los productos viejos asociados a ese carrito.
   * Usa `addBatch()` y `executeBatch()` para insertar los productos nuevos de un solo golpe.
   * Hace `commit()`. Si falla, hace `rollback()`.

---

## 5. 💳 Flujo de Checkout (Creacion del Pedido)
**Objetivo:** Convertir el carrito en una venta real descontando el inventario de forma segura (ACID).

1. **Frontend (`carritoController.js`):** 
   El usuario marca los productos (check `seleccionado`) que desea pagar ahora. JS filtra el arreglo del carrito y envia al servidor solo los elementos seleccionados.
2. **Backend (Pedido DAO):**
   Este es el proceso transaccional mas estricto del sistema.
   * `INSERT` en la tabla `pedido` (obtiene el ID maestro de la factura).
   * Bucle: Por cada producto seleccionado en el carrito, hace un `INSERT` en `detalle_pedido` guardando el precio unitario actual (snapshot) y calculando el subtotal.
   * Bucle: Hace un `UPDATE` en la tabla `producto` para restar la cantidad comprada al `stock` disponible.
   * Si todo sale perfecto: `commit()`. Si un producto se quedo sin stock justo en ese milisegundo: `rollback()`.
3. **Limpieza (Post-Compra):**
   Java devuelve un `200 OK`. JavaScript aplica un `.filter()` al arreglo `carrito` para eliminar solo los items cuya propiedad `seleccionado` era `true`.
   Inmediatamente se dispara `programarSincronizacion()`, enviando el carrito restante a la base de datos. El DAO de MySQL reemplaza el contenido viejo por este nuevo estado filtrado, logrando una limpieza selectiva sin logica SQL compleja. Finalmente, se redirige a `#historial`.

---

## 6. 🏪 Flujo del Catalogo y Filtrado Inteligente (SPA)
**Objetivo:** Mostrar los productos al cliente de forma veloz sin sobrecargar la base de datos con consultas `SELECT` repetitivas.

1. **Carga Inicial (`productosController.js`):**
   Al entrar a la tienda, JS hace un unico `fetch('listar?activos=true')` al servidor.
2. **Backend (`productoDAO.java`):**
   Java hace una consulta pesada con `LEFT JOIN` a las tablas `categoria`, `proveedor`, e `imagenes`, agrupando etiquetas con `GROUP_CONCAT`. Devuelve un mega-JSON con toda la tienda.
3. **Filtrado en Memoria (`filtroService.js`):**
   Una vez que JavaScript tiene el JSON, no vuelve a molestar a Java. Si el usuario escribe en el buscador, selecciona la etiqueta "Dulce" o la categoria "Reposteria", JavaScript filtra el arreglo directamente en la memoria RAM del dispositivo (`Array.filter()`) y redibuja las tarjetas instantaneamente.
