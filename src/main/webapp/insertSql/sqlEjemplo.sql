USE DMari;

-- ==========================================
-- 1. POBLACION DE ROLES
-- ==========================================
INSERT INTO rol (tipo_rol) VALUES 
('administrador'), -- ID 1
('cliente'),       -- ID 2
('repartidor'),    -- ID 3
('proveedor');     -- ID 4

-- ==========================================
-- 2. POBLACION DE CATEGORIAS (15 Registros para SENA)
-- ==========================================
INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES 
('Reposteria', 'Postres, donas y dulces artesanales', true), -- ID 1
('Decoracion', 'Velas y articulos decorativos para el hogar', true), -- ID 2
('Floristeria', 'Arreglos florales hermosos para toda ocasion', true), -- ID 3
('Desayunos Sorpresa', 'Bandejas de desayuno para regalar', true), -- ID 4
('Anchetas', 'Cestas de regalos surtidas', true), -- ID 5
('Chocolateria', 'Bombones y trufas de chocolate fino', true), -- ID 6
('Globos', 'Globos de helio y metalizados', true), -- ID 7
('Peluches', 'Peluches de todos los tamanos', true), -- ID 8
('Licores', 'Vinos y licores para acompanar regalos', true), -- ID 9
('Tarjetas', 'Tarjetas con mensajes personalizados', true), -- ID 10
('Empaques', 'Cajas y bolsas de regalo especiales', true), -- ID 11
('Combos', 'Paquetes de regalo prearmados', true), -- ID 12
('Eventos', 'Bocaditos y decoracion para fiestas', true), -- ID 13
('Estacionales', 'Productos de navidad, amor y amistad, etc', true), -- ID 14
('Saludables', 'Postres sin azucar y opciones fit', true); -- ID 15

-- ==========================================
-- 3. REGISTRO DE USUARIOS BASE (15 Registros para SENA)
-- ==========================================
-- Se distribuyen: 1 Admin, 3 Proveedores, 2 Repartidores, 9 Clientes
INSERT INTO usuario (nombre, apellido, id_rol_fk, estado_cuenta) VALUES 
('Alvaro', 'Jefe', 1, true),       -- ID 1: Administrador
('Maria', 'Gomez', 2, true),       -- ID 2: Cliente VIP
('Carlos', 'Suministros', 4, true), -- ID 3: Proveedor Ceras
('Ana', 'Repostera', 4, true),     -- ID 4: Proveedor Postres
('Luis', 'Flores', 4, true),       -- ID 5: Proveedor Vivero
('Pedro', 'Moto', 3, true),        -- ID 6: Repartidor
('Laura', 'Perez', 2, true),       -- ID 7: Cliente
('Jorge', 'Diaz', 2, true),        -- ID 8: Cliente
('Diana', 'Rojas', 2, true),       -- ID 9: Cliente
('Camilo', 'Mendez', 2, true),     -- ID 10: Cliente
('Sofia', 'Vargas', 2, true),      -- ID 11: Cliente
('Andres', 'Castillo', 2, true),   -- ID 12: Cliente
('Valentina', 'Ortiz', 2, true),   -- ID 13: Cliente
('Diego', 'Ramirez', 2, true),     -- ID 14: Cliente
('Miguel', 'Reparte', 3, true);    -- ID 15: Repartidor 2

-- ==========================================
-- 4. REGISTRO DE CORREOS SATELITE (15 Registros)
-- ==========================================
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

-- ==========================================
-- 5. ENCRIPTACION DE CONTRASENAS (15 Registros)
-- ==========================================
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

-- ==========================================
-- 6. COMPLETAR PERFILES DE CLIENTES
-- ==========================================
INSERT INTO cliente (id_cliente_pk, direccion_envio, telefono_secundario, referencia_ubicacion) VALUES 
(2, 'Calle 10 # 5-20, Giron', '3100000000', 'Casa blanca esquinera'),
(7, 'Carrera 15 # 22-10, Bucaramanga', '3111111111', 'Edificio Torres del Sol'),
(8, 'Calle 45 # 9-50, Floridablanca', '3122222222', 'Frente al parque'),
(9, 'Avenida 33 # 10-12, Piedecuesta', NULL, 'Conjunto cerrado'),
(10, 'Calle 50 # 14-20, Bucaramanga', '3144444444', 'Casa rejas negras');

-- ==========================================
-- 7. COMPLETAR PERFILES DE PROVEEDORES
-- ==========================================
INSERT INTO proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES
(3, '900123456-1', 'Ceras Giron', '123456789', 'Bancolombia', 'Ahorros'),
(4, '900987654-2', 'Delicias de Ana', '987654321', 'Davivienda', 'Corriente'),
(5, '900555666-3', 'Vivero San Luis', '555666777', 'Nequi', 'Ahorros');

-- ==========================================
-- 9. REGISTRO DE PRODUCTOS (15 Registros para SENA)
-- ==========================================
INSERT INTO producto (id_categoria_fk, nombre_producto, descripcion, precio, stock, estado) VALUES 
(2, 'Vela de Vainilla y Canela', 'Aroma dulce ideal para relajacion', 18000, 15, true),     -- prod 1
(1, 'Dona Glaseada Especial', 'Clasica con glaseado de azucar', 4500, 30, true),          -- prod 2
(2, 'Vela Decorativa de Flores', 'Vela artesanal con petalos secos', 22000, 10, true),    -- prod 3
(3, 'Ramo de Rosas Rojas', 'Hermoso arreglo floral para regalar a mama', 65000, 5, true), -- prod 4
(1, 'Dona Rellena de Arequipe', 'Masa suave con relleno tradicional', 5500, 25, true),    -- prod 5
(1, 'Caja de Mini Donas', 'Set de 6 mini donas surtidas para regalo', 15000, 10, true),   -- prod 6
(4, 'Desayuno Feliz', 'Bandeja con jugo, sanduche, fruta y globo', 85000, 8, true),       -- prod 7
(5, 'Ancheta Cumpleanos', 'Dulces surtidos y cervezas', 110000, 4, true),                 -- prod 8
(6, 'Caja de Trufas', '12 trufas de chocolate belga', 35000, 20, true),                   -- prod 9
(7, 'Globo Helio Te Amo', 'Globo metalizado gigante', 12000, 50, true),                   -- prod 10
(8, 'Peluche Oso Gigante', 'Oso de felpa de 1 metro de alto', 150000, 3, true),           -- prod 11
(9, 'Vino Tinto Reserva', 'Botella de vino tinto importado', 75000, 12, true),            -- prod 12
(10, 'Tarjeta 3D Cumpleanos', 'Tarjeta artesanal con relieve', 8000, 100, true),          -- prod 13
(1, 'Pastel de Chocolate', 'Pastel humedo para 10 personas', 55000, 6, true),             -- prod 14
(1, 'Cupcakes Decorados', 'Caja de 4 cupcakes personalizados', 20000, 15, true);          -- prod 15

-- ==========================================
-- 10. REGISTRO DE IMAGENES RELACIONADAS (15 Registros)
-- ==========================================
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

-- ==========================================
-- 11. POBLACION DE ETIQUETAS (15 Registros para SENA)
-- ==========================================
INSERT INTO etiqueta (nombre_etiqueta) VALUES 
('Aromaterapia'), -- ID 1
('Relajacion'),   -- ID 2
('Dulce'),        -- ID 3
('Decoracion'),   -- ID 4
('Arequipe'),     -- ID 5
('Regalo'),       -- ID 6
('Dia de la Madre'), -- ID 7
('San Valentin'), -- ID 8
('Cumpleanos'),   -- ID 9
('Aniversario'),  -- ID 10
('Chocolate'),    -- ID 11
('Premium'),      -- ID 12
('Infantil'),     -- ID 13
('Para Ella'),    -- ID 14
('Para El');      -- ID 15

-- ==========================================
-- 12. ASIGNACION DE ETIQUETAS A PRODUCTOS
-- ==========================================
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

-- ==========================================
-- 13. RELACION PROVEEDOR - PRODUCTO (Asignacion de dueño)
-- ==========================================
INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES 
(3, 1), -- Vela (Ceras Giron)
(3, 3), -- Vela Flores (Ceras Giron)
(4, 2), -- Dona (Ana Repostera)
(4, 5), -- Dona Arequipe (Ana Repostera)
(4, 6), -- Mini Donas (Ana Repostera)
(4, 14), -- Pastel (Ana Repostera)
(4, 15), -- Cupcakes (Ana Repostera)
(5, 4); -- Ramo de Rosas (Luis Vivero)
-- Los demas productos (Anchetas, Globos, Licores, etc) son propios de DMari (no tienen proveedor asignado)

-- ==========================================
-- 14. POBLACION DE METODOS DE PAGO
-- ==========================================
INSERT INTO metodo_pago (descripcion_pago, estado_activo) VALUES 
('Nequi', true),
('Daviplata', true),
('Tarjeta de Credito / Debito', true),
('Efectivo (Contra Entrega)', true),
('PSE', true);

-- ==========================================
-- 15. DIRECCIONES Y TELEFONOS DE CLIENTES
-- ==========================================
INSERT INTO telefono (id_usuario_fk, numero_telefonico) VALUES 
(2, '3101234567'),
(7, '3119876543'),
(8, '3124567890');

INSERT INTO direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) VALUES 
(2, 'Calle 10 # 5-20, Giron', 'Casa blanca esquinera, timbre 2', true),
(7, 'Carrera 15 # 22-10, Bucaramanga', 'Apto 402', true),
(8, 'Calle 45 # 9-50, Floridablanca', 'Casa 3', true);

-- ==========================================
-- 16. SIMULACION DE CARRITO DE COMPRAS Y PEDIDOS (Transacciones reales)
-- ==========================================

-- === PEDIDO 1: Maria (ID 2) ===
INSERT INTO carrito (id_cliente_fk) VALUES (2);
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad) VALUES 
(1, 1, 2), 
(1, 2, 1);

INSERT INTO pedido (id_cliente_fk, total_pagar, estado_pedido) VALUES 
(2, 40500.00, 'Pendiente');
INSERT INTO detalle_pedido (id_producto_fk, id_pedido_fk, cantidad, precio_unitario, subtotal) VALUES 
(1, 1, 2, 18000.00, 36000.00), 
(2, 1, 1, 4500.00, 4500.00);

INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, numero_cuenta_ahorro, comision_dmari, monto_total, estado_activo, estado_pago) VALUES 
(1, 1, '3101234567', 2025.00, 38475.00, true, 'Aprobado');

-- === PEDIDO 2: Laura (ID 7) - Entregado ===
INSERT INTO pedido (id_cliente_fk, total_pagar, estado_pedido) VALUES 
(7, 150000.00, 'Entregado');
INSERT INTO detalle_pedido (id_producto_fk, id_pedido_fk, cantidad, precio_unitario, subtotal) VALUES 
(11, 2, 1, 150000.00, 150000.00); -- Peluche Oso Gigante
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, numero_cuenta_ahorro, comision_dmari, monto_total, estado_activo, estado_pago) VALUES 
(2, 3, '444455556666', 7500.00, 142500.00, true, 'Aprobado');

-- === PEDIDO 3: Jorge (ID 8) - En Camino ===
INSERT INTO pedido (id_cliente_fk, total_pagar, estado_pedido) VALUES 
(8, 85000.00, 'En Camino');
INSERT INTO detalle_pedido (id_producto_fk, id_pedido_fk, cantidad, precio_unitario, subtotal) VALUES 
(7, 3, 1, 85000.00, 85000.00); -- Desayuno Feliz
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, numero_cuenta_ahorro, comision_dmari, monto_total, estado_activo, estado_pago) VALUES 
(3, 2, '3124567890', 4250.00, 80750.00, true, 'Aprobado');