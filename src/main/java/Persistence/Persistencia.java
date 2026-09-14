/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Persistence;

import Model.DetalleVenta;
import Model.Producto;
import Model.Vendedor;
import Model.Venta;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 *
 * @author serpe
 */
public class Persistencia {
    
    public static List<String[]> leerArchivoCSV(File archivo) throws IOException
    {
        List<String[]> datos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) 
        {
            String linea;
            while ((linea = br.readLine()) != null) 
            {
                datos.add(linea.split(";"));
            }
        }
        return datos;
    }

    public static void exportarVendedoresCSV(List<Map.Entry<Vendedor, Double>> datos, String ruta, String timestamp) throws IOException
    {
        File csvFile = new File(ruta, "reporte_vendedores_" + timestamp + ".csv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8))) 
        {
            pw.println("Documento;Nombres;Apellidos;Total Recaudado");
            for (Map.Entry<Vendedor, Double> entry : datos) 
            {
                Vendedor v = entry.getKey();
                pw.printf(Locale.US, "%s;%s;%s;%.2f\n", v.getDocumento(), v.getNombres(), v.getApellidos(), entry.getValue());
            }
        }
    }

    public static void exportarVendedoresJSON(List<Map.Entry<Vendedor, Double>> datos, String ruta, String timestamp) throws IOException
    {
        File jsonFile = new File(ruta, "reporte_vendedores_" + timestamp + ".json");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)))
        {
            pw.println("{\n  \"fechaExportacion\": \"" + timestamp + "\",\n  \"vendedores\": [");
            for (int i = 0; i < datos.size(); i++) 
            {
                Vendedor v = datos.get(i).getKey();
                pw.println("    {");
                pw.println("      \"documento\": \"" + v.getDocumento() + "\",");
                pw.println("      \"nombre\": \"" + v.getNombres() + " " + v.getApellidos() + "\",");
                pw.printf(Locale.US, "      \"totalRecaudado\": %.2f\n", datos.get(i).getValue());
                pw.print("    }");
                if (i < datos.size() - 1) pw.println(","); else pw.println();
            }
            pw.println("  ]\n}");
        }
    }

    public static void exportarProductosCSV(List<Map.Entry<Producto, Integer>> datos, String ruta, String timestamp) throws IOException
    {
        File csvFile = new File(ruta, "reporte_productos_" + timestamp + ".csv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8)))
        {
            pw.println("ID Producto;Nombre;Cantidad Vendida");
            for (Map.Entry<Producto, Integer> entry : datos)
            {
                Producto p = entry.getKey();
                pw.printf(Locale.US, "%s;%s;%d\n", p.getIdProducto(), p.getNombre(), entry.getValue());
            }
        }
    }

    public static void exportarProductosJSON(List<Map.Entry<Producto, Integer>> datos, String ruta, String timestamp) throws IOException
    {
        File jsonFile = new File(ruta, "reporte_productos_" + timestamp + ".json");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)))
        {
            pw.println("{\n  \"fechaExportacion\": \"" + timestamp + "\",\n  \"productos\": [");
            for (int i = 0; i < datos.size(); i++)
            {
                Producto p = datos.get(i).getKey();
                pw.println("    {");
                pw.println("      \"id\": \"" + p.getIdProducto() + "\",");
                pw.println("      \"nombre\": \"" + p.getNombre() + "\",");
                pw.println("      \"cantidadVendida\": " + datos.get(i).getValue());
                pw.print("    }");
                if (i < datos.size() - 1) pw.println(","); else pw.println();
            }
            pw.println("  ]\n}");
        }
    }
}