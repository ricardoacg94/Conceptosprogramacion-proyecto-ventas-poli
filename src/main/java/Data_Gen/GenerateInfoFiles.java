/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Gen;
import java.io.PrintWriter;
import java.util.Random;

/**
 *
 * @author serpe
 */
public class GenerateInfoFiles {

private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan", "Pedro", "Laura"};
private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez", "Lopez", "Garcia"};
private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};
private static final String[] PRODUCTOS_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor", "Impresora"};
private static final double[] PRODUCTOS_PRECIOS = {2500000.0, 80000.0, 150000.0, 950000.0, 450000.0};

public static void generarArchivosPrueba() throws Exception {
        createProductsFile(PRODUCTOS_NOMBRES.length);
        createSalesManInfoFile(5); 
    }

    private static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", "UTF-8")) {
            for (int i = 0; i < productsCount; i++) {
                writer.println((i + 1) + ";" + PRODUCTOS_NOMBRES[i] + ";" + PRODUCTOS_PRECIOS[i]);
            }
        }
    }
    
    private static void createSalesManInfoFile(int salesmanCount) throws Exception {
        Random aleatorio = new Random();
        try (PrintWriter writer = new PrintWriter("vendedores.csv", "UTF-8")) {
            for (int i = 0; i < salesmanCount; i++) {
                long id = 100000000 + aleatorio.nextInt(900000000);
                String nombre = NOMBRES[aleatorio.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[aleatorio.nextInt(APELLIDOS.length)];
                String tipoDoc = TIPOS_DOC[aleatorio.nextInt(TIPOS_DOC.length)];
                writer.println(tipoDoc + ";" + id + ";" + nombre + ";" + apellido);
                createSalesMenFile(aleatorio.nextInt(6) + 3, nombre, id, tipoDoc);
            }
        }
    }

    private static void createSalesMenFile(int randomSalesCount, String name, long id, String tipoDoc) throws Exception {
        Random aleatorio = new Random();
        String fileName = "vendedor_" + id + ".csv";
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            writer.println(tipoDoc + ";" + id);
            for (int i = 0; i < randomSalesCount; i++) {
                writer.println((aleatorio.nextInt(PRODUCTOS_NOMBRES.length) + 1) + ";" + (aleatorio.nextInt(10) + 1) + ";");
            }
        }
    }
}

