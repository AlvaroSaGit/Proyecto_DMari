-- =================================================================
-- archivo: sqlEjemplo.sql
-- proposito: poblar la base de datos con datos de prueba realistas
-- para el ambiente de desarrollo y presentacion del proyecto DMari.
-- 
-- ORDEN DE EJECUCION OBLIGATORIO:
-- Este script respeta las dependencias del schema (tablaMysql.sql).
-- ejecutar en el mismo orden de las secciones numeradas.
-- Requisito: tablaMysql.sql debe haberse ejecutado antes que este archivo.
-- =================================================================

use dmari;

-- ==========================================================
-- 1. poblacion de roles
-- se insertan los 3 roles del sistema con IDs fijos y conocidos.
-- id 1 = administrador | id 2 = cliente | id 4 = proveedor
-- (el id 3 se omite intencionalmente para dejar margen de crecimiento)
-- ==========================================================
insert into rol (id_rol_pk, tipo_rol) values 
(1, 'administrador'),
(2, 'cliente'),
(4, 'proveedor');

-- ==========================================================
-- 2. poblacion de categorias
-- las 3 categorias base del negocio: reposteria, decoracion, floristeria.
-- estado_activo = true: visibles en el catalogo del frontend.
-- ==========================================================
insert into categoria (nombre, descripcion, estado_activo) values 
('reposteria', 'postres, donas y dulces artesanales', true),   -- id 1
('decoracion', 'velas y articulos decorativos para el hogar', true), -- id 2
('floristeria', 'arreglos florales hermosos para toda ocasion', true); -- id 3

-- ==========================================================
-- 3. registro de usuarios base
-- 15 usuarios que cubren todos los roles del sistema.
-- id 1 = admin, id 2-5 = proveedores o admin, id 6-15 = clientes.
-- estado_cuenta = true: ninguna cuenta esta suspendida en los datos de prueba.
-- ==========================================================
insert into usuario (nombre, apellido, id_rol_fk, estado_cuenta) values 
('alvaro', 'jefe', 1, true),        -- id 1: administrador del sistema
('maria', 'gomez', 2, true),        -- id 2: cliente principal (tiene historial de compras)
('carlos', 'suministros', 4, true), -- id 3: proveedor de velas (ceras giron)
('ana', 'repostera', 4, true),      -- id 4: proveedor de postres (delicias de ana)
('luis', 'flores', 4, true),        -- id 5: proveedor de flores (vivero san luis)
('pedro', 'moto', 2, true),         -- id 6: cliente
('laura', 'perez', 2, true),        -- id 7: cliente con historial de compras
('jorge', 'diaz', 2, true),         -- id 8: cliente con historial de compras
('diana', 'rojas', 2, true),        -- id 9: cliente con historial de compras
('camilo', 'mendez', 2, true),      -- id 10: cliente
('sofia', 'vargas', 2, true),       -- id 11: cliente
('andres', 'castillo', 2, true),    -- id 12: cliente (tiene solicitud de proveedor rechazada)
('valentina', 'ortiz', 2, true),    -- id 13: cliente
('diego', 'ramirez', 2, true),      -- id 14: cliente
('miguel', 'reparte', 2, true);     -- id 15: cliente (tiene solicitud de proveedor pendiente)

-- ==========================================================
-- 4. correos electronicos
-- correo_primario = true: este es el correo principal de cada usuario.
-- la tabla correo tiene UNIQUE en el campo correo,
-- por lo que no se puede repetir el mismo correo entre usuarios.
-- ==========================================================
insert into correo (id_usuario_fk, correo, correo_primario) values 
(1,  'alvaro@dmari.com',              true),
(2,  'maria@gmail.com',               true),
(3,  'carlos@ceras.com',              true),
(4,  'ana@postres.com',               true),
(5,  'luis@vivero.com',               true),
(6,  'pedro.moto@envios.com',         true),
(7,  'laura.p@hotmail.com',           true),
(8,  'jorge.d@yahoo.com',             true),
(9,  'diana.r@gmail.com',             true),
(10, 'camilo.m@empresa.co',           true),
(11, 'sofia.v@gmail.com',             true),
(12, 'andres.c@hotmail.com',          true),
(13, 'valentina.o@yahoo.com',         true),
(14, 'diego.r@gmail.com',             true),
(15, 'miguel.entregas@envios.com',    true);

-- ==========================================================
-- 5. credenciales con encriptacion aes
-- passwd_encript: nunca se guarda texto plano.
-- se usa AES_ENCRYPT de MySQL con la llave 'llave_dmari'.
-- NOTA: en produccion la llave debe venir de una variable de entorno, no del codigo.
-- contrasenas de prueba:
--   administrador: Admin12345 (cumple: 8+ chars, 1 mayuscula, 1 numero)
--   clientes: Cliente123
--   proveedores: Proveedor123
-- ==========================================================
insert into credenciales (id_usuario, passwd_encript) values 
(1,  aes_encrypt('Admin12345',    'llave_dmari')),
(2,  aes_encrypt('Cliente123',    'llave_dmari')),
(3,  aes_encrypt('Proveedor123',  'llave_dmari')),
(4,  aes_encrypt('Proveedor123',  'llave_dmari')),
(5,  aes_encrypt('Proveedor123',  'llave_dmari')),
(6,  aes_encrypt('Reparto123',    'llave_dmari')),
(7,  aes_encrypt('Cliente123',    'llave_dmari')),
(8,  aes_encrypt('Cliente123',    'llave_dmari')),
(9,  aes_encrypt('Cliente123',    'llave_dmari')),
(10, aes_encrypt('Cliente123',    'llave_dmari')),
(11, aes_encrypt('Cliente123',    'llave_dmari')),
(12, aes_encrypt('Cliente123',    'llave_dmari')),
(13, aes_encrypt('Cliente123',    'llave_dmari')),
(14, aes_encrypt('Cliente123',    'llave_dmari')),
(15, aes_encrypt('Reparto123',    'llave_dmari'));

-- ==========================================================
-- 6. perfiles de clientes
-- la tabla 'cliente' solo necesita el id_usuario_fk como id_cliente_pk.
-- referencia_ubicacion se deja nula aqui; el usuario la completa desde su perfil.
-- solo los usuarios con rol 2 (cliente) tienen entrada en esta tabla.
-- ==========================================================
insert into cliente (id_cliente_pk) values 
(2),  -- maria
(6),  -- pedro
(7),  -- laura
(8),  -- jorge
(9),  -- diana
(10), -- camilo
(11), -- sofia
(12), -- andres
(13), -- valentina
(14), -- diego
(15); -- miguel

-- ==========================================================
-- 7. perfiles de proveedores
-- la tabla 'proveedor' solo tiene los usuarios con rol 4.
-- nit_empresa y nombre_marca son obligatorios (NOT NULL en el schema).
-- los datos bancarios son opcionales pero importantes para los pagos.
-- ==========================================================
insert into proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) values
(3, '900123456-1', 'ceras giron',     '123456789', 'bancolombia', 'ahorros'),
(4, '900987654-2', 'delicias de ana', '987654321', 'davivienda',  'corriente'),
(5, '900555666-3', 'vivero san luis', '555666777', 'nequi',       'ahorros');

-- ==========================================================
-- 8. catalogo de productos
-- id_categoria_fk enlaza con la tabla categoria (1=reposteria, 2=decoracion, 3=floristeria).
-- precio: en pesos colombianos con 2 decimales.
-- stock: unidades fisicas disponibles. se descuenta automaticamente al registrar un pedido.
-- estado = true: visible en el catalogo del frontend.
-- ==========================================================
insert into producto (id_categoria_fk, nombre_producto, descripcion, precio, stock, estado) values 
(2, 'vela de vainilla y canela',   'aroma dulce ideal para relajacion',              18000, 15,  true), -- id 1
(1, 'dona glaseada especial',      'clasica con glaseado de azucar',                  4500, 30,  true), -- id 2
(2, 'vela decorativa de flores',   'vela artesanal con petalos secos',               22000, 10,  true), -- id 3
(3, 'ramo de rosas rojas',         'hermoso arreglo floral para regalar a mama',     65000,  5,  true), -- id 4
(1, 'dona rellena de arequipe',    'masa suave con relleno tradicional',              5500, 25,  true), -- id 5
(2, 'vela de leon',                'vela decorativa infantil en forma de leon',      15000, 10,  true), -- id 6
(1, 'ancheta de frutas',           'bandeja con jugo, sanduche y fruta fresca',      85000,  8,  true), -- id 7
(2, 'peluche de stitch',           'adorable peluche azul de lilo y stitch',        110000,  4,  true), -- id 8
(1, 'caja de trufas',              '12 trufas de chocolate belga',                   35000, 20,  true), -- id 9
(2, 'globo helio te amo',          'globo metalizado gigante',                       12000, 50,  true), -- id 10
(2, 'peluche oso gigante',         'oso de felpa de 1 metro de alto',               150000,  3,  true), -- id 11
(2, 'vino tinto reserva',          'botella de vino tinto importado',                75000, 12,  true), -- id 12
(2, 'tarjeta de regalo especial',  'tarjeta artesanal con relieve para obsequios',   8000, 100, true), -- id 13
(2, 'vela en forma de oso',        'vela artesanal con forma de tierno oso',         55000,  6,  true), -- id 14
(2, 'vela de dios',                'vela sagrada para iluminacion espiritual',       20000, 15,  true); -- id 15

-- ==========================================================
-- 9. galeria de imagenes
-- cada producto tiene exactamente una imagen marcada como principal (imagen_principal = 1).
-- url_ruta es relativa a la raiz del proyecto web (src/img/productos/default/).
-- ==========================================================
insert into imagenes (id_producto_fk, url_ruta, imagen_principal) values 
(1,  'src/img/productos/default/velas.jpg',            1),
(2,  'src/img/productos/default/gato_programador.jpg', 1),
(3,  'src/img/productos/default/velaFlores.jpg',       1),
(4,  'src/img/productos/default/ramoFlores.jpeg',      1),
(5,  'src/img/productos/default/cajadona.jpg',         1),
(6,  'src/img/productos/default/velaLeon.jpg',         1),
(7,  'src/img/productos/default/anchetaFrutas.jpg',    1),
(8,  'src/img/productos/default/stichPeluche.png',     1),
(9,  'src/img/productos/default/cajaTrufa.jpg',        1),
(10, 'src/img/productos/default/globoHelio.jpg',       1),
(11, 'src/img/productos/default/toyOso.jpg',           1),
(12, 'src/img/productos/default/vino.png',             1),
(13, 'src/img/productos/default/tarjetaRegalo.png',    1),
(14, 'src/img/productos/default/velaOso.jpg',          1),
(15, 'src/img/productos/default/velaSagrada.jpg',      1);

-- ==========================================================
-- 10. etiquetas de busqueda
-- las etiquetas permiten filtrar productos en el catalogo.
-- se relacionan con productos via la tabla puente 'producto_etiqueta'.
-- ==========================================================
insert into etiqueta (nombre_etiqueta) values 
('aromaterapia'), ('relajacion'), ('dulce'), ('decoracion'), ('arequipe'), 
('regalo'), ('dia de la madre'), ('san valentin'), ('cumpleanos'), ('aniversario'), 
('chocolate'), ('premium'), ('infantil'), ('para ella'), ('para el');

-- ==========================================================
-- 11. vinculacion producto-etiqueta
-- tabla puente: cada fila asocia un producto con una etiqueta.
-- unique(id_producto, id_etiqueta) garantiza que no se repita la misma asociacion.
-- ==========================================================
insert into producto_etiqueta (id_producto, id_etiqueta) values 
(1, 1), (1, 2), (1, 14),            -- vela vainilla: aromaterapia, relajacion, para ella
(2, 3), (2, 13),                    -- dona glaseada: dulce, infantil
(3, 4), (3, 2), (3, 7),            -- vela flores: decoracion, relajacion, dia de la madre
(4, 6), (4, 7), (4, 8), (4, 10), (4, 14), -- ramo rosas: regalo, dia madre, san valentin, aniversario, para ella
(5, 3), (5, 5),                    -- dona arequipe: dulce, arequipe
(6, 6), (6, 3), (6, 13),           -- vela leon: regalo, dulce, infantil
(7, 6), (7, 9), (7, 10),           -- ancheta: regalo, cumpleanos, aniversario
(8, 6), (8, 9), (8, 15),           -- stitch: regalo, cumpleanos, para el
(9, 3), (9, 6), (9, 8), (9, 11),   -- trufas: dulce, regalo, san valentin, chocolate
(10, 8), (10, 10),                 -- globo: san valentin, aniversario
(11, 6), (11, 8), (11, 14),        -- oso peluche: regalo, san valentin, para ella
(12, 6), (12, 10), (12, 12), (12, 15), -- vino: regalo, aniversario, premium, para el
(13, 9),                           -- tarjeta: cumpleanos
(14, 3), (14, 9), (14, 11),        -- vela oso: dulce, cumpleanos, chocolate
(15, 3), (15, 9), (15, 13);        -- vela dios: dulce, cumpleanos, infantil

-- ==========================================================
-- 12. vinculacion proveedor-producto
-- establece que proveedor surte cada producto.
-- los productos sin fila aqui son de 'DMari Oficial' (sin proveedor externo).
-- ==========================================================
insert into proveedor_producto (id_proveedor_fk, id_producto_fk) values 
(3, 1),  -- vela vainilla y canela -> carlos (ceras giron)
(3, 3),  -- vela flores -> carlos (ceras giron)
(4, 2),  -- dona glaseada -> ana (delicias de ana)
(4, 5),  -- dona arequipe -> ana (delicias de ana)
(4, 6),  -- vela leon -> ana (delicias de ana)
(4, 14), -- vela oso -> ana (delicias de ana)
(4, 15), -- vela dios -> ana (delicias de ana)
(5, 4);  -- ramo de rosas -> luis (vivero san luis)

-- ==========================================================
-- 13. metodos de pago habilitados
-- estado_activo = true: disponible para el cliente en el checkout.
-- estos ids son referenciados en la tabla 'pago' de cada transaccion.
-- ==========================================================
insert into metodo_pago (descripcion_pago, estado_activo) values 
('nequi',                         true),  -- id 1
('daviplata',                     true),  -- id 2
('tarjeta de credito / debito',   true),  -- id 3
('efectivo (contra entrega)',     true),  -- id 4
('pse',                           true);  -- id 5

-- ==========================================================
-- 14. datos logisticos (direcciones y telefonos)
-- solo los clientes con historial de compras tienen registros aqui.
-- la direccion_primario = true es la que se usa por defecto en el checkout.
-- IMPORTANTE: para que un pedido pueda registrarse, el cliente
-- debe tener al menos UNA direccion con direccion_primario = true.
-- ==========================================================
insert into telefono (id_usuario_fk, numero_telefonico) values 
(2, '3101234567'), -- telefono principal de maria
(2, '3100000000'), -- telefono secundario de maria
(7, '3119876543'), -- telefono principal de laura
(7, '3111111111'), -- telefono secundario de laura
(8, '3124567890'), -- telefono principal de jorge
(8, '3122222222'), -- telefono secundario de jorge
(10,'3144444444'); -- unico telefono de camilo

insert into direccion (id_usuario_fk, direccion, direccion_detallada, direccion_primario) values 
(2, 'calle 10 # 5-20, giron',                 'casa blanca esquinera, timbre 2', true),  -- id 1: direccion de maria (primaria)
(7, 'carrera 15 # 22-10, bucaramanga',        'apto 402',                         true),  -- id 2: direccion de laura (primaria)
(8, 'calle 45 # 9-50, floridablanca',         'casa 3',                           true),  -- id 3: direccion de jorge (primaria)
(9, 'avenida 33 # 10-12, piedecuesta',        'conjunto cerrado',                  true),  -- id 4: direccion de diana (primaria)
(10,'calle 50 # 14-20, bucaramanga',          'casa rejas negras',                true);  -- id 5: direccion de camilo (primaria)

-- ==========================================================
-- 15. simulacion del flujo operativo de ventas (transaccion principal)
-- LOGICA DEL FLUJO COMPLETO:
--   1. se crea el carrito en estado 'Procesado' (ya cerrado por el checkout).
--   2. se usa @last_carrito_id para amarrar el pedido al carrito correcto.
--   3. el pedido NO usa id_usuario_fk directo. el cliente se identifica via:
--      pedido -> id_carrito_fk -> carrito.id_cliente_fk
--   4. se registra el pago con la comision calculada al 5%.
-- ==========================================================

-- variables de sesion para capturar IDs generados automaticamente (LAST_INSERT_ID)
SET @last_carrito_id = 0;
SET @last_pedido_id  = 0;

-- transaccion 1: compra de maria (id cliente: 2, direccion id: 1)
-- maria compra 2 velas de vainilla (prod 1) + 1 dona glaseada (prod 2)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID(); -- capturamos el id del carrito recien creado
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(@last_carrito_id, 1, 2, TRUE), -- 2 velas de vainilla
(@last_carrito_id, 2, 1, TRUE); -- 1 dona glaseada

-- NOTA: id_carrito_fk e id_direccion_fk son NOT NULL en pedido.
-- id_direccion_fk = 1 corresponde a la unica direccion primaria de maria.
-- total: (2 x 18000) + (1 x 4500) = 40500
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) VALUES 
(@last_carrito_id, 1, 40500.00, 'Pendiente', CURRENT_TIMESTAMP);
SET @last_pedido_id = LAST_INSERT_ID(); -- capturamos el id del pedido para los detalles

-- copiamos los precios exactos al detalle_pedido (snapshot inmutable de precios)
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES 
(@last_pedido_id, 1, 2, 18000.00, 36000.00), -- 2 velas = 36000
(@last_pedido_id, 2, 1,  4500.00,  4500.00); -- 1 dona  = 4500

-- registro del pago: comision 5% = 2025, monto proveedor = 38475
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES 
(@last_pedido_id, 1, 'celular nequi: 3101234567', 2025.00, 38475.00, 'Aprobado');

-- ==========================================================
-- 16. data variada para estadisticas (enero - mayo 2024)
-- cada bloque repite el flujo: carrito -> pedido -> detalle -> pago.
-- los pedidos tienen fechas historicas usando DATE_SUB para generar grafica de tendencias.
-- ==========================================================

-- hace 5 meses: maria compra un oso gigante (prod 11, DMari Oficial)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 11, 1, TRUE);
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (@last_carrito_id, 1, 150000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 11, 1, 150000.00, 150000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 1, 7500.00, 142500.00, 'Aprobado');

-- hace 4 meses: laura compra 10 donas de ana (proveedor id 4)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (7, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 2, 10, TRUE);
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (@last_carrito_id, 2, 45000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 2, 10, 4500.00, 45000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 2, 2250.00, 42750.00, 'Aprobado');

-- hace 3 meses: jorge compra 5 velas de carlos (proveedor id 3)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (8, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 1, 5, TRUE);
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (@last_carrito_id, 3, 90000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 1, 5, 18000.00, 90000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 3, 4500.00, 85500.00, 'Aprobado');

-- hace 2 meses: diana compra 2 ramos de flores de luis (proveedor id 5)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (9, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 4, 2, TRUE);
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (@last_carrito_id, 4, 130000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 4, 2, 65000.00, 130000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 5, 6500.00, 123500.00, 'Aprobado');

-- hace 1 mes: maria compra vino (prod 12, DMari) y 4 donas de ana (prod 5)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(@last_carrito_id, 12, 1, TRUE), -- vino tinto
(@last_carrito_id,  5, 4, TRUE); -- 4 donas de arequipe
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido, fecha) 
VALUES (@last_carrito_id, 1, 97000.00, 'Entregado', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 MONTH));
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES 
(@last_pedido_id, 12, 1, 75000.00, 75000.00), -- vino: DMari Oficial
(@last_pedido_id,  5, 4,  5500.00, 22000.00); -- donas: Ana (proveedor 4)
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 3, 4850.00, 92150.00, 'Aprobado');

-- ==========================================================
-- vinculacion de productos adicionales a proveedores para completar estadisticas
-- productos que aun no tenian proveedor asignado en el paso 12.
-- ==========================================================
INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES 
(3, 3),  -- vela decorativa de flores -> carlos (ceras giron)
(4, 9),  -- caja de trufas -> ana (delicias de ana)
(5, 10); -- globo helio -> luis (vivero san luis)

-- ==========================================================
-- 17. solicitudes de nuevos proveedores
-- estado_solicitud diferenciados por caso:
--   'pendiente' = en revision por el administrador.
--   'rechazada' = no cumplio los requisitos (ej. nit invalido).
-- ==========================================================

-- miguel (id 15) quiere vender artesanias de cuero: solicitud pendiente
insert into solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) 
values (15, '800555444-9', 'Cueros Miguel', '444555666', 'Banco de Bogota', 'Corriente', 'pendiente');

-- andres (id 12) fue rechazado por nit invalido (todos ceros)
insert into solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) 
values (12, '000000000-0', 'Andres Manualidades', '111222333', 'Nequi', 'Ahorros', 'rechazada');

-- ==========================================================
-- 18. transacciones adicionales para el panel de gestion
-- transaccion 2: laura compra un oso gigante (entregado) via tarjeta
-- ==========================================================
INSERT INTO carrito (id_cliente_fk, estado) VALUES (7, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 11, 1, TRUE);
-- id_direccion_fk = 2 es la direccion registrada de laura
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES (@last_carrito_id, 2, 150000.00, 'Entregado');
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 11, 1, 150000.00, 150000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 3, 'voucher tarjeta: 444455556666', 7500.00, 142500.00, 'Aprobado');

-- transaccion 3: jorge compra ancheta (en camino) via daviplata
INSERT INTO carrito (id_cliente_fk, estado) VALUES (8, 'Procesado');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (@last_carrito_id, 7, 1, TRUE);
-- id_direccion_fk = 3 es la direccion registrada de jorge
INSERT INTO pedido (id_carrito_fk, id_direccion_fk, total_pagar, estado_pedido) VALUES (@last_carrito_id, 3, 85000.00, 'En Camino');
SET @last_pedido_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (id_pedido_fk, id_producto_fk, cantidad, precio_unitario, subtotal) VALUES (@last_pedido_id, 7, 1, 85000.00, 85000.00);
INSERT INTO pago (id_pedido_fk, id_metodo_pago_fk, referencia_transaccion, comision_dmari, monto_total, estado_pago) VALUES (@last_pedido_id, 2, 'celular daviplata: 3124567890', 4250.00, 80750.00, 'Aprobado');

-- ==========================================================
-- 19. carrito abandonado (sin pedido asociado)
-- este carrito esta en estado 'Activo', lo que significa que
-- el cliente agrego productos pero nunca completo la compra.
-- sirve para probar el widget del carrito en el frontend.
-- ==========================================================

-- carrito abandonado por maria (tiene 3 donas de arequipe sin procesar)
INSERT INTO carrito (id_cliente_fk, estado) VALUES (2, 'Activo');
SET @last_carrito_id = LAST_INSERT_ID();
INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES 
(@last_carrito_id, 5, 3, TRUE); -- 3 donas de arequipe pendientes de compra