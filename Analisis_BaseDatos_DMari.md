# Análisis de Arquitectura y Flujos de Base de Datos - DMari

Este documento es una guía de estudio formal diseñada para comprender la estructura, las relaciones y el ciclo de vida de los datos dentro de la aplicación DMari.

**Propósito de este documento:** Entender no solo *qué* tablas existen, sino *cómo* interactúan entre sí en tiempo real cuando un usuario navega, compra o se registra en el sistema.

---

## 1. Orden de Ejecución del Sistema

Para levantar el entorno desde cero, los archivos SQL deben ejecutarse en un orden estricto de dependencias. MySQL no te dejará insertar datos en una tabla si la tabla padre no existe primero.

1. **`tablaMysql.sql` (El Esqueleto):** 
   - **Qué hace:** Crea la base de datos `dmari` y define todas las tablas, columnas, tipos de datos y llaves foráneas. No inserta datos reales (solo la estructura).
   - **Cuándo ejecutarlo:** Una sola vez al configurar el servidor por primera vez, o si necesitas destruir la base de datos y reconstruirla desde cero.

2. **`sqlEjemplo.sql` (La Sangre):**
   - **Qué hace:** Inyecta datos realistas (usuarios, productos, carritos, pagos). Sigue su propio orden interno (primero roles, luego usuarios, luego productos, y finalmente transacciones).
   - **Cuándo ejecutarlo:** Después de `tablaMysql.sql`. Es ideal para rellenar tu entorno de desarrollo y hacer pruebas en el frontend sin ver pantallas vacías.

---

## 2. Flujo Operativo de Ventas (Del Carrito al Pago)

Este es el corazón financiero de DMari. ¿Cómo se transforma un "clic" en la tienda a dinero en el sistema?

Flujo lógico:
`carrito` -> `detalle_carrito` -> `pedido` -> `detalle_pedido` -> `pago`

### Paso A Paso del Flujo:

1. **`carrito` y `detalle_carrito` (La Etapa Activa)**
   - Cuando el cliente entra y añade algo, se crea un registro en `carrito` con `estado = 'Activo'`.
   - Cada producto diferente que añade va a `detalle_carrito`. Si agrega 3 donas, es una sola fila con `cantidad = 3`.
   - **Transición:** Si el cliente cierra la pestaña, el carrito se queda 'Activo' (Carrito abandonado). Si le da a "Comprar", el código Java cambia el estado a `'Procesado'`.

2. **`pedido` (La Cabecera Formal)**
   - Una vez el carrito es `'Procesado'`, nace un `pedido`.
   - El pedido **no guarda qué compró el cliente**, sino metadatos logísticos: a qué `direccion` va, quién canceló (si se cancela), y el `total_pagar` final.
   - Está amarrado directamente al `id_carrito` original.

3. **`detalle_pedido` (La Fotografía Inmutable)**
   - **¿Por qué existe si ya tenemos `detalle_carrito`?** 
   - Si mañana el proveedor le sube el precio a la dona de $4,500 a $6,000, los recibos viejos no pueden cambiar mágicamente. `detalle_pedido` toma una "fotografía" del `precio_unitario` exacto en el segundo en que se hizo la compra. Es intocable.

4. **`pago` (El Cierre Financiero)**
   - Se amarra al pedido. Registra con qué método pagó (Nequi, Efectivo), el número de comprobante, y calcula la `comision_dmari` (la ganancia de la plataforma frente al proveedor).

---

## 3. Arquitectura Central de Usuarios (El Núcleo)

DMari usa un diseño modular para los usuarios. En lugar de tener una tabla gigante con 50 columnas, se divide en piezas de lego.

- **`usuario`:** Contiene solo lo básico (Nombre, Apellido, Rol, Estado de la cuenta).
- **Extensiones (Hojas):** `correo`, `telefono`, `direccion`, `credenciales`.
  - **Ventaja:** Un usuario puede tener 3 correos, 2 teléfonos y 5 direcciones sin romper la tabla principal. Siempre hay un campo booleano (`correo_primario`, `direccion_primario`) para saber cuál usar por defecto en un envío.

---

## 4. El Ciclo de Vida de los Proveedores

El ecosistema de proveedores es especial porque requiere "Aprobación Humana" por parte del Administrador.

Flujo Lógico:
Usuario crea cuenta -> Llena solicitud -> Admin aprueba -> Se crea Proveedor.

### Tablas Clave del Proveedor:
1. **`proveedor`:** Solo existen aquí si ya son empresas aprobadas. Aquí viven sus datos bancarios para depositarles las ventas.
2. **`solicitud_proveedor`:** Es el "buzón de entrada" del Administrador. Aquí caen las intenciones de ser proveedor.
3. **`solicitud_categoria`:** Funciona igual que la anterior, pero para sugerir nuevas categorías. Si a un proveedor se le ocurre vender "Mascotas" y la categoría no existe, manda una justificación. El admin la lee, si la aprueba, la inserta en la tabla maestra `categoria`.
4. **`proveedor_producto` (Muchos a Muchos):** Conecta el catálogo de productos con la billetera del proveedor. Garantiza que sepamos a qué banco depositar cuando se venda un artículo específico.
