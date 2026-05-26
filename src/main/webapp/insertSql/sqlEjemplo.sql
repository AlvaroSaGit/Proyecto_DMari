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
-- 2. POBLACION DE CATEGORIAS
-- ==========================================
INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES 
('Reposteria', 'Postres, donas y dulces artesanales', true), -- ID 1
('Decoracion', 'Velas y articulos decorativos para el hogar', true), -- ID 2
('Floristeria', 'Arreglos florales hermosos para toda ocasion', true); -- ID 3

-- ==========================================
-- 3. REGISTRO DE USUARIOS BASE (Con sus roles reales)
-- ==========================================
INSERT INTO usuario (nombre, apellido, id_rol_fk, estado_cuenta) VALUES 
('Alvaro', 'Jefe', 1, true),       -- ID 1: Administrador
('Maria', 'Gomez', 2, true),       -- ID 2: Cliente
('Carlos', 'Suministros', 4, true); -- ID 3: Proveedor

-- ==========================================
-- 4. REGISTRO DE CORREOS SATELITE
-- ==========================================
INSERT INTO correo (id_usuario_fk, correo, correo_primario) VALUES 
(1, 'alvaro@dmari.com', true),
(2, 'maria@gmail.com', true),
(3, 'carlos@ceras.com', true);

-- ==========================================
-- 5. ENCRIPTACION DE CONTRASENAS (Formatos BLOB)
-- ==========================================
INSERT INTO credenciales (id_usuario, passwd_encript) VALUES 
(1, AES_ENCRYPT('admin123', 'llave_dmari')),
(2, AES_ENCRYPT('cliente123', 'llave_dmari')),
(3, AES_ENCRYPT('proveedor123', 'llave_dmari'));

-- ==========================================
-- 6. COMPLETAR PERFIL DE CLIENTE (Maria - ID 2)
-- ==========================================
INSERT INTO cliente (id_cliente_pk, direccion_envio, telefono_secundario, referencia_ubicacion) VALUES 
(2, 'Calle 10 # 5-20, Giron', '3100000000', 'Casa blanca esquinera');

-- ==========================================
-- 7. COMPLETAR PERFIL DE PROVEEDOR (Carlos - ID 3)
-- ==========================================
-- Se unifico en una sola tabla usando el ID que viene de la tabla usuario (ID 3)
INSERT INTO proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES
(3, '900123456-1', 'Ceras Giron', '123456789', 'Bancolombia', 'Ahorros');

-- ==========================================
-- 8. REGISTRO DE PRODUCTOS
-- ==========================================
INSERT INTO producto (id_categoria_fk, nombre_producto, descripcion, precio, stock, estado) VALUES 
(2, 'Vela de Vainilla y Canela', 'Aroma dulce ideal para relajacion', 18000, 15, true),     -- prod 1
(1, 'Dona Glaseada Especial', 'Clasica con glaseado de azucar', 4500, 30, true),          -- prod 2
(2, 'Vela Decorativa de Flores', 'Vela artesanal con petalos secos', 22000, 10, true),    -- prod 3
(3, 'Ramo de Rosas Rojas', 'Hermoso arreglo floral para regalar a mama', 65000, 5, true), -- prod 4
(1, 'Dona Rellena de Arequipe', 'Masa suave con relleno tradicional', 5500, 25, true),    -- prod 5
(1, 'Caja de Mini Donas', 'Set de 6 mini donas surtidas para regalo', 15000, 10, true);   -- prod 6

-- ==========================================
-- 9. REGISTRO DE IMAGENES RELACIONADAS
-- ==========================================
INSERT INTO imagenes (id_producto_fk, url_ruta, imagen_principal) VALUES 
(1, 'src/img/productos/default/gato_programador.jpg', 1),
(2, 'src/img/productos/default/gato_programador.jpg', 1),
(3, 'src/img/productos/default/gato_programador.jpg', 1),
(4, 'src/img/productos/default/gato_programador.jpg', 1),
(5, 'src/img/productos/default/gato_programador.jpg', 1),
(6, 'src/img/productos/default/gato_programador.jpg', 1);

-- ==========================================
-- 10. POBLACION DE ETIQUETAS
-- ==========================================
INSERT INTO etiqueta (nombre_etiqueta) VALUES 
('Aromaterapia'), -- ID 1
('Relajacion'),   -- ID 2
('Dulce'),        -- ID 3
('Decoracion'),   -- ID 4
('Arequipe'),     -- ID 5
('Regalo'),       -- ID 6
('Dia de la Madre'), -- ID 7
('San Valentin'); -- ID 8

-- ==========================================
-- 11. ASIGNACION DE ETIQUETAS A PRODUCTOS
-- ==========================================
INSERT INTO producto_etiqueta (id_producto, id_etiqueta) VALUES 
(1, 1), (1, 2), 
(2, 3),         
(3, 4), (3, 2), (3, 7), 
(4, 4), (4, 6), (4, 7), 
(5, 3), (5, 5), 
(6, 6), (6, 3), (6, 8); 

-- ==========================================
-- 12. RELACION PROVEEDOR - PRODUCTO (Asignacion de dueño)
-- ==========================================
-- Se cambia el ID del proveedor a 3, que corresponde al ID de Carlos
INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES 
(3, 1),
(3, 2),
(3, 3),
(3, 4),
(3, 5),
(3, 6);

-- ==========================================
-- 13. POBLACION DE METODOS DE PAGO
-- ==========================================
INSERT INTO metodo_pago (descripcion_pago, estado_activo) VALUES 
('Nequi', true),
('Daviplata', true),
('Tarjeta de Credito / Debito', true),
('Efectivo (Contra Entrega)', true);

-- ==========================================
-- 14. POBLACION DE TABLAS SATELITE (DIRECCION Y TELEFONO)
-- ==========================================
-- Agregamos el telefono y direccion especificos para el cliente Maria (ID 2)
INSERT INTO telefono (id_usuario_fk, numero_telefonico) VALUES 
(2, '3101234567');

INSERT INTO direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) VALUES 
(2, 'Calle 10 # 5-20, Giron', 'Casa blanca esquinera, timbre 2', true);

-- ==========================================
-- 15. SIMULACION DE CARRITO DE COMPRAS
-- ==========================================
-- Maria (ID 2) crea un carrito (Asume ID auto_increment 1)
INSERT INTO carrito (id_cliente_fk) VALUES (2);

-- Maria agrega 2 Velas de Vainilla (ID 1) y 1 Dona Glaseada (ID 2) al carrito 
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad) VALUES 
(1, 1, 2), 
(1, 2, 1);

-- ==========================================
-- 16. SIMULACION DE PEDIDO (CHECKOUT)
-- ==========================================
-- Convertimos el carrito en un pedido real. (El id_repartidor_fk queda NULL por ahora)
-- Total: (2 * 18000) + (1 * 4500) = 36000 + 4500 = 40500. 
INSERT INTO pedido (id_cliente_fk, total_pagar, estado_pedido) VALUES 
(2, 40500.00, 'Pendiente');

-- Detalle de los productos comprados en el pedido (Asumiendo que el ID de pedido es 1)
INSERT INTO detalle_pedido (id_producto_fk, id_pedido_fk, cantidad, precio_unitario, subtotal) VALUES 
(1, 1, 2, 18000.00, 36000.00), 
(2, 1, 1, 4500.00, 4500.00);

-- ==========================================
-- 17. SIMULACION DE PAGO
-- ==========================================
-- Maria paga el pedido 1 con Nequi (ID 1). 
-- Comision DMari (5% de 40500 = 2025). Total a transferir al proveedor = 38475.
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, numero_cuenta_ahorro, comision_dmari, monto_total, estado_activo, estado_pago) VALUES 
(1, 1, '3101234567', 2025.00, 38475.00, true, 'Aprobado');