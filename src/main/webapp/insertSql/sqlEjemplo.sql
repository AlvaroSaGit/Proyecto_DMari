
USE DMari;

-- ==========================================
-- 1. POBLACIÓN DE ROLES (Ahora con proveedor)
-- ==========================================
INSERT INTO rol (tipo_rol) VALUES 
('administrador'), -- ID 1
('cliente'),       -- ID 2
('repartidor'),    -- ID 3
('proveedor');     -- ID 4

-- ==========================================
-- 2. POBLACIÓN DE CATEGORÍAS
-- ==========================================
INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES 
('Donas', 'Donas artesanales con diferentes glaseados y rellenos', true),
('Velas Aromaticas', 'Velas decorativas y con aromas relajantes', true),
('Combos de Regalo', 'Cajas especiales combinando donas y velas', true);

-- ==========================================
-- 3. REGISTRO DE USUARIOS BASE (Con sus roles reales)
-- ==========================================
INSERT INTO usuario (nombre, apellido, id_rol_fk, estado_cuenta) VALUES 
('Alvaro', 'Jefe', 1, true),       -- ID 1: Administrador
('Maria', 'Gomez', 2, true),       -- ID 2: Cliente
('Carlos', 'Suministros', 4, true); -- ID 3: Proveedor (Ahora sí con rol 4!)

-- ==========================================
-- 4. REGISTRO DE CORREOS SATÉLITE
-- ==========================================
INSERT INTO correo (id_usuario_fk, correo, correo_primario) VALUES 
(1, 'alvaro@dmari.com', true),
(2, 'maria@gmail.com', true),
(3, 'carlos@ceras.com', true);

-- ==========================================
-- 5. ENCRIPTACOÓN DE CONTRASEÑAS (Formatos BLOB)
-- ==========================================
INSERT INTO credenciales (id_usuario, passwd_encript) VALUES 
(1, AES_ENCRYPT('admin123', 'llave_dmari')),
(2, AES_ENCRYPT('cliente123', 'llave_dmari')),
(3, AES_ENCRYPT('proveedor123', 'llave_dmari'));

-- ==========================================
-- 6. COMPLETAR PERFIL DE CLIENTE (María - ID 2)
-- ==========================================
INSERT INTO cliente (id_cliente_pk, direccion_envio, telefono_secundario, referencia_ubicacion) VALUES 
(2, 'Calle 10 # 5-20, Giron', '3100000000', 'Casa blanca esquinera');

-- ==========================================
-- 7. COMPLETAR PERFIL DE PROVEEDOR (Carlos - ID 3)
-- ==========================================
INSERT INTO datos_proveedor (id_datos_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES
(3, '900123456-1', 'Ceras Giron', '123456789', 'Bancolombia', 'Ahorros');

INSERT INTO proveedor (id_datos_proveedor_fk) VALUES (3);

-- ==========================================
-- 8. REGISTRO DE PRODUCTOS
-- ==========================================
INSERT INTO producto (id_categoria_fk, nombre_producto, descripcion, precio, stock, estado) VALUES 
(2, 'Vela de Vainilla y Canela', 'Aroma dulce ideal para relajacion', 18000, 15, true),
(1, 'Dona Glaseada Especial', 'Clasica con glaseado de azucar', 4500, 30, true),
(2, 'Vela Decorativa de Flores', 'Vela artesanal con petalos secos', 22000, 10, true),
(1, 'Dona Rellena de Arequipe', 'Masa suave con relleno tradicional', 5500, 25, true),
(3, 'Caja de Mini Velas Regalo', 'Set de 3 mini velas surtidas', 35000, 5, true);

-- ==========================================
-- 9. REGISTRO DE IMÁGENES RELACIONADAS
-- ==========================================
INSERT INTO imagenes (id_producto_fk, url_ruta, imagen_principal) VALUES 
(1, 'src/img/productos/default/gato_programador.jpg', 1),
(2, 'src/img/productos/default/gato_programador.jpg', 1),
(3, 'src/img/productos/default/gato_programador.jpg', 1),
(4, 'src/img/productos/default/gato_programador.jpg', 1),
(5, 'src/img/productos/default/gato_programador.jpg', 1);

-- ==========================================
-- 10. POBLACIÓN DE ETIQUETAS
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
-- 11. ASIGNACIÓN DE ETIQUETAS A PRODUCTOS (Tabla intermedia)
-- ==========================================
INSERT INTO producto_etiqueta (id_producto, id_etiqueta) VALUES 
(1, 1), (1, 2), -- Vela de Vainilla: Aromaterapia y Relajacion
(2, 3),         -- Dona Glaseada: Dulce
(3, 4), (3, 2), (3, 7), -- Vela Flores: Decoracion, Relajacion, Dia de la Madre
(4, 3), (4, 5), -- Dona de Arequipe: Dulce y Arequipe
(5, 6), (5, 1), (5, 8); -- Caja Mini Velas: Regalo, Aromaterapia, San Valentin

-- ==========================================
-- 12. RELACION PROVEEDOR - PRODUCTO (Otorgando dueños)
-- ==========================================
-- Carlos es el proveedor 1 en la tabla proveedor
INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES 
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5);
