package Logic;

import Model.*;
import Persistence.Persistencia;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pruebas funcionales ejecutables sin JUnit; requieren Gson en el classpath.
 * Ejecutar en un directorio temporal, porque generan la carpeta Data.
 */
public class PruebasIntegracionVentas {
    private static int aserciones = 0;

    private static void verificar(boolean valor, String mensaje) {
        aserciones++;
        if (!valor) throw new AssertionError(mensaje);
    }

    private static File csv(String nombre, String contenido) throws Exception {
        Path archivo = Files.createTempFile(nombre, ".csv");
        Files.write(archivo, contenido.getBytes(StandardCharsets.UTF_8));
        return archivo.toFile();
    }

    private static boolean cercano(double actual, double esperado) {
        return Math.abs(actual - esperado) < 0.001;
    }

    private static Producto producto(Service_Ventas servicio, String id) {
        for (Producto p : servicio.getProductos()) if (p.getIdProducto().equals(id)) return p;
        throw new AssertionError("Producto no encontrado: " + id);
    }

    private static void pruebaPrincipal() throws Exception {
        Service_Ventas service = new Service_Ventas();
        File sinCatalogo = csv("vendedor_100", "CC;100\nA;2;\nB;1;\nA;3;\n");
        AtomicInteger peticiones = new AtomicInteger();
        verificar(service.procesarArchivoVentas(sinCatalogo, (id, nombre) -> {
            peticiones.incrementAndGet();
            return id.equals("A") ? 120.0 : 40.0;
        }), "Debe importar archivo original");
        verificar(peticiones.get() == 2, "Debe preguntar una vez por producto faltante");
        verificar(service.getVentas().size() == 1, "Una venta por CSV");
        verificar(cercano(service.getVentas().get(0).getTotalVenta(), 640), "Precio solicitado calculado en total");
        verificar(!service.procesarArchivoVentas(sinCatalogo, (id, nombre) -> {
            throw new AssertionError("No debe pedir precio al reimportar");
        }), "Mismo archivo no puede duplicar la venta");
        verificar(service.getVentas().size() == 1, "Lista sin duplicados");

        // Un archivo con contenido equivalente en una ruta distinta tambien se omite.
        File copia = csv("duplicado", "CC;100\nA;2;\nB;1;\nA;3;\n");
        verificar(!service.procesarArchivoVentas(copia, null), "Copia exacta en otra ruta debe deduplicar");

        // Precio historico no se reescribe al importar un catalogo con precio nuevo.
        File catalogo = csv("productos", "A;Producto A;200\nB;Producto B;60\n");
        service.procesarArchivoProductos(catalogo, null);
        verificar(cercano(producto(service, "A").getValorUnidad(), 200), "Catalogo actualizado");
        verificar(cercano(service.getVentas().get(0).getTotalVenta(), 640), "Precio de venta historico intacto");

        // Tras reiniciar, archivo importado mantiene la huella SHA en datos maestros.
        service.sincronizarArchivosConstantes();
        Service_Ventas reabierto = new Service_Ventas();
        reabierto.cargarDatosMaestros((id, nombre) -> {
            throw new AssertionError("Precio conocido: no pedir de nuevo");
        });
        verificar(reabierto.getVentas().size() == 1, "JSON recupera ventas");
        verificar(!reabierto.procesarArchivoVentas(sinCatalogo, null), "Deduplicacion persiste entre reinicios");
        verificar(cercano(reabierto.getVentas().get(0).getTotalVenta(), 640), "Precio historico persiste entre reinicios");
        verificar(cercano(producto(reabierto, "A").getValorUnidad(), 200), "Precio actual del catalogo persiste");

        // Venta nueva usa nuevo precio del catalogo.
        File otraVenta = csv("vendedor_101", "CC;101\nA;1;\n");
        verificar(reabierto.procesarArchivoVentas(otraVenta, null), "Se permite importar otra venta");
        verificar(cercano(reabierto.getVentas().get(1).getTotalVenta(), 200), "Venta nueva usa precio vigente");
        // Actualizar un producto directamente desde la aplicacion tampoco revaloriza ventas.
        reabierto.agregarProducto(new Producto("A", "Producto A renovado", 310));
        verificar(cercano(producto(reabierto, "A").getValorUnidad(), 310), "Producto existente actualizable desde UI");
        verificar(cercano(reabierto.getVentas().get(0).getTotalVenta(), 640)
                && cercano(reabierto.getVentas().get(1).getTotalVenta(), 200),
                "Cambiar precio base mantiene historico de todas las ventas anteriores");

        // Rechazo de cancelacion: no agrega vendedor ni producto ni venta.
        int productosPrevios = reabierto.getProductos().size();
        int vendedoresPrevios = reabierto.getVendedores().size();
        int ventasPrevias = reabierto.getVentas().size();
        File cancelado = csv("vendedor_400", "CC;400\nFALTANTE;5\n");
        boolean huboCancelacion = false;
        try { reabierto.procesarArchivoVentas(cancelado, (id, nombre) -> null); }
        catch (Service_Ventas.ImportacionCanceladaException e) { huboCancelacion = true; }
        verificar(huboCancelacion, "Cancelar precio debe cancelar archivo");
        verificar(reabierto.getProductos().size() == productosPrevios
                && reabierto.getVendedores().size() == vendedoresPrevios
                && reabierto.getVentas().size() == ventasPrevias,
                "Cancelacion no debe alterar datos en memoria");
        verificar(reabierto.procesarArchivoVentas(cancelado, (id, nombre) -> 95.0),
                "Archivo cancelado debe permitir reintentar");
        verificar(cercano(reabierto.getVentas().get(2).getTotalVenta(), 475),
                "Venta reprocesada recibe precio valido");

        // CSV invalido: no se registran ventas parciales.
        int antesInvalidos = reabierto.getVentas().size();
        File invalido = csv("vendedor_500", "CC;500\nA;3\nA;-2\n");
        boolean rechazo = false;
        try { reabierto.procesarArchivoVentas(invalido, null); }
        catch (java.io.IOException e) { rechazo = true; }
        verificar(rechazo && reabierto.getVentas().size() == antesInvalidos,
                "Cantidad invalida impide importar parcialmente");

        // CSV de productos con precio omitido requiere solicitarlo.
        AtomicInteger prompts = new AtomicInteger();
        File catalogoSinPrecio = csv("catalogo_incompleto", "P;Producto P;\n");
        reabierto.procesarArchivoProductos(catalogoSinPrecio, (id, nombre) -> {
            prompts.incrementAndGet();
            return 88.0;
        });
        verificar(prompts.get() == 1 && cercano(producto(reabierto, "P").getValorUnidad(), 88),
                "Catalogo sin precio debe solicitarlo explicitamente");
        File catalogoCancelado = csv("catalogo_cancelado", "Q;Producto Q;\n");
        int catalogoPrevio = reabierto.getProductos().size();
        boolean canceladoCatalogo = false;
        try { reabierto.procesarArchivoProductos(catalogoCancelado, (id, nombre) -> null); }
        catch (Service_Ventas.ImportacionCanceladaException e) { canceladoCatalogo = true; }
        verificar(canceladoCatalogo && reabierto.getProductos().size() == catalogoPrevio,
                "Cancelar CSV catalogo no debe crear producto sin precio");

        // Venta manual identica no se confunde con reimportacion del CSV.
        Service_Ventas manual = new Service_Ventas();
        Producto prod = new Producto("X", "X", 30);
        manual.agregarProducto(prod);
        Vendedor vend = new Vendedor("CC", "777", "Prueba", "Manual");
        manual.agregarVendedor(vend);
        manual.registrarVentaManual(vend, Collections.singletonList(new DetalleVenta(prod, 1, 0)));
        File csvManual = csv("vendedor_777", "CC;777\nX;1\n");
        verificar(manual.procesarArchivoVentas(csvManual, null) && manual.getVentas().size() == 2,
                "Venta manual no debe bloquear venta importada de contenido coincidente");
    }

    private static void pruebaRecuperacionLegacy() throws Exception {
        // Simula el JSON antiguo: producto a precio 0 y detalle historico a 0.
        Producto antiguo = new Producto("Z", "Prod_Z", 5.0);
        DetalleVenta detalle = new DetalleVenta(antiguo, 3, 0);
        Field precio = DetalleVenta.class.getDeclaredField("valorUnitario");
        precio.setAccessible(true);
        precio.setDouble(detalle, 0.0);
        antiguo.setValorUnidad(0);
        Vendedor v = new Vendedor("CC", "900", "Antiguo", "Vendedor");
        Venta venta = new Venta(v);
        venta.agregarDetalle(detalle);
        Persistencia.guardarDatosMaestros(new DatosMaestros(
                Arrays.asList(v), Arrays.asList(antiguo), Arrays.asList(venta)));
        Service_Ventas recuperado = new Service_Ventas();
        AtomicInteger prompts = new AtomicInteger();
        recuperado.cargarDatosMaestros((id, nombre) -> {
            prompts.incrementAndGet();
            return 42.0;
        });
        verificar(prompts.get() == 1, "Se solicita precio base faltante de JSON antiguo");
        verificar(cercano(recuperado.getVentas().get(0).getTotalVenta(), 126),
                "Se corrige UNICAMENTE el precio historico originalmente cero");
        verificar(cercano(producto(recuperado, "Z").getValorUnidad(), 42),
                "Precio indicado se guarda en catalogo");
        recuperado.sincronizarArchivosConstantes();
        Service_Ventas segunda = new Service_Ventas();
        segunda.cargarDatosMaestros((id, nombre) -> {
            throw new AssertionError("No volver a solicitar precio migrado");
        });
        verificar(cercano(segunda.getVentas().get(0).getTotalVenta(), 126),
                "Precio corregido sobrevive reinicio JSON");
    }

    public static void main(String[] args) throws Exception {
        pruebaPrincipal();
        pruebaRecuperacionLegacy();
        System.out.println("OK - " + aserciones + " verificaciones exitosas.");
    }
}
