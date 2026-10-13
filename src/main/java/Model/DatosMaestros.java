package Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatosMaestros {
    private List<Vendedor> vendedores;
    private List<Producto> productos;
    private List<Venta> ventas;
    // Adicion compatible con los JSON anteriores, en los que el campo no existe.
    private Set<String> importacionesProcesadas;

    public DatosMaestros(List<Vendedor> vendedores, List<Producto> productos, List<Venta> ventas) {
        this(vendedores, productos, ventas, new HashSet<String>());
    }

    public DatosMaestros(List<Vendedor> vendedores, List<Producto> productos,
                        List<Venta> ventas, Set<String> importacionesProcesadas) {
        this.vendedores = vendedores;
        this.productos = productos;
        this.ventas = ventas;
        this.importacionesProcesadas = new HashSet<>(importacionesProcesadas);
    }

    public List<Vendedor> getVendedores() { return vendedores; }
    public List<Producto> getProductos() { return productos; }
    public List<Venta> getVentas() { return ventas; }
    public Set<String> getImportacionesProcesadas() { return importacionesProcesadas; }
}
