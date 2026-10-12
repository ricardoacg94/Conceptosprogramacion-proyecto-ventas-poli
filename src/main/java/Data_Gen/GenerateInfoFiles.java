package Data_Gen;

import Model.DetalleVenta;
import Model.Producto;
import Model.Vendedor;
import Model.Venta;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;

public class GenerateInfoFiles {

    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan", "Pedro", "Laura"};
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez", "Lopez", "Garcia"};
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};
    private static final String[] PRODUCTOS_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor", "Impresora"};
    private static final double[] PRODUCTOS_PRECIOS = {2500000.0, 80000.0, 150000.0, 950000.0, 450000.0};
    private static final String[] DOCUMENTOS_QUEMADOS = {"100100100", "200200200", "300300300", "400400400", "500500500"};

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
            for (int i = 0; i < salesmanCount && i < DOCUMENTOS_QUEMADOS.length; i++) {
                String idStr = DOCUMENTOS_QUEMADOS[i];
                String nombre = NOMBRES[aleatorio.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[aleatorio.nextInt(APELLIDOS.length)];
                String tipoDoc = TIPOS_DOC[aleatorio.nextInt(TIPOS_DOC.length)];
                writer.println(tipoDoc + ";" + idStr + ";" + nombre + ";" + apellido);
                createSalesMenFile(aleatorio.nextInt(6) + 3, idStr, tipoDoc);
            }
        }
    }

    private static void createSalesMenFile(int randomSalesCount, String idStr, String tipoDoc) throws Exception {
        Random aleatorio = new Random();
        String fileName = "vendedor_" + idStr + ".csv";
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            writer.println(tipoDoc + ";" + idStr);
            for (int i = 0; i < randomSalesCount; i++) {
                writer.println((aleatorio.nextInt(PRODUCTOS_NOMBRES.length) + 1) + ";" + (aleatorio.nextInt(10) + 1) + ";");
            }
        }
    }

    public static void exportarCSVVendedoresDetallado(List<Vendedor> vendedores, List<Venta> ventas) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("reporte_detallado_vendedores.csv"), false)) {
            writer.println("TipoDocumento;Documento;Nombres;Apellidos;Producto_Vendido;Cantidad");
            for (Vendedor v : vendedores) {
                boolean tieneVentas = false;
                for (Venta venta : ventas) {
                    if (venta.getVendedor().getDocumento().equals(v.getDocumento())) {
                        for (DetalleVenta detalle : venta.getDetalles()) {
                            writer.println(v.getTipoDocumento() + ";" + v.getDocumento() + ";" + 
                                          v.getNombres() + ";" + v.getApellidos() + ";" + 
                                          detalle.getProducto().getNombre() + ";" + 
                                          detalle.getCantidad());
                            tieneVentas = true;
                        }
                    }
                }
                if (!tieneVentas) {
                    writer.println(v.getTipoDocumento() + ";" + v.getDocumento() + ";" + v.getNombres() + ";" + v.getApellidos() + ";Ninguno;0");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al generar CSV Vendedores: " + e.getMessage());
        }
    }

    public static void exportarCSVProductosDetallado(List<Producto> productos, List<Venta> ventas) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("reporte_detallado_productos.csv"), false)) {
            writer.println("ID;Nombre;Precio;Total_Historico_Vendido;Vendedor_Asociado;Cantidad_Por_Vendedor");
            for (Producto p : productos) {
                int totalVendido = 0;
                boolean tieneVentas = false;
                
                for (Venta venta : ventas) {
                    for (DetalleVenta detalle : venta.getDetalles()) {
                        if (detalle.getProducto().getIdProducto().equals(p.getIdProducto())) {
                            totalVendido += detalle.getCantidad();
                        }
                    }
                }
                for (Venta venta : ventas) {
                    for (DetalleVenta detalle : venta.getDetalles()) {
                        if (detalle.getProducto().getIdProducto().equals(p.getIdProducto())) {
                            writer.println(p.getIdProducto() + ";" + p.getNombre() + ";" + p.getValorUnidad() + ";" + 
                                          totalVendido + ";" + venta.getVendedor().getNombres() + " " + venta.getVendedor().getApellidos() + ";" + 
                                          detalle.getCantidad());
                            tieneVentas = true;
                        }
                    }
                }
                if (!tieneVentas) {
                    writer.println(p.getIdProducto() + ";" + p.getNombre() + ";" + p.getValorUnidad() + ";0;Ninguno;0");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al generar CSV Productos: " + e.getMessage());
        }
    }
}