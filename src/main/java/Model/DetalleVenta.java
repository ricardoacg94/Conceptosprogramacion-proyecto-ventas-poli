package Model;

public class DetalleVenta {
    private Producto producto;
    private int cantidad;
    // Precio unitario historico al registrar la venta, NO el precio actual del catalogo.
    private double valorUnitario;
    private double impuesto;

    public DetalleVenta(Producto producto, int cantidad, double impuesto) {
        if (producto == null) throw new IllegalArgumentException("Producto obligatorio");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        if (!Double.isFinite(producto.getValorUnidad()) || producto.getValorUnidad() <= 0)
            throw new IllegalArgumentException("El producto no tiene precio base valido: " + producto.getIdProducto());
        if (!Double.isFinite(impuesto) || impuesto < 0)
            throw new IllegalArgumentException("Impuesto invalido");
        this.producto = producto;
        this.cantidad = cantidad;
        this.valorUnitario = producto.getValorUnidad();
        this.impuesto = impuesto;
    }

    public double getValorTotal() { return cantidad * valorUnitario + impuesto; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getValorUnitario() { return valorUnitario; }
    public void setProducto(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("Producto obligatorio");
        this.producto = producto;
        // No alterar el valor historico al enlazar con el catalogo.
    }
    /** Exclusivamente para migrar ventas antiguas guardadas con precio 0. */
    public void completarPrecioHistoricoSiFalta(double precioBase) {
        if (!Double.isFinite(precioBase) || precioBase <= 0)
            throw new IllegalArgumentException("Precio base invalido");
        if (valorUnitario <= 0) valorUnitario = precioBase;
    }
}
