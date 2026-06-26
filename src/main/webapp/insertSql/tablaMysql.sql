-- =================================================================
-- archivo: tablamysql.sql
-- proposito: definicion completa del esquema de la base de datos dmari.
-- cada tabla esta ordenada segun sus dependencias (primero las maestras,
-- luego las hijas) para que mysql no rompa las restricciones de clave foranea.
-- estrategia: on delete restrict fuerza el uso de soft delete (estado).
-- =================================================================

create database if not exists DMari;
use DMari;

-- ==========================================================
-- 1. tablas de catalogos y roles (tablas maestras)
-- estas tablas no dependen de nadie, se crean primero.
-- ==========================================================

-- define los tipos de cuenta del sistema: cliente, administrador o proveedor.
create table rol(
    id_rol_pk int auto_increment primary key,
    tipo_rol enum('cliente','administrador','proveedor') not null default 'cliente'
);

-- categorias a las que pertenecen los productos del catalogo.
create table categoria(
    id_categoria_pk int auto_increment primary key,
    nombre varchar(50) not null,
    descripcion varchar(100),
    estado_activo boolean not null default true
);

-- etiquetas de busqueda y filtrado para los productos.
create table etiqueta(
    id_etiqueta_pk int auto_increment primary key,
    nombre_etiqueta varchar(30) not null
);

-- metodos de pago disponibles para los clientes.
create table metodo_pago(
    id_metodo_pago_pk int auto_increment primary key,
    descripcion_pago varchar(255) not null,
    estado_activo boolean not null default true
);

-- ==========================================================
-- 2. tablas nucleo principales y extensiones de usuario
-- ==========================================================

-- tabla central de personas del sistema.
-- estado_cuenta soporta soft delete: false = usuario desactivado.
create table usuario(
    id_usuario_pk int auto_increment primary key,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
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
create table correo(
    id_correo_pk int auto_increment primary key,
    id_usuario_fk int not null,
    correo varchar(100) not null unique,
    correo_primario boolean not null default false,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk) on delete restrict
);

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
create table producto_etiqueta(
    id_producto_etiqueta_pk int auto_increment primary key,
    id_producto int not null,
    id_etiqueta int not null,
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
    fecha_actualizacion timestamp default current_timestamp on update current_timestamp,
    foreign key (id_cliente_fk) references cliente(id_cliente_pk) on delete restrict
);

-- lineas de producto dentro del carrito para compras parciales.
create table detalle_carrito(
    id_detalle_carrito int auto_increment primary key,
    id_carrito_fk int not null,
    id_producto_fk int not null,
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
create table detalle_pedido(
    id_detalle_pedido int auto_increment primary key,
    id_pedido_fk int not null,
    id_producto_fk int not null,
    cantidad int not null,
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
create table pago(
    id_pago_pk int auto_increment primary key,
    id_pedido_fk int not null,
    id_metodo_pago_fk int not null,
    referencia_transaccion varchar(100) null,
    fecha_pago timestamp default current_timestamp,
    comision_dmari decimal(10,2) not null,
    monto_total decimal(10,2) not null,
    estado_pago enum('Pendiente','Aprobado','Rechazado') default 'Pendiente',
    foreign key (id_pedido_fk) references pedido(id_pedido_pk) on delete restrict,
    foreign key (id_metodo_pago_fk) references metodo_pago(id_metodo_pago_pk) on delete restrict
);