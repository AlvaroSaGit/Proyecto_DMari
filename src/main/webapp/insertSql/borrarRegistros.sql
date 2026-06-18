/*
    objetivo de este archivo:
    vaciar (truncate) los registros de todas las tablas de dmari.
    esto borra los datos por completo y reinicia los contadores auto_increment a 1.
*/

use dmari;

-- 1. apagamos la seguridad de llaves foraneas temporalmente para evitar bloqueos
set foreign_key_checks = 0;

-- ==========================================================
-- 2. vaciamos las tablas de solicitudes y devoluciones
-- ==========================================================
truncate table devolucion;
truncate table solicitud_proveedor;
truncate table solicitud_categoria;

-- ==========================================================
-- 3. vaciamos las tablas transaccionales del flujo de compras
-- ==========================================================
truncate table pago;
truncate table detalle_pedido;
truncate table pedido; 
truncate table detalle_carrito;
truncate table carrito;

-- ==========================================================
-- 4. vaciamos las tablas intermedias y relaciones muchos a muchos
-- ==========================================================
truncate table proveedor_producto;
truncate table producto_etiqueta;

-- ==========================================================
-- 5. vaciamos las tablas satelites y de extension
-- ==========================================================
truncate table imagenes;
truncate table cliente;
truncate table proveedor;
truncate table credenciales;
truncate table correo;
truncate table telefono;
truncate table direccion;

-- ==========================================================
-- 6. vaciamos las tablas principales
-- ==========================================================
truncate table producto;
truncate table usuario;

-- ==========================================================
-- 7. vaciamos las tablas maestras y catalogos de base
-- ==========================================================
truncate table metodo_pago;
truncate table etiqueta;
truncate table categoria;
truncate table rol;

-- 8. volvemos a encender la seguridad de llaves foraneas
set foreign_key_checks = 1;