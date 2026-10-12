# Proyecto Ventas — Generación y clasificación de datos

Proyecto Conceptos Fundamentales de Programación  
Politécnico Grancolombiano

## Requisitos

- Java 8
- Maven

## Clases principales

El proyecto tiene exactamente dos clases con método `main`:

| Clase | Paquete | Descripción |
|---|---|---|
| `GenerateInfoFiles` | `Data_Gen` | Genera los archivos planos de entrada (pseudoaleatorios) |
| `main` | (paquete raíz) | Lee los archivos de entrada y genera los reportes |

## Ejecución paso a paso

### Paso 1 — Generar archivos de datos

Ejecutar la clase `Data_Gen.GenerateInfoFiles`.

```
mvn exec:java -Dexec.mainClass="Data_Gen.GenerateInfoFiles"
```

Se generan en el directorio de trabajo:
- `productos.csv` — catálogo de productos
- `vendedores.csv` — lista de vendedores
- `vendedor_[id].csv` — archivo de ventas por cada vendedor (5 archivos)

### Paso 2 — Generar reportes

Ejecutar la clase `main`.

```
mvn exec:java -Dexec.mainClass="main"
```

Se generan en el directorio de trabajo:
- `reporte_vendedores_[timestamp].csv` y `.json` — vendedores ordenados por dinero recaudado (mayor a menor)
- `reporte_productos_[timestamp].csv` y `.json` — productos ordenados por cantidad vendida (mayor a menor)

Ninguno de los dos programas solicita información al usuario.

## Estructura del proyecto

- `Model`: clases de dominio (`Vendedor`, `Producto`, `Venta`, `DetalleVenta`, `TipoDocumento`)
- `Logic`: reglas de negocio y cálculo de reportes (`Service_Ventas`)
- `Persistence`: lectura y escritura de archivos (`Persistencia`)
- `Data_Gen`: generación de archivos pseudoaleatorios (`GenerateInfoFiles`)
- `UI`: interfaz gráfica complementaria (`MainFrame`)

## Integrantes

- JAVIER CRUZ AGUILAR
- RICARDO ANDRES CORONADO GONZALEZ
- ALEJANDRO CIFUENTES OSORIO
