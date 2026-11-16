-- ============================================================
-- SISTEMA DE GESTIÓN Y AUDITORÍA DE BODEGAS
-- MySQL 8.x — Modelo plano, sin herencia
-- 10 tablas + 1 vista
-- ============================================================

-- ------------------------------------------------------------
-- 0. BASE DE DATOS
-- ------------------------------------------------------------

DROP DATABASE IF EXISTS logitrack_jkq;

CREATE DATABASE logitrack_jkq
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE logitrack_jkq;

-- ------------------------------------------------------------
-- 1. DEPARTAMENTO (independiente)
-- ------------------------------------------------------------

CREATE TABLE departamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    pais VARCHAR(100) NOT NULL DEFAULT 'Colombia'
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. CIUDAD (FK a departamento)
-- ------------------------------------------------------------

CREATE TABLE ciudad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20),
    departamento_id BIGINT,
    CONSTRAINT uk_ciudad_nombre_depto UNIQUE (nombre, departamento_id),
    CONSTRAINT fk_ciudad_departamento FOREIGN KEY (departamento_id)
        REFERENCES departamento(id)
) ENGINE=InnoDB;

CREATE INDEX idx_ciudad_departamento ON ciudad(departamento_id);

-- ------------------------------------------------------------
-- 3. USUARIO (FK a ciudad)
-- ------------------------------------------------------------

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento VARCHAR(30) UNIQUE,
    email VARCHAR(150) UNIQUE,
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    ciudad_id BIGINT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN','EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
    turno VARCHAR(50),
    fecha_ingreso DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_ciudad FOREIGN KEY (ciudad_id) REFERENCES ciudad(id)
) ENGINE=InnoDB;

CREATE INDEX idx_usuario_ciudad ON usuario(ciudad_id);
CREATE INDEX idx_usuario_rol ON usuario(rol);

-- ------------------------------------------------------------
-- 4. SOLICITUD_REGISTRO (FK a ciudad y a usuario)
-- ------------------------------------------------------------

CREATE TABLE solicitud_registro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento VARCHAR(30),
    email VARCHAR(150),
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    ciudad_id BIGINT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol_solicitado ENUM('ADMIN','EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
    turno VARCHAR(50),
    fecha_ingreso DATE,
    estado ENUM('PENDIENTE','APROBADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo VARCHAR(255),
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME,
    resuelto_por_id BIGINT,
    usuario_creado_id BIGINT,
    CONSTRAINT fk_solicitud_ciudad FOREIGN KEY (ciudad_id) REFERENCES ciudad(id),
    CONSTRAINT fk_solicitud_resuelto_por FOREIGN KEY (resuelto_por_id) REFERENCES usuario(id),
    CONSTRAINT fk_solicitud_usuario_creado FOREIGN KEY (usuario_creado_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE INDEX idx_solicitud_estado ON solicitud_registro(estado);
CREATE INDEX idx_solicitud_fecha ON solicitud_registro(fecha_solicitud);
CREATE INDEX idx_solicitud_email ON solicitud_registro(email);
CREATE INDEX idx_solicitud_username ON solicitud_registro(username);
CREATE INDEX idx_solicitud_usuario_creado ON solicitud_registro(usuario_creado_id);

-- ------------------------------------------------------------
-- 5. BODEGA (FK a usuario y ciudad)
-- ------------------------------------------------------------

CREATE TABLE bodega (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    ubicacion VARCHAR(200) NOT NULL,
    capacidad INT NOT NULL,
    encargado_id BIGINT,
    ciudad_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bodega_encargado FOREIGN KEY (encargado_id) REFERENCES usuario(id),
    CONSTRAINT fk_bodega_ciudad FOREIGN KEY (ciudad_id) REFERENCES ciudad(id)
) ENGINE=InnoDB;

CREATE INDEX idx_bodega_ciudad ON bodega(ciudad_id);
CREATE INDEX idx_bodega_encargado ON bodega(encargado_id);

-- ------------------------------------------------------------
-- 6. PRODUCTO (catálogo puro, SIN stock)
--  CORREGIDO: sin coma final, con stock_minimo y fecha_actualizacion
-- ------------------------------------------------------------

CREATE TABLE producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    stock_minimo INT NOT NULL DEFAULT 10,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_producto_categoria ON producto(categoria);

-- ------------------------------------------------------------
-- 7. STOCK_BODEGA (FK a bodega y producto)
-- ------------------------------------------------------------

CREATE TABLE stock_bodega (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bodega_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_bodega_producto UNIQUE (bodega_id, producto_id),
    CONSTRAINT fk_stock_bodega FOREIGN KEY (bodega_id) REFERENCES bodega(id),
    CONSTRAINT fk_stock_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT chk_stock_no_negativo CHECK (cantidad >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_stock_producto ON stock_bodega(producto_id);

-- ------------------------------------------------------------
-- 8. MOVIMIENTO (FK a usuario y bodegas)
-- ------------------------------------------------------------

CREATE TABLE movimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('ENTRADA','SALIDA','TRANSFERENCIA') NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    bodega_origen_id BIGINT,
    bodega_destino_id BIGINT,
    observacion VARCHAR(255),
    CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_mov_origen FOREIGN KEY (bodega_origen_id) REFERENCES bodega(id),
    CONSTRAINT fk_mov_destino FOREIGN KEY (bodega_destino_id) REFERENCES bodega(id)
) ENGINE=InnoDB;

CREATE INDEX idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX idx_movimiento_fecha_tipo ON movimiento(fecha, tipo);
CREATE INDEX idx_movimiento_tipo ON movimiento(tipo);
CREATE INDEX idx_movimiento_usuario ON movimiento(usuario_id);

-- ------------------------------------------------------------
-- 9. DETALLE_MOVIMIENTO (FK a movimiento y producto)
--  CORREGIDO: con las 4 columnas de snapshot
-- ------------------------------------------------------------

CREATE TABLE detalle_movimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    CONSTRAINT fk_det_mov FOREIGN KEY (movimiento_id)
        REFERENCES movimiento(id) ON DELETE CASCADE,
    CONSTRAINT fk_det_producto FOREIGN KEY (producto_id) REFERENCES producto(id)
) ENGINE=InnoDB;

CREATE INDEX idx_detalle_movimiento ON detalle_movimiento(movimiento_id);

-- ------------------------------------------------------------
-- 10. AUDITORIA (FK a usuario)
-- ------------------------------------------------------------

CREATE TABLE auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operacion ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT,
    entidad_afectada VARCHAR(100) NOT NULL,
    registro_id BIGINT,
    valores_anteriores JSON,
    valores_nuevos JSON,
    ip_origen VARCHAR(45),
    user_agent VARCHAR(255),
    CONSTRAINT fk_aud_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
CREATE INDEX idx_auditoria_operacion ON auditoria(operacion);
CREATE INDEX idx_auditoria_fecha ON auditoria(fecha_hora);
CREATE INDEX idx_auditoria_entidad ON auditoria(entidad_afectada);
CREATE INDEX idx_auditoria_usuario_operacion ON auditoria(usuario_id, operacion);

-- ------------------------------------------------------------
-- 11. VISTA: STOCK GLOBAL POR PRODUCTO
-- ------------------------------------------------------------

CREATE VIEW v_stock_global AS
SELECT
    p.id            AS producto_id,
    p.nombre        AS producto_nombre,
    p.categoria     AS categoria,
    p.stock_minimo  AS stock_minimo,
    COALESCE(SUM(sb.cantidad), 0) AS stock_total
FROM producto p
LEFT JOIN stock_bodega sb ON sb.producto_id = p.id
GROUP BY p.id, p.nombre, p.categoria, p.stock_minimo;

-- ============================================================
-- DATOS INICIALES
-- ============================================================

USE logitrack_jkq;

-- Geografía
INSERT INTO departamento (nombre, pais) VALUES
('Valle del Cauca', 'Colombia'),
('Antioquia', 'Colombia'),
('Cundinamarca', 'Colombia');

INSERT INTO ciudad (nombre, codigo_postal, departamento_id) VALUES
('Cali', '760001', 1),
('Palmira', '763531', 1),
('Medellín', '050001', 2),
('Bogotá', '110111', 3);

-- Usuarios (persona + empleado en una sola tabla)
-- password = BCrypt de "admin123" / "empleado123" (reemplaza por hashes reales)
INSERT INTO usuario
(nombre, apellido, documento, email, telefono, direccion, ciudad_id,
 username, password, rol, turno, fecha_ingreso, activo)
VALUES
('Ana',   'Gómez',    '1001', 'ana.gomez@campus.edu',    '3001112233', 'Calle 1 #2-3',   1,
 'admin',     '$2a$10$REEMPLAZA_HASH_ADMIN',    'ADMIN',    'Completo', '2023-06-01', TRUE),
('Luis',  'Pérez',    '1002', 'luis.perez@campus.edu',   '3004445566', 'Carrera 4 #5-6', 1,
 'empleado1', '$2a$10$REEMPLAZA_HASH_EMPLEADO', 'EMPLEADO', 'Mañana',   '2024-01-15', TRUE),
('María', 'Rodríguez','1003', 'maria.rodriguez@campus.edu','3007778899','Av. 7 #8-9',    2,
 'empleado2', '$2a$10$REEMPLAZA_HASH_EMPLEADO', 'EMPLEADO', 'Tarde',    '2024-03-01', TRUE);

-- Bodegas
INSERT INTO bodega (nombre, ubicacion, capacidad, encargado_id, ciudad_id) VALUES
('Bodega Central', 'Edificio A, piso 1', 500, 2, 1),
('Bodega Norte',   'Bloque B, piso 2',   300, 3, 2);

-- Productos (SIN stock; el stock vive en stock_bodega)
INSERT INTO producto (nombre, categoria, precio, stock_minimo) VALUES
('Resma papel A4',  'Papelería', 12500.00, 20),
('Bolígrafo azul',  'Papelería',  1500.00, 10),
('Café molido 500g','Alimentos', 18000.00, 15),
('Mouse USB',       'Tecnología', 35000.00, 10);

-- Stock por bodega (única fuente de stock)
INSERT INTO stock_bodega (bodega_id, producto_id, cantidad) VALUES
(1, 1, 80), (2, 1, 40),
(1, 2, 5),  (2, 2, 3),
(1, 3, 30), (2, 3, 15),
(1, 4, 3),  (2, 4, 2);

-- Solicitud de ejemplo pendiente (para probar el flujo de aprobación)
INSERT INTO solicitud_registro
(nombre, apellido, documento, email, telefono, direccion, ciudad_id,
 username, password, rol_solicitado, turno, fecha_ingreso, estado)
VALUES
('Carlos', 'Ramírez', '1004', 'carlos.ramirez@campus.edu', '3009998877',
 'Calle 10 #11-12', 1, 'carlos.ramirez', '$2a$10$REEMPLAZA_HASH_EMPLEADO',
 'EMPLEADO', 'Mañana', '2024-05-01', 'PENDIENTE');

-- Movimiento de ejemplo: transferencia de 10 resmas de Central a Norte
INSERT INTO movimiento (tipo, usuario_id, bodega_origen_id, bodega_destino_id, observacion)
VALUES ('TRANSFERENCIA', 2, 1, 2, 'Reabastecimiento semanal');

-- Snapshot de stock antes/después (valores previos al ajuste manual de abajo)
INSERT INTO detalle_movimiento
(movimiento_id, producto_id, cantidad,
 stock_origen_antes, stock_origen_despues,
 stock_destino_antes, stock_destino_despues)
VALUES
(LAST_INSERT_ID(), 1, 10, 80, 70, 40, 50);

-- Ajustar stock (en producción lo hace el MovimientoService en @Transactional)
UPDATE stock_bodega SET cantidad = cantidad - 10 WHERE bodega_id = 1 AND producto_id = 1;
UPDATE stock_bodega SET cantidad = cantidad + 10 WHERE bodega_id = 2 AND producto_id = 1;

-- Auditoría de ejemplo
INSERT INTO auditoria
(operacion, usuario_id, entidad_afectada, registro_id, valores_nuevos, ip_origen, user_agent)
VALUES
('INSERT', 2, 'Movimiento', 1,
 '{"tipo":"TRANSFERENCIA","productoId":1,"cantidad":10}',
 '127.0.0.1', 'PostmanRuntime/7.x');