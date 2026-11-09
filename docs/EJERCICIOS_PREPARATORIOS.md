# Ejercicios preparatorios de Java · ERP «Frescura Total»

Solución de los 5 ejercicios de la guía de práctica, desarrollada en la rama `feature/ejercicios-preparatorios`.

Todas las rutas de código parten de `src/main/java/com/frescuratotal/erp/` y las de pruebas de `src/test/java/com/frescuratotal/erp/`.
Leyenda: 🆕 archivo nuevo · ✏️ archivo existente modificado.

---

## Ejercicio 1 · Caché de clientes con Singleton

**Menú:** Sales → `6. Find customer by ID`

**Qué hace:** la primera vez que se busca un cliente se consulta la base de datos y el resultado se guarda en memoria. Las siguientes búsquedas del mismo ID usan la copia en memoria sin repetir la consulta SQL. `CustomerCache` es un Singleton: su constructor es privado y su única instancia se obtiene con `getInstance()`, así que `CustomerService` y `SaleService` comparten el mismo almacén. Nunca se guardan clientes inexistentes ni incompletos, y al actualizar o borrar un cliente su copia se elimina de la caché. `CustomerService` implementa `CustomerDao`, por eso los controladores siguen llamando a `findById` igual que antes: la decisión entre memoria y base de datos queda dentro del servicio.

| Archivo | Cambio |
|---|---|
| `sales/model/CustomerCache.java` | 🆕 Caché Singleton (mapa ID → cliente, contadores de aciertos y fallos) |
| `sales/model/CustomerService.java` | 🆕 Servicio de clientes: primero la caché, después la base de datos |
| `sales/model/Customer.java` | ✏️ Constructor de copia, para que la caché entregue copias y nadie modifique lo guardado |
| `sales/model/SaleService.java` | ✏️ Usa el mismo `CustomerService`, y con él la misma caché |
| `sales/controller/SalesController.java` | ✏️ Opción para buscar un cliente por ID; la venta busca el cliente a través de la caché |
| `sales/view/SalesView.java` | ✏️ Muestra el cliente y el estado de la caché |
| `Main.java` | ✏️ Entrega `CustomerService` en lugar del DAO a los módulos de ventas y logística |
| `sales/model/CustomerCacheTest.java` (pruebas) | 🆕 Casos ID 5, ID 5 otra vez, ID 8, otro módulo e ID 999 |
| `sales/model/FakeCustomerDao.java` (pruebas) | 🆕 DAO falso que cuenta cuántas consultas SQL se habrían hecho |

---

## Ejercicio 2 · Reporte diario de compras en CSV

**Menú:** Suppliers → `7. Generate daily purchase report (CSV)`

**Qué hace:** toma la fecha actual con `LocalDate.now()`, sin pedírsela al usuario, y consulta las compras de ese día. El modelo no tiene una tabla `compras`: cada recepción de mercancía es un lote en `lotes_inventario`. Por eso `NumeroCompra` es `id_lote` y `MontoTotal` es `cantidad_inicial × precio_compra`. Crea el archivo `compras_AAAA-MM-DD.csv` en la carpeta desde la que se ejecuta el proyecto, separado por `;`, con la cabecera exacta y montos con punto decimal y dos decimales. Si no hubo compras, crea el archivo solo con la cabecera y avisa al usuario.

| Archivo | Cambio |
|---|---|
| `suppliers/model/PurchaseRecord.java` | 🆕 Una compra: número, proveedor y monto |
| `suppliers/model/PurchaseReport.java` | 🆕 Resultado: ruta del archivo, fecha y cantidad de compras |
| `suppliers/model/PurchaseReportDao.java` | 🆕 Interfaz de acceso a datos |
| `suppliers/model/PurchaseReportDaoImpl.java` | 🆕 SQL con JOIN a `proveedores`, filtrado por la fecha de hoy |
| `suppliers/model/PurchaseReportService.java` | 🆕 Obtiene la fecha, arma el nombre del archivo y escribe el CSV |
| `suppliers/controller/SupplierController.java` | ✏️ Nueva opción y sus mensajes |
| `suppliers/view/SupplierView.java` | ✏️ Nueva línea en el menú |
| `Main.java` | ✏️ Entrega el servicio del reporte al módulo de proveedores |
| `.gitignore` | ✏️ Ignora los `compras_*.csv` generados |
| `suppliers/model/PurchaseReportServiceTest.java` (pruebas) | 🆕 Formato, día sin compras, fecha de hoy y decimales |

---

## Ejercicio 3 · Tarifas de entrega con el patrón Strategy

**Menú:** Sales → `3. New sale` (después de agregar los productos se elige la modalidad de entrega)

**Qué hace:** la interfaz `DeliveryFeeStrategy` define el cálculo del valor final. Hay una estrategia por modalidad: `StandardDeliveryStrategy` (ESTANDAR, 0 %) y `UrgentDeliveryStrategy` (URGENTE, +12 %). `DeliveryFeeService` elige la estrategia con un `EnumMap` y le delega el cálculo, sin `if` ni `switch` con porcentajes. Agregar una modalidad nueva solo requiere una clase nueva y una línea en el mapa. Antes de confirmar la venta se muestra el valor a pagar. La modalidad y el recargo se guardan en la venta y aparecen en la factura.

| Archivo | Cambio |
|---|---|
| `sales/model/delivery/DeliveryFeeStrategy.java` | 🆕 Interfaz común (contrato) |
| `sales/model/delivery/StandardDeliveryStrategy.java` | 🆕 Estrategia ESTANDAR, 0 % |
| `sales/model/delivery/UrgentDeliveryStrategy.java` | 🆕 Estrategia URGENTE, 12 % |
| `sales/model/delivery/DeliveryMode.java` | 🆕 Enum con las modalidades |
| `sales/model/delivery/DeliveryFeeService.java` | 🆕 Elige la estrategia y calcula el valor final |
| `sales/model/delivery/DeliveryAmounts.java` | 🆕 Validación compartida: el valor no puede ser nulo ni negativo |
| `sales/model/Sale.java` | ✏️ Campos `deliveryMode` y `deliveryFee` |
| `sales/model/SaleService.java` | ✏️ `previewTotal` y `confirmSale(sale, mode)` aplican la estrategia |
| `sales/model/SaleDaoImpl.java` | ✏️ Guarda y lee `modalidad_entrega` y `recargo_entrega` |
| `sales/model/InvoiceGenerator.java` | ✏️ La factura indica el recargo de entrega |
| `sales/controller/SalesController.java` | ✏️ Pide la modalidad y muestra el valor antes de confirmar |
| `sales/view/SalesView.java` | ✏️ Menú de modalidades y valor a pagar |
| `docs/database.sql` | ✏️ Columnas `modalidad_entrega` y `recargo_entrega` en `ventas` |
| `docs/migrations/001_ejercicios_preparatorios.sql` | 🆕 Agrega esas columnas a una base ya creada |
| `pom.xml` | ✏️ Dependencia `junit-jupiter-params` para la prueba parametrizada |
| `sales/model/delivery/DeliveryFeeServiceTest.java` (pruebas) | 🆕 Los 5 casos de la guía (100000, 112000, 280000, 50000, 0) |

---

## Ejercicio 4 · Importación masiva de clientes desde texto plano

**Menú:** Sales → `7. Import customers from file`

**Qué hace:** lee `clientes_importar.txt` desde la raíz del proyecto, junto al README. Trabaja en dos pasos:

1. **Validar todo sin guardar nada.** Comprueba que el archivo exista, que la cabecera sea exacta, que cada fila tenga 5 campos no vacíos, que el correo sea válido, que el tipo sea `MAYORISTA` o `MINORISTA` y que ningún documento esté repetido en el archivo ni exista ya en la base. Las líneas vacías se ignoran. Si hay errores, los lista todos y cancela.
2. **Guardar todo en una transacción.** Si cualquier inserción falla, se hace *rollback* y no queda ningún registro guardado a medias.

| Archivo | Cambio |
|---|---|
| `sales/model/CustomerImportService.java` | 🆕 Lectura, validación y guardado con transacción |
| `sales/model/CustomerImportResult.java` | 🆕 Resultado: archivo no encontrado, inválido, fallo al guardar o completado |
| `sales/controller/SalesController.java` | ✏️ Nueva opción con los mensajes exactos de la guía |
| `sales/view/SalesView.java` | ✏️ Lista de errores por línea |
| `Main.java` | ✏️ Entrega el servicio de importación al módulo de ventas |
| `clientes_importar.txt` (raíz) | 🆕 Archivo de ejemplo con los 5 clientes de la guía |
| `sales/model/CustomerImportServiceTest.java` (pruebas) | 🆕 Archivo inexistente, cabecera incorrecta, 4 campos, duplicados, todo válido, línea vacía y fallo al insertar |

---

## Ejercicio 5 · Estadísticas comerciales con Stream API

**Menú:** Sales → `8. Generate commercial statistics` (y `9. Register sale return` para marcar devoluciones)

**Qué hace:** un JOIN entre `ventas`, `detalles_venta`, `lotes_inventario`, `productos`, `categorias_producto` y `clientes` se convierte en objetos `SaleStatisticsRow`. Con Stream API, sin `for` ni `while`:
- **Categoría con más unidades:** `filter` deja solo las ventas confirmadas, `groupingBy` y `reducing` suman las cantidades por categoría y `max` elige la mayor.
- **Clientes con devoluciones:** `filter` deja las ventas devueltas, `map` toma el nombre y `distinct` quita los repetidos.

El ERP no tenía los estados CONFIRMADA ni DEVUELTA. Se agregó `RETURNED` (Devuelta), y se considera confirmada toda venta que no esté cancelada ni devuelta. Si no hay ventas, se muestra el mensaje informativo de la guía.

| Archivo | Cambio |
|---|---|
| `sales/model/stats/SaleStatisticsRow.java` | 🆕 Una fila del JOIN convertida en objeto |
| `sales/model/stats/SaleStatisticsDao.java` | 🆕 Interfaz de acceso a datos |
| `sales/model/stats/SaleStatisticsDaoImpl.java` | 🆕 Consulta con los JOIN |
| `sales/model/stats/CommercialStatisticsService.java` | 🆕 Cálculos con Stream API |
| `sales/model/stats/CategoryTotal.java` | 🆕 Categoría y total de unidades |
| `sales/model/stats/CommercialStatistics.java` | 🆕 Resultado completo de las estadísticas |
| `sales/model/SaleStatus.java` | ✏️ Estado `RETURNED` (id 6) y método `isConfirmed()` |
| `sales/model/SaleService.java` | ✏️ `registerReturn`: solo una venta entregada puede devolverse |
| `sales/controller/SalesController.java` | ✏️ Opciones de estadísticas y de devolución |
| `sales/view/SalesView.java` | ✏️ Salida con el formato de la guía |
| `Main.java` | ✏️ Entrega el servicio de estadísticas al módulo de ventas |
| `docs/database.sql` | ✏️ Estado `Devuelta` en `estados_venta` |
| `docs/migrations/001_ejercicios_preparatorios.sql` | 🆕 Agrega ese estado a una base ya creada |
| `sales/model/stats/CommercialStatisticsServiceTest.java` (pruebas) | 🆕 Datos de ejemplo de la guía, empate, sin ventas y sin devoluciones |

---

## Antes de ejecutar

- **Base de datos existente:** ejecutar `docs/migrations/001_ejercicios_preparatorios.sql`.
- **Base nueva:** basta con `docs/database.sql`, que ya incluye los cambios.
- Ejecutar el programa desde la raíz del proyecto, para que encuentre `clientes_importar.txt` y genere ahí el CSV de compras.
- Pruebas: `mvn test`.
