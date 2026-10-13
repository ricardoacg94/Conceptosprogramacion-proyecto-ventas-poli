package Logic;

import Data_Gen.GenerateInfoFiles;
import Model.*;
import Persistence.Persistencia;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Service_Ventas {
    /** La interfaz grafica solicita el precio al usuario; null equivale a cancelar. */
    @FunctionalInterface
    public interface PrecioSolicitante {
        Double solicitarPrecio(String idProducto, String nombreProducto);
    }

    public static class ImportacionCanceladaException extends IOException {
        public ImportacionCanceladaException(String mensaje) { super(mensaje); }
    }

    private List<Vendedor> vendedores = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();
    private List<Venta> ventas = new ArrayList<>();
    private Set<String> importacionesProcesadas = new HashSet<>();

    public Service_Ventas() { }

    private static boolean precioValido(double precio) {
        return Double.isFinite(precio) && precio > 0;
    }

    private static double solicitarPrecioObligatorio(String id, String nombre,
                                                       PrecioSolicitante solicitante) throws IOException {
        if (solicitante == null) {
            throw new IOException("Se requiere indicar explicitamente el precio base del producto " + id);
        }
        Double precio = solicitante.solicitarPrecio(id, nombre);
        if (precio == null) throw new ImportacionCanceladaException("Se cancelo el precio del producto " + id);
        if (!precioValido(precio)) throw new IOException("Precio base invalido para el producto " + id);
        return precio;
    }

    /**
     * Rehidrata el JSON y completa precios 0 de versiones anteriores.
     * Si falta un precio solicita uno antes de cambiar los datos en memoria.
     */
    public void cargarDatosMaestros(PrecioSolicitante solicitante) throws IOException {
        DatosMaestros datos = Persistencia.cargarDatosMaestros();
        if (datos == null) return;
        List<Vendedor> nuevosVendedores = datos.getVendedores() == null
                ? new ArrayList<Vendedor>() : datos.getVendedores();
        List<Producto> nuevosProductos = datos.getProductos() == null
                ? new ArrayList<Producto>() : datos.getProductos();
        List<Venta> nuevasVentas = datos.getVentas() == null
                ? new ArrayList<Venta>() : datos.getVentas();
        Set<String> nuevasHuellas = datos.getImportacionesProcesadas() == null
                ? new HashSet<String>() : new HashSet<>(datos.getImportacionesProcesadas());

        // Validacion y recopilacion de precios: cancelar no modifica el estado actual.
        Map<String, Double> preciosPendientes = new LinkedHashMap<>();
        boolean reparacionHistorica = false;
        Map<String, Producto> catalogo = new LinkedHashMap<>();
        for (Producto p : nuevosProductos) {
            if (p != null && p.getIdProducto() != null) catalogo.put(p.getIdProducto().trim(), p);
        }
        for (Venta v : nuevasVentas) {
            if (v == null || v.getVendedor() == null || v.getDetalles() == null)
                throw new IOException("El JSON contiene una venta incompleta");
            for (DetalleVenta dv : v.getDetalles()) {
                if (dv == null || dv.getProducto() == null || dv.getProducto().getIdProducto() == null)
                    throw new IOException("El JSON contiene un detalle incompleto");
                String id = dv.getProducto().getIdProducto().trim();
                Producto catalogado = catalogo.get(id);
                if (catalogado == null) {
                    catalogado = dv.getProducto();
                    catalogo.put(id, catalogado);
                    nuevosProductos.add(catalogado);
                }
            }
        }
        for (Producto p : catalogo.values()) {
            if (!precioValido(p.getValorUnidad())) {
                double precio = solicitarPrecioObligatorio(p.getIdProducto(), p.getNombre(), solicitante);
                preciosPendientes.put(p.getIdProducto().trim(), precio);
            }
        }
        // Confirmados todos los precios: efectuar migracion y resolver referencias.
        for (Map.Entry<String, Double> e : preciosPendientes.entrySet()) {
            catalogo.get(e.getKey()).setValorUnidad(e.getValue());
        }
        Map<String, Vendedor> vendedoresPorDoc = new HashMap<>();
        for (Vendedor v : nuevosVendedores) {
            if (v != null && v.getDocumento() != null)
                vendedoresPorDoc.put(v.getDocumento().trim(), v);
        }
        for (Venta venta : nuevasVentas) {
            String doc = venta.getVendedor().getDocumento();
            if (doc == null) throw new IOException("Una venta no tiene documento del vendedor");
            Vendedor v = vendedoresPorDoc.get(doc.trim());
            if (v == null) {
                v = venta.getVendedor();
                nuevosVendedores.add(v);
                vendedoresPorDoc.put(doc.trim(), v);
            }
            venta.setVendedor(v);
            for (DetalleVenta dv : venta.getDetalles()) {
                Producto p = catalogo.get(dv.getProducto().getIdProducto().trim());
                dv.setProducto(p);
                if (dv.getValorUnitario() <= 0) reparacionHistorica = true;
                dv.completarPrecioHistoricoSiFalta(p.getValorUnidad());
            }
            if (venta.getOrigenImportacion() != null)
                nuevasHuellas.add(venta.getOrigenImportacion());
        }
        vendedores = nuevosVendedores;
        productos = nuevosProductos;
        ventas = nuevasVentas;
        importacionesProcesadas = nuevasHuellas;
        // Persistir inmediatamente migracion de precios sin repetir el prompt al reiniciar.
        if (!preciosPendientes.isEmpty() || reparacionHistorica) sincronizarArchivosConstantes();
    }

    public void cargarDatosMaestros() throws IOException { cargarDatosMaestros(null); }

    public void sincronizarArchivosConstantes() {
        DatosMaestros datos = new DatosMaestros(vendedores, productos, ventas, importacionesProcesadas);
        Persistencia.guardarDatosMaestros(datos);
        GenerateInfoFiles.exportarCSVVendedoresDetallado(vendedores, ventas);
        GenerateInfoFiles.exportarCSVProductosDetallado(productos, ventas);
    }

    public void agregarVendedor(Vendedor v) {
        if (v == null || v.getDocumento() == null || v.getDocumento().trim().isEmpty())
            throw new IllegalArgumentException("Documento obligatorio");
        Vendedor existente = buscarVendedor(v.getDocumento().trim());
        if (existente == null) {
            vendedores.add(v);
        } else if ("Desconocido".equals(existente.getNombres())) {
            existente.setNombres(v.getNombres());
            existente.setApellidos(v.getApellidos());
            existente.setTipoDocumento(v.getTipoDocumento());
        }
    }

    public void agregarProducto(Producto p) {
        if (p == null || p.getIdProducto() == null || p.getIdProducto().trim().isEmpty()
                || !precioValido(p.getValorUnidad()))
            throw new IllegalArgumentException("Todo producto requiere un precio base positivo");
        Producto existente = buscarProducto(p.getIdProducto().trim());
        if (existente == null) {
            productos.add(p);
        } else {
            // Actualizar el catalogo no cambia precios historicos ya registrados.
            existente.setNombre(p.getNombre());
            existente.setValorUnidad(p.getValorUnidad());
            repararDetallesSinPrecio(existente);
        }
    }

    private void repararDetallesSinPrecio(Producto producto) {
        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                if (detalle.getProducto().getIdProducto().equals(producto.getIdProducto())) {
                    detalle.setProducto(producto);
                    detalle.completarPrecioHistoricoSiFalta(producto.getValorUnidad());
                }
            }
        }
    }

    public void registrarVentaManual(Vendedor vendedor, List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty())
            throw new IllegalArgumentException("Una venta debe tener detalles");
        Venta venta = new Venta(vendedor);
        venta.setOrigenImportacion("MANUAL");
        for (DetalleVenta dv : detalles) venta.agregarDetalle(dv);
        ventas.add(venta);
        sincronizarArchivosConstantes();
    }

    public List<Vendedor> getVendedores() { return vendedores; }
    public List<Producto> getProductos() { return productos; }
    public List<Venta> getVentas() { return ventas; }
    public Set<String> getImportacionesProcesadas() { return new HashSet<>(importacionesProcesadas); }

    private Vendedor buscarVendedor(String doc) {
        for (Vendedor v : vendedores)
            if (v != null && v.getDocumento() != null && v.getDocumento().trim().equals(doc)) return v;
        return null;
    }

    private Producto buscarProducto(String id) {
        for (Producto p : productos)
            if (p != null && p.getIdProducto() != null && p.getIdProducto().trim().equals(id)) return p;
        return null;
    }

    private static String campo(String[] fila, int indice) {
        return indice < fila.length ? fila[indice].trim() : "";
    }

    /** CSV de vendedores: acepta encabezado, valida completamente antes de importar. */
    public void procesarArchivoVendedores(File archivo) throws IOException {
        List<Vendedor> nuevos = new ArrayList<>();
        List<String[]> filas = Persistencia.leerArchivoCSV(archivo);
        for (int i = 0; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length == 0 || campo(fila, 0).isEmpty()) continue;
            if (i == 0 && campo(fila, 0).equalsIgnoreCase("TipoDocumento")) continue;
            if (fila.length < 4 || campo(fila, 1).isEmpty() || campo(fila, 2).isEmpty()
                    || campo(fila, 3).isEmpty())
                throw new IOException("Vendedor incompleto en la linea " + (i + 1));
            nuevos.add(new Vendedor(campo(fila, 0), campo(fila, 1), campo(fila, 2), campo(fila, 3)));
        }
        for (Vendedor v : nuevos) agregarVendedor(v);
    }

    /** Solicita precios faltantes tanto en CSV de catalogo como en CSV de ventas. */
    public void procesarArchivoProductos(File archivo, PrecioSolicitante solicitante) throws IOException {
        List<String[]> filas = Persistencia.leerArchivoCSV(archivo);
        Map<String, Producto> nuevos = new LinkedHashMap<>();
        for (int i = 0; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length == 0 || campo(fila, 0).isEmpty()) continue;
            if (i == 0 && (campo(fila, 0).equalsIgnoreCase("ID") || campo(fila, 0).equalsIgnoreCase("ID Producto"))) continue;
            String id = campo(fila, 0);
            String nombre = campo(fila, 1);
            if (nombre.isEmpty()) throw new IOException("Nombre de producto faltante en linea " + (i + 1));
            Double precio = null;
            if (!campo(fila, 2).isEmpty()) {
                try { precio = Double.parseDouble(campo(fila, 2).replace(',', '.')); }
                catch (NumberFormatException e) { /* Se solicitara explicitamente */ }
            }
            if (precio == null || !precioValido(precio)) {
                Producto registrado = nuevos.containsKey(id) ? nuevos.get(id) : buscarProducto(id);
                if (registrado != null && precioValido(registrado.getValorUnidad()))
                    precio = registrado.getValorUnidad();
                else precio = solicitarPrecioObligatorio(id, nombre, solicitante);
            }
            nuevos.put(id, new Producto(id, nombre, precio));
        }
        for (Producto producto : nuevos.values()) {
            Producto anterior = buscarProducto(producto.getIdProducto());
            if (anterior == null) productos.add(producto);
            else {
                anterior.setNombre(producto.getNombre());
                anterior.setValorUnidad(producto.getValorUnidad());
                repararDetallesSinPrecio(anterior);
            }
        }
    }

    public void procesarArchivoProductos(File archivo) throws IOException {
        procesarArchivoProductos(archivo, null);
    }

    private static String sha256(File archivo) throws IOException {
        try {
            byte[] content = Files.readAllBytes(archivo.toPath());
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    private static class LineaVenta {
        final String id;
        final int cantidad;
        LineaVenta(String id, int cantidad) { this.id = id; this.cantidad = cantidad; }
    }

    private static boolean igualALegada(Venta antigua, String documento, List<LineaVenta> lineas) {
        if (antigua == null || antigua.getOrigenImportacion() != null || antigua.getVendedor() == null
                || antigua.getDetalles() == null || antigua.getDetalles().size() != lineas.size()) return false;
        if (!documento.equals(antigua.getVendedor().getDocumento().trim())) return false;
        for (int i = 0; i < lineas.size(); i++) {
            DetalleVenta d = antigua.getDetalles().get(i);
            if (d == null || d.getProducto() == null || !lineas.get(i).id.equals(d.getProducto().getIdProducto())
                    || lineas.get(i).cantidad != d.getCantidad()) return false;
        }
        return true;
    }

    /**
     * @return true si se importo, false si era un archivo duplicado.
     * Valida todas las filas y resuelve todos los precios antes de registrar la venta.
     */
    public boolean procesarArchivoVentas(File archivo, PrecioSolicitante solicitante) throws IOException {
        String huella = sha256(archivo);
        if (importacionesProcesadas.contains(huella)) return false;
        List<String[]> filas = Persistencia.leerArchivoCSV(archivo);
        if (filas.size() < 2 || filas.get(0).length < 2) throw new IOException("Archivo de ventas sin cabecera o sin detalles");
        String tipoDoc = campo(filas.get(0), 0);
        String doc = campo(filas.get(0), 1);
        if (tipoDoc.isEmpty() || doc.isEmpty()) throw new IOException("Tipo y numero de documento obligatorios");
        List<LineaVenta> lineas = new ArrayList<>();
        for (int i = 1; i < filas.size(); i++) {
            String id = campo(filas.get(i), 0);
            if (id.isEmpty() && campo(filas.get(i), 1).isEmpty()) continue;
            if (id.isEmpty() || campo(filas.get(i), 1).isEmpty())
                throw new IOException("Linea de venta incompleta: " + (i + 1));
            int cantidad;
            try { cantidad = Integer.parseInt(campo(filas.get(i), 1)); }
            catch (NumberFormatException ex) { throw new IOException("Cantidad invalida en linea " + (i + 1), ex); }
            if (cantidad <= 0) throw new IOException("La cantidad debe ser positiva en linea " + (i + 1));
            lineas.add(new LineaVenta(id, cantidad));
        }
        if (lineas.isEmpty()) throw new IOException("Archivo sin detalles validos");

        // Compatibilidad con JSON anteriores a las huellas de importacion:
        // evitar reimportar la misma venta antigua incluso si no tenia identificador.
        for (Venta antigua : ventas) {
            if (igualALegada(antigua, doc, lineas)) {
                importacionesProcesadas.add(huella);
                antigua.setOrigenImportacion(huella);
                return false;
            }
        }

        Map<String, Producto> faltantes = new LinkedHashMap<>();
        for (LineaVenta linea : lineas) {
            Producto p = buscarProducto(linea.id);
            if (p == null || !precioValido(p.getValorUnidad())) {
                if (!faltantes.containsKey(linea.id)) {
                    String nombre = p == null ? "Prod_" + linea.id : p.getNombre();
                    double precio = solicitarPrecioObligatorio(linea.id, nombre, solicitante);
                    faltantes.put(linea.id, new Producto(linea.id, nombre, precio));
                }
            }
        }
        // No se crea ni modifica ninguna venta/producto mientras falte algun precio.
        Vendedor vendedor = buscarVendedor(doc);
        if (vendedor == null) vendedor = new Vendedor(tipoDoc, doc, "Desconocido", "Desconocido");
        Map<String, Producto> referencias = new LinkedHashMap<>();
        for (LineaVenta linea : lineas) {
            Producto p = faltantes.containsKey(linea.id) ? faltantes.get(linea.id) : buscarProducto(linea.id);
            referencias.put(linea.id, p);
        }
        Venta venta = new Venta(vendedor);
        for (LineaVenta linea : lineas)
            venta.agregarDetalle(new DetalleVenta(referencias.get(linea.id), linea.cantidad, 0.0));
        venta.setOrigenImportacion(huella);
        if (buscarVendedor(doc) == null) vendedores.add(vendedor);
        for (Producto p : faltantes.values()) {
            Producto existente = buscarProducto(p.getIdProducto());
            if (existente == null) productos.add(p);
            else {
                existente.setValorUnidad(p.getValorUnidad());
                repararDetallesSinPrecio(existente);
                // Reenlazar la venta nueva con el objeto del catalogo.
                for (DetalleVenta d : venta.getDetalles()) {
                    if (d.getProducto().getIdProducto().equals(existente.getIdProducto())) d.setProducto(existente);
                }
            }
        }
        ventas.add(venta);
        importacionesProcesadas.add(huella);
        return true;
    }

    public boolean procesarArchivoVentas(File archivo) throws IOException {
        return procesarArchivoVentas(archivo, null);
    }

    public void exportarReporteVendedores(String rutaDirectorio) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Map<Vendedor, Double> totales = new HashMap<>();
        for (Vendedor v : vendedores) totales.put(v, 0.0);
        for (Venta v : ventas) {
            if (v == null || v.getVendedor() == null) continue;
            totales.put(v.getVendedor(), totales.getOrDefault(v.getVendedor(), 0.0) + v.getTotalVenta());
        }
        List<Map.Entry<Vendedor, Double>> ordenados = new ArrayList<>(totales.entrySet());
        ordenados.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Persistencia.exportarVendedoresCSV(ordenados, rutaDirectorio, timestamp);
        Persistencia.exportarVendedoresJSON(ordenados, rutaDirectorio, timestamp);
    }

    public void exportarReporteProductos(String rutaDirectorio) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Map<Producto, Integer> cantidades = new HashMap<>();
        for (Producto p : productos) cantidades.put(p, 0);
        for (Venta v : ventas) {
            if (v == null || v.getDetalles() == null) continue;
            for (DetalleVenta dv : v.getDetalles()) {
                if (dv == null || dv.getProducto() == null) continue;
                cantidades.put(dv.getProducto(), cantidades.getOrDefault(dv.getProducto(), 0) + dv.getCantidad());
            }
        }
        List<Map.Entry<Producto, Integer>> ordenados = new ArrayList<>(cantidades.entrySet());
        ordenados.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        Persistencia.exportarProductosCSV(ordenados, rutaDirectorio, timestamp);
        Persistencia.exportarProductosJSON(ordenados, rutaDirectorio, timestamp);
    }
}
