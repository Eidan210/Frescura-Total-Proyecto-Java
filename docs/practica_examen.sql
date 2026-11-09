-- =====================================================================
-- PRÁCTICA EXAMEN - script de apoyo (ejecutar en MySQL Workbench)
-- =====================================================================
USE erp_frescura_total;

-- ---------------------------------------------------------------------
-- EJERCICIO 5: estado "Devuelta" (id 6). Debe coincidir con SaleStatus.RETURNED(6).
-- INSERT IGNORE: si ya existe, no da error.
-- ---------------------------------------------------------------------
INSERT IGNORE INTO estados_venta (id_estado, nombre, descripcion)
VALUES (6, 'Devuelta', 'Venta devuelta por el cliente');

-- Para PROBAR devoluciones: marca 2 ventas como devueltas (cambia los ids por ventas tuyas).
-- Primero mira cuáles hay:
SELECT id_venta, numero_venta, id_cliente, id_estado FROM ventas ORDER BY id_venta DESC LIMIT 10;
-- UPDATE ventas SET id_estado = 6 WHERE id_venta IN (1, 2);
-- Para deshacerlo:
-- UPDATE ventas SET id_estado = 1 WHERE id_venta IN (1, 2);

-- Ver lo que leerá el ejercicio 5 (mismo JOIN que SalesStatsDaoImpl):
SELECT v.id_venta, dv.cantidad_vendida, c.nombre AS categoria, cl.nombre AS cliente, v.id_estado
FROM ventas v
JOIN detalles_venta dv     ON dv.id_venta = v.id_venta
JOIN lotes_inventario l    ON l.id_lote = dv.id_lote
JOIN productos p           ON p.id_producto = l.id_producto
JOIN categorias_producto c ON c.id_categoria = p.id_categoria
JOIN clientes cl           ON cl.id_cliente = v.id_cliente;

-- ---------------------------------------------------------------------
-- EJERCICIO 2: ver las "compras" (recepciones de lotes) de HOY.
-- Para tener compras de hoy: menú Inventory -> 3 (Register goods reception).
-- ---------------------------------------------------------------------
SELECT l.numero_lote, p.nombre AS proveedor, l.cantidad_inicial * l.precio_compra AS monto_total
FROM lotes_inventario l
JOIN proveedores p ON p.id_proveedor = l.id_proveedor
WHERE DATE(l.fecha_ingreso) = CURDATE();

-- ---------------------------------------------------------------------
-- EJERCICIO 1: IDs de clientes que existen (para la prueba con ID 5 y 8).
-- ---------------------------------------------------------------------
SELECT id_cliente, nombre FROM clientes ORDER BY id_cliente LIMIT 10;

-- ---------------------------------------------------------------------
-- EJERCICIO 4: borrar los clientes importados para repetir la prueba.
-- (solo funciona si esos clientes no tienen ventas)
-- ---------------------------------------------------------------------
-- DELETE FROM clientes WHERE documento IN ('901234567','1020304050','800456789','1080998877','1099887766');
