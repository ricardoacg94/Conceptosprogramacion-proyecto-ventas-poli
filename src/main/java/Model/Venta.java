package Model;

import java.util.ArrayList;
import java.util.List;

public class Venta {
    private Vendedor vendedor;
    private List<DetalleVenta> detalles;
    // SHA-256 del contenido del CSV. null para ventas de JSON antiguos.
    private String origenImportacion;

    public Venta(Vendedor vendedor) {
        if (vendedor == null) throw new IllegalArgumentException("Vendedor obligatorio");
        this.vendedor = vendedor;
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetalleVenta detalle) {
        if (detalle == null) throw new IllegalArgumentException("Detalle obligatorio");
        detalles.add(detalle);
    }
    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) {
        if (vendedor == null) throw new IllegalArgumentException("Vendedor obligatorio");
        this.vendedor = vendedor;
    }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public String getOrigenImportacion() { return origenImportacion; }
    public void setOrigenImportacion(String origenImportacion) { this.origenImportacion = origenImportacion; }
    public double getTotalVenta() {
        return detalles.stream().mapToDouble(DetalleVenta::getValorTotal).sum();
    }
}
