(1H) ERP "Frescura Total" - Estadísticas de Rendimiento de Proveedores

Contexto del Requerimiento


El departamento de compras de "Frescura Total" necesita herramientas más potentes para evaluar a sus proveedores. Ya no es suficiente saber quiénes son; ahora necesitan datos concretos sobre su rendimiento para negociar mejores condiciones y asegurar la calidad del inventario.



Como desarrollador principal del ERP, se te ha encargado crear un nuevo reporte de analítica que proporcione dos indicadores de rendimiento clave de forma rápida y directa desde el menú principal del sistema. Esta nueva herramienta permitirá al equipo de compras identificar tanto a sus socios más importantes como a aquellos que podrían representar un riesgo para la calidad del inventario.



Descripción Detallada del Requerimiento
Tu tarea es implementar una nueva funcionalidad que, al ser seleccionada desde el menú, realice un análisis de todos los datos del inventario y presente en consola dos estadísticas clave sobre los proveedores.



1. Integración en el Menú Principal
   Debes añadir una nueva opción claramente visible en la interfaz de tu aplicación, por ejemplo: "Generar Reporte de Rendimiento de Proveedores".



2. Obtención de Datos desde la Base de Datos
   Antes de procesar los datos con Stream API, debes consultar la base de datos para obtener la información necesaria. Tu consulta SQL debe relacionar las tablas correspondientes para traer los datos completos.



Consulta SQL de Referencia:

A continuación se proporciona un ejemplo de consulta SQL que obtiene toda la información necesaria para generar las estadísticas. Puedes usar esta consulta como referencia o adaptarla según tu implementación:



SELECT
l.id_lote,
l.cantidad_inicial,
e.nombre AS estado_lote,
p.id_proveedor,
p.nombre AS nombre_proveedor
FROM lotes_inventario l
INNER JOIN proveedores p ON l.id_proveedor = p.id_proveedor
INNER JOIN estados_lote e ON l.id_estado = e.id_estado;


Esta consulta te proporciona para cada lote:

El identificador del lote
La cantidad inicial de productos
El nombre del estado del lote (Disponible, Vencido, etc.)
El identificador del proveedor
El nombre del proveedor


Una vez que hayas obtenido estos datos y los hayas mapeado a objetos Java, deberás procesarlos usando Stream API para calcular las estadísticas requeridas.



3. Lógica de Cálculo (Restricción Tecnológica)
   El núcleo de tu implementación, es decir, la lógica para procesar los datos obtenidos de la base de datos y calcular ambas estadísticas, debe utilizar obligatoriamente la API de Streams de Java. El objetivo principal de este examen es evaluar tu dominio en el procesamiento declarativo de colecciones de datos.



4. Estadística 1: Identificación del Proveedor con Mayor Volumen
   Tu lógica debe analizar todos los lotes obtenidos de la consulta.
   Para cada proveedor, debe calcular el volumen total de productos suministrados, sumando la cantidad inicial de todos sus lotes.
   Finalmente, debe identificar y mostrar en consola el nombre del proveedor que tenga el mayor volumen total acumulado.
   Si existe un empate (dos o más proveedores con el mismo volumen máximo), puedes mostrar cualquiera de ellos.


5. Estadística 2: Lista de Proveedores con Lotes Vencidos
   Tu lógica debe primero filtrar todos los lotes para encontrar únicamente aquellos cuyo estado sea "Vencido".
   A partir de este grupo de lotes vencidos, debes compilar una lista con los nombres de los proveedores asociados.
   La lista final que se muestre en consola no debe contener nombres de proveedores duplicados, incluso si un mismo proveedor tiene múltiples lotes vencidos.

6. Presentación de Resultados
   Los resultados de ambos cálculos deben mostrarse de forma clara en la consola, siguiendo un formato como el del ejemplo que se muestra más adelante.



7. Manejo del Caso Vacío
   Si el sistema no tiene ningún lote de inventario registrado y, por lo tanto, no puede realizar los cálculos, no debe fallar. En su lugar, debe mostrar un mensaje informativo como: "No hay datos de inventario suficientes para generar estadísticas."



Ejemplo de Salida en Consola




*** Reporte de Rendimiento de Proveedores ***

>> Proveedor con Mayor Volumen de Suministro:
Lácteos del Campo

>> Proveedores con Lotes Vencidos Registrados:
- Frutas del Valle
- Carnes La Excelencia


Resultado esperado

1. Código Fuente y Estructura del Proyecto
   Nuevas Clases: Se espera que toda la lógica para los cálculos estadísticos esté encapsulada en una nueva clase ubicada dentro de un paquete apropiado, por ejemplo: com.frescuratotal.erp.analytics o com.frescuratotal.erp.suppliers.analytics.

Modificación de Código Existente: Deberás modificar las clases de la vista y/o controlador del menú principal para añadir la nueva opción que invoque esta funcionalidad.



2. Gestión de Versiones con Git y GitHub
   Rama de Desarrollo: Todo tu trabajo debe realizarse en una nueva rama llamada feat/supplier-analytics-report.

Commits Atómicos: Debes registrar tu progreso en un mínimo de dos commits separados:

Primer Commit: Con la implementación de la clase de servicio, la consulta SQL y toda la lógica de cálculo con Streams. Mensaje sugerido: "feat: Implement supplier analytics service with Stream API".
Segundo Commit: Con la integración de la nueva funcionalidad en el menú principal. Mensaje sugerido: "feat: Add supplier analytics option to main menu".
Pull Request: Debes crear un Pull Request (PR) desde tu rama de desarrollo hacia main y dejarlo abierto para su revisión.



3. Documentación y Entrega Final
   Actualización del README.md: Añade una nueva subsección titulada Módulo de Analíticas que explique brevemente el propósito de este nuevo reporte, qué información proporciona y cómo ayuda al departamento de compras en la toma de decisiones sobre proveedores.

Entrega en la Plataforma: Sube a la plataforma del bootcamp los siguientes dos datos:

La URL de tu repositorio privado de GitHub (con el trainer como colaborador).
El hash completo del último commit en tu rama de desarrollo.
