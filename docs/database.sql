-- =============================================
-- SECCIÓN 1: DDL (estructura de la base de datos)
-- =============================================

-- *** Script Proyecto ERP Frescura Total **

-- Script de Creación de Base de Datos: erp_frescura_total
-- El estudiante puede agregar elementos a la base de datos si considera que hace falta la modelo actual


SET NAMES utf8mb4;

DROP DATABASE IF EXISTS erp_frescura_total;
CREATE DATABASE erp_frescura_total CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_frescura_total;

-- TABLAS DE CATÁLOGO (LOOKUP TABLES)

CREATE TABLE estados_lote (
id_estado INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(50) NOT NULL UNIQUE,
descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE tipos_cliente (
id_tipo INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(50) NOT NULL UNIQUE,
descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE estados_venta (
id_estado INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(50) NOT NULL UNIQUE,
descripcion TEXT
) ENGINE=InnoDB;

-- INFORMACIÓN DE LA EMPRESA

CREATE TABLE informacion_empresa (
id_empresa INT AUTO_INCREMENT PRIMARY KEY,
nombre_empresa VARCHAR(255) NOT NULL,
nit VARCHAR(20) NOT NULL UNIQUE,
direccion TEXT NOT NULL,
ciudad VARCHAR(100),
telefono VARCHAR(20),
email VARCHAR(100)
) ENGINE=InnoDB;

-- MÓDULO: PROVEEDORES

CREATE TABLE proveedores (
id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(255) NOT NULL,
nit VARCHAR(20) NOT NULL UNIQUE,
persona_contacto VARCHAR(100),
email VARCHAR(100) NOT NULL,
telefono VARCHAR(20),
direccion TEXT,
activo BOOLEAN DEFAULT TRUE,
fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- MÓDULO: PRODUCTOS

CREATE TABLE categorias_producto (
id_categoria INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL UNIQUE,
descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE productos (
id_producto INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(255) NOT NULL,
descripcion TEXT,
id_categoria INT NOT NULL,
unidad_medida VARCHAR(20) NOT NULL COMMENT 'Ej: kg, litro, unidad',
precio_base DECIMAL(10, 2),
FOREIGN KEY (id_categoria) REFERENCES categorias_producto(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE productos_proveedor (
id_proveedor INT NOT NULL,
id_producto INT NOT NULL,
PRIMARY KEY (id_proveedor, id_producto),
FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor) ON DELETE CASCADE,
FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
) ENGINE=InnoDB;

-- MÓDULO: INVENTARIO Y CALIDAD

CREATE TABLE lotes_inventario (
id_lote INT AUTO_INCREMENT PRIMARY KEY,
numero_lote VARCHAR(50) NOT NULL UNIQUE,
id_producto INT NOT NULL,
id_proveedor INT NOT NULL,
fecha_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_vencimiento DATE NOT NULL,
cantidad_inicial DECIMAL(10, 2) NOT NULL,
cantidad DECIMAL(10, 2) NOT NULL,
precio_compra DECIMAL(10, 2) NOT NULL,
precio_venta DECIMAL(10, 2) NOT NULL,
pasillo VARCHAR(50),
estante VARCHAR(50),
id_estado INT NOT NULL,
FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor),
FOREIGN KEY (id_estado) REFERENCES estados_lote(id_estado),
INDEX idx_fecha_vencimiento (fecha_vencimiento),
INDEX idx_estado (id_estado),
INDEX idx_producto (id_producto)
) ENGINE=InnoDB;

CREATE TABLE registro_cuarentena (
id_registro INT AUTO_INCREMENT PRIMARY KEY,
id_lote INT NOT NULL,
motivo TEXT NOT NULL,
usuario VARCHAR(100) NOT NULL,
fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
resuelta BOOLEAN DEFAULT FALSE,
fecha_resolucion DATETIME NULL,
notas_resolucion TEXT,
FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
INDEX idx_resuelta (resuelta)
) ENGINE=InnoDB;

CREATE TABLE inspecciones_calidad (
id_inspeccion INT AUTO_INCREMENT PRIMARY KEY,
id_lote INT NOT NULL,
fecha_inspeccion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
resultado ENUM('Aprobado', 'Rechazado') NOT NULL,
nombre_inspector VARCHAR(100) NOT NULL,
motivo_rechazo TEXT COMMENT 'Motivo detallado si el resultado es Rechazado',
notas TEXT,
FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
INDEX idx_resultado (resultado)
) ENGINE=InnoDB;

CREATE TABLE registro_mermas (
id_registro INT AUTO_INCREMENT PRIMARY KEY,
id_lote INT NOT NULL,
id_inspeccion INT COMMENT 'Inspección que generó esta merma (si aplica)',
cantidad_perdida DECIMAL(10, 2) NOT NULL,
motivo TEXT NOT NULL,
fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
usuario_registro VARCHAR(100),
FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
FOREIGN KEY (id_inspeccion) REFERENCES inspecciones_calidad(id_inspeccion) ON DELETE SET NULL,
INDEX idx_fecha (fecha_registro)
) ENGINE=InnoDB;

-- MÓDULO: PROMOCIONES

CREATE TABLE promociones (
id_promocion INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(255) NOT NULL,
descripcion TEXT,
tasa_descuento DECIMAL(5, 2) NOT NULL COMMENT 'Ej: 0.50 para 50%',
aplica_estado_lote INT,
aplica_categoria_producto INT,
activa BOOLEAN DEFAULT TRUE,
fecha_inicio DATE,
fecha_fin DATE,
FOREIGN KEY (aplica_estado_lote) REFERENCES estados_lote(id_estado),
FOREIGN KEY (aplica_categoria_producto) REFERENCES categorias_producto(id_categoria),
INDEX idx_activa (activa)
) ENGINE=InnoDB;

-- MÓDULO: CLIENTES Y VENTAS

CREATE TABLE clientes (
id_cliente INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(255) NOT NULL,
documento VARCHAR(20) NOT NULL UNIQUE,
email VARCHAR(100),
telefono VARCHAR(20),
direccion TEXT,
id_tipo INT NOT NULL,
fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (id_tipo) REFERENCES tipos_cliente(id_tipo)
) ENGINE=InnoDB;

CREATE TABLE ventas (
id_venta INT AUTO_INCREMENT PRIMARY KEY,
numero_venta VARCHAR(50) NOT NULL UNIQUE,
id_cliente INT NOT NULL,
fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
subtotal DECIMAL(12, 2) NOT NULL,
total_descuento DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
total_impuesto DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
monto_total DECIMAL(12, 2) NOT NULL,
id_estado INT NOT NULL,
usuario_registro VARCHAR(100) COMMENT 'Usuario que registró la venta',
modalidad_entrega VARCHAR(20) NOT NULL DEFAULT 'STANDARD' COMMENT 'STANDARD (0%) o URGENT (+12%)',
recargo_entrega DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Recargo de la entrega, ya incluido en monto_total',
FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
FOREIGN KEY (id_estado) REFERENCES estados_venta(id_estado),
INDEX idx_estado (id_estado),
INDEX idx_fecha (fecha_venta)
) ENGINE=InnoDB;

CREATE TABLE detalles_venta (
id_detalle INT AUTO_INCREMENT PRIMARY KEY,
id_venta INT NOT NULL,
id_lote INT NOT NULL,
cantidad_vendida DECIMAL(10, 2) NOT NULL,
precio_unitario DECIMAL(10, 2) NOT NULL,
descuento_aplicado DECIMAL(5, 2) DEFAULT 0.00,
monto_descuento DECIMAL(10, 2) DEFAULT 0.00,
tasa_impuesto DECIMAL(5, 2) DEFAULT 0.00,
monto_impuesto DECIMAL(10, 2) DEFAULT 0.00,
subtotal DECIMAL(10, 2) NOT NULL,
total_linea DECIMAL(10, 2) NOT NULL,
FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote)
) ENGINE=InnoDB;

CREATE TABLE facturas (
id_factura INT AUTO_INCREMENT PRIMARY KEY,
id_venta INT NOT NULL,
numero_factura VARCHAR(50) NOT NULL UNIQUE,
cufe VARCHAR(255) NOT NULL UNIQUE COMMENT 'Código simulado para factura electrónica',
enlace_qr VARCHAR(255) COMMENT 'URL o datos para generar código QR',
fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
subtotal DECIMAL(12, 2) NOT NULL,
total_descuento DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
monto_impuesto DECIMAL(12, 2) NOT NULL,
monto_total DECIMAL(12, 2) NOT NULL,
FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
INDEX idx_numero (numero_factura)
) ENGINE=InnoDB;

-- MÓDULO: LOGÍSTICA Y ENTREGAS

CREATE TABLE conductores (
id_conductor INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(255) NOT NULL,
numero_licencia VARCHAR(50) NOT NULL UNIQUE,
telefono VARCHAR(20),
email VARCHAR(100),
activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE vehiculos (
id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
placa VARCHAR(20) NOT NULL UNIQUE,
modelo VARCHAR(100),
capacidad DECIMAL(10, 2) COMMENT 'Capacidad en kg o unidades',
estado ENUM('Activo', 'Inactivo', 'Mantenimiento') DEFAULT 'Activo'
) ENGINE=InnoDB;

CREATE TABLE rutas_entrega (
id_ruta INT AUTO_INCREMENT PRIMARY KEY,
nombre_ruta VARCHAR(255) NOT NULL,
fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
usuario_creacion VARCHAR(100) COMMENT 'Usuario logístico que creó la ruta',
hora_salida_estimada DATETIME,
hora_llegada_estimada DATETIME,
hora_salida_real DATETIME,
hora_llegada_real DATETIME,
estado ENUM('Planificada', 'En Transito', 'Completada', 'Cancelada') DEFAULT 'Planificada',
INDEX idx_estado (estado),
INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB;

CREATE TABLE asignaciones_entrega (
id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
id_ruta INT NOT NULL,
id_vehiculo INT NOT NULL,
id_conductor INT NOT NULL,
fecha_asignacion DATETIME DEFAULT CURRENT_TIMESTAMP,
usuario_asignacion VARCHAR(100) COMMENT 'Usuario que realizó la asignación',
notas TEXT,
FOREIGN KEY (id_ruta) REFERENCES rutas_entrega(id_ruta),
FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo),
FOREIGN KEY (id_conductor) REFERENCES conductores(id_conductor),
INDEX idx_ruta (id_ruta)
) ENGINE=InnoDB;

CREATE TABLE ventas_ruta (
id INT AUTO_INCREMENT PRIMARY KEY,
id_ruta INT NOT NULL,
id_venta INT NOT NULL,
orden_entrega INT COMMENT 'Orden de entrega en la ruta',
direccion_destino TEXT NOT NULL,
fecha_entrega DATETIME,
recibido_por VARCHAR(255),
observaciones TEXT,
FOREIGN KEY (id_ruta) REFERENCES rutas_entrega(id_ruta),
FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
UNIQUE KEY (id_ruta, id_venta),
INDEX idx_ruta (id_ruta),
INDEX idx_venta (id_venta)
) ENGINE=InnoDB;

-- =============================================
-- SECCIÓN 2: DML (datos de prueba)
-- =============================================

INSERT INTO estados_lote (nombre, descripcion) VALUES
('Disponible', 'Lote disponible para venta'),
('Proximo a Vencer', 'Lote próximo a vencer (7 días o menos)'),
('Vencido', 'Lote vencido, no apto para venta'),
('En Cuarentena', 'Lote en cuarentena pendiente de inspección');

INSERT INTO tipos_cliente (nombre, descripcion) VALUES
('Mayorista', 'Cliente que compra en grandes cantidades'),
('Minorista', 'Cliente que compra en pequeñas cantidades');

INSERT INTO estados_venta (nombre, descripcion) VALUES
('Registrada', 'Venta registrada, pendiente de preparación'),
('En Preparacion', 'Pedido en preparación para despacho'),
('En Transito', 'Pedido en tránsito hacia el cliente'),
('Entregada', 'Pedido entregado al cliente'),
('Cancelada', 'Venta cancelada'),
('Devuelta', 'Venta entregada que el cliente devolvió');

INSERT INTO informacion_empresa (nombre_empresa, nit, direccion, ciudad, telefono, email) VALUES
('Frescura Total S.A.S.', '900123456-7', 'Calle 123 #45-67', 'Bogotá', '(601) 234-5678', 'contacto@frescuratotal.com');

INSERT INTO categorias_producto (nombre, descripcion) VALUES
('Frutas', 'Frutas frescas'),
('Verduras', 'Verduras y hortalizas'),
('Lacteos', 'Productos lácteos'),
('Carnes', 'Carnes y derivados'),
('Granos', 'Granos y cereales'),
('Bebidas', 'Bebidas y líquidos'),
('Enlatados', 'Productos enlatados'),
('Panaderia', 'Productos de panadería');

-- PROVEEDORES
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroindustrial del Norte SAS', '900.754.214-0', 'Carlos Mario Gómez', 'contacto1@agroindustrial.com', '32974230', 'Calle 88 # 55-77, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Distribuciones Agrícolas El Trébol', '900.859.381-3', 'Ana María Restrepo', 'contacto2@distribuciones.com', '31849458', 'Calle 77 # 60-68, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Comercializadora Frutas y Verduras Santander', '900.328.242-1', 'Jorge Enrique Pérez', 'contacto3@comercializadora.com', '34266611', 'Calle 88 # 15-88, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Cultivos y Cosechas de la Sabana', '900.792.858-8', 'Lucía Fernanda Rojas', 'contacto4@cultivos.com', '37886844', 'Calle 99 # 83-44, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Inversiones Agropecuarias San José', '900.189.704-6', 'Luis Fernando Morales', 'contacto5@inversiones.com', '32440065', 'Calle 21 # 59-1, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Frutícola Los Andes Ltda', '900.132.130-1', 'Diana Marcela Silva', 'contacto6@frutícola.com', '34361232', 'Calle 65 # 14-81, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agrocoop del Magdalena', '900.323.338-8', 'Héctor Julio Vargas', 'contacto7@agrocoop.com', '34896191', 'Calle 65 # 78-26, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Productores Unidos de Boyacá', '900.716.127-8', 'Claudia Patricia Torres', 'contacto8@productores.com', '32948230', 'Calle 70 # 68-1, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Central de Abastos del Oriente', '900.303.833-8', 'Andrés Felipe Castro', 'contacto9@central.com', '38643172', 'Calle 3 # 15-47, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroexportaciones del Valle', '900.529.325-7', 'Martha Lucía Ramirez', 'contacto10@agroexportaciones.com', '34934517', 'Calle 31 # 73-11, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Hortalizas y Frutas del Cauca', '900.703.384-0', 'Gabriel Jaime Restrepo', 'contacto11@hortalizas.com', '32084972', 'Calle 9 # 69-99, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Comercializadora Agroforestal', '900.877.925-2', 'Sandra Milena Ortiz', 'contacto12@comercializadora.com', '32623194', 'Calle 61 # 71-22, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agropecuaria La Esperanza', '900.814.532-5', 'Mauricio Alejandro Ríos', 'contacto13@agropecuaria.com', '34364087', 'Calle 55 # 28-70, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Frutas Fuch S.A.S.', '900.384.259-3', 'Gloria Inés Herrera', 'contacto14@frutas.com', '39830549', 'Calle 52 # 86-84, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Distribuidora El Progreso', '900.881.444-1', 'Fernando José Gómez', 'contacto15@distribuidora.com', '35754876', 'Calle 58 # 16-32, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroinsumos y Cosechas S.A.', '900.194.489-1', 'Adriana María Cardona', 'contacto16@agroinsumos.com', '33816553', 'Calle 3 # 76-71, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Cultivos Orgánicos de Colombia', '900.467.967-5', 'Ricardo Andrés Pineda', 'contacto17@cultivos.com', '33970238', 'Calle 1 # 10-91, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Productora Agrícola El Sol', '900.718.370-0', 'Liliana Patricia Mendoza', 'contacto18@productora.com', '39016039', 'Calle 9 # 5-43, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroindustrial Los Pinos', '900.847.570-8', 'Javier Alirio Cárdenas', 'contacto19@agroindustrial.com', '31962640', 'Calle 36 # 86-63, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Frutas y Legumbres del Sur', '900.227.487-1', 'Rosa Elena Valencia', 'contacto20@frutas.com', '33765226', 'Calle 93 # 74-74, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Asociación de Horticultores Andinos', '900.665.400-9', 'Oscar Darío Gutiérrez', 'contacto21@asociación.com', '37034870', 'Calle 53 # 25-13, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Comercializadora Verde Vivo', '900.982.470-9', 'Mónica Alejandra Parra', 'contacto22@comercializadora.com', '32277465', 'Calle 46 # 55-53, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agropecuaria Villa Hermosa', '900.296.821-1', 'Humberto de Jesús Londoño', 'contacto23@agropecuaria.com', '36998416', 'Calle 87 # 84-83, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Distribuidora Agrícola Central', '900.146.777-3', 'Beatriz Eugenia Noreña', 'contacto24@distribuidora.com', '32216261', 'Calle 94 # 44-14, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Cultivos del Eje Cafetero', '900.891.396-1', 'Gustavo Adolfo Rincón', 'contacto25@cultivos.com', '34129634', 'Calle 69 # 58-18, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroindustrial El Cosechero', '900.975.338-1', 'Paola Andrea Cifuentes', 'contacto26@agroindustrial.com', '36428745', 'Calle 60 # 32-10, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Frutas Selectas de Colombia', '900.489.384-7', 'Wilson Hernán Bermúdez', 'contacto27@frutas.com', '36692780', 'Calle 13 # 7-84, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Inversiones Agrícolas del Caribe', '900.750.954-5', 'Esperanza Orozco', 'contacto28@inversiones.com', '37995611', 'Calle 12 # 31-22, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agrocoop de Cundinamarca', '900.266.479-5', 'Nelson Enrique Bohórquez', 'contacto29@agrocoop.com', '36259771', 'Calle 28 # 52-8, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Productores del Llano Oriental', '900.314.786-4', 'Clara Inés Jaramillo', 'contacto30@productores.com', '33148810', 'Calle 50 # 34-59, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agropecuaria San Fernando', '900.818.799-1', 'Eduardo Antonio Suescún', 'contacto31@agropecuaria.com', '34653399', 'Calle 94 # 72-85, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Distribuidora de Alimentos Frescos', '900.723.750-2', 'Luz Dary Quintero', 'contacto32@distribuidora.com', '37225834', 'Calle 38 # 28-8, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroindustrial del Tolima', '900.646.846-3', 'Germán Alonso Agudelo', 'contacto33@agroindustrial.com', '38485379', 'Calle 8 # 41-8, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Frutícola del Valle del Cauca', '900.267.573-6', 'Martha Cecilia Salazar', 'contacto34@frutícola.com', '31669871', 'Calle 65 # 68-21, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Cultivos y Semillas del Norte', '900.376.755-8', 'Hernán Darío Echavarría', 'contacto35@cultivos.com', '31762020', 'Calle 24 # 9-77, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Comercializadora Agrícola Andina', '900.324.801-5', 'Sandra Patricia Giraldo', 'contacto36@comercializadora.com', '31879140', 'Calle 52 # 16-73, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agropecuaria Las Margaritas', '900.963.886-0', 'Carlos Alberto Henao', 'contacto37@agropecuaria.com', '34169286', 'Calle 6 # 80-11, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Frutas Tropicales de Colombia', '900.334.941-0', 'Diana Carolina Ospina', 'contacto38@frutas.com', '36377384', 'Calle 73 # 67-41, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Distribuidora Agropecuaria Global', '900.924.423-6', 'Fabio Nelson Echeverry', 'contacto39@distribuidora.com', '34330995', 'Calle 92 # 41-31, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroindustrial Santa Rosa', '900.374.167-3', 'Maritza Alejandra Rueda', 'contacto40@agroindustrial.com', '34350526', 'Calle 86 # 83-39, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Cultivos del Altiplano', '900.680.997-5', 'Rodrigo Alberto Meza', 'contacto41@cultivos.com', '36842319', 'Calle 2 # 59-80, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Comercializadora Frutifresco', '900.317.771-7', 'Adriana Lucía Pimiento', 'contacto42@comercializadora.com', '38220219', 'Calle 69 # 28-65, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agropecuaria El Diamante', '900.505.758-7', 'Leonardo Fabio Quijano', 'contacto43@agropecuaria.com', '34323554', 'Calle 9 # 32-48, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Productores Agrícolas del Cauca', '900.246.371-2', 'Liliana María Becerra', 'contacto44@productores.com', '34626166', 'Calle 70 # 39-79, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Asociación Agroindustrial de Antioquia', '900.352.862-8', 'Diego Fernando Téllez', 'contacto45@asociación.com', '39364111', 'Calle 86 # 71-39, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Frutas y Verduras del Quindío', '900.651.369-9', 'Claudia Milena Angarita', 'contacto46@frutas.com', '39420627', 'Calle 34 # 15-14, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Distribuidora Agrícola San Isidro', '900.538.697-6', 'Alvaro José Sotomayor', 'contacto47@distribuidora.com', '38025944', 'Calle 37 # 78-27, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Agroindustrial Buena Vista', '900.470.324-2', 'Yolanda Marín', 'contacto48@agroindustrial.com', '35330897', 'Calle 82 # 34-65, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Cultivos Hidropónicos de Colombia', '900.621.605-1', 'Sergio Andrés Carvajal', 'contacto49@cultivos.com', '37235716', 'Calle 12 # 82-55, Bucaramanga');
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES ('Inversiones Agropecuarias El Retiro', '900.873.148-1', 'Natalia Andrea Vargas', 'contacto50@inversiones.com', '34514510', 'Calle 43 # 17-82, Bucaramanga');











-- PRODUCTOS
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Papa Criolla', 'Papa criolla limpia calibre seleccionado', 1, 'kg', 3800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Papa Pastusa', 'Papa pastusa comercial bolsa 50kg', 1, 'kg', 2400);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Papa Capiro', 'Papa capiro extra para consumo masivo', 1, 'kg', 2200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Yuca Industrial', 'Yuca fresca raiz entera calidad primera', 1, 'kg', 2600);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Plátano Hartón Verde', 'Plátano hartón verde racimo seleccionado', 1, 'kg', 3100);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Plátano Maduro', 'Plátano maduro selecto por kilo', 1, 'kg', 2900);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Tomate Chonto', 'Tomate chonto maduro extra caja de madera', 2, 'kg', 3500);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Tomate Long Life', 'Tomate larga vida en gaveta plástica', 2, 'kg', 3800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Cebolla Cabezona Blanca', 'Cebolla cabezona blanca importada/nacional', 2, 'kg', 4200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Cebolla Cabezona Roja', 'Cebolla cabezona roja ocaña bulto', 2, 'kg', 4500);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Cebolla Junca', 'Cebolla larga o junca manojo fresco', 2, 'kg', 2800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Zanahoria', 'Zanahoria lavada primera calidad', 2, 'kg', 2500);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Habichuela', 'Habichuela fresca cultivada en altiplano', 2, 'kg', 5100);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Arveja Verde en Vaina', 'Arveja verde desgranada/en vaina fresca', 2, 'kg', 7800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Fríjol Verde en Vaina', 'Fríjol cargamanto rojo en vaina', 2, 'kg', 8200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Lechuga Crespa', 'Lechuga crespa verde hidropónica unidad', 2, 'unidad', 2200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Lechuga Batavia', 'Lechuga batavia fresca por unidad', 2, 'unidad', 2400);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Pepino Cohombro', 'Pepino cohombro fresco seleccionado', 2, 'kg', 2600);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Calabacín', 'Calabacín italiano verde fresco', 2, 'kg', 3200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Ahuyama', 'Ahuyama madura por kilo', 2, 'kg', 2100);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Mazorca de Maíz', 'Mazorca dulce choclo paquete x 4', 2, 'paquete', 4500);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Remolacha', 'Remolacha roja fresca lavada', 2, 'kg', 2800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Espinaca', 'Espinaca fresca atado limpio', 2, 'atado', 3000);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Acelga', 'Acelga verde fresca por atado', 2, 'atado', 2700);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Repollo Blanco', 'Repollo blanco holandés entero', 2, 'kg', 2300);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Repollo Morado', 'Repollo morado fresco compacto', 2, 'kg', 2900);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Coliflor', 'Coliflor blanco cabeza compacta primera', 2, 'kg', 5500);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Brócoli', 'Brócoli verde fresco exportación calidad', 2, 'kg', 5800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Apio en Rama', 'Apio en rama atado fresco', 2, 'atado', 3600);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Cilantro', 'Cilantro fresco manojo grande', 2, 'atado', 1800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Perejil Liso', 'Perejil liso aromático atado', 2, 'atado', 1900);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Banano Urabá', 'Banano exportación caja x 20kg', 3, 'kg', 2300);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Banano Bocadillo', 'Banano bocadillo maduro dulce', 3, 'kg', 3400);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Mango Tommy', 'Mango tommy enmielado maduro primera', 3, 'kg', 4800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Mangoiche', 'Mangoiche jugoso selecto', 3, 'kg', 4200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Papaya Maradol', 'Papaya maradol grande dulce', 3, 'kg', 2700);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Sandía Redonda', 'Sandía redonda entera por kilo', 3, 'kg', 2200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Melón Cantaloupe', 'Melón cantaloupe aromático maduro', 3, 'kg', 3900);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Piña Perolera', 'Piña perolera dulce de primera', 3, 'kg', 3100);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Maracuyá', 'Maracuyá para zumo seleccionado', 3, 'kg', 5200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Lulo de Castilla', 'Lulo de castilla pulpa alta calidad', 3, 'kg', 6500);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Tomate de Árbol', 'Tomate de árbol rojo maduro', 3, 'kg', 4100);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Guayaba Pera', 'Guayaba pera para dulce o fresco', 3, 'kg', 3000);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Fresa de Altura', 'Fresa seleccionada canastilla 500g', 3, 'kg', 9500);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Mora de Castilla', 'Mora de castilla fresca sin espinas', 3, 'kg', 7200);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Curuba Redonda', 'Curuba redonda bogotana fresca', 3, 'kg', 5900);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Aguacate Hass', 'Aguacate hass calibre exportación', 3, 'kg', 8800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Limón Tahití', 'Limón tahití jugoso sin semilla', 3, 'kg', 3600);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Naranja Valencia', 'Naranja valencia para jugo', 3, 'kg', 2800);
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES ('Mandarina Arrayana', 'Mandarina arrayana dulce fácil pelar', 3, 'kg', 3900);











-- PRODUCTOS POR PROVEEDOR
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (1, 8);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (1, 20);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (1, 34);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (2, 3);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (2, 8);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (2, 12);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (2, 43);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (3, 7);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (3, 23);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (3, 24);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (5, 30);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (5, 45);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (5, 50);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (6, 49);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (7, 23);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (8, 25);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (9, 3);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (10, 28);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (10, 35);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (10, 42);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (12, 27);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (12, 40);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (13, 10);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (13, 24);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (13, 26);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (13, 27);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (13, 30);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (14, 44);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (15, 15);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (16, 11);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (16, 31);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (16, 43);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (16, 44);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (17, 3);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (17, 11);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (17, 12);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (18, 11);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (18, 23);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (19, 14);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (19, 33);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (20, 15);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (20, 24);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (21, 26);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (21, 28);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (21, 30);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (22, 18);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (23, 20);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (23, 47);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (25, 3);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (25, 32);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (26, 16);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (26, 36);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (26, 44);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (27, 40);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (27, 41);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (27, 43);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (28, 5);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (28, 36);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (28, 38);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (29, 29);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (31, 15);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (33, 20);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (33, 31);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (35, 22);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (35, 44);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (36, 9);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (36, 46);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (37, 13);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (37, 20);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (38, 17);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (38, 36);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (39, 28);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (39, 33);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (39, 42);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (40, 21);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (40, 22);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (40, 50);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (42, 33);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (43, 6);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (43, 8);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (43, 22);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (43, 25);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (43, 27);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (43, 41);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (44, 14);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (44, 48);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (45, 1);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (45, 7);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (45, 19);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (46, 28);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (46, 45);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (47, 20);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (47, 48);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (48, 10);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (48, 11);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (48, 16);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (48, 22);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (48, 29);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (48, 43);
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (50, 36);





















-- CLIENTES
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Supermercados La Canasta Mayorista SAS', '900.868.981-1', 'cliente1@comercial1.com', '36432432', 'Carrera 45 # 67-60, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Abastos del Oriente S.A.S.', '900.151.670-3', 'cliente2@comercial2.com', '32556727', 'Carrera 30 # 86-68, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Comercializadora de Alimentos El Vergel S.A.S.', '900.672.709-5', 'cliente3@comercial3.com', '36672774', 'Carrera 28 # 71-58, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Central Mayorista de Víveres Bucaramanga', '900.262.861-7', 'cliente4@comercial4.com', '36736541', 'Carrera 41 # 36-99, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Distribuciones Alimenticias El Cacique', '900.896.633-7', 'cliente5@comercial5.com', '39034445', 'Carrera 29 # 10-92, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Mercados e Inversiones Andinas', '900.392.340-4', 'cliente6@comercial6.com', '35242779', 'Carrera 6 # 18-20, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Comercializadora Global de Alimentos', '900.336.492-2', 'cliente7@comercial7.com', '33716563', 'Carrera 27 # 43-70, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Supertiendas y Mercados del Norte', '900.577.525-0', 'cliente8@comercial8.com', '33695263', 'Carrera 25 # 75-90, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Abastos y Frutas de Santander', '900.120.977-9', 'cliente9@comercial9.com', '35858810', 'Carrera 23 # 39-97, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Corporación de Abastecimiento Mayorista', '900.499.973-6', 'cliente10@comercial10.com', '37886579', 'Carrera 39 # 29-63, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Inversiones y Comercializadora El Puente', '900.324.379-6', 'cliente11@comercial11.com', '37212959', 'Carrera 22 # 86-87, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Distribuidora Mayorista de Víveres', '900.917.514-2', 'cliente12@comercial12.com', '36923089', 'Carrera 35 # 4-51, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Sociedad Comercializadora de Alimentos', '900.706.677-0', 'cliente13@comercial13.com', '32075864', 'Carrera 9 # 60-24, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Central de Frutas y Verduras del Valle', '900.151.366-6', 'cliente14@comercial14.com', '35131668', 'Carrera 21 # 44-98, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Comercializadora Agromercadeo S.A.', '900.488.384-6', 'cliente15@comercial15.com', '34295420', 'Carrera 31 # 3-96, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Supermercado Mayorista El Libertador', '900.652.153-5', 'cliente16@comercial16.com', '33876518', 'Carrera 50 # 84-6, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Abastos y Frutas de Colombia S.A.S.', '900.872.131-3', 'cliente17@comercial17.com', '33595912', 'Carrera 40 # 20-31, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Distribuidora de Alimentos San Martín', '900.229.584-1', 'cliente18@comercial18.com', '38232369', 'Carrera 45 # 33-99, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Comercializadora de Viveres y Frutas', '900.477.271-9', 'cliente19@comercial19.com', '38786524', 'Carrera 50 # 21-40, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Mercados Mayoristas del Magdalena', '900.210.692-0', 'cliente20@comercial20.com', '34968996', 'Carrera 25 # 51-92, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Inversiones Alimenticias del Cauca', '900.303.177-9', 'cliente21@comercial21.com', '39895090', 'Carrera 16 # 14-90, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Central de Abastos e Inversiones SAS', '900.891.408-9', 'cliente22@comercial22.com', '32591582', 'Carrera 3 # 45-69, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Comercializadora de Cosechas Nacionales', '900.538.777-5', 'cliente23@comercial23.com', '31861892', 'Carrera 22 # 2-54, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Distribuidora Mayorista La Sabana', '900.942.601-1', 'cliente24@comercial24.com', '36547091', 'Carrera 30 # 20-56, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Supermercados e Hipermercados del Sol', '900.280.851-8', 'cliente25@comercial25.com', '39337688', 'Carrera 35 # 62-60, Bucaramanga', 1);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Frutería y Verdulería Doña Rosa', '1.065.945.848', 'cliente26@comercial26.com', '38537451', 'Carrera 16 # 12-36, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Minimercado El Vecino', '1.067.349.868', 'cliente27@comercial27.com', '36968388', 'Carrera 43 # 49-44, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Tienda de Víveres La Economía', '1.013.606.971', 'cliente28@comercial28.com', '35128672', 'Carrera 14 # 46-34, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Granero y Miscelánea El Punto Clave', '1.053.386.710', 'cliente29@comercial29.com', '39938281', 'Carrera 1 # 67-25, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Verdulería Fresco y Natural', '1.020.347.837', 'cliente30@comercial30.com', '36260081', 'Carrera 49 # 31-89, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Minimercado San Judas Tadeo', '1.070.761.828', 'cliente31@comercial31.com', '37255812', 'Carrera 6 # 38-29, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Tienda y Granero La Esquina', '1.061.808.349', 'cliente32@comercial32.com', '34977984', 'Carrera 24 # 61-71, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Frutería El Paraíso de las Frutas', '1.077.452.535', 'cliente33@comercial33.com', '38043855', 'Carrera 45 # 59-35, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Minimercado Los Olivos', '1.049.357.336', 'cliente34@comercial34.com', '32583834', 'Carrera 21 # 16-96, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Granero La Bendición de Dios', '1.078.880.806', 'cliente35@comercial35.com', '33329637', 'Carrera 48 # 62-36, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Verdulería y Frutas El Triunfo', '1.085.878.637', 'cliente36@comercial36.com', '38638922', 'Carrera 13 # 38-30, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Minimercado La Esperanza', '1.056.283.409', 'cliente37@comercial37.com', '31182578', 'Carrera 9 # 36-6, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Tienda Comunitaria El Progreso', '1.016.666.399', 'cliente38@comercial38.com', '39922991', 'Carrera 49 # 63-14, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Granero y Abarrotes San José', '1.011.687.391', 'cliente39@comercial39.com', '37059066', 'Carrera 22 # 24-7, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Frutería Natural Fresh', '1.042.982.589', 'cliente40@comercial40.com', '32494118', 'Carrera 26 # 63-10, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Minimercado Santa Bárbara', '1.083.744.802', 'cliente41@comercial41.com', '31625529', 'Carrera 37 # 39-11, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Tienda y Granero El Progreso Vecino', '1.041.221.671', 'cliente42@comercial42.com', '36372086', 'Carrera 40 # 29-67, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Granero La gran Parada', '1.058.561.553', 'cliente43@comercial43.com', '34898085', 'Carrera 28 # 40-73, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Verdulería El Buen Sabor', '1.089.161.724', 'cliente44@comercial44.com', '32288136', 'Carrera 41 # 28-34, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Minimercado Villa del Río', '1.094.183.260', 'cliente45@comercial45.com', '34027780', 'Carrera 5 # 21-1, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Tienda de Víveres El Sol Naciente', '1.062.561.805', 'cliente46@comercial46.com', '38658147', 'Carrera 3 # 30-37, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Granero San Antonio', '1.046.819.980', 'cliente47@comercial47.com', '36817297', 'Carrera 15 # 34-81, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Frutería y Frutos Verdes', '1.085.777.923', 'cliente48@comercial48.com', '33553524', 'Carrera 35 # 29-83, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Minimercado El Diamante', '1.029.372.946', 'cliente49@comercial49.com', '32817317', 'Carrera 11 # 40-77, Bucaramanga', 2);
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES ('Tienda La Principal', '1.082.395.549', 'cliente50@comercial50.com', '32557998', 'Carrera 20 # 90-52, Bucaramanga', 2);











-- LOTES DE INVENTARIO
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0001', 18, 33, '2026-10-27 08:00:00', '2027-01-09', 818, 818, 2290, 3217, 'Pasillo E', 'Estante 1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0002', 6, 15, '2026-11-02 08:00:00', '2027-01-02', 790, 609, 2738, 3819, 'Pasillo A', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0003', 49, 12, '2026-10-31 08:00:00', '2027-01-23', 850, 648, 2437, 3584, 'Frío 3', 'Estante 1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0004', 31, 23, '2026-10-23 08:00:00', '2026-11-07', 621, 554, 1636, 2307, 'Frío 3', 'Estante 3', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0005', 43, 26, '2026-10-28 08:00:00', '2026-12-31', 531, 408, 2766, 3780, 'Pasillo B', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0006', 26, 33, '2026-10-26 08:00:00', '2027-01-20', 255, 149, 2678, 3335, 'Pasillo D', 'RACK-1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0007', 24, 40, '2026-10-30 08:00:00', '2027-01-02', 762, 448, 2466, 3442, 'Pasillo C', 'Estante 3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0008', 29, 45, '2026-10-30 08:00:00', '2026-12-19', 518, 428, 3141, 4160, 'Pasillo A', 'RACK-1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0009', 27, 6, '2026-10-24 08:00:00', '2026-11-10', 357, 231, 4585, 7250, 'Frío 3', 'RACK-2', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0010', 19, 33, '2026-10-26 08:00:00', '2026-12-30', 667, 517, 2898, 3945, 'Pasillo C', 'Estante 4', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0011', 13, 39, '2026-10-29 08:00:00', '2027-01-21', 548, 548, 4468, 6761, 'Pasillo E', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0012', 1, 19, '2026-10-26 08:00:00', '2026-12-18', 657, 626, 3453, 5006, 'Frío 3', 'Estante 3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0013', 22, 36, '2026-10-30 08:00:00', '2026-12-29', 785, 640, 2560, 3431, 'Frío 2', 'Estante 2', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0014', 50, 27, '2026-10-27 08:00:00', '2027-01-17', 879, 826, 3145, 5065, 'Pasillo C', 'Estante 4', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0015', 3, 9, '2026-10-26 08:00:00', '2026-12-11', 738, 593, 1925, 2789, 'Frío 3', 'Estante 1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0016', 47, 10, '2026-10-18 08:00:00', '2026-11-11', 471, 311, 7581, 11272, 'Frío 1', 'RACK-1', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0017', 45, 26, '2026-10-25 08:00:00', '2027-01-16', 524, 471, 6461, 9506, 'Frío 3', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0018', 35, 3, '2026-10-29 08:00:00', '2026-12-28', 964, 897, 3748, 5027, 'Pasillo B', 'Estante 4', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0019', 7, 49, '2026-10-29 08:00:00', '2026-12-02', 247, 82, 3132, 4635, 'Frío 1', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0020', 4, 19, '2026-10-19 08:00:00', '2026-11-09', 898, 807, 2219, 3213, 'Pasillo C', 'Estante 2', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0021', 12, 6, '2026-10-26 08:00:00', '2026-12-17', 437, 367, 2228, 3066, 'Frío 3', 'RACK-2', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0022', 17, 30, '2026-10-29 08:00:00', '2026-12-19', 275, 255, 2011, 2764, 'Frío 3', 'Estante 3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0023', 38, 20, '2026-11-01 08:00:00', '2026-11-30', 593, 593, 3493, 4815, 'Frío 3', 'Estante 1', 4);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0024', 16, 25, '2026-10-29 08:00:00', '2026-12-01', 874, 553, 1948, 2782, 'Frío 2', 'Estante 3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0025', 1, 37, '2026-10-26 08:00:00', '2027-01-04', 994, 974, 3533, 4961, 'Pasillo D', 'RACK-1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0026', 23, 15, '2026-10-31 08:00:00', '2026-12-11', 842, 768, 2686, 3822, 'Pasillo A', 'Estante 3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0027', 29, 3, '2026-10-06 08:00:00', '2026-10-16', 965, 866, 3192, 4667, 'Frío 2', 'Estante 2', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0028', 13, 9, '2026-10-25 08:00:00', '2027-01-02', 368, 323, 4681, 6758, 'Pasillo E', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0029', 31, 19, '2026-10-24 08:00:00', '2026-11-06', 972, 805, 1732, 2313, 'Pasillo D', 'RACK-3', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0030', 44, 47, '2026-10-25 08:00:00', '2027-01-14', 292, 246, 8561, 11680, 'Frío 2', 'Estante 1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0031', 17, 35, '2026-10-29 08:00:00', '2027-01-16', 853, 743, 1964, 2936, 'Frío 1', 'Estante 1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0032', 44, 15, '2026-10-28 08:00:00', '2026-12-27', 863, 785, 8271, 12102, 'Pasillo B', 'RACK-2', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0033', 30, 45, '2026-10-25 08:00:00', '2026-11-07', 704, 482, 1521, 2216, 'Pasillo B', 'Estante 1', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0034', 16, 35, '2026-10-25 08:00:00', '2027-01-21', 801, 771, 1804, 2729, 'Pasillo C', 'Estante 4', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0035', 42, 7, '2026-10-29 08:00:00', '2026-12-03', 906, 906, 3792, 5219, 'Frío 1', 'Estante 2', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0036', 29, 29, '2026-10-04 08:00:00', '2026-10-17', 860, 860, 3393, 4755, 'Frío 1', 'Estante 1', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0037', 26, 18, '2026-10-26 08:00:00', '2026-12-10', 878, 822, 2402, 3405, 'Pasillo D', 'RACK-2', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0038', 41, 39, '2026-11-02 08:00:00', '2026-12-19', 778, 778, 6146, 7540, 'Pasillo D', 'Estante 1', 4);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0039', 49, 36, '2026-10-28 08:00:00', '2026-12-17', 810, 752, 2327, 3340, 'Pasillo A', 'Estante 3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0040', 10, 9, '2026-10-25 08:00:00', '2026-11-06', 215, 215, 3964, 5893, 'Frío 1', 'RACK-3', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0041', 16, 38, '2026-10-02 08:00:00', '2026-10-15', 316, 196, 1866, 2606, 'Pasillo B', 'Estante 4', 2);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0042', 29, 50, '2026-10-30 08:00:00', '2026-12-04', 944, 761, 3075, 4567, 'Pasillo E', 'Estante 4', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0043', 42, 2, '2026-11-01 08:00:00', '2027-01-30', 929, 929, 3317, 5107, 'Frío 3', 'Estante 1', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0044', 6, 21, '2026-10-30 08:00:00', '2026-12-11', 929, 929, 2584, 3373, 'Frío 1', 'Estante 4', 4);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0045', 39, 34, '2026-11-01 08:00:00', '2026-12-26', 640, 640, 2617, 3878, 'Frío 3', 'Estante 2', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0046', 27, 22, '2026-10-28 08:00:00', '2027-01-22', 520, 406, 5082, 6763, 'Pasillo E', 'Estante 3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0047', 10, 44, '2026-11-01 08:00:00', '2027-01-23', 298, 126, 4223, 5235, 'Frío 1', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0048', 9, 36, '2026-11-01 08:00:00', '2027-01-20', 562, 470, 3397, 5633, 'Frío 2', 'RACK-3', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0049', 47, 4, '2026-10-25 08:00:00', '2026-12-26', 358, 221, 8318, 11176, 'Frío 3', 'Estante 2', 1);
INSERT INTO lotes_inventario (numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES ('LOT-0050', 7, 23, '2026-10-30 08:00:00', '2027-01-22', 774, 493, 3243, 4282, 'Pasillo A', 'RACK-1', 1);











-- VENTAS
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0001', 43, '2026-11-03 08:00:00', 358975, 0, 68205.25, 427180.25, 3, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0002', 12, '2026-11-03 08:00:00', 293262, 39321, 48248.79, 302189.79, 3, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0003', 23, '2026-11-04 08:00:00', 218833, 109416.5, 20789.14, 130205.64, 2, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0004', 37, '2026-11-03 08:00:00', 317686, 0, 60360.34, 378046.34, 4, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0005', 10, '2026-11-02 08:00:00', 217856, 35456, 34656, 217056, 1, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0006', 48, '2026-11-02 08:00:00', 624459, 89050.5, 101727.62, 637136.12, 2, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0007', 27, '2026-11-03 08:00:00', 240444, 0, 45684.36, 286128.36, 3, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0008', 24, '2026-11-04 08:00:00', 520816, 123675.5, 75456.7, 472597.2, 3, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0009', 39, '2026-11-03 08:00:00', 189992, 67756, 23224.84, 145460.84, 1, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0010', 11, '2026-11-04 08:00:00', 399638, 0, 75931.22, 475569.22, 5, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0011', 44, '2026-11-04 08:00:00', 346485, 0, 65832.15, 412317.15, 3, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0012', 43, '2026-11-04 08:00:00', 171760, 0, 32634.4, 204394.4, 4, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0013', 29, '2026-11-04 08:00:00', 417884, 0, 79397.96, 497281.96, 3, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0014', 49, '2026-11-04 08:00:00', 331354, 165677, 31478.63, 197155.63, 1, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0015', 28, '2026-11-02 08:00:00', 795734, 279125, 98155.71, 614764.71, 3, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0016', 32, '2026-11-03 08:00:00', 337536, 62048, 52342.72, 327830.72, 3, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0017', 15, '2026-11-02 08:00:00', 319893, 53014.5, 50706.92, 317585.42, 5, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0018', 1, '2026-11-04 08:00:00', 576749, 0, 109582.31, 686331.31, 3, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0019', 50, '2026-11-04 08:00:00', 340628, 105125, 44745.57, 280248.57, 3, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0020', 21, '2026-11-02 08:00:00', 432368, 0, 82149.92, 514517.92, 1, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0021', 48, '2026-11-03 08:00:00', 772019, 0, 146683.61, 918702.61, 2, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0022', 25, '2026-11-02 08:00:00', 301284, 0, 57243.96, 358527.96, 2, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0023', 36, '2026-11-03 08:00:00', 312070, 0, 59293.3, 371363.3, 3, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0024', 26, '2026-11-03 08:00:00', 500696, 0, 95132.24, 595828.24, 1, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0025', 2, '2026-11-03 08:00:00', 333488, 0, 63362.72, 396850.72, 1, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0026', 37, '2026-11-02 08:00:00', 855190, 53199, 152378.29, 954369.29, 5, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0027', 46, '2026-11-03 08:00:00', 769870, 276164, 93804.14, 587510.14, 4, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0028', 8, '2026-11-04 08:00:00', 283436, 0, 53852.84, 337288.84, 1, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0029', 11, '2026-11-04 08:00:00', 785636, 0, 149270.84, 934906.84, 5, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0030', 45, '2026-11-02 08:00:00', 310642, 155321, 29510.99, 184831.99, 3, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0031', 28, '2026-11-02 08:00:00', 244733, 0, 46499.27, 291232.27, 1, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0032', 28, '2026-11-04 08:00:00', 546817, 0, 103895.23, 650712.23, 4, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0033', 6, '2026-11-03 08:00:00', 452952, 0, 86060.88, 539012.88, 3, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0034', 15, '2026-11-02 08:00:00', 436859, 0, 83003.21, 519862.21, 2, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0035', 33, '2026-11-03 08:00:00', 203966, 31024, 32858.98, 205800.98, 1, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0036', 33, '2026-11-03 08:00:00', 238148, 0, 45248.12, 283396.12, 3, 'ventas_carlos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0037', 47, '2026-11-03 08:00:00', 357500, 37336, 60831.16, 380995.16, 2, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0038', 7, '2026-11-03 08:00:00', 210500, 0, 39995, 250495, 3, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0039', 12, '2026-11-04 08:00:00', 296666, 0, 56366.54, 353032.54, 2, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0040', 12, '2026-11-03 08:00:00', 285499, 0, 54244.81, 339743.81, 4, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0041', 49, '2026-11-04 08:00:00', 244136, 0, 46385.84, 290521.84, 5, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0042', 44, '2026-11-02 08:00:00', 805450, 11565, 150838.15, 944723.15, 5, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0043', 21, '2026-11-02 08:00:00', 391742, 0, 74430.98, 466172.98, 3, 'vendedor_ana');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0044', 29, '2026-11-04 08:00:00', 718695, 0, 136552.05, 855247.05, 4, 'marina_ventas');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0045', 41, '2026-11-03 08:00:00', 359164, 0, 68241.16, 427405.16, 3, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0046', 4, '2026-11-04 08:00:00', 607616, 270528, 64046.72, 401134.72, 5, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0047', 20, '2026-11-04 08:00:00', 887344, 443672, 84297.68, 527969.68, 4, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0048', 4, '2026-11-04 08:00:00', 1034676, 0, 196588.44, 1231264.44, 3, 'cajero01');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0049', 42, '2026-11-04 08:00:00', 422625, 0, 80298.75, 502923.75, 1, 'pedro_pos');
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES ('SALE-0050', 39, '2026-11-04 08:00:00', 486004, 0, 92340.76, 578344.76, 4, 'marina_ventas');











-- DETALLES DE VENTA
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (1, 13, 80, 3431, 0, 0, 0.19, 52151.2, 274480, 326631.2);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (1, 48, 15, 5633, 0, 0, 0.19, 16054.05, 84495, 100549.05);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (2, 29, 34, 2313, 0.5, 39321, 0.19, 7470.99, 78642, 46791.99);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (2, 21, 70, 3066, 0, 0, 0.19, 40777.8, 214620, 255397.8);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (3, 33, 29, 2216, 0.5, 32132, 0.19, 6105.08, 64264, 38237.08);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (3, 4, 67, 2307, 0.5, 77284.5, 0.19, 14684.06, 154569, 91968.56);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (4, 7, 53, 3442, 0, 0, 0.19, 34660.94, 182426, 217086.94);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (4, 46, 20, 6763, 0, 0, 0.19, 25699.4, 135260, 160959.4);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (5, 33, 32, 2216, 0.5, 35456, 0.19, 6736.64, 70912, 42192.64);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (5, 3, 41, 3584, 0, 0, 0.19, 27919.36, 146944, 174863.36);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (6, 46, 66, 6763, 0, 0, 0.19, 84808.02, 446358, 531166.02);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (6, 29, 77, 2313, 0.5, 89050.5, 0.19, 16919.6, 178101, 105970.1);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (7, 34, 30, 2729, 0, 0, 0.19, 15555.3, 81870, 97425.3);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (7, 24, 57, 2782, 0, 0, 0.19, 30129.06, 158574, 188703.06);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (8, 19, 59, 4635, 0, 0, 0.19, 51958.35, 273465, 325423.35);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (8, 27, 53, 4667, 0.5, 123675.5, 0.19, 23498.35, 247351, 147173.85);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (9, 37, 16, 3405, 0, 0, 0.19, 10351.2, 54480, 64831.2);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (9, 41, 52, 2606, 0.5, 67756, 0.19, 12873.64, 135512, 80629.64);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (10, 5, 52, 3780, 0, 0, 0.19, 37346.4, 196560, 233906.4);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (10, 7, 59, 3442, 0, 0, 0.19, 38584.82, 203078, 241662.82);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (11, 19, 42, 4635, 0, 0, 0.19, 36987.3, 194670, 231657.3);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (11, 47, 29, 5235, 0, 0, 0.19, 28844.85, 151815, 180659.85);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (12, 22, 20, 2764, 0, 0, 0.19, 10503.2, 55280, 65783.2);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (12, 8, 28, 4160, 0, 0, 0.19, 22131.2, 116480, 138611.2);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (13, 31, 49, 2936, 0, 0, 0.19, 27334.16, 143864, 171198.16);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (13, 42, 60, 4567, 0, 0, 0.19, 52063.8, 274020, 326083.8);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (14, 9, 20, 7250, 0.5, 72500, 0.19, 13775, 145000, 86275);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (14, 20, 58, 3213, 0.5, 93177, 0.19, 17703.63, 186354, 110880.63);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (15, 42, 52, 4567, 0, 0, 0.19, 45121.96, 237484, 282605.96);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (15, 9, 77, 7250, 0.5, 279125, 0.19, 53033.75, 558250, 332158.75);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (16, 6, 64, 3335, 0, 0, 0.19, 40553.6, 213440, 253993.6);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (16, 33, 56, 2216, 0.5, 62048, 0.19, 11789.12, 124096, 73837.12);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (17, 2, 56, 3819, 0, 0, 0.19, 40634.16, 213864, 254498.16);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (17, 20, 33, 3213, 0.5, 53014.5, 0.19, 10072.76, 106029, 63087.26);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (18, 14, 53, 5065, 0, 0, 0.19, 51004.55, 268445, 319449.55);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (18, 50, 72, 4282, 0, 0, 0.19, 58577.76, 308304, 366881.76);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (19, 13, 38, 3431, 0, 0, 0.19, 24771.82, 130378, 155149.82);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (19, 9, 29, 7250, 0.5, 105125, 0.19, 19973.75, 210250, 125098.75);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (20, 5, 47, 3780, 0, 0, 0.19, 33755.4, 177660, 211415.4);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (20, 7, 74, 3442, 0, 0, 0.19, 48394.52, 254708, 303102.52);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (21, 50, 79, 4282, 0, 0, 0.19, 64272.82, 338278, 402550.82);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (21, 48, 77, 5633, 0, 0, 0.19, 82410.79, 433741, 516151.79);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (22, 3, 53, 3584, 0, 0, 0.19, 36090.88, 189952, 226042.88);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (22, 50, 26, 4282, 0, 0, 0.19, 21153.08, 111332, 132485.08);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (23, 39, 58, 3340, 0, 0, 0.19, 36806.8, 193720, 230526.8);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (23, 10, 30, 3945, 0, 0, 0.19, 22486.5, 118350, 140836.5);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (24, 12, 31, 5006, 0, 0, 0.19, 29485.34, 155186, 184671.34);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (24, 47, 66, 5235, 0, 0, 0.19, 65646.9, 345510, 411156.9);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (25, 3, 62, 3584, 0, 0, 0.19, 42219.52, 222208, 264427.52);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (25, 24, 40, 2782, 0, 0, 0.19, 21143.2, 111280, 132423.2);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (26, 29, 46, 2313, 0.5, 53199, 0.19, 10107.81, 106398, 63306.81);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (26, 49, 67, 11176, 0, 0, 0.19, 142270.48, 748792, 891062.48);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (27, 15, 78, 2789, 0, 0, 0.19, 41332.98, 217542, 258874.98);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (27, 16, 49, 11272, 0.5, 276164, 0.19, 52471.16, 552328, 328635.16);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (28, 31, 34, 2936, 0, 0, 0.19, 18966.56, 99824, 118790.56);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (28, 24, 66, 2782, 0, 0, 0.19, 34886.28, 183612, 218498.28);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (29, 30, 46, 11680, 0, 0, 0.19, 102083.2, 537280, 639363.2);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (29, 50, 58, 4282, 0, 0, 0.19, 47187.64, 248356, 295543.64);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (30, 33, 77, 2216, 0.5, 85316, 0.19, 16210.04, 170632, 101526.04);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (30, 27, 30, 4667, 0.5, 70005, 0.19, 13300.95, 140010, 83305.95);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (31, 13, 27, 3431, 0, 0, 0.19, 17601.03, 92637, 110238.03);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (31, 17, 16, 9506, 0, 0, 0.19, 28898.24, 152096, 180994.24);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (32, 42, 71, 4567, 0, 0, 0.19, 61608.83, 324257, 385865.83);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (32, 24, 80, 2782, 0, 0, 0.19, 42286.4, 222560, 264846.4);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (33, 7, 76, 3442, 0, 0, 0.19, 49702.48, 261592, 311294.48);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (33, 8, 46, 4160, 0, 0, 0.19, 36358.4, 191360, 227718.4);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (34, 6, 30, 3335, 0, 0, 0.19, 19009.5, 100050, 119059.5);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (34, 18, 67, 5027, 0, 0, 0.19, 63993.71, 336809, 400802.71);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (35, 33, 28, 2216, 0.5, 31024, 0.19, 5894.56, 62048, 36918.56);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (35, 28, 21, 6758, 0, 0, 0.19, 26964.42, 141918, 168882.42);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (36, 15, 67, 2789, 0, 0, 0.19, 35503.97, 186863, 222366.97);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (36, 10, 13, 3945, 0, 0, 0.19, 9744.15, 51285, 61029.15);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (37, 27, 16, 4667, 0.5, 37336, 0.19, 7093.84, 74672, 44429.84);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (37, 26, 74, 3822, 0, 0, 0.19, 53737.32, 282828, 336565.32);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (38, 24, 40, 2782, 0, 0, 0.19, 21143.2, 111280, 132423.2);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (38, 25, 20, 4961, 0, 0, 0.19, 18851.8, 99220, 118071.8);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (39, 24, 38, 2782, 0, 0, 0.19, 20086.04, 105716, 125802.04);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (39, 2, 50, 3819, 0, 0, 0.19, 36280.5, 190950, 227230.5);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (40, 7, 52, 3442, 0, 0, 0.19, 34006.96, 178984, 212990.96);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (40, 10, 27, 3945, 0, 0, 0.19, 20237.85, 106515, 126752.85);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (41, 3, 46, 3584, 0, 0, 0.19, 31324.16, 164864, 196188.16);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (41, 31, 27, 2936, 0, 0, 0.19, 15061.68, 79272, 94333.68);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (42, 49, 70, 11176, 0, 0, 0.19, 148640.8, 782320, 930960.8);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (42, 29, 10, 2313, 0.5, 11565, 0.19, 2197.35, 23130, 13762.35);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (43, 6, 12, 3335, 0, 0, 0.19, 7603.8, 40020, 47623.8);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (43, 17, 37, 9506, 0, 0, 0.19, 66827.18, 351722, 418549.18);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (44, 10, 80, 3945, 0, 0, 0.19, 59964, 315600, 375564);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (44, 47, 77, 5235, 0, 0, 0.19, 76588.05, 403095, 479683.05);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (45, 28, 24, 6758, 0, 0, 0.19, 30816.48, 162192, 193008.48);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (45, 50, 46, 4282, 0, 0, 0.19, 37424.68, 196972, 234396.68);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (46, 16, 48, 11272, 0.5, 270528, 0.19, 51400.32, 541056, 321928.32);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (46, 8, 16, 4160, 0, 0, 0.19, 12646.4, 66560, 79206.4);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (47, 16, 63, 11272, 0.5, 355068, 0.19, 67462.92, 710136, 422530.92);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (47, 41, 68, 2606, 0.5, 88604, 0.19, 16834.76, 177208, 105438.76);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (48, 5, 24, 3780, 0, 0, 0.19, 17236.8, 90720, 107956.8);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (48, 32, 78, 12102, 0, 0, 0.19, 179351.64, 943956, 1123307.64);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (49, 2, 75, 3819, 0, 0, 0.19, 54420.75, 286425, 340845.75);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (49, 37, 40, 3405, 0, 0, 0.19, 25878, 136200, 162078);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (50, 46, 28, 6763, 0, 0, 0.19, 35979.16, 189364, 225343.16);
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) VALUES (50, 19, 64, 4635, 0, 0, 0.19, 56361.6, 296640, 353001.6);


-- CONDUCTORES
INSERT INTO conductores (nombre, numero_licencia, telefono, email) VALUES
                                                                       ('Carlos Alberto Gómez', 'LIC-7482910', '3104567890', 'carlos.gomez@frescuratotal.com'),
                                                                       ('Ana María Restrepo', 'LIC-8392019', '3112345678', 'ana.restrepo@frescuratotal.com'),
                                                                       ('Jorge Enrique Pérez', 'LIC-9201928', '3128765432', 'jorge.perez@frescuratotal.com'),
                                                                       ('Diana Marcela Silva', 'LIC-6382910', '3139876541', 'diana.silva@frescuratotal.com'),
                                                                       ('Luis Fernando Morales', 'LIC-5492018', '3145678901', 'luis.morales@frescuratotal.com');

-- VEHICULOS
INSERT INTO vehiculos (placa, modelo, capacidad, estado) VALUES
                                                             ('SXT-412', 'Chevrolet NHR 2023', 2500, 'Activo'),
                                                             ('BOG-983', 'JAC HFC 2022', 3500, 'Activo'),
                                                             ('MDC-204', 'Foton Aumark 2024', 4000, 'Activo'),
                                                             ('CAL-715', 'Hino Dutro 2021', 3000, 'Activo'),
                                                             ('BUC-556', 'Hyundai HD65 2020', 2800, 'Mantenimiento');