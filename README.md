<div align="center">

<img src="docs/assets/banner.svg" alt="ERP Frescura Total" width="100%"/>

<br/>

![Java 21](https://img.shields.io/badge/Java-21-047857?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-047857?style=for-the-badge&logo=apachemaven&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-database-047857?style=for-the-badge&logo=mysql&logoColor=white)
![Consola](https://img.shields.io/badge/Interfaz-consola-047857?style=for-the-badge&logo=windowsterminal&logoColor=white)

**Sistema de gestión para la bodega de alimentos Frescura Total.**
Controla lotes, ventas, proveedores y entregas desde la consola.

[Módulos](#módulos) ·
[Flujo](#flujo-del-negocio) ·
[Arquitectura](#arquitectura) ·
[Instalación](#instalación) ·
[Patrones](#principios-y-patrones-de-diseño) ·
[Base de datos](#base-de-datos) ·
[Autores](#autores)

</div>

---

## Sobre el proyecto

En una bodega de alimentos, un lote que vence sin venderse es dinero perdido. Este ERP lleva cada producto desde que llega del proveedor hasta que se entrega al cliente: prioriza los lotes que vencen primero (FEFO), aísla los que tienen problemas de calidad y permite rastrear a qué clientes llegó cada lote.

## Módulos

<img src="docs/assets/modules.svg" alt="Módulos: Inventario, Ventas, Proveedores y Logística" width="100%"/>

<details>
<summary><b>Ver las opciones de cada menú</b></summary>

<br/>

| Módulo | Opciones en la aplicación |
|---|---|
| **Inventory** | List all lots · List available lots by product (FEFO) · Register goods reception · Put lot in quarantine · Inspect quarantined lot · Register waste · Run expiration check · Trace lot · List lots by status |
| **Sales** | Register customer · List customers · New sale · List sales · Send sale to preparation (logistics) |
| **Suppliers** | Register supplier · List suppliers · Find supplier by ID · Update supplier · Delete supplier · Search supplier by name |
| **Logistics** | Dispatch panel · Create delivery route · List delivery routes · Mark sale as delivered · Register vehicle · Register driver · List vehicles · List drivers |

</details>

## Flujo del negocio

<img src="docs/assets/flow.svg" alt="Proveedor, lote, venta, factura, ruta y entrega" width="100%"/>

| Entidad | Estados |
|---|---|
| **Lote** | `AVAILABLE` · `NEAR_EXPIRY` · `EXPIRED` · `IN_QUARANTINE` |
| **Venta** | `REGISTERED` → `IN_PREPARATION` → `IN_TRANSIT` → `DELIVERED` |
| **Ruta** | `PLANNED` · `IN_TRANSIT` · `COMPLETED` · `CANCELLED` |

> [!NOTE]
> Las rutas se crean directamente en `IN_TRANSIT` y pasan a `COMPLETED` al terminar. `PLANNED` y `CANCELLED` existen en el modelo, pero todavía ninguna opción del menú los asigna.

## Arquitectura

Cada módulo sigue el patrón MVC con DAO para el acceso a datos. Inventario y Ventas añaden una capa de servicio con las reglas de negocio; en los demás casos, el controller llama directamente al DAO. `DatabaseConnection` es un singleton que comparte la conexión JDBC.

<img src="docs/assets/architecture.svg" alt="Main, Controller, View, Service, DAO y MySQL" width="100%"/>

```text
src/main/java/com/frescuratotal/erp/
├── Main.java        Punto de entrada y menú principal
├── common/          Conexión, utilidades y excepciones
├── inventory/       controller · model · view
├── sales/           controller · model · view
├── suppliers/       controller · model · view
└── logistics/       controller · model · view · Dao
```

## Principios y patrones de diseño

### Modelo-Vista-Controlador (MVC)

Cada módulo (`inventory`, `sales`, `suppliers`, `logistics`) separa sus clases en tres paquetes. La **vista** (por ejemplo `SalesView`) solo muestra menús y lee lo que escribe el usuario; el **controlador** (`SalesController`) recibe la opción y decide a quién llamar; el **modelo** (entidades, servicios y DAO) contiene los datos y las reglas del negocio.

Lo elegimos para que cada parte tenga un solo trabajo: si mañana la consola se cambia por una interfaz gráfica, solo se reemplaza la vista. Además, cada integrante pudo trabajar en su propio paquete sin pisar el código de los demás.

### DAO (Data Access Object)

Todo el acceso a MySQL pasa por pares interfaz + implementación: `CustomerDao` / `CustomerDaoImpl`, `SaleDao` / `SaleDaoImpl`, `LotDao` / `LotDaoImpl`, `SupplierDao`, `DriverDao`, entre otros. La interfaz dice **qué** operaciones existen (guardar, buscar, actualizar, borrar) y la implementación dice **cómo** se hacen con SQL.

Así el resto del programa no depende de MySQL: si cambia la base de datos, solo cambia la implementación. Todas las consultas usan `PreparedStatement` con parámetros `?`, lo que evita la inyección SQL.

### Singleton

`DatabaseConnection` tiene un constructor privado y un método `getInstance()` que siempre devuelve la misma instancia. De esta forma todo el programa comparte **una sola conexión JDBC**, configurada una vez con los datos de `db.properties` (leídos por `DatabaseConfig`).

### Capa de servicio

`InventoryService` y `SaleService` reúnen las reglas del negocio para que los controladores y los DAO no las mezclen. En ventas, `SaleService` aplica la estrategia **FEFO** (usa `findAvailableLots`, que ordena los lotes por fecha de vencimiento), reparte un pedido entre varios lotes cuando uno no alcanza, aplica el **50 % de descuento** a los lotes próximos a vencer, calcula el **IVA del 19 %** sobre el valor con descuento y lanza `InsufficientStockException` si no hay inventario suficiente.

### Transacciones

Confirmar una venta toca tres tablas: `ventas`, `detalles_venta` y `lotes_inventario`. `SaleDaoImpl.save` las actualiza dentro de **una sola transacción** (`setAutoCommit(false)`, `commit` y `rollback`): si algo falla a mitad de camino, se deshace todo y el inventario nunca queda descuadrado. El `UPDATE ... AND cantidad >= ?` impide vender más de lo que tiene un lote. Logística usa el mismo mecanismo al asignar rutas de entrega.

### Principios SOLID

- **Responsabilidad única (S):** cada clase tiene un solo trabajo. La vista muestra, el controlador coordina, el servicio aplica reglas, el DAO guarda y consulta, e `InvoiceGenerator` solo construye el CUFE, el QR y la imagen de la factura.
- **Abierto/cerrado (O):** se puede agregar una nueva implementación de un DAO sin modificar las clases que lo usan.
- **Sustitución de Liskov (L):** cualquier implementación de `CustomerDao` puede reemplazar a `CustomerDaoImpl` sin que el controlador lo note, porque cumple el mismo contrato.
- **Segregación de interfaces (I):** en lugar de una interfaz gigante, cada entidad tiene su propia interfaz pequeña (`CustomerDao`, `SaleDao`, `InvoiceDao`, `LotDao`...).
- **Inversión de dependencias (D):** los controladores y servicios reciben interfaces, no clases concretas (por ejemplo `InventoryService(LotDao, ProductDao, SupplierDao, QualityDao)` o `LogisticsController(..., SaleDao, CustomerDao, ...)`). `Main` es quien decide qué implementación usar.

### Enums con identificador y records

Los catálogos de la base de datos se representan con enums (`CustomerType`, `SaleStatus`, `LotStatus`). Cada opción guarda su id de la tabla: `getId()` la convierte en número para guardarla y `fromId()` hace el camino contrario al leerla. Para resultados que solo transportan datos se usan `record` (`SaleReceipt`, `LotTrace`, `LotDelivery`).

## Tecnologías

| Tecnología | Uso |
|---|---|
| **Java 21** | Lenguaje de la aplicación |
| **Maven** | Compilación y dependencias |
| **MySQL** + `mysql-connector-j` | Persistencia mediante JDBC |
| **ZXing** | Código QR de las facturas |

## Instalación

### Requisitos

- JDK 21
- MySQL
- Maven o IntelliJ IDEA

### Pasos

**1. Clona el repositorio**

```bash
git clone https://github.com/Eidan210/Frescura-Total-Proyecto-Java.git
cd Frescura-Total-Proyecto-Java
```

**2. Crea la base de datos con sus datos de prueba**

```bash
mysql -u root -p < docs/database.sql
```

> [!WARNING]
> El script ejecuta `DROP DATABASE IF EXISTS erp_frescura_total`. Si ya tienes esa base de datos, se borra por completo.

**3. Configura la conexión**

Copia `src/main/resources/db.properties.example` como `src/main/resources/db.properties` y pon tu contraseña:

```properties
db.url=jdbc:mysql://localhost:3306/erp_frescura_total
db.user=root
db.password=your_password_here
```

> [!NOTE]
> `db.properties` está en `.gitignore`, así que tus credenciales no se suben al repositorio.

**4. Ejecuta la aplicación**

Abre el proyecto en IntelliJ y ejecuta `Main`, o usa Maven:

```bash
mvn compile exec:java -Dexec.mainClass=com.frescuratotal.erp.Main
```

Las facturas se guardan como PNG en la carpeta `invoices/`.

## Base de datos

El script [`docs/database.sql`](docs/database.sql) crea 22 tablas y carga datos de prueba.

| Área | Tablas |
|---|---|
| **Catálogos** | `estados_lote` · `tipos_cliente` · `estados_venta` · `categorias_producto` |
| **Empresa** | `informacion_empresa` |
| **Proveedores y productos** | `proveedores` · `productos` · `productos_proveedor` |
| **Inventario y calidad** | `lotes_inventario` · `registro_cuarentena` · `inspecciones_calidad` · `registro_mermas` |
| **Promociones** | `promociones` |
| **Clientes y ventas** | `clientes` · `ventas` · `detalles_venta` · `facturas` |
| **Logística** | `conductores` · `vehiculos` · `rutas_entrega` · `asignaciones_entrega` · `ventas_ruta` |

<details>
<summary><b>Diagrama entidad-relación</b></summary>
<br/>
<img src="docs/diagrama_er.png" alt="Diagrama entidad-relación" width="100%"/>
</details>

<details>
<summary><b>Diagrama de clases</b></summary>
<br/>
<img src="docs/diagrama_clases.png" alt="Diagrama de clases" width="100%"/>
</details>

## Flujo de trabajo

| Rama | Propósito |
|---|---|
| `main` | Versión estable |
| `develop` | Integración del equipo |
| `feature/*`, `fix/*`, `docs/*` | Cambios puntuales, que entran a `develop` por pull request |

## Autores

| Nombre | GitHub |
| --- | --- |
| Eidan Cuadros | [Eidan210](https://github.com/Eidan210) |
| Adriana Marcela Reatigui Mateus | [reatiguiadriana901](https://github.com/reatiguiadriana901) |
| Sergio Daniel Flórez López | [sergioRepository](https://github.com/sergioRepository) |

<div align="center">
<br/>
<sub>Hecho por el equipo de Frescura Total</sub>
</div>
