USE DMari;

-- ==========================================================
-- 1. poblacion de roles
-- ==========================================================
INSERT INTO rol (id_rol_pk, tipo_rol) VALUES 
(1, 'administrador'),
(2, 'cliente'),
(4, 'proveedor');

-- ==========================================================
-- 2. poblacion de categorias
-- ==========================================================
INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES 
('Reposteria', 'Postres, donas y dulces artesanales', true),   -- id 1
('Decoracion', 'Velas y articulos decorativos para el hogar', true), -- id 2
('Floristeria', 'Arreglos florales hermosos para toda ocasion', true); -- id 3

-- ==========================================================
-- 3. registro de usuarios base (15 registros para sena)
-- ==========================================================
INSERT INTO usuario (nombre, apellido, id_rol_fk, estado_cuenta) VALUES 
('Alvaro', 'Jefe', 1, true),       -- id 1: administrador
('Maria', 'Gomez', 2, true),       -- id 2: cliente vip
('Carlos', 'Suministros', 4, true), -- id 3: proveedor ceras
('Ana', 'Repostera', 4, true),     -- id 4: proveedor postres
('Luis', 'Flores', 4, true),       -- id 5: proveedor vivero
('Pedro', 'Moto', 2, true),        -- id 6: cliente
('Laura', 'Perez', 2, true),       -- id 7: cliente
('Jorge', 'Diaz', 2, true),        -- id 8: cliente
('Diana', 'Rojas', 2, true),       -- id 9: cliente
('Camilo', 'Mendez', 2, true),     -- id 10: cliente
('Sofia', 'Vargas', 2, true),      -- id 11: cliente
('Andres', 'Castillo', 2, true),   -- id 12: cliente
('Valentina', 'Ortiz', 2, true),   -- id 13: cliente
('Diego', 'Ramirez', 2, true),     -- id 14: cliente
('Miguel', 'Reparte', 2, true);    -- id 15: cliente

-- ==========================================================
-- 4. registro de correos satelite
-- ==========================================================
INSERT INTO correo (id_usuario_fk, correo, correo_primario) VALUES 
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
-- 5. encriptacion de contrasenas
-- los valores se insertan usando aes_encrypt, lo cual requiere que la columna sea de tipo varbinary
-- ==========================================================
INSERT INTO credenciales (id_usuario, passwd_encript) VALUES 
(1, AES_ENCRYPT('admin123', 'llave_dmari')),
(2, AES_ENCRYPT('cliente123', 'llave_dmari')),
(3, AES_ENCRYPT('proveedor123', 'llave_dmari')),
(4, AES_ENCRYPT('proveedor123', 'llave_dmari')),
(5, AES_ENCRYPT('proveedor123', 'llave_dmari')),
(6, AES_ENCRYPT('reparto123', 'llave_dmari')),
(7, AES_ENCRYPT('cliente123', 'llave_dmari')),
(8, AES_ENCRYPT('cliente123', 'llave_dmari')),
(9, AES_ENCRYPT('cliente123', 'llave_dmari')),
(10, AES_ENCRYPT('cliente123', 'llave_dmari')),
(11, AES_ENCRYPT('cliente123', 'llave_dmari')),
(12, AES_ENCRYPT('cliente123', 'llave_dmari')),
(13, AES_ENCRYPT('cliente123', 'llave_dmari')),
(14, AES_ENCRYPT('cliente123', 'llave_dmari')),
(15, AES_ENCRYPT('reparto123', 'llave_dmari'));

-- ==========================================================
-- 6. completar perfiles de clientes (solo datos de extension)
-- ==========================================================
INSERT INTO cliente (id_cliente_pk, referencia_ubicacion) VALUES 
(2, 'Casa blanca esquinera'),
(7, 'Edificio Torres del Sol'),
(8, 'Frente al parque'),
(9, 'Conjunto cerrado'),
(10, 'Casa rejas negras'),
(6, 'Cerca a la estacion de transporte'),
(11, 'Apartamento residencial piso 2'),
(12, 'Al lado de la tienda de la esquina'),
(13, 'Frente al colegio principal'),
(14, 'Barrio nuevo sector b'),
(15, 'Conjunto residencial bloques del norte');

-- ==========================================================
-- 7. completar perfiles de proveedores
-- ==========================================================
INSERT INTO proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES
(3, '900123456-1', 'Ceras Giron', '123456789', 'Bancolombia', 'Ahorros'),
(4, '900987654-2', 'Delicias de Ana', '987654321', 'Davivienda', 'Corriente'),
(5, '900555666-3', 'Vivero San Luis', '555666777', 'Nequi', 'Ahorros');

-- ==========================================================
-- 8. registro de productos (15 registros para sena)
-- ==========================================================
INSERT INTO producto (id_categoria_fk, nombre_producto, descripcion, precio, stock, estado) VALUES 
(2, 'Vela de Vainilla y Canela', 'Aroma dulce ideal para relajacion', 18000, 15, true),     -- id 1
(1, 'Dona Glaseada Especial', 'Clasica con glaseado de azucar', 4500, 30, true),           -- id 2
(2, 'Vela Decorativa de Flores', 'Vela artesanal con petalos secos', 22000, 10, true),    -- id 3
(3, 'Ramo de Rosas Rojas', 'Hermoso arreglo floral para regalar a mama', 65000, 5, true), -- id 4
(1, 'Dona Rellena de Arequipe', 'Masa suave con relleno tradicional', 5500, 25, true),    -- id 5
(1, 'Caja de Mini Donas', 'Set de 6 mini donas surtidas para regalo', 15000, 10, true),   -- id 6
(1, 'Desayuno Feliz', 'Bandeja con jugo, sanduche, fruta y globo', 85000, 8, true),       -- id 7
(2, 'Ancheta Cumpleanos', 'Dulces surtidos y cervezas', 110000, 4, true),                 -- id 8
(1, 'Caja de Trufas', '12 trufas de chocolate belga', 35000, 20, true),                   -- id 9
(2, 'Globo Helio Te Amo', 'Globo metalizado gigante', 12000, 50, true),                   -- id 10
(2, 'Peluche Oso Gigante', 'Oso de felpa de 1 metro de alto', 150000, 3, true),           -- id 11
(2, 'Vino Tinto Reserva', 'Botella de vino tinto importado', 75000, 12, true),            -- id 12
(2, 'Tarjeta 3D Cumpleanos', 'Tarjeta artesanal con relieve', 8000, 100, true),           -- id 13
(1, 'Pastel de Chocolate', 'Pastel humedo para 10 personas', 55000, 6, true),             -- id 14
(1, 'Cupcakes Decorados', 'Caja de 4 cupcakes personalizados', 20000, 15, true);          -- id 15

-- ==========================================================
-- 9. registro de imagenes relacionadas
-- ==========================================================
INSERT INTO imagenes (id_producto_fk, url_ruta, imagen_principal) VALUES 
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
-- 10. poblacion de etiquetas (15 registros)
-- ==========================================================
INSERT INTO etiqueta (nombre_etiqueta) VALUES 
('Aromaterapia'), ('Relajacion'), ('Dulce'), ('Decoracion'), ('Arequipe'), 
('Regalo'), ('Dia de la Madre'), ('San Valentin'), ('Cumpleanos'), ('Aniversario'), 
('Chocolate'), ('Premium'), ('Infantil'), ('Para Ella'), ('Para El');

-- ==========================================================
-- 11. asignacion de etiquetas a productos
-- ==========================================================
INSERT INTO producto_etiqueta (id_producto, id_etiqueta) VALUES 
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
-- 12. relacion proveedor - producto
-- ==========================================================
INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES 
(3, 1), (3, 3), (4, 2), (4, 5), (4, 6), (4, 14), (4, 15), (5, 4);

-- ==========================================================
-- 13. poblacion de metodos de pago
-- ==========================================================
INSERT INTO metodo_pago (descripcion_pago, estado_activo) VALUES 
('Nequi', true),
('Daviplata', true),
('Tarjeta de Credito / Debito', true),
('Efectivo (Contra Entrega)', true),
('PSE', true);

-- ==========================================================
-- 14. direcciones y telefonos de clientes (unificados de secc. 6 y 15)
-- ==========================================================
INSERT INTO telefono (id_usuario_fk, numero_telefonico) VALUES 
(2, '3101234567'), (2, '3100000000'), -- telefono base y secundario de maria
(7, '3119876543'), (7, '3111111111'), -- laura
(8, '3124567890'), (8, '3122222222'), -- jorge
(10, '3144444444');                   -- camilo

INSERT INTO direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) VALUES 
(2, 'Calle 10 # 5-20, Giron', 'Casa blanca esquinera, timbre 2', true),            -- id direccion: 1
(7, 'Carrera 15 # 22-10, Bucaramanga', 'Apto 402', true),                           -- id direccion: 2
(8, 'Calle 45 # 9-50, Floridablanca', 'Casa 3', true),                              -- id direccion: 3
(9, 'Avenida 33 # 10-12, Piedecuesta', 'Conjunto cerrado', true),                   -- id direccion: 4
(10, 'Calle 50 # 14-20, Bucaramanga', 'Casa rejas negras', true);                   -- id direccion: 5

-- ==========================================================
-- 15. simulacion de historial de carritos y pedidos (transacciones conectadas)
-- ==========================================================

-- === transaccion 1: maria (id cliente: 2) ===
-- el carrito nace, se llena y se cierra como procesado al pagar
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Procesado'); -- genera id carrito: 1
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(1, 1, 2, true), 
(1, 2, 1, true);

INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES 
(2, 1, 1, 40500.00, 'Pendiente'); -- id pedido: 1
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES 
(1, 1, 2, 18000.00, 36000.00), 
(1, 2, 1, 4500.00, 4500.00);

INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES 
(1, 1, 'Celular Nequi: 3101234567', 2025.00, 38475.00, 'Aprobado');


-- === transaccion 2: laura (id cliente: 7) ===
INSERT INTO carrito (id_cliente_fk, estado) VALUES (7, 'Procesado'); -- genera id carrito: 2
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(2, 11, 1, true);

INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES 
(7, 2, 2, 150000.00, 'Entregado'); -- id pedido: 2
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES 
(2, 11, 1, 150000.00, 150000.00);

INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES 
(2, 3, 'Voucher Tarjeta: 444455556666', 7500.00, 142500.00, 'Aprobado');


-- === transaccion 3: jorge (id cliente: 8) ===
INSERT INTO carrito (id_cliente_fk, estado) VALUES (8, 'Procesado'); -- genera id carrito: 3
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(3, 7, 1, true);

INSERT INTO pedido (id_cliente_fk, id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES 
(8, 3, 3, 85000.00, 'En Camino'); -- id pedido: 3
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES 
(3, 7, 1, 85000.00, 85000.00);

INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES 
(3, 2, 'Celular Daviplata: 3124567890', 4250.00, 80750.00, 'Aprobado');