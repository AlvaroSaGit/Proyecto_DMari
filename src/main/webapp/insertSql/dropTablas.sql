/*
    objetivo de este archivo:
    Eliminar (Drop) por completo la estructura de todas las tablas de la base de datos DMari.
    Ideal para cuando necesitas reestructurar la base de datos desde cero.
*/

USE DMari;

-- desactivamos temporalmente las llaves foraneas para evitar bloqueos al borrar
SET FOREIGN_KEY_CHECKS = 0;

-- eliminamos todas las tablas si existen
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS proveedor_producto;
DROP TABLE IF EXISTS producto_etiqueta;
DROP TABLE IF EXISTS imagenes;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS proveedor;
DROP TABLE IF EXISTS datos_proveedor;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS credenciales;
DROP TABLE IF EXISTS correo;
DROP TABLE IF EXISTS telefono;
DROP TABLE IF EXISTS direccion;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS rol;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS etiqueta;
DROP TABLE IF EXISTS metodo_pago;
DROP TABLE IF EXISTS carrito;
DROP TABLE IF EXISTS detalle_carrito;

-- reactivamos las llaves foraneas
SET FOREIGN_KEY_CHECKS = 1;