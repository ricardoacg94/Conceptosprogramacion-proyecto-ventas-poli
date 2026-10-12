import Logic.Service_Ventas;
import java.io.File;
import java.io.IOException;

/**
 * Clase principal de ejecucion.
 * Lee los archivos generados por GenerateInfoFiles, los procesa y genera
 * los reportes de vendedores (por dinero recaudado) y productos (por
 * cantidad vendida) en formato CSV y JSON.
 * No solicita ninguna entrada al usuario.
 */
public class main {

    public static void main(String[] args) {
        try {
            Service_Ventas servicio = new Service_Ventas();
            File directorio = new File(".");

            File archivoProductos = new File(directorio, "productos.csv");
            if (archivoProductos.exists()) {
                servicio.procesarArchivoProductos(archivoProductos);
            }

            File archivoVendedores = new File(directorio, "vendedores.csv");
            if (archivoVendedores.exists()) {
                servicio.procesarArchivoVendedores(archivoVendedores);
            }

            File[] archivosVentas = directorio.listFiles(
                (dir, nombre) -> nombre.matches("vendedor_\\d+\\.csv")
            );
            if (archivosVentas != null) {
                for (File archivo : archivosVentas) {
                    servicio.procesarArchivoVentas(archivo);
                }
            }

            if (servicio.getVendedores().isEmpty() && servicio.getProductos().isEmpty()) {
                System.err.println("Error: no se encontraron archivos de entrada. " +
                    "Ejecute primero GenerateInfoFiles para generar los datos.");
                return;
            }

            String ruta = directorio.getCanonicalPath();
            servicio.exportarReporteVendedores(ruta);
            servicio.exportarReporteProductos(ruta);

            System.out.println("Reportes generados exitosamente en: " + ruta);
        } catch (IOException e) {
            System.err.println("Error al procesar los archivos: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }
    }
}
