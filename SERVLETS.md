# 🔌 API RESTful: Los Servlets (Controladores Java)

Los Servlets actúan como los Controladores en el patrón MVC del backend. Son las puertas de enlace que reciben las peticiones HTTP (GET, POST) desde JavaScript, llaman a los DAOs para operar la base de datos, y devuelven respuestas (generalmente en formato JSON).

## 🔐 Servlets de Autenticación

### `/login` (POST)
Recibe el correo y contraseña empaquetados por `URLSearchParams`. 
* **Lógica:** Consulta a la BD si el usuario existe y si las contraseñas encriptadas coinciden. Si es exitoso, crea una sesión real en el servidor (`HttpSession`) y devuelve un JSON con el rol del usuario (ID 1, 2 o 4).

### `/session` (GET)
Utilizado por el Frontend cada vez que se carga la página o se intenta hacer una compra.
* **Lógica:** Revisa si la `HttpSession` existe. Devuelve código `200 OK` si el usuario está logueado, o `401 Unauthorized` si es un visitante.

### `/logout` (GET/POST)
* **Lógica:** Invalida y destruye la `HttpSession` en el servidor, obligando al usuario a volver a iniciar sesión.

## 📦 Servlets de Catálogo e Inventario

### `/listar` (GET)
* **Lógica:** Retorna un arreglo JSON con todos los productos. Si recibe el parámetro `?activos=true`, filtra la lista para no enviarle a los clientes productos que el administrador haya pausado o eliminado lógicamente.

### `/insertar` y `/actualizar` (POST)
Controladores exclusivos para administradores y proveedores.
* **Lógica:** Reciben los datos del Modal (nombre, precio, stock, id_categoria). Dependiendo de la ruta, el Servlet decide si crear un registro completamente nuevo en MySQL o si hacer un UPDATE basado en el ID recibido.

### `/cambiar-estado` (POST)
* **Lógica:** No elimina el producto de la base de datos. Simplemente cambia un booleano (true/false) para ocultarlo temporalmente del catálogo público ("Pausar" producto).

## 🏷️ Servlets de Apoyo

### `/categorias` y `/etiquetas` (GET)
* **Lógica:** Devuelven los listados dinámicos de las categorías (Repostería, Decoración) y los Tags. Son consumidos por los `<select>` de los formularios para evitar escribirlos "quemados" (Hardcoded) en el HTML.