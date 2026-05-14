-- Seleccionar la base de datos
USE dmari;

-- INSERTAR PRODUCTOS
-- El ID se genera solo si lo tienes como auto_increment
INSERT INTO producto (nombre_producto, precio, stock) VALUES 
('Vela de Vainilla y Canela', 18000, 15),
('Dona Glaseada Especial', 4500, 30),
('Vela Decorativa de Flores', 22000, 10),
('Dona Rellena de Arequipe', 5500, 25),
('Caja de Mini Velas Regalo', 35000, 5);

-- INSERTAR IMAGENES
-- Asumimos que los IDs de producto creados arriba son del 1 al 5
INSERT INTO imagenes (id_producto_fk, url_ruta, imagen_principal) VALUES 
(2, 'src/img/productos/default/gato_programador.jpg', 1),
(3, 'src/img/productos/default/gato_programador.jpg', 1),
(4, 'src/img/productos/default/gato_programador.jpg', 1),
(1, 'src/img/productos/default/gato_programador.jpg', 1),
(5, 'src/img/productos/default/gato_programador.jpg', 1);

-- Borrar insert

-- Primero borramos todas las imagenes
DELETE FROM imagenes WHERE id_imagen_pk > 0;

-- Luego borramos todos los productos
DELETE FROM producto WHERE id_producto_pk > 0;