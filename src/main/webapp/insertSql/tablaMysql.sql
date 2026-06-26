-- =================================================================
<<<<<<< HEAD
-- archivo: tablamysql.sql
-- proposito: definicion completa del esquema de la base de datos dmari.
-- cada tabla esta ordenada segun sus dependencias (primero las maestras,
-- luego las hijas) para que mysql no rompa las restricciones de clave foranea.
-- estrategia: on delete restrict fuerza el uso de soft delete (estado).
=======
-- archivo: tablaMysql.sql
-- proposito: definicion completa del esquema de la base de datos DMari.
-- cada tabla esta ordenada segun sus dependencias (primero las maestras,
-- luego las hijas) para que mysql no rompa las restricciones de clave foranea.
-- estrategia de borrado: se usa soft delete (campo estado/estado_cuenta)
-- en lugar de DELETE fisico para conservar historial.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
-- =================================================================

create database if not exists DMari;
use DMari;

-- ==========================================================
-- 1. tablas de catalogos y roles (tablas maestras)
-- estas tablas no dependen de nadie, se crean primero.
-- ==========================================================

-- define los tipos de cuenta del sistema: cliente, administrador o proveedor.
<<<<<<< HEAD
=======
-- el enum garantiza que solo se inserten valores validos.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table rol(
    id_rol_pk int auto_increment primary key,
    tipo_rol enum('cliente','administrador','proveedor') not null default 'cliente'
);

-- categorias a las que pertenecen los productos del catalogo.
<<<<<<< HEAD
=======
-- estado_activo permite ocultar categorias sin borrarlas (soft delete).
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table categoria(
    id_categoria_pk int auto_increment primary key,
    nombre varchar(50) not null,
    descripcion varchar(100),
    estado_activo boolean not null default true -- false = categoria desactivada pero con historial
);

<<<<<<< HEAD
-- etiquetas de busqueda y filtrado para los productos.
=======
-- etiquetas de busqueda y filtrado para los productos (ej: 'aromaterapia', 'regalo').
-- permite un sistema de tags flexible sin alterar la tabla producto.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table etiqueta(
    id_etiqueta_pk int auto_increment primary key,
    nombre_etiqueta varchar(30) not null
);

<<<<<<< HEAD
-- metodos de pago disponibles para los clientes.
=======
-- metodos de pago disponibles para los clientes (ej: Nequi, PSE, Efectivo).
-- estado_activo permite activar o desactivar medios de pago sin borrarlos.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table metodo_pago(
    id_metodo_pago_pk int auto_increment primary key,
    descripcion_pago varchar(255) not null,
    estado_activo boolean not null default true -- false = este medio de pago fue desactivado
);

-- ==========================================================
<<<<<<< HEAD
-- 2. tablas nucleo principales y extensiones de usuario
-- ==========================================================

-- tabla central de personas del sistema.
-- estado_cuenta soporta soft delete: false = usuario desactivado.
=======
-- 2. tablas nucleo principales
-- dependen de las tablas maestras de la seccion 1.
-- ==========================================================

-- tabla central de personas del sistema: clientes, admins y proveedores.
-- estado_cuenta soporta soft delete: false = usuario desactivado sin borrar su historial.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table usuario(
    id_usuario_pk int auto_increment primary key,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
<<<<<<< HEAD
    id_rol_fk int not null,
    estado_cuenta boolean not null default true,
    foreign key (id_rol_fk) references rol(id_rol_pk) on delete restrict
);

-- datos adicionales exclusivos para usuarios con rol cliente.
create table cliente(
    id_cliente_pk int primary key,
    referencia_ubicacion text null,
    foreign key (id_cliente_pk) references usuario(id_usuario_pk) on delete restrict
);

-- la tabla proveedor fusiona la solicitud inicial y el perfil operativo vigente.
-- estado_aprobacion maneja el flujo de admision sin tablas secundarias.
create table proveedor(
    id_proveedor_pk int primary key,
    nit_empresa varchar(20) not null unique,
    nombre_marca varchar(100) not null,
    cuenta_bancaria varchar(30) not null,
    banco_nombre varchar(50) not null,
    tipo_cuenta enum('Ahorros','Corriente') not null,
    estado_aprobacion enum('pendiente', 'aprobada', 'rechazada') default 'pendiente',
    fecha_solicitud timestamp default current_timestamp,
    foreign key (id_proveedor_pk) references usuario(id_usuario_pk) on delete restrict
);

-- direcciones de envio de cada usuario.
create table direccion(
    id_direccion_pk int auto_increment primary key,
    id_usuario_fk int not null,
    direccion text not null,
    direccion_detallada text,
    direccion_primario boolean not null default false,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk) on delete restrict
);

-- telefonos de contacto de cada usuario.
create table telefono(
    id_telefono_pk int auto_increment primary key,
    id_usuario_fk int not null,
    numero_telefonico varchar(20) not null unique,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk) on delete restrict
);

-- correos electronicos de cada usuario.
=======
    id_rol_fk int not null,        -- enlace con la tabla rol (cliente=2, admin=1, proveedor=4)
    estado_cuenta boolean not null default true, -- true = activo, false = suspendido (soft delete)
    foreign key (id_rol_fk) references rol(id_rol_pk)
);

-- catalogo de articulos a la venta.
-- estado soporta soft delete: false = producto oculto del catalogo pero conserva su historial de ventas.
create table producto(
    id_producto_pk int auto_increment primary key,
    id_categoria_fk int not null,              -- obligatorio: todo producto debe pertenecer a una categoria
    nombre_producto varchar(100) not null,
    descripcion text null,                     -- puede ser nulo si el proveedor no agrego descripcion aun
    precio decimal(10,2) not null,             -- formato monetario con 2 decimales (ej: 18000.00)
    stock int not null default 0,              -- unidades disponibles; se descuenta automaticamente al comprar
    estado boolean not null default true,      -- true = visible en catalogo, false = producto desactivado
    fecha_creacion timestamp default current_timestamp,
    foreign key (id_categoria_fk) references categoria(id_categoria_pk)
);

-- ==========================================================
-- 3. tablas detalladas e hijas (extensiones de datos de usuario)
-- estas tablas amplian la informacion de la tabla usuario.
-- ==========================================================

-- direcciones de envio de cada usuario.
-- un usuario puede tener multiples direcciones; solo una puede ser primaria.
create table direccion(
    id_direccion_pk int auto_increment primary key,
    id_usuario_fk int not null,
    direccion text not null,                               -- nombre de la calle, barrio y municipio
    direccion_detallada text,                              -- indicaciones adicionales (ej: "casa blanca, timbre 2")
    direccion_primario boolean not null default false,     -- true = esta es la direccion preferida del cliente
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- telefonos de contacto de cada usuario.
-- unique en numero_telefonico evita duplicados en todo el sistema.
create table telefono(
    id_telefono_pk int auto_increment primary key,
    id_usuario_fk int not null,
    numero_telefonico varchar(20) not null unique, -- un numero solo puede pertenecer a un usuario
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- correos electronicos de cada usuario.
-- unique en correo garantiza que no existan cuentas duplicadas.
-- correo_primario indica cual se usa para notificaciones principales.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table correo(
    id_correo_pk int auto_increment primary key,
    id_usuario_fk int not null,
    correo varchar(100) not null unique,          -- campo unico: no se permiten dos usuarios con el mismo correo
    correo_primario boolean not null default false,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk) on delete restrict
);

<<<<<<< HEAD
-- almacena la contrasena encriptada del usuario de forma binaria.
create table credenciales(
    id_usuario int primary key,
    passwd_encript varbinary(255) not null,
    foreign key (id_usuario) references usuario(id_usuario_pk) on delete restrict
);

-- ==========================================================
-- 3. nucleo de catalogo e inventario
-- ==========================================================

-- catalogo de articulos a la venta con relacion directa al proveedor.
create table producto(
    id_producto_pk int auto_increment primary key,
    id_categoria_fk int not null,
    id_proveedor_fk int not null, 
    nombre_producto varchar(100) not null,
    descripcion text null,
    precio decimal(10,2) not null,
    stock int not null default 0,
    estado boolean not null default true,
    fecha_creacion timestamp default current_timestamp,
    foreign key (id_categoria_fk) references categoria(id_categoria_pk) on delete restrict,
    foreign key (id_proveedor_fk) references proveedor(id_proveedor_pk) on delete restrict
);

-- galeria de imagenes de los productos.
create table imagenes(
    id_imagen_pk int auto_increment primary key,
    id_producto_fk int not null,
    url_ruta varchar(255) not null,
    imagen_principal boolean not null default false,
    foreign key (id_producto_fk) references producto(id_producto_pk) on delete restrict
);

-- tabla puente de muchos a muchos entre producto y etiqueta.
=======
-- almacena la contrasena encriptada del usuario.
-- id_usuario es a la vez llave primaria y foranea (relacion 1:1 estricta con usuario).
-- el tipo varbinary(255) guarda el resultado binario del cifrado AES/BCrypt.
create table credenciales(
    id_usuario int primary key,                     -- un usuario tiene exactamente una sola credencial
    passwd_encript varbinary(255) not null,         -- nunca se guarda texto plano, siempre binario encriptado
    foreign key (id_usuario) references usuario(id_usuario_pk)
);

-- datos adicionales exclusivos para usuarios con rol 'cliente'.
-- id_cliente_pk comparte el mismo id que la tabla usuario (relacion 1:1 herencia).
create table cliente(
    id_cliente_pk int primary key,             -- mismo id que el usuario base (herencia de tabla)
    referencia_ubicacion text null,            -- indicacion informal de llegada (ej: "casa rejas negras")
    foreign key (id_cliente_pk) references usuario(id_usuario_pk)
);

-- 1. La solicitud se crea primero con toda la información (Solo existe aquí)
create table solicitud_proveedor(
    id_solicitud_pk int auto_increment primary key,
    id_usuario_fk int not null,
    nit_solicitado varchar(20) not null,
    nombre_marca varchar(100) not null,
    cuenta_bancaria varchar(30),
    banco_nombre varchar(50),
    tipo_cuenta enum('Ahorros','Corriente'),
    estado_solicitud enum('pendiente', 'aprobada', 'rechazada') default 'pendiente',
    fecha_solicitud timestamp default current_timestamp,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- 2. El proveedor se crea después (Solo si se aprueba)
create table proveedor(
    nit_empresa varchar(20) not null primary key, -- PK sigue siendo el NIT
    id_solicitud_fk int not null unique,          -- FK obligatoria (NOT NULL) y única
    foreign key (id_solicitud_fk) references solicitud_proveedor(id_solicitud_pk)
);

-- galeria de imagenes de los productos (relacion 1:N con producto).
-- imagen_principal indica cual foto se muestra en la tarjeta del catalogo.
create table imagenes(
    id_imagen_pk int auto_increment primary key,
    id_producto_fk int not null,
    url_ruta varchar(255) not null,              -- ruta relativa desde la raiz del proyecto web
    imagen_principal boolean not null default false, -- true = esta imagen es el thumbnail del producto
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- tabla puente de muchos a muchos entre producto y etiqueta.
-- unique(id_producto, id_etiqueta) evita asignar la misma etiqueta dos veces.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table producto_etiqueta(
    id_producto_etiqueta_pk int auto_increment primary key,
    id_producto int not null,
    id_etiqueta int not null,
<<<<<<< HEAD
    foreign key (id_producto) references producto(id_producto_pk) on delete restrict,
    foreign key (id_etiqueta) references etiqueta(id_etiqueta_pk) on delete restrict,
    unique (id_producto, id_etiqueta)
);

-- ==========================================================
-- 4. tablas transaccionales (flujo de compras conectado)
-- ==========================================================

-- canasta de compras del cliente.
create table carrito(
    id_carrito_pk int auto_increment primary key,
    id_cliente_fk int not null,
    estado enum('Activo', 'Procesado') default 'Activo',
=======
    foreign key (id_producto) references producto(id_producto_pk),
    foreign key (id_etiqueta) references etiqueta(id_etiqueta_pk),
    unique (id_producto, id_etiqueta)           -- restriccion: evita etiquetas duplicadas en un producto
);

-- tabla puente de muchos a muchos entre proveedor y producto.
-- un proveedor puede surtir varios productos, y un producto puede tener varios proveedores.
create table proveedor_producto(
    id_proveedor_producto_pk int auto_increment primary key,
    id_proveedor_fk varchar(20) not null,
    id_producto_fk int not null,
    foreign key (id_proveedor_fk) references proveedor(nit_empresa),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- ==========================================================
-- 4. tablas transaccionales (flujo de compras conectado)
-- contienen el nucleo financiero y logistico del sistema.
-- ==========================================================

-- canasta de compras del cliente.
-- funciona como un historial ciclico: cada compra crea un carrito nuevo.
-- estado 'Procesado' indica que ya se genero el pedido a partir de este carrito.
create table carrito(
    id_carrito_pk int auto_increment primary key,
    id_cliente_fk int not null,
    estado enum('Activo', 'Procesado') default 'Activo', -- 'Activo' = el cliente todavia esta comprando
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
    fecha_actualizacion timestamp default current_timestamp on update current_timestamp,
    foreign key (id_cliente_fk) references cliente(id_cliente_pk) on delete restrict
);

<<<<<<< HEAD
-- lineas de producto dentro del carrito para compras parciales.
=======
-- lineas de producto dentro del carrito.
-- seleccionado permite que el cliente haga compras parciales (marcar/desmarcar items).
-- unique(id_carrito_fk, id_producto_fk) evita duplicar el mismo producto en un carrito.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table detalle_carrito(
    id_detalle_carrito int auto_increment primary key,
    id_carrito_fk int not null,
    id_producto_fk int not null,
<<<<<<< HEAD
    cantidad int not null check (cantidad > 0),
    seleccionado boolean not null default true,
    foreign key (id_carrito_fk) references carrito(id_carrito_pk) on delete restrict,
    foreign key (id_producto_fk) references producto(id_producto_pk) on delete restrict,
    unique(id_carrito_fk, id_producto_fk)
);

-- cabecera del pedido (conexion fisica y directa con el carrito y direccion).
create table pedido(
    id_pedido_pk int auto_increment primary key,
    id_carrito_fk int not null,
    id_direccion_fk int not null,
    fecha timestamp default current_timestamp,
    total_pagar decimal(10,2) not null,
    estado_pedido enum('Pendiente', 'Preparando', 'En Camino', 'Entregado', 'Cancelado') default 'Pendiente',
    foreign key (id_carrito_fk) references carrito(id_carrito_pk) on delete restrict,
    foreign key (id_direccion_fk) references direccion(id_direccion_pk) on delete restrict
);

-- registro inmutable de cada producto dentro de la compra (precios congelados).
=======
    cantidad int not null check (cantidad > 0), -- restriccion: no se puede agregar 0 unidades
    seleccionado boolean not null default true, -- para compras parciales (el cliente puede deseleccionar)
    foreign key (id_carrito_fk) references carrito(id_carrito_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk),
    unique(id_carrito_fk, id_producto_fk)       -- restriccion: un producto solo puede aparecer una vez por carrito
);

-- ============================================================
-- DECISION DE DISEÑO IMPORTANTE: cabecera del pedido
-- ============================================================
-- se REMOVIO la columna id_usuario_fk de esta tabla intencionalmente.
-- motivo: el cliente ya esta identificado de forma INDIRECTA a traves de la cadena:
--   pedido -> id_carrito_fk -> carrito -> id_cliente_fk -> cliente -> usuario
-- mantener un id_usuario_fk directo en pedido seria informacion REDUNDANTE y generaria
-- inconsistencias si el mismo usuario tuviera varios carritos activos.
-- para cualquier consulta que necesite saber quien compro, se hace JOIN via carrito.
-- ============================================================
create table pedido(
    id_pedido_pk int auto_increment primary key,
    id_carrito_fk int not null,     -- enlace con el carrito que origino la compra (NO NULL obligatorio)
    id_direccion_fk int not null,   -- direccion de entrega capturada al momento de la compra (NO NULL obligatorio)
    fecha timestamp default current_timestamp,
    total_pagar decimal(10,2) not null,   -- suma total de todos los productos (calculado en java, no en js)
    estado_pedido enum(
        'Pendiente',              -- el pedido acaba de crearse, nadie lo ha visto aun
        'Preparando',             -- el proveedor confirmo y esta alistando el paquete
        'En Camino',              -- el paquete fue enviado al domicilio del cliente
        'Entregado',              -- el cliente recibio el paquete fisicamente
        'Cancelado_por_Proveedor',-- el proveedor no pudo surtir el pedido
        'Cancelado_por_Cliente'   -- el cliente cancelo antes de que se preparara
    ) default 'Pendiente',
    motivo_cancelacion text null,       -- texto opcional con la razon de la cancelacion
    cancelado_por_id_fk int null,       -- auditoria: id del usuario que ejecuto la cancelacion
    foreign key (id_carrito_fk) references carrito(id_carrito_pk),
    foreign key (id_direccion_fk) references direccion(id_direccion_pk),
    foreign key (cancelado_por_id_fk) references usuario(id_usuario_pk)
);

-- registro inmutable de cada producto dentro de la compra.
-- precio_unitario y subtotal se capturan al momento exacto de la compra.
-- esto asegura que si el proveedor cambia el precio manana, la factura historica quede intacta.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table detalle_pedido(
    id_detalle_pedido int auto_increment primary key,
    id_pedido_fk int not null,
    id_producto_fk int not null,
    cantidad int not null,
<<<<<<< HEAD
    precio_unitario decimal(10,2) not null,
    subtotal decimal(10,2) not null,
    foreign key (id_pedido_fk) references pedido(id_pedido_pk) on delete restrict,
    foreign key (id_producto_fk) references producto(id_producto_pk) on delete restrict
);

-- historial inmutable del flujo logistico.
create table historial_pedido(
    id_historial_pk int auto_increment primary key,
    id_pedido_fk int not null,
    estado_anterior varchar(50) null,
    estado_nuevo varchar(50) not null,
    motivo_cambio text null,
    fecha_registro timestamp default current_timestamp,
    foreign key (id_pedido_fk) references pedido(id_pedido_pk) on delete restrict
);

-- comprobante financiero del pago asociado a un pedido.
=======
    precio_unitario decimal(10,2) not null, -- precio congelado en el momento de la venta
    subtotal decimal(10,2) not null,         -- cantidad * precio_unitario, calculado en java
    foreign key (id_pedido_fk) references pedido(id_pedido_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- comprobante financiero del pago asociado a un pedido.
-- referencia_transaccion guarda el numero de cuenta o celular del comprobante.
-- comision_dmari: porcentaje que retiene la plataforma (actualmente 5%).
-- monto_total: lo que realmente recibe el proveedor despues de la comision.
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
create table pago(
    id_pago_pk int auto_increment primary key,
    id_pedido_fk int not null,
    id_metodo_pago_fk int not null,
    referencia_transaccion varchar(100) null,  -- numero de celular nequi, voucher tarjeta, etc.
    fecha_pago timestamp default current_timestamp,
    comision_dmari decimal(10,2) not null,     -- 5% del total que cobra la plataforma
    monto_total decimal(10,2) not null,        -- total - comision = lo que recibe el proveedor
    estado_pago enum('Pendiente','Aprobado','Rechazado') default 'Pendiente',
<<<<<<< HEAD
    foreign key (id_pedido_fk) references pedido(id_pedido_pk) on delete restrict,
    foreign key (id_metodo_pago_fk) references metodo_pago(id_metodo_pago_pk) on delete restrict
);
=======
    foreign key (id_pedido_fk) references pedido(id_pedido_pk),
    foreign key (id_metodo_pago_fk) references metodo_pago(id_metodo_pago_pk)
);


-- ==========================================================
-- 5. tablas de solicitudes internas
-- ==========================================================

-- solicitudes de nuevas categorias sugeridas por proveedores al administrador.
-- el admin decide si aprobarla, rechazarla o dejarla pendiente.
create table solicitud_categoria (
    id_solicitud_pk int auto_increment primary key,
    id_proveedor_fk varchar(20) not null,
    nombre_sugerido varchar(100) not null,
    justificacion text not null,
    estado_solicitud enum('pendiente', 'aprobada', 'rechazada') default 'pendiente',
    fecha_creacion timestamp default current_timestamp,
    foreign key (id_proveedor_fk) references proveedor(nit_empresa)
);
>>>>>>> 55262a54dd65e5c0d48af12dd604c876983cff90
