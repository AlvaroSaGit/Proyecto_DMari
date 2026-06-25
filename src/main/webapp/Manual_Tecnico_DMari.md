# Manual Técnico - DMari E-commerce

*(Esta es una plantilla lista para que la copies y pegues en Microsoft Word, apliques el formato APA en fuentes y márgenes, e incluyas tu portada oficial).*

## Contenido
1. Introducción
2. Descripción General del Sistema
3. Requisitos Técnicos del Sistema
4. Instalación y Configuración
5. Arquitectura del Software y Base de Datos
6. Diseño del Software
7. Interfaces del Sistema
8. Mensajes de ayuda y Errores
9. Conclusión(es)
10. Mantenimiento y Actualizaciones
11. Referencia Bibliográfica
12. Anexos

---

## 1. Introducción

### 1.1 Propósito del Manual
El propósito de este manual técnico es proporcionar una guía detallada y exhaustiva sobre la arquitectura, configuración, diseño y mantenimiento del sistema web E-commerce "DMari". Está dirigido a la dirección de IT, administradores del sistema, desarrolladores de software y personal de auditoría de sistemas para facilitar la comprensión del código fuente, la estructura de la base de datos y los procesos de despliegue y mantenimiento continuo.

### 1.2 Alcance del Software
DMari es una plataforma de comercio electrónico tipo "Marketplace" diseñada para conectar a proveedores independientes con clientes finales. El software gestiona el ciclo completo de venta: desde la publicación de productos por parte de los proveedores y la validación de categorías por el administrador, hasta la gestión del carrito de compras, procesamiento de pedidos y notificaciones. Su desarrollo se justifica por la necesidad de una plataforma centralizada, escalable y segura que ofrezca interfaces diferenciadas según el rol del usuario (Administrador, Proveedor, Cliente).

## 2. Descripción General del Sistema

### Objetivo(s) del Sistema
Proveer una plataforma web robusta y eficiente que permita la comercialización de productos en línea, facilitando a los proveedores la gestión de sus inventarios y a los clientes una experiencia de compra fluida, todo bajo la supervisión y control de un administrador general.

### Requisitos Funcionales
- **Autenticación y Autorización**: Registro e inicio de sesión con roles definidos (Admin, Proveedor, Cliente) mediante JWT/Sesiones.
- **Gestión de Usuarios**: El administrador puede bloquear, activar y gestionar usuarios.
- **Gestión de Catálogo**: Los proveedores pueden crear y editar productos, subir imágenes y asignar etiquetas.
- **Gestión de Categorías**: El administrador controla la creación de categorías principales y subcategorías (los proveedores deben solicitar nuevas categorías).
- **Proceso de Compra**: Carrito de compras persistente, validación de stock, cálculo de totales y generación de pedidos.
- **Gestión de Pedidos**: Los proveedores pueden cambiar el estado de los pedidos (Pendiente, Enviado, Entregado, Cancelado, etc.).
- **Sistema de Notificaciones**: Alertas en tiempo real o persistentes para usuarios sobre el estado de sus solicitudes o pedidos.

### Requisitos No Funcionales
- **Rendimiento**: Tiempos de respuesta menores a 2 segundos en la carga del catálogo.
- **Seguridad**: Encriptación de contraseñas (BCrypt/SHA), protección contra inyecciones SQL (uso de PreparedStatement).
- **Usabilidad**: Interfaz web responsiva (adaptable a dispositivos móviles y escritorio).
- **Mantenibilidad**: Código modular basado en el patrón DAO (Data Access Object) y controladores Servlets.

## 3. Requisitos Técnicos del Sistema

### Requisitos de Hardware
**Servidor (Recomendado para Producción):**
- Procesador: 4 Cores (ej. Intel Xeon o AWS EC2 t3.medium).
- Memoria RAM: 8 GB mínimo (para Tomcat y MySQL).
- Almacenamiento: 50 GB SSD mínimo (crecimiento dependiente de las imágenes de productos).

**Cliente (Usuario final):**
- Dispositivo con conexión a internet (PC, Tablet, Smartphone).

### Requisitos de Software
- **Navegador Web**: Google Chrome, Mozilla Firefox, Safari o Microsoft Edge (versiones actualizadas).
- **Servidor Web / Contenedor de Servlets**: Apache Tomcat 10 o superior.
- **Motor de Base de Datos**: MySQL 8.0 o superior.
- **Entorno de Ejecución**: Java Development Kit (JDK) 17 o superior (Jakarta EE).

## 4. Instalación y Configuración

- **Tipo de aplicación**: Aplicación Web (Monolítica con separación de capas lógica/presentación).
- **Lenguaje o framework desarrollado**:
  - Backend: Java (Jakarta EE / Servlets).
  - Frontend: JavaScript (Vanilla JS con arquitectura de enrutador SPA), HTML5, CSS3.
- **Base de datos utilizada**: MySQL.
- **Sistema operativo previsto para la instalación**: Multiplataforma (Linux Ubuntu Server recomendado para producción, Windows para desarrollo).

### Configuración inicial (Entorno de Desarrollo)
1. **Gestor de BD**: Instalar MySQL Server. Ejecutar los scripts proporcionados en la carpeta `insertSql` (primero `tablaMysql.sql` para la estructura y luego datos semilla si existen).
2. **IDE de desarrollo**: Instalar Apache NetBeans (versión compatible con Jakarta EE).
3. **Servidor**: Configurar Apache Tomcat dentro de NetBeans.
4. **Archivos y Dependencias**: Abrir el proyecto en NetBeans. Asegurarse de que las librerías JDBC de MySQL y las dependencias de Jakarta EE estén correctamente referenciadas en el `pom.xml` o en la carpeta de librerías.
5. **Conexión a BD**: Verificar las credenciales de conexión en la clase de configuración de la base de datos, asegurando que el puerto (por defecto 3306), usuario y contraseña coincidan con la instalación local.
6. **Despliegue**: Ejecutar la acción "Clean and Build" en NetBeans y luego "Run" para desplegar la aplicación (por defecto en `http://localhost:8080/DMari`).

## 5. Arquitectura del Software y Base de Datos

### Diagramas Casos de Uso
*(Nota para la edición en Word: Insertar aquí los diagramas UML, como el diagrama de Casos de Uso general y el diagrama de Clases del modelo).*
- **Caso de Uso Principal**: Cliente añade producto al carrito -> Sistema valida stock -> Cliente procesa pago -> Proveedor recibe notificación -> Proveedor cambia estado a 'Enviado'.

### Estructura de Tablas
La base de datos relacional está altamente normalizada e incluye las siguientes tablas clave:
- `usuario` y `direccion`: Gestión de perfiles y ubicaciones.
- `categoria` y `subcategoria`: Jerarquía del catálogo gestionada por el admin.
- `producto`, `imagen_producto`, `tag` y `producto_tag`: Catálogo de artículos de los proveedores.
- `carrito` e `item_carrito`: Gestión de la intención de compra.
- `pedido` y `detalle_pedido`: Consolidación de compras. (El pedido se vincula al carrito, y este al cliente por normalización).
- `solicitud`: Gestión de peticiones de proveedores hacia administradores (ej. solicitar nueva categoría).
- `notificacion`: Mensajería interna del sistema.

### Scripts de Creación y Migración
Los scripts SQL se encuentran centralizados en el directorio `src/main/webapp/insertSql/`.
- `tablaMysql.sql`: Contiene los `CREATE TABLE`, `ALTER TABLE` para llaves foráneas y enumeradores (`ENUM`).
- `borrarRegistros.sql`: Útil para desarrollo, limpia los datos sin eliminar las estructuras.
- `dropTablas.sql`: Script de rollback estructural para reiniciar la BD completa.

## 6. Diseño del Software

### Descripción de los Módulos o interfaz del aplicativo
- **Módulo Auth (`AuthController.java`, `/views/Auth/`)**: Maneja el login y registro. Se comunica con `usuarioDAO`.
- **Módulo Administrador (`/views/Administrador/`)**: Interfaz para visualizar estadísticas, aprobar solicitudes de categorías y gestionar el estado de los usuarios.
- **Módulo Proveedor (`/views/Proveedor/`)**: Dashboard propio para gestión de CRUD de productos y visualización de pedidos entrantes.
- **Módulo Cliente (`/views/Cliente/` y `/views/Tienda/`)**: Vista de vitrina de productos, filtros de búsqueda, carrito de compras interactivo y pasarela de checkout.

### Descripción de los botones componentes utilizados
- **Botones de Acción Global (Verdes/Azules)**: Utilizados para acciones positivas ("Guardar", "Procesar Compra", "Aprobar", "Registrarse").
- **Botones de Acción Destructiva (Rojos)**: Utilizados para "Eliminar", "Cancelar Pedido", "Bloquear Usuario". Incorporan validación previa (alertas/modales de confirmación).
- **Menú Lateral (Sidebar)**: Componente dinámico (`router.js` y `*SideBarController.js`) que renderiza los enlaces disponibles según el rol activo en sesión.

### Interacción entre Componentes
El frontend utiliza un enfoque de Aplicación de Página Única (SPA) nativa. Un archivo central `router.js` intercepta la navegación y carga el contenido HTML de las vistas dinámicamente dentro de un contenedor principal, ejecutando posteriormente el script controlador (`Controller.js`) correspondiente a la vista cargada. Las peticiones de datos al servidor Java se realizan de forma asíncrona mediante la API `fetch()`.

## 7. Interfaces del Sistema

### APIs
El backend expone endpoints basados en Servlets que actúan como una API consumida por el frontend en formato JSON y Multipart (para imágenes).
- `POST /DMari/auth`: Endpoints de autenticación.
- `GET/POST /DMari/productos`: CRUD del catálogo.
- `GET/POST /DMari/carrito`: Gestión interactiva de items en el carrito.
- `POST /DMari/pedidos`: Transición de carrito procesado a un pedido formal.

### Formatos de Entrada/Salida
- **Entrada**: Las solicitudes del frontend envían datos a través de `FormData` (para formularios con imágenes) o estructuras JSON en el cuerpo de la petición.
- **Salida**: El servidor responde con objetos JSON estandarizados para facilitar su lectura en JS.

### Autenticación y Autorización
Se utilizan Sesiones de Servidor (`HttpSession`). Al loguearse exitosamente, el ID del usuario y su Rol se guardan en sesión de backend. En cada endpoint de acceso restringido, un filtro o validación interna verifica que la sesión exista y que el usuario tenga los permisos adecuados para ejecutar la acción.

## 8. Mensajes de ayuda y Errores

### Mensaje de Ayuda
Los tooltips, textos inferiores y placeholders en los inputs guían al usuario. Ejemplo: En el registro, un placeholder indica el formato correcto del correo electrónico.

### Mensaje Errores Comunes
- **"El correo ya se encuentra registrado"**: Ocurre en el formulario de registro; el controlador detecta la duplicidad en la base de datos y lo informa al frontend.
- **"Stock insuficiente"**: Ocurre al intentar agregar al carrito más unidades de las actualmente disponibles en el inventario del proveedor.
- **"Error 404 / Recurso no encontrado"**: Manejado visualmente por el enrutador si se navega a una URL no configurada en el sistema.
- **"Error 500 / Error interno"**: Envuelve excepciones no controladas de Java, devolviendo un mensaje genérico al usuario mientras registra el fallo detallado en la consola del servidor Tomcat para el equipo técnico.

## 9. Conclusión(es)
El sistema DMari cumple a cabalidad con los requisitos técnicos de un modelo de negocio Marketplace moderno. Ofrece un backend sólidamente estructurado bajo los estándares de Jakarta EE y un frontend dinámico que mejora la experiencia del usuario. Su arquitectura de base de datos relacional altamente normalizada y su estricta separación de roles garantizan seguridad, integridad de datos y escalabilidad para el crecimiento futuro de la plataforma.

## 10. Mantenimiento y Actualizaciones

### Control de Versiones
El código fuente debe ser versionado obligatoriamente utilizando Git (repositorios como GitHub, GitLab o Bitbucket). Se sugiere emplear una estrategia estructurada (ej. GitFlow) separando el entorno de producción (`main`/`master`) del desarrollo activo (`develop`) y características nuevas (`feature/*`).

### Registro de Cambios
- **v1.0.0**: Lanzamiento inicial de la arquitectura base, roles de seguridad y carrito de compras.
- **v1.1.0**: Normalización avanzada de la tabla `pedido` (eliminación del campo `id_usuario_fk`, estructurando la relación a través del `carrito`).
- **v1.1.1**: Depuración de código, correcciones de cascadas SQL y eliminación de módulos obsoletos (Devoluciones) para la optimización de las consultas.

## 11. Referencia Bibliográfica
- Oracle Corporation. (2023). *Jakarta EE Documentation*. Recuperado de https://jakarta.ee/
- Mozilla Developer Network (MDN). (2023). *JavaScript y Fetch API*. Recuperado de https://developer.mozilla.org/
- MySQL. (2023). *MySQL 8.0 Reference Manual*. Recuperado de https://dev.mysql.com/doc/refman/8.0/en/

## 12. Anexos

### Glosario de Términos
- **DAO (Data Access Object)**: Patrón de diseño de software que abstrae y encapsula los accesos a la fuente de datos.
- **SPA (Single Page Application)**: Aplicación web que interactúa con el usuario reescribiendo dinámicamente la página web actual en lugar de cargar páginas enteras nuevas desde el servidor.
- **Servlet**: Clase en el lenguaje de programación Java utilizada para ampliar las capacidades de los servidores web.

### Contacto del Equipo Técnico
- **Área de Soporte IT**: soporte-it@dmari.com
- **Administrador de Base de Datos**: dba@dmari.com
- **Líder de Desarrollo**: [Colocar el nombre aquí]
