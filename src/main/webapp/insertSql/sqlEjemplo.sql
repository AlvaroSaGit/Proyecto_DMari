use dmari;

-- ==========================================================
-- 1. poblacion de roles
-- ==========================================================
insert into rol (id_rol_pk, tipo_rol) values 
(1, 'administrador'),
(2, 'cliente'),
(4, 'proveedor');

-- ==========================================================
-- 2. poblacion de categorias
-- ==========================================================
insert into categoria (nombre, descripcion, estado_activo) values 
('reposteria', 'postres, donas y dulces artesanales', true),   -- id 1
('decoracion', 'velas y articulos decorativos para el hogar', true), -- id 2
('floristeria', 'arreglos florales hermosos para toda ocasion', true); -- id 3

-- ==========================================================
-- 3. registro de usuarios base (15 registros para sena)
-- ==========================================================
insert into usuario (nombre, apellido, id_rol_fk, estado_cuenta) values 
('alvaro', 'jefe', 1, true),       -- id 1: administrador
('maria', 'gomez', 2, true),       -- id 2: cliente vip
('carlos', 'suministros', 4, true), -- id 3: proveedor ceras
('ana', 'repostera', 4, true),     -- id 4: proveedor postres
('luis', 'flores', 4, true),       -- id 5: proveedor vivero
('pedro', 'moto', 2, true),        -- id 6: cliente
('laura', 'perez', 2, true),       -- id 7: cliente
('jorge', 'diaz', 2, true),        -- id 8: cliente
('diana', 'rojas', 2, true),       -- id 9: cliente
('camilo', 'mendez', 2, true),     -- id 10: cliente
('sofia', 'vargas', 2, true),      -- id 11: cliente
('andres', 'castillo', 2, true),   -- id 12: cliente
('valentina', 'ortiz', 2, true),   -- id 13: cliente
('diego', 'ramirez', 2, true),     -- id 14: cliente
('miguel', 'reparte', 2, true);    -- id 15: cliente

-- ==========================================================
-- 4. correos electronicos
-- ==========================================================
insert into correo (id_usuario_fk, correo, correo_primario) values 
(1, 'alvaro@dmari.com', true),
(2, 'maria@gmail.com', true),
(3, 'carlos@ceras.com', true),
(4, 'ana@postres.com', true),
(5, 'luis@vivero.com', true),
(6, 'pedro.moto@envios.com', true),
(7, 'laura.p@hotmail.com', true),
(8, 'jorge.d@yahoo.com', true),
(9, 'diana.r@gmail.com', true),
(10, 'camilo.m@empresa.co', true),
(11, 'sofia.v@gmail.com', true),
(12, 'andres.c@hotmail.com', true),
(13, 'valentina.o@yahoo.com', true),
(14, 'diego.r@gmail.com', true),
(15, 'miguel.entregas@envios.com', true);

-- ==========================================================
-- 5. credenciales con encriptacion aes
-- ==========================================================
insert into credenciales (id_usuario, passwd_encript) values 
(1, aes_encrypt('Admin12345', 'llave_dmari')),
(2, aes_encrypt('Cliente123', 'llave_dmari')),
(3, aes_encrypt('Proveedor123', 'llave_dmari')),
(4, aes_encrypt('Proveedor123', 'llave_dmari')),
(5, aes_encrypt('Proveedor123', 'llave_dmari')),
(6, aes_encrypt('Reparto123', 'llave_dmari')),
(7, aes_encrypt('Cliente123', 'llave_dmari')),
(8, aes_encrypt('Cliente123', 'llave_dmari')),
(9, aes_encrypt('Cliente123', 'llave_dmari')),
(10, aes_encrypt('Cliente123', 'llave_dmari')),
(11, aes_encrypt('Cliente123', 'llave_dmari')),
(12, aes_encrypt('Cliente123', 'llave_dmari')),
(13, aes_encrypt('Cliente123', 'llave_dmari')),
(14, aes_encrypt('Cliente123', 'llave_dmari')),
(15, aes_encrypt('Reparto123', 'llave_dmari'));

-- ==========================================================
-- 6. perfiles de clientes
-- ==========================================================
insert into cliente (id_cliente_pk, referencia_ubicacion) values 
(2, 'casa blanca esquinera'),
(7, 'edificio torres del sol'),
(8, 'frente al parque'),
(9, 'conjunto cerrado'),
(10, 'casa rejas negras'),
(6, 'cerca a la estacion de transporte'),
(11, 'apartamento residencial piso 2'),
(12, 'al lado de la tienda de la esquina'),
(13, 'frente al colegio principal'),
(14, 'barrio nuevo sector b'),
(15, 'conjunto residencial bloques del norte');

-- ==========================================================
-- 7. perfiles de proveedores
-- ==========================================================
insert into proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) values
(3, '900123456-1', 'ceras giron', '123456789', 'bancolombia', 'ahorros'),
(4, '900987654-2', 'delicias de ana', '987654321', 'davivienda', 'corriente'),
(5, '900555666-3', 'vivero san luis', '555666777', 'nequi', 'ahorros');

-- ==========================================================
-- 8. catalogo de productos
-- ==========================================================
insert into producto (id_categoria_fk, nombre_producto, descripcion, precio, stock, estado) values 
(2, 'vela de vainilla y canela', 'aroma dulce ideal para relajacion', 18000, 15, true),     
(1, 'dona glaseada especial', 'clasica con glaseado de azucar', 4500, 30, true),           
(2, 'vela decorativa de flores', 'vela artesanal con petalos secos', 22000, 10, true),    
(3, 'ramo de rosas rojas', 'hermoso arreglo floral para regalar a mama', 65000, 5, true), 
(1, 'dona rellena de arequipe', 'masa suave con relleno tradicional', 5500, 25, true),    
(1, 'caja de mini donas', 'set de 6 mini donas surtidas para regalo', 15000, 10, true),   
(1, 'desayuno feliz', 'bandeja con jugo, sanduche, fruta y globo', 85000, 8, true),       
(2, 'ancheta cumpleanos', 'dulces surtidos y cervezas', 110000, 4, true),                 
(1, 'caja de trufas', '12 trufas de chocolate belga', 35000, 20, true),                   
(2, 'globo helio te amo', 'globo metalizado gigante', 12000, 50, true),                   
(2, 'peluche oso gigante', 'oso de felpa de 1 metro de alto', 150000, 3, true),           
(2, 'vino tinto reserva', 'botella de vino tinto importado', 75000, 12, true),            
(2, 'tarjeta 3d cumpleanos', 'tarjeta artesanal con relieve', 8000, 100, true),           
(1, 'pastel de chocolate', 'pastel humedo para 10 personas', 55000, 6, true),             
(1, 'cupcakes decorados', 'caja de 4 cupcakes personalizados', 20000, 15, true);          

-- ==========================================================
-- 9. galeria de imagenes
-- ==========================================================
insert into imagenes (id_producto_fk, url_ruta, imagen_principal) values 
(1, 'src/img/productos/default/gato_programador.jpg', 1),
(2, 'src/img/productos/default/gato_programador.jpg', 1),
(3, 'src/img/productos/default/gato_programador.jpg', 1),
(4, 'src/img/productos/default/gato_programador.jpg', 1),
(5, 'src/img/productos/default/gato_programador.jpg', 1),
(6, 'src/img/productos/default/gato_programador.jpg', 1),
(7, 'src/img/productos/default/gato_programador.jpg', 1),
(8, 'src/img/productos/default/gato_programador.jpg', 1),
(9, 'src/img/productos/default/gato_programador.jpg', 1),
(10, 'src/img/productos/default/gato_programador.jpg', 1),
(11, 'src/img/productos/default/gato_programador.jpg', 1),
(12, 'src/img/productos/default/gato_programador.jpg', 1),
(13, 'src/img/productos/default/gato_programador.jpg', 1),
(14, 'src/img/productos/default/gato_programador.jpg', 1),
(15, 'src/img/productos/default/gato_programador.jpg', 1);

-- ==========================================================
-- 10. etiquetas de busqueda
-- ==========================================================
insert into etiqueta (nombre_etiqueta) values 
('aromaterapia'), ('relajacion'), ('dulce'), ('decoracion'), ('arequipe'), 
('regalo'), ('dia de la madre'), ('san valentin'), ('cumpleanos'), ('aniversario'), 
('chocolate'), ('premium'), ('infantil'), ('para ella'), ('para el');

-- ==========================================================
-- 11. vinculacion producto-etiqueta
-- ==========================================================
insert into producto_etiqueta (id_producto, id_etiqueta) values 
(1, 1), (1, 2), (1, 14),
(2, 3), (2, 13),        
(3, 4), (3, 2), (3, 7),
(4, 6), (4, 7), (4, 8), (4, 10), (4, 14),
(5, 3), (5, 5),
(6, 6), (6, 3), (6, 13),
(7, 6), (7, 9), (7, 10),
(8, 6), (8, 9), (8, 15),
(9, 3), (9, 6), (9, 8), (9, 11),
(10, 8), (10, 10),
(11, 6), (11, 8), (11, 14),
(12, 6), (12, 10), (12, 12), (12, 15),
(13, 9),
(14, 3), (14, 9), (14, 11),
(15, 3), (15, 9), (15, 13);

-- ==========================================================
-- 12. vinculacion proveedor-producto
-- ==========================================================
insert into proveedor_producto (id_proveedor_fk, id_producto_fk) values 
(3, 1), (3, 3), (4, 2), (4, 5), (4, 6), (4, 14), (4, 15), (5, 4);

-- ==========================================================
-- 13. metodos de pago habilitados
-- ==========================================================
insert into metodo_pago (descripcion_pago, estado_activo) values 
('nequi', true),
('daviplata', true),
('tarjeta de credito / debito', true),
('efectivo (contra entrega)', true),
('pse', true);

-- ==========================================================
-- 14. datos logisticos (direcciones y telefonos)
-- ==========================================================
insert into telefono (id_usuario_fk, numero_telefonico) values 
(2, '3101234567'), (2, '3100000000'), 
(7, '3119876543'), (7, '3111111111'), 
(8, '3124567890'), (8, '3122222222'), 
(10, '3144444444');                   

insert into direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) values 
(2, 'calle 10 # 5-20, giron', 'casa blanca esquinera, timbre 2', true),            
(7, 'carrera 15 # 22-10, bucaramanga', 'apto 402', true),                           
(8, 'calle 45 # 9-50, floridablanca', 'casa 3', true),                              
(9, 'avenida 33 # 10-12, piedecuesta', 'conjunto cerrado', true),                   
(10, 'calle 50 # 14-20, bucaramanga', 'casa rejas negras', true);                   

-- ==========================================================
-- 15. simulacion del flujo operativo de ventas
-- ==========================================================

-- Variables para almacenar los IDs generados automáticamente
SET @last_carrito_id = 0;
SET @last_pedido_id = 0;

-- Transacción 1: Compra de Maria (ID Cliente: 2)
-- El carrito debe estar en estado Procesado para generar un pedido
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(@last_carrito_id, 1, 2, TRUE), 
(@last_carrito_id, 2, 1, TRUE);

-- Cabecera del pedido (ahora vincula correctamente el id_carrito_fk)
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES 
(2, @last_carrito_id, 1, 40500.00, 'Pendiente');
SET @last_pedido_id = LAST_INSERT_ID();

-- Detalle inmutable del pedido
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES 
(@last_pedido_id, 1, 2, 18000.00, 36000.00), 
(@last_pedido_id, 2, 1, 4500.00, 4500.00);

-- Registro de pago
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES 
(@last_pedido_id, 1, 'celular nequi: 3101234567', 2025.00, 38475.00, 'Aprobado');

-- ==========================================================
-- 16. DATA VARIADA PARA ESTADÍSTICAS (Enero - Mayo 2024)
-- ==========================================================

-- Venta de hace 5 meses: Maria compra un Oso Gigante (DMari Oficial)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 11, 1, TRUE);
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (2, @last_carrito_id, 1, 150000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 11, 1, 150000.00, 150000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 1, 7500.00, 142500.00, 'Aprobado');

-- Venta de hace 4 meses: Laura compra Donas de Ana (Proveedor ID 4)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (7, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 2, 10, TRUE);
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (7, @last_carrito_id, 2, 45000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 2, 10, 4500.00, 45000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 2, 2250.00, 42750.00, 'Aprobado');

-- Venta de hace 3 meses: Jorge compra Velas de Carlos (Proveedor ID 3)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (8, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 1, 5, TRUE);
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (8, @last_carrito_id, 3, 90000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 1, 5, 18000.00, 90000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 3, 4500.00, 85500.00, 'Aprobado');

-- Venta de hace 2 meses: Diana compra Flores de Luis (Proveedor ID 5)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (9, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 4, 2, TRUE);
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (9, @last_carrito_id, 4, 130000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 4, 2, 65000.00, 130000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 5, 6500.00, 123500.00, 'Aprobado');

-- Venta de hace 1 mes: Maria compra Vino (DMari) y Donas (Ana - Prov 4)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 12, 1, TRUE), (@last_carrito_id, 5, 4, TRUE);
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (2, @last_carrito_id, 1, 97000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES 
(@last_pedido_id, 12, 1, 75000.00, 75000.00), -- DMari
(@last_pedido_id, 5, 4, 5500.00, 22000.00);   -- Ana
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 3, 4850.00, 92150.00, 'Aprobado');

-- Vinculamos productos que estaban "sueltos" a proveedores para que ellos vean data
INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES 
(3, 3),  -- Vela decorativa -> Carlos
(4, 9),  -- Trufas -> Ana
(5, 10); -- Globos -> Luis

-- ==========================================================
-- 17. SOLICITUDES DE NUEVOS PROVEEDORES
-- ==========================================================
-- Miguel (ID 15) quiere vender artesanias de cuero
insert into solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) 
values (15, '800555444-9', 'Cueros Miguel', '444555666', 'Banco de Bogota', 'Corriente', 'pendiente');

-- Andres (ID 12) mando solicitud pero fue rechazada por falta de NIT real
insert into solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) 
values (12, '000000000-0', 'Andres Manualidades', '111222333', 'Nequi', 'Ahorros', 'rechazada');

-- Transacción 2: Compra de Laura (ID Cliente: 7)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (7, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 11, 1, TRUE);
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES (7, @last_carrito_id, 2, 150000.00, 'Entregado');
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 11, 1, 150000.00, 150000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 3, 'voucher tarjeta: 444455556666', 7500.00, 142500.00, 'Aprobado');

-- Transacción 3: Compra de Jorge (ID Cliente: 8)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (8, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 7, 1, TRUE);
INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES (8, @last_carrito_id, 3, 85000.00, 'En Camino');
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 7, 1, 85000.00, 85000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 2, 'celular daviplata: 3124567890', 4250.00, 80750.00, 'Aprobado');

-- ==========================================================
-- 18. GESTIÓN DE DEVOLUCIONES (Sobre pedidos existentes)
-- ==========================================================
-- Ahora que los pedidos han sido creados arriba, podemos insertar las devoluciones
-- Laura (ID 7) solicita devolucion del Oso Gigante (Pedido de Transaccion 2)
insert into devolucion (id_pedido_fk, id_cliente_fk, motivo, estado_devolucion) 
values (@last_pedido_id - 1, 7, 'El peluche oso gigante tiene una costura suelta en la espalda.', 'solicitada');

-- Maria (ID 2) solicita devolucion de las Donas (Pedido de la seccion de estadisticas)
insert into devolucion (id_pedido_fk, id_cliente_fk, motivo, estado_devolucion) 
values (8, 2, 'Las donas llegaron con el glaseado pegado a la caja y aplastadas.', 'solicitada');

-- Transacción 4: Carrito abandonado por Maria
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Activo');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(@last_carrito_id, 5, 3, TRUE);