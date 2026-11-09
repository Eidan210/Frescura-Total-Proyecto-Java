-- =============================================
-- Migración para bases creadas con una versión anterior de docs/database.sql
-- Ejercicios preparatorios (entrega a domicilio y devoluciones).
-- Si la base se crea desde cero con docs/database.sql NO hace falta ejecutarla.
-- =============================================
SET NAMES utf8mb4;
USE erp_frescura_total;

-- Ejercicio 3: modalidad de entrega y su recargo en cada venta
ALTER TABLE ventas
    ADD COLUMN modalidad_entrega VARCHAR(20) NOT NULL DEFAULT 'STANDARD'
        COMMENT 'STANDARD (0%) o URGENT (+12%)' AFTER usuario_registro,
    ADD COLUMN recargo_entrega DECIMAL(12, 2) NOT NULL DEFAULT 0.00
        COMMENT 'Recargo de la entrega, ya incluido en monto_total' AFTER modalidad_entrega;

-- Ejercicio 5: estado para las ventas devueltas (id 6 = SaleStatus.RETURNED)
INSERT INTO estados_venta (id_estado, nombre, descripcion)
VALUES (6, 'Devuelta', 'Venta entregada que el cliente devolvió');
