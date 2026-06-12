create database if not exists DMari;
use DMari;

-- ==========================================================
-- 1. tablas de catalogos y roles
-- ==========================================================

-- roles del sistema
create table rol(
    id_rol_pk int auto_increment primary key,
    tipo_rol enum('cliente','administrador','proveedor') not null default 'cliente'
);

-- categorias de los productos
create table categoria(
    id_categoria_pk int auto_increment primary key,
    nombre varchar(50) not null,
    descripcion varchar(100),
    estado_activo boolean not null default true
);

-- etiquetas de busqueda para los productos
create table etiqueta(
    id_etiqueta_pk int auto_increment primary key,
    nombre_etiqueta varchar(30) not null
);

-- metodos de pago
create table metodo_pago(
    id_metodo_pago_pk int auto_increment primary key,
    descripcion_pago varchar(255) not null,
    estado_activo boolean not null default true
);

-- ==========================================================
-- 2. tablas nucleo principales
-- ==========================================================

-- usuarios generales con soporte para borrado logico
create table usuario(
    id_usuario_pk int auto_increment primary key,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
    id_rol_fk int not null,
    estado_cuenta boolean not null default true,
    foreign key (id_rol_fk) references rol(id_rol_pk)
);

-- productos del catalogo general
create table producto(
    id_producto_pk int auto_increment primary key,
    id_categoria_fk int not null,
    nombre_producto varchar(100) not null,
    descripcion text null,
    precio decimal(10,2) not null,
    stock int not null default 0,
    estado boolean not null default true,
    fecha_creacion timestamp default current_timestamp,
    foreign key (id_categoria_fk) references categoria(id_categoria_pk)
);

-- ==========================================================
-- 3. tablas detalladas e hijas
-- ==========================================================

-- direcciones asociadas a los usuarios
create table direccion(
    id_direccion_pk int auto_increment primary key,
    id_usuario_fk int not null,
    direccion text not null,
    direccion_detallada text,
    direccion_primario boolean not null default false,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- telefonos asociados a los usuarios
create table telefono(
    id_telefono_pk int auto_increment primary key,
    id_usuario_fk int not null,
    numero_telefonico varchar(20) not null unique,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- correos electronicos asociados a los usuarios
create table correo(
    id_correo_pk int auto_increment primary key,
    id_usuario_fk int not null,
    correo varchar(100) not null unique,
    correo_primario boolean not null default false,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- credenciales de acceso con formato varbinary para encriptacion
create table credenciales(
    id_usuario int primary key,
    passwd_encript varbinary(255) not null,
    foreign key (id_usuario) references usuario(id_usuario_pk)
);

-- informacion especifica de los proveedores
create table proveedor(
    id_proveedor_pk int primary key,
    nit_empresa varchar(20) not null,
    nombre_marca varchar(100) not null,
    cuenta_bancaria varchar(30),
    banco_nombre varchar(50),
    tipo_cuenta enum('Ahorros','Corriente'),
    foreign key (id_proveedor_pk) references usuario(id_usuario_pk)
);

-- informacion especifica de los clientes
create table cliente(
    id_cliente_pk int primary key,
    referencia_ubicacion text null,
    foreign key (id_cliente_pk) references usuario(id_usuario_pk)
);

-- galeria de imagenes de los productos
create table imagenes(
    id_imagen_pk int auto_increment primary key,
    id_producto_fk int not null,
    url_ruta varchar(255) not null,
    imagen_principal boolean not null default false,
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- relacion entre productos y etiquetas
create table producto_etiqueta(
    id_producto_etiqueta_pk int auto_increment primary key,
    id_producto int not null,
    id_etiqueta int not null,
    foreign key (id_producto) references producto(id_producto_pk),
    foreign key (id_etiqueta) references etiqueta(id_etiqueta_pk),
    unique (id_producto, id_etiqueta)
);

-- relacion entre proveedores y los productos que surten
create table proveedor_producto(
    id_proveedor_producto_pk int auto_increment primary key,
    id_proveedor_fk int not null,
    id_producto_fk int not null,
    foreign key (id_proveedor_fk) references proveedor(id_proveedor_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- ==========================================================
-- 4. tablas transaccionales del flujo de compras
-- ==========================================================

-- contenedor inicial asociado al cliente
create table carrito(
    id_carrito_pk int auto_increment primary key,
    id_cliente_fk int not null,
    estado enum('Activo', 'Procesado') default 'Activo',
    fecha_creacion timestamp default current_timestamp,
    fecha_actualizacion timestamp default current_timestamp on update current_timestamp,
    foreign key (id_cliente_fk) references cliente(id_cliente_pk)
);

-- productos en espera dentro del carrito
create table detalle_carrito(
    id_detalle_carrito int auto_increment primary key,
    id_carrito_fk int not null,
    id_producto_fk int not null,
    cantidad int not null check (cantidad > 0),
    seleccionado boolean not null default false,
    foreign key (id_carrito_fk) references carrito(id_carrito_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk),
    unique(id_carrito_fk, id_producto_fk)
);

-- historial inmutable de lo que el cliente decidio comprar
create table productos_confirmados(
    id_confirmado_pk int auto_increment primary key,
    id_carrito_fk int not null,
    id_producto_fk int not null,
    cantidad int not null,
    fecha_confirmacion timestamp default current_timestamp,
    foreign key (id_carrito_fk) references carrito(id_carrito_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- cabecera logistica y financiera de la compra
create table pedido(
    id_pedido_pk int auto_increment primary key,
    id_cliente_fk int not null,
    id_direccion_fk int not null,
    fecha timestamp default current_timestamp,
    total_pagar decimal(10,2) not null,
    estado_pedido enum('Pendiente', 'Preparando', 'En Camino', 'Entregado', 'Cancelado_por_Proveedor', 'Cancelado_por_Cliente', 'Reembolso_Solicitado', 'Devuelto') default 'Pendiente',
    motivo_cancelacion text null,
    cancelado_por_id_fk int null,
    foreign key (id_cliente_fk) references cliente(id_cliente_pk),
    foreign key (id_direccion_fk) references direccion(id_direccion_pk),
    foreign key (cancelado_por_id_fk) references usuario(id_usuario_pk)
);

-- conector final entre el pedido global y los productos confirmados
create table detalle_pedido(
    id_detalle_pedido int auto_increment primary key,
    id_pedido_fk int not null,
    id_confirmado_fk int not null,
    precio_unitario decimal(10,2) not null,
    subtotal decimal(10,2) not null,
    foreign key (id_pedido_fk) references pedido(id_pedido_pk),
    foreign key (id_confirmado_fk) references productos_confirmados(id_confirmado_pk)
);

-- registro financiero del pago
create table pago(
    id_pago_pk int auto_increment primary key,
    id_pedido_fk int not null,
    id_metodo_pago_fk int not null,
    referencia_transaccion varchar(100) null,
    fecha_pago timestamp default current_timestamp,
    comision_dmari decimal(10,2) not null,
    monto_total decimal(10,2) not null,
    estado_pago enum('Pendiente','Aprobado','Rechazado') default 'Pendiente',
    foreign key (id_pedido_fk) references pedido(id_pedido_pk),
    foreign key (id_metodo_pago_fk) references metodo_pago(id_metodo_pago_pk)
);

-- ==========================================================
-- 5. tablas de solicitudes y devoluciones
-- ==========================================================

-- solicitudes de categorias sugeridas por proveedores
create table solicitud_categoria (
    id_solicitud_pk int auto_increment primary key,
    id_proveedor_fk int not null,
    nombre_sugerido varchar(100) not null,
    justificacion text not null,
    estado_solicitud enum('pendiente', 'aprobada', 'rechazada') default 'pendiente',
    fecha_creacion timestamp default current_timestamp,
    foreign key (id_proveedor_fk) references proveedor(id_proveedor_pk)
);

-- solicitudes de nuevos proveedores
create table solicitud_proveedor (
    id_solicitud_pk int auto_increment primary key,
    id_usuario_fk int not null,
    nit_empresa varchar(20) not null,
    nombre_marca varchar(100) not null,
    cuenta_bancaria varchar(30) not null,
    banco_nombre varchar(50) not null,
    tipo_cuenta enum('ahorros','corriente') not null,
    estado_solicitud enum('pendiente', 'aprobada', 'rechazada') default 'pendiente',
    fecha_creacion timestamp default current_timestamp,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- gestion de solicitudes de devolucion de pedidos entregados
create table devolucion (
    id_devolucion_pk int auto_increment primary key,
    id_pedido_fk int not null,
    id_cliente_fk int not null,
    motivo text not null,
    estado_devolucion enum('solicitada', 'aprobada', 'rechazada') default 'solicitada',
    fecha_solicitud timestamp default current_timestamp,
    fecha_resolucion timestamp null,
    foreign key (id_pedido_fk) references pedido(id_pedido_pk),
    foreign key (id_cliente_fk) references cliente(id_cliente_pk)
);