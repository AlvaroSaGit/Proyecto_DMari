/*
    objetivo de este archivo:
    limpiar por completo la base de datos de dmari de forma segura.
    utiliza truncate para borrar todos los registros y reiniciar los 
    contadores de id autoincrementables a 1.
*/

USE DMari;

-- desactivamos temporalmente las llaves foraneas para evitar bloqueos de seguridad al vaciar las tablas
SET FOREIGN_KEY_CHECKS = 0;

-- 1. vaciamos las tablas transaccionales (ventas y carritos)
TRUNCATE TABLE pago;
TRUNCATE TABLE detalle_pedido;
TRUNCATE TABLE pedido;

-- 2. vaciamos las tablas puente y satelites de los productos
TRUNCATE TABLE proveedor_producto;
TRUNCATE TABLE producto_etiqueta;
TRUNCATE TABLE imagenes;

-- 3. vaciamos la tabla nucleo de productos
TRUNCATE TABLE producto;

-- 4. vaciamos las tablas satelites de los usuarios
TRUNCATE TABLE proveedor;
TRUNCATE TABLE datos_proveedor;
TRUNCATE TABLE cliente;
TRUNCATE TABLE repartidor;
TRUNCATE TABLE dato_repartidor;
TRUNCATE TABLE credenciales;
TRUNCATE TABLE notificacion;
TRUNCATE TABLE correo;
TRUNCATE TABLE telefono;
TRUNCATE TABLE direccion;

-- 5. vaciamos la tabla nucleo de usuarios
TRUNCATE TABLE usuario;

-- 6. vaciamos las tablas de seguridad y maestras independientes
TRUNCATE TABLE rol_permiso;
TRUNCATE TABLE rol;
TRUNCATE TABLE permiso;
TRUNCATE TABLE categoria;
TRUNCATE TABLE etiqueta;
TRUNCATE TABLE metodo_pago;

-- reactivamos las llaves foraneas para que la base de datos vuelva a estar protegida
SET FOREIGN_KEY_CHECKS = 1;