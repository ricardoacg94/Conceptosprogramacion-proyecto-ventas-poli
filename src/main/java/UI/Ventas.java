package UI;

import Logic.Service_Ventas;
import java.io.File;

/**
 * Clase principal del programa de reportes.
 *
 * Lee los archivos planos generados previamente por GenerateInfoFiles,
 * consolida la informacion de ventas y genera los archivos de reporte
 * de vendedores y de productos, sin solicitar informacion al usuario.
 *
 * @author Grupo Proyecto Ventas
 */
public class Ventas {

    /**
     * Punto de entrada del programa de reportes.
     *
     * @param args argumentos de linea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        try {
            Service_Ventas servicio = new Service_Ventas();
            File carpetaTrabajo = new File(".");

            File archivoProductos = new File(carpetaTrabajo, "productos.csv");
            if (!archivoProductos.exists()) {
                throw new Exception("No se encontro el archivo productos.csv. "
                        + "Ejecute primero la clase GenerateInfoFiles.");
            }
            servicio.procesarArchivoProductos(archivoProductos);

            File archivoVendedores = new File(carpetaTrabajo, "vendedores.csv");
            if (!archivoVendedores.exists()) {
                throw new Exception("No se encontro el archivo vendedores.csv. "
                        + "Ejecute primero la clase GenerateInfoFiles.");
            }
            servicio.procesarArchivoVendedores(archivoVendedores);

            File[] archivosVentas = carpetaTrabajo.listFiles(
                    (File dir, String nombre) -> nombre.startsWith("vendedor_") && nombre.endsWith(".csv"));

            if (archivosVentas == null || archivosVentas.length == 0) {
                throw new Exception("No se encontraron archivos de ventas de vendedores. "
                        + "Ejecute primero la clase GenerateInfoFiles.");
            }

            int archivosProcesados = 0;
            for (File archivoVenta : archivosVentas) {
                try {
                    servicio.procesarArchivoVentas(archivoVenta);
                    archivosProcesados++;
                } catch (Exception ex) {
                    System.out.println("Advertencia: se omitio el archivo "
                            + archivoVenta.getName() + " por formato incorrecto.");
                }
            }

            servicio.exportarReporteVendedores(".");
            servicio.exportarReporteProductos(".");

            System.out.println("Proceso finalizado exitosamente.");
            System.out.println("Archivos de ventas procesados: " + archivosProcesados);
            System.out.println("Se generaron los reportes de vendedores y de productos.");

        } catch (Exception ex) {
            System.out.println("Error durante la generacion de reportes: " + ex.getMessage());
        }
    }
}