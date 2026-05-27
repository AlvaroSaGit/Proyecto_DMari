USE DMari;

-- 1. Apagamos la seguridad de llaves foraneas temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Vaciamos las tablas transaccionales (Hijas)
TRUNCATE TABLE detalle_pedido;
TRUNCATE TABLE pago;
TRUNCATE TABLE pedido;
TRUNCATE TABLE detalle_carrito;
TRUNCATE TABLE carrito;

-- 3. Vaciamos las tablas intermedias y satelites
TRUNCATE TABLE proveedor_producto;
TRUNCATE TABLE producto_etiqueta;
TRUNCATE TABLE imagenes;
TRUNCATE TABLE cliente;
TRUNCATE TABLE proveedor;
TRUNCATE TABLE credenciales;
TRUNCATE TABLE correo;
TRUNCATE TABLE telefono;
TRUNCATE TABLE direccion;

-- 4. Vaciamos las tablas principales
TRUNCATE TABLE producto;
TRUNCATE TABLE usuario;

-- 5. Vaciamos las tablas maestras (Padres)
TRUNCATE TABLE metodo_pago;
TRUNCATE TABLE etiqueta;
TRUNCATE TABLE categoria;
TRUNCATE TABLE rol;

-- 6. Volvemos a prender la seguridad de llaves foraneas (¡MUY IMPORTANTE!)
SET FOREIGN_KEY_CHECKS = 1;