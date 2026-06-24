/*
    objetivo de este archivo:
    eliminar (drop) por completo la estructura de todas las tablas de la base de datos dmari.
    ideal para cuando necesitas reestructurar la base de datos desde cero.
*/

use dmari;

-- desactivamos temporalmente las llaves foraneas para evitar bloqueos al borrar
set foreign_key_checks = 0;

-- ==========================================================
-- 1. eliminacion de tablas de solicitudes
-- ==========================================================
drop table if exists solicitud_proveedor;
drop table if exists solicitud_categoria;

-- ==========================================================
-- 2. eliminacion de tablas transaccionales y de pago
-- ==========================================================
drop table if exists pago;
drop table if exists detalle_pedido;
drop table if exists pedido; 
drop table if exists detalle_carrito;
drop table if exists carrito;

-- ==========================================================
-- 3. eliminacion de tablas de relaciones muchos a muchos
-- ==========================================================
drop table if exists proveedor_producto;
drop table if exists producto_etiqueta;

-- ==========================================================
-- 4. eliminacion de tablas de extension e imagenes
-- ==========================================================
drop table if exists imagenes;
drop table if exists producto;
drop table if exists proveedor;
drop table if exists cliente;
drop table if exists credenciales;
drop table if exists correo;
drop table if exists telefono;
drop table if exists direccion;
drop table if exists usuario;

-- ==========================================================
-- 5. eliminacion de tablas maestras y catalogos
-- ==========================================================
drop table if exists metodo_pago;
drop table if exists etiqueta;
drop table if exists categoria;
drop table if exists rol;

-- reactivamos las llaves foraneas para mantener la integridad del motor
set foreign_key_checks = 1;