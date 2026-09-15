package Persistence;

import Model.Producto;
import Model.Vendedor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Clase encargada de la lectura y escritura de archivos del sistema.
 *
 * Centraliza el acceso a disco: lee los archivos planos de entrada y
 * exporta los reportes en formato CSV y JSON.
 *
 * @author Grupo Proyecto Ventas
 */
public class Persistencia {

    /**
     * Lee un archivo CSV separado por punto y coma y devuelve sus lineas
     * divididas en campos.
     *
     * @param archivo archivo a leer
     * @return lista de arreglos, cada uno con los campos de una linea
     * @throws IOException si ocurre un error de lectura
     */
    public static List<String[]> leerArchivoCSV(File archivo) throws IOException {
        List<String[]> datos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    datos.add(linea.trim().split(";"));
                }
            }
        }
        return datos;
    }

    /**
     * Exporta el reporte de vendedores en formato CSV, ordenado de mayor a
     * menor por dinero recaudado.
     *
     * @param datos lista ordenada de vendedores con su total recaudado
     * @param ruta directorio donde se escribe el archivo
     * @param timestamp marca de tiempo para el nombre del archivo
     * @throws IOException si ocurre un error de escritura
     */
    public static void exportarVendedoresCSV(List<Map.Entry<Vendedor, Double>> datos, String ruta, String timestamp) throws IOException {
        File csvFile = new File(ruta, "reporte_vendedores_" + timestamp + ".csv");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {
            pw.println("Documento;Nombres;Apellidos;Total Recaudado");
            for (Map.Entry<Vendedor, Double> entry : datos) {
                Vendedor v = entry.getKey();
                pw.printf(Locale.US, "%s;%s;%s;%.2f\n",
                        v.getDocumento(), v.getNombres(), v.getApellidos(), entry.getValue());
            }
        }
    }

    /**
     * Exporta el reporte de vendedores en formato JSON.
     *
     * @param datos lista ordenada de vendedores con su total recaudado
     * @param ruta directorio donde se escribe el archivo
     * @param timestamp marca de tiempo para el nombre del archivo
     * @throws IOException si ocurre un error de escritura
     */
    public static void exportarVendedoresJSON(List<Map.Entry<Vendedor, Double>> datos, String ruta, String timestamp) throws IOException {
        File jsonFile = new File(ruta, "reporte_vendedores_" + timestamp + ".json");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8))) {
            pw.println("{\n  \"fechaExportacion\": \"" + timestamp + "\",\n  \"vendedores\": [");
            for (int i = 0; i < datos.size(); i++) {
                Vendedor v = datos.get(i).getKey();
                pw.println("    {");
                pw.println("      \"documento\": \"" + v.getDocumento() + "\",");
                pw.println("      \"nombre\": \"" + v.getNombres() + " " + v.getApellidos() + "\",");
                pw.printf(Locale.US, "      \"totalRecaudado\": %.2f\n", datos.get(i).getValue());
                pw.print("    }");
                if (i < datos.size() - 1) {
                    pw.println(",");
                } else {
                    pw.println();
                }
            }
            pw.println("  ]\n}");
        }
    }

    /**
     * Exporta el reporte de productos en formato CSV, ordenado de mayor a
     * menor por cantidad vendida. Incluye el nombre y el precio unitario
     * segun lo solicitado en el enunciado del proyecto.
     *
     * @param datos lista ordenada de productos con su cantidad vendida
     * @param ruta directorio donde se escribe el archivo
     * @param timestamp marca de tiempo para el nombre del archivo
     * @throws IOException si ocurre un error de escritura
     */
    public static void exportarProductosCSV(List<Map.Entry<Producto, Integer>> datos, String ruta, String timestamp) throws IOException {
        File csvFile = new File(ruta, "reporte_productos_" + timestamp + ".csv");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {
            pw.println("ID Producto;Nombre;Precio Unitario;Cantidad Vendida");
            for (Map.Entry<Producto, Integer> entry : datos) {
                Producto p = entry.getKey();
                pw.printf(Locale.US, "%s;%s;%.2f;%d\n",
                        p.getIdProducto(), p.getNombre(), p.getValorUnidad(), entry.getValue());
            }
        }
    }

    /**
     * Exporta el reporte de productos en formato JSON.
     *
     * @param datos lista ordenada de productos con su cantidad vendida
     * @param ruta directorio donde se escribe el archivo
     * @param timestamp marca de tiempo para el nombre del archivo
     * @throws IOException si ocurre un error de escritura
     */
    public static void exportarProductosJSON(List<Map.Entry<Producto, Integer>> datos, String ruta, String timestamp) throws IOException {
        File jsonFile = new File(ruta, "reporte_productos_" + timestamp + ".json");
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8))) {
            pw.println("{\n  \"fechaExportacion\": \"" + timestamp + "\",\n  \"productos\": [");
            for (int i = 0; i < datos.size(); i++) {
                Producto p = datos.get(i).getKey();
                pw.println("    {");
                pw.println("      \"id\": \"" + p.getIdProducto() + "\",");
                pw.println("      \"nombre\": \"" + p.getNombre() + "\",");
                pw.printf(Locale.US, "      \"precioUnitario\": %.2f,\n", p.getValorUnidad());
                pw.println("      \"cantidadVendida\": " + datos.get(i).getValue());
                pw.print("    }");
                if (i < datos.size() - 1) {
                    pw.println(",");
                } else {
                    pw.println();
                }
            }
            pw.println("  ]\n}");
        }
    }
}