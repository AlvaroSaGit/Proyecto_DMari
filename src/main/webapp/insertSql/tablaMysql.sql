
use DMari;

-- ==========================================================
-- 1. TABLAS MAESTRAS INDEPENDIENTES
-- ==========================================================

-- tabla rol (Ahora incluye explicitamente al proveedor en el ENUM)
create table rol(
    id_rol_pk int auto_increment primary key,
    tipo_rol enum('cliente','repartidor','administrador','proveedor') not null default 'cliente'
);

-- tabla permisos
create table permiso(
    id_permiso_pk int auto_increment primary key,
    nombre_permiso varchar(100) not null unique
);

-- tabla de categoria
create table categoria(
    id_categoria_pk int auto_increment primary key,
    nombre varchar(50),
    descripcion varchar(100),
    estado_activo boolean
);

-- tabla etiqueta
create table etiqueta(
    id_etiqueta_pk int auto_increment primary key,
    nombre_etiqueta varchar(30)
);

-- tabla metodo pago
create table metodo_pago(
    id_metodo_pago_pk int auto_increment primary key,
    descripcion_pago varchar(255),
    estado_activo boolean
);

-- ==========================================================
-- 2. TABLAS PRINCIPALES NUCLEO
-- ==========================================================

-- tabla rol permisos conecta rol y permiso
create table rol_permiso(
    id_rol_permiso_pk int auto_increment primary key,
    id_rol_fk int,
    id_permiso_fk int,
    foreign key (id_rol_fk) references rol(id_rol_pk),
    foreign key (id_permiso_fk) references permiso(id_permiso_pk)
);

-- tabla de usuarios para todos los roles
create table usuario(
    id_usuario_pk int auto_increment primary key,
    nombre varchar(50),
    apellido varchar(50),
    id_rol_fk int,
    estado_cuenta boolean,
    foreign key (id_rol_fk) references rol(id_rol_pk)
);

-- tabla producto
create table producto(
    id_producto_pk int auto_increment primary key,
    id_categoria_fk int,
    nombre_producto varchar(100) not null,
    descripcion text null,
    precio decimal(10,2) not null,
    stock int default 0,
    estado boolean,
    fecha_creacion timestamp default current_timestamp,
    foreign key (id_categoria_fk) references categoria(id_categoria_pk)
);

-- ==========================================================
-- 3. TABLAS SATELITE E HIJAS (CONEXION DIRECTA A USUARIO Y PRODUCTO)
-- ==========================================================

-- direccion tabla
create table direccion(
    id_direccion_pk int auto_increment primary key,
    id_usuario_fk int,
    direccion text not null,
    direccion_detallada text,
    direccion_primario boolean,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- telefono de la tabla usuario
create table telefono(
    id_telefono_pk int auto_increment primary key,
    id_usuario_fk int,
    numero_telefonico varchar(20) not null unique,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- correo de la tabla usuario
create table correo(
    id_correo_pk int auto_increment primary key,
    id_usuario_fk int,
    correo varchar(100) not null unique,
    correo_primario boolean,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- tabla notificaciones
create table notificacion(
    id_notificacion_pk int auto_increment primary key,
    id_receptor_fk int,
    id_emisor_fk int,
    titulo varchar(50) not null,
    mensaje text not null,
    estado_leido boolean default false,
    fecha_envio timestamp default current_timestamp,
    foreign key (id_emisor_fk) references usuario(id_usuario_pk),
    foreign key (id_receptor_fk) references usuario(id_usuario_pk)
);

-- tabla credenciales
create table credenciales(
    id_usuario int primary key,
    passwd_encript blob,
    foreign key (id_usuario) references usuario(id_usuario_pk)
);

-- tabla proveedor unificada (Maneja relacion 1:1 limpia con usuario)
create table proveedor(
    id_proveedor_pk int primary key,
    nit_empresa varchar(20),
    nombre_marca varchar(100),
    cuenta_bancaria varchar(30),
    banco_nombre varchar(50),
    tipo_cuenta enum('Ahorros','Corriente'),
    foreign key (id_proveedor_pk) references usuario(id_usuario_pk)
);

-- tabla repartidor unificada (Elimina la duplicidad y reune los datos del vehiculo)
create table repartidor(
    id_repartidor_pk int primary key,
    placa_vehiculo varchar(10),
    tipo_vehiculo enum('Moto','Carro','Bicicleta'),
    modelo_vehiculo varchar(50),
    licencia varchar(20),
    foreign key (id_repartidor_pk) references usuario(id_usuario_pk)
);

-- tabla cliente
create table cliente(
    id_cliente_pk int primary key,
    direccion_envio varchar(255) not null,
    telefono_secundario varchar(20),
    referencia_ubicacion text,
    foreign key (id_cliente_pk) references usuario(id_usuario_pk)
);

-- tabla imagenes
create table imagenes(
    id_imagen_pk int auto_increment primary key,
    id_producto_fk int,
    url_ruta varchar(255),
    imagen_principal boolean,
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- tabla producto etiqueta
create table producto_etiqueta(
    id_producto int,
    id_etiqueta int,
    foreign key (id_producto) references producto(id_producto_pk),
    foreign key (id_etiqueta) references etiqueta(id_etiqueta_pk)
);

-- tabla proveedor producto (Conectada a la nueva estructura de proveedor unificada)
create table proveedor_producto(
    id_proveedor_producto_pk int auto_increment primary key,
    id_proveedor_fk int,
    id_producto_fk int,
    foreign key (id_proveedor_fk) references proveedor(id_proveedor_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- ==========================================================
-- 4. TABLAS TRANSACCIONALES (VENTAS, CARRITO Y LOGISTICA)
-- ==========================================================

-- tabla carrito
create table carrito(
    id_carrito_pk int auto_increment primary key,
    id_cliente_fk int,
    foreign key (id_cliente_fk) references cliente(id_cliente_pk)
);

-- tabla detalle carrito
create table detalle_carrito(
    id_detalle_carrito int auto_increment primary key,
    id_carrito_fk int,
    id_producto_fk int,
    cantidad int not null,
    foreign key (id_carrito_fk) references carrito(id_carrito_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- tabla pedido (Apunta directamente a las tablas hijas cliente y repartidor)
create table pedido(
    id_pedido_pk int auto_increment primary key,
    id_cliente_fk int,
    id_repartidor_fk int,
    fecha timestamp default current_timestamp,
    total_pagar decimal(10,2) not null,
    estado_pedido enum('Pendiente', 'Preparando', 'En Camino', 'Entregado', 'Cancelado') default 'Pendiente',
    foreign key (id_cliente_fk) references cliente(id_cliente_pk),
    foreign key (id_repartidor_fk) references repartidor(id_repartidor_pk)
);

-- tabla detalle pedido
create table detalle_pedido(
    id_detalle_pedido int auto_increment primary key,
    id_producto_fk int,
    id_pedido_fk int,
    cantidad int not null,
    precio_unitario decimal(10,2) not null,
    subtotal decimal(10,2) not null,
    foreign key (id_producto_fk) references producto(id_producto_pk),
    foreign key (id_pedido_fk) references pedido(id_pedido_pk)
);

-- tabla pago
create table pago(
    id_pago_pk int auto_increment primary key,
    id_pedido_fk int,
    id_metodo_pago_fk int,
    numero_cuenta_ahorro varchar(30) not null,
    fecha_pago timestamp default current_timestamp,
    comision_dmari decimal(10,2) not null,
    monto_total decimal(10,2) not null,
    estado_activo boolean,
    estado_pago enum('Pendiente','Aprobado','Rechazado') default 'Pendiente',
    foreign key (id_pedido_fk) references pedido(id_pedido_pk),
    foreign key (id_metodo_pago_fk) references metodo_pago(id_metodo_pago_pk)
);
