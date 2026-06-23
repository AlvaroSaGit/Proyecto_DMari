create database if not exists DMari;
use DMari;

-- ==========================================================
-- 1. TABLAS DE CATÁLOGOS Y ROLES (Tablas Maestras)
-- ==========================================================

-- Roles del sistema
create table rol(
    id_rol_pk int auto_increment primary key,
    tipo_rol enum('cliente','administrador','proveedor') not null default 'cliente'
);

-- Categorías de los productos
create table categoria(
    id_categoria_pk int auto_increment primary key,
    nombre varchar(50) not null,
    descripcion varchar(100),
    estado_activo boolean not null default true
);

-- Etiquetas de búsqueda para los productos
create table etiqueta(
    id_etiqueta_pk int auto_increment primary key,
    nombre_etiqueta varchar(30) not null
);

-- Métodos de pago
create table metodo_pago(
    id_metodo_pago_pk int auto_increment primary key,
    descripcion_pago varchar(255) not null,
    estado_activo boolean not null default true
);

-- ==========================================================
-- 2. TABLAS NÚCLEO PRINCIPALES
-- ==========================================================

-- Usuarios generales (Soporta Soft Delete con estado_cuenta)
create table usuario(
    id_usuario_pk int auto_increment primary key,
    nombre varchar(50) not null,
    apellido varchar(50) not null,
    id_rol_fk int not null,
    estado_cuenta boolean not null default true,
    foreign key (id_rol_fk) references rol(id_rol_pk)
);

-- Productos del catálogo (Soporta Soft Delete con el campo estado)
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
-- 3. TABLAS DETALLADAS E HIJAS (Extensiones de datos)
-- ==========================================================

-- Direcciones asociadas a los usuarios
create table direccion(
    id_direccion_pk int auto_increment primary key,
    id_usuario_fk int not null,
    direccion text not null,
    direccion_detallada text,
    direccion_primario boolean not null default false,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- Teléfonos asociados a los usuarios
create table telefono(
    id_telefono_pk int auto_increment primary key,
    id_usuario_fk int not null,
    numero_telefonico varchar(20) not null unique,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- Correos electrónicos asociados a los usuarios
create table correo(
    id_correo_pk int auto_increment primary key,
    id_usuario_fk int not null,
    correo varchar(100) not null unique,
    correo_primario boolean not null default false,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- Credenciales de acceso
create table credenciales(
    id_usuario int primary key,
    passwd_encript varbinary(255) not null,
    foreign key (id_usuario) references usuario(id_usuario_pk)
);

-- Datos específicos si el usuario es un proveedor
create table proveedor(
    id_proveedor_pk int primary key,
    nit_empresa varchar(20) not null,
    nombre_marca varchar(100) not null,
    cuenta_bancaria varchar(30),
    banco_nombre varchar(50),
    tipo_cuenta enum('Ahorros','Corriente'),
    foreign key (id_proveedor_pk) references usuario(id_usuario_pk)
);

-- Datos específicos si el usuario es un cliente
create table cliente(
    id_cliente_pk int primary key,
    referencia_ubicacion text null,
    foreign key (id_cliente_pk) references usuario(id_usuario_pk)
);

-- solicitudes para ascenso a proveedor
create table solicitud_proveedor(
    id_solicitud_pk int auto_increment primary key,
    id_usuario_fk int not null,
    nit_empresa varchar(20) not null,
    nombre_marca varchar(100) not null,
    cuenta_bancaria varchar(30),
    banco_nombre varchar(50),
    tipo_cuenta varchar(20),
    estado_solicitud enum('pendiente', 'aprobada', 'rechazada') default 'pendiente',
    fecha_solicitud timestamp default current_timestamp,
    foreign key (id_usuario_fk) references usuario(id_usuario_pk)
);

-- Galería de imágenes de los productos
create table imagenes(
    id_imagen_pk int auto_increment primary key,
    id_producto_fk int not null,
    url_ruta varchar(255) not null,
    imagen_principal boolean not null default false,
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- Relación de muchos a muchos: Productos y sus Etiquetas
create table producto_etiqueta(
    id_producto_etiqueta_pk int auto_increment primary key,
    id_producto int not null,
    id_etiqueta int not null,
    foreign key (id_producto) references producto(id_producto_pk),
    foreign key (id_etiqueta) references etiqueta(id_etiqueta_pk),
    unique (id_producto, id_etiqueta)
);

-- Relación de muchos a muchos: Proveedores y los Productos que surten
create table proveedor_producto(
    id_proveedor_producto_pk int auto_increment primary key,
    id_proveedor_fk int not null,
    id_producto_fk int not null,
    foreign key (id_proveedor_fk) references proveedor(id_proveedor_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- ==========================================================
-- 4. TABLAS TRANSACCIONALES (Flujo de Compras Conectado)
-- ==========================================================

-- Carrito de compras (Historial cíclico basado en estados)
create table carrito(
    id_carrito_pk int auto_increment primary key,
    id_cliente_fk int not null,
    estado enum('Activo', 'Procesado') default 'Activo', -- Controla si ya cerró la compra o sigue abierto
    fecha_actualizacion timestamp default current_timestamp on update current_timestamp,
    foreign key (id_cliente_fk) references cliente(id_cliente_pk)
);

-- Productos dentro del carrito
create table detalle_carrito(
    id_detalle_carrito int auto_increment primary key,
    id_carrito_fk int not null,
    id_producto_fk int not null,
    cantidad int not null check (cantidad > 0),
    seleccionado boolean not null default true, -- Para compras parciales
    foreign key (id_carrito_fk) references carrito(id_carrito_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk),
    unique(id_carrito_fk, id_producto_fk)
);

-- Cabecera del Pedido (Conexión física y directa con el carrito de origen)
create table pedido(
    id_pedido_pk int auto_increment primary key,
    id_carrito_fk int not null, -- ¡CONEXIÓN EXPLICÍTADA!
    id_direccion_fk int not null,
    fecha timestamp default current_timestamp,
    total_pagar decimal(10,2) not null,
    estado_pedido enum('Pendiente', 'Preparando', 'En Camino', 'Entregado', 'Cancelado_por_Proveedor', 'Cancelado_por_Cliente', 'Reembolso_Solicitado', 'Devuelto') default 'Pendiente',
    motivo_cancelacion text null,
    cancelado_por_id_fk int null,
    foreign key (id_carrito_fk) references carrito(id_carrito_pk), -- Restriccion de integridad referencial
    foreign key (id_direccion_fk) references direccion(id_direccion_pk),
    foreign key (cancelado_por_id_fk) references usuario(id_usuario_pk)
);

-- Detalle del Pedido (Copia de seguridad inmutable de los precios de venta)
create table detalle_pedido(
    id_detalle_pedido int auto_increment primary key,
    id_pedido_fk int not null,
    id_producto_fk int not null,
    cantidad int not null,
    precio_unitario decimal(10,2) not null,
    subtotal decimal(10,2) not null,
    foreign key (id_pedido_fk) references pedido(id_pedido_pk),
    foreign key (id_producto_fk) references producto(id_producto_pk)
);

-- Registro financiero del pago asociado al pedido
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
-- 5. tablas de solicitudes 
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
