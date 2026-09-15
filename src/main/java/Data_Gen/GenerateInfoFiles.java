package Data_Gen;

import java.io.PrintWriter;
import java.util.Random;

/**
 * Clase encargada de generar los archivos planos pseudoaleatorios que
 * sirven como entrada al programa principal de reportes.
 *
 * Genera el archivo de productos, el archivo de informacion de vendedores
 * y un archivo de ventas por cada vendedor.
 *
 * @author Grupo Proyecto Ventas
 */
public class GenerateInfoFiles {

    /** Nombres reales usados para generar vendedores coherentes. */
    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan", "Pedro", "Laura"};

    /** Apellidos reales usados para generar vendedores coherentes. */
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez", "Lopez", "Garcia"};

    /** Tipos de documento validos. */
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};

    /** Nombres del catalogo de productos. */
    private static final String[] PRODUCTOS_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor", "Impresora"};

    /** Precios unitarios del catalogo de productos. */
    private static final double[] PRODUCTOS_PRECIOS = {2500000.0, 80000.0, 150000.0, 950000.0, 450000.0};

    /**
     * Punto de entrada. Genera todos los archivos de prueba sin solicitar
     * informacion al usuario y muestra un mensaje de resultado.
     *
     * @param args argumentos de linea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        try {
            generarArchivosPrueba();
            System.out.println("Archivos de prueba generados exitosamente.");
            System.out.println("Se crearon: productos.csv, vendedores.csv y un archivo por vendedor.");
        } catch (Exception ex) {
            System.out.println("Error al generar los archivos de prueba: " + ex.getMessage());
        }
    }

    /**
     * Genera el archivo de productos y el archivo de vendedores, junto con
     * los archivos de ventas correspondientes a cada vendedor.
     *
     * @throws Exception si ocurre un error de escritura
     */
    public static void generarArchivosPrueba() throws Exception {
        createProductsFile(PRODUCTOS_NOMBRES.length);
        createSalesManInfoFile(5);
    }

    /**
     * Crea el archivo productos.csv con la cantidad de productos indicada.
     *
     * @param productsCount cantidad de productos a generar
     * @throws Exception si ocurre un error de escritura
     */
    public static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", "UTF-8")) {
            for (int i = 0; i < productsCount; i++) {
                writer.println((i + 1) + ";" + PRODUCTOS_NOMBRES[i] + ";" + PRODUCTOS_PRECIOS[i]);
            }
        }
    }

    /**
     * Crea el archivo vendedores.csv con informacion pseudoaleatoria y
     * coherente, y genera el archivo de ventas de cada vendedor.
     *
     * @param salesmanCount cantidad de vendedores a generar
     * @throws Exception si ocurre un error de escritura
     */
    public static void createSalesManInfoFile(int salesmanCount) throws Exception {
        Random aleatorio = new Random();
        try (PrintWriter writer = new PrintWriter("vendedores.csv", "UTF-8")) {
            for (int i = 0; i < salesmanCount; i++) {
                long id = 100000000L + aleatorio.nextInt(900000000);
                String nombre = NOMBRES[aleatorio.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[aleatorio.nextInt(APELLIDOS.length)];
                String tipoDoc = TIPOS_DOC[aleatorio.nextInt(TIPOS_DOC.length)];

                writer.println(tipoDoc + ";" + id + ";" + nombre + ";" + apellido);
                createSalesMenFile(aleatorio.nextInt(6) + 3, nombre, id, tipoDoc);
            }
        }
    }

    /**
     * Crea un archivo de ventas pseudoaleatorio para un vendedor.
     * Esta es la firma solicitada en el enunciado del proyecto.
     *
     * @param randomSalesCount cantidad de lineas de venta a generar
     * @param name nombre del vendedor
     * @param id numero de documento del vendedor
     * @throws Exception si ocurre un error de escritura
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws Exception {
        createSalesMenFile(randomSalesCount, name, id, TIPOS_DOC[0]);
    }

    /**
     * Crea un archivo de ventas pseudoaleatorio para un vendedor,
     * permitiendo especificar el tipo de documento.
     *
     * @param randomSalesCount cantidad de lineas de venta a generar
     * @param name nombre del vendedor
     * @param id numero de documento del vendedor
     * @param tipoDoc tipo de documento del vendedor
     * @throws Exception si ocurre un error de escritura
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id, String tipoDoc) throws Exception {
        Random aleatorio = new Random();
        String fileName = "vendedor_" + id + ".csv";

        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            writer.println(tipoDoc + ";" + id);
            for (int i = 0; i < randomSalesCount; i++) {
                int idProducto = aleatorio.nextInt(PRODUCTOS_NOMBRES.length) + 1;
                int cantidad = aleatorio.nextInt(10) + 1;
                writer.println(idProducto + ";" + cantidad + ";");
            }
        }
    }
}