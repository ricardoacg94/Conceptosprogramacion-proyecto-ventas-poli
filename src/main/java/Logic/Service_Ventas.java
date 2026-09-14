/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logic;

import Model.*;
import Persistence.Persistencia;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author serpe
 */
public class Service_Ventas {
    
    
    private List<Vendedor> vendedores;
    private List<Producto> productos;
    private List<Venta> ventas;

    public Service_Ventas() {
        this.vendedores = new ArrayList<>();
        this.productos = new ArrayList<>();
        this.ventas = new ArrayList<>();
    }

    public List<Vendedor> getVendedores() 
    {
        return vendedores;
    }
    public List<Producto> getProductos()
    {
        return productos;
    }
    public List<Venta> getVentas() 
    {
        return ventas;
    }

    public void agregarVendedor(Vendedor v) 
    {
        Vendedor existente = buscarVendedor(v.getDocumento());
        if (existente == null)
        {
            vendedores.add(v);
        } else if ("Desconocido".equals(existente.getNombres())) 
        {
            // Conciliación: Si ya existía como desconocido, actualizamos sus datos reales
            existente.setNombres(v.getNombres());
            existente.setApellidos(v.getApellidos());
            existente.setTipoDocumento(v.getTipoDocumento());
        }
    }

    public void agregarProducto(Producto p) 
    {
        Producto existente = buscarProducto(p.getIdProducto());
        if (existente == null) 
        {
            productos.add(p);
        } else if (existente.getNombre().startsWith("Prod_")) 
        {
            // Conciliación: Actualizamos el producto si era un temporal genérico
            existente.setNombre(p.getNombre());
            existente.setValorUnidad(p.getValorUnidad());
        }
    }

    private Vendedor buscarVendedor(String doc) 
    {
        return vendedores.stream().filter(v -> v.getDocumento().equals(doc)).findFirst().orElse(null);
    }

    private Producto buscarProducto(String id) 
    {
        return productos.stream().filter(p -> p.getIdProducto().equals(id)).findFirst().orElse(null);
    }

    public void procesarArchivoVendedores(File archivo) throws IOException 
    {
        List<String[]> lineas = Persistencia.leerArchivoCSV(archivo);
        for (String[] datos : lineas) {
            if (datos.length >= 4) {
                agregarVendedor(new Vendedor(datos[0], datos[1], datos[2], datos[3]));
            }
        }
    }

    public void procesarArchivoProductos(File archivo) throws IOException 
    {
        List<String[]> lineas = Persistencia.leerArchivoCSV(archivo);
        for (String[] datos : lineas) {
            if (datos.length >= 3) {
                agregarProducto(new Producto(datos[0], datos[1], Double.parseDouble(datos[2])));
            }
        }
    }

    public void procesarArchivoVentas(File archivo) throws IOException {
        List<String[]> lineas = Persistencia.leerArchivoCSV(archivo);
        if (lineas.isEmpty() || lineas.get(0).length < 2) return;
        String docVendedor = lineas.get(0)[1];
        Vendedor vendedor = buscarVendedor(docVendedor);
        if (vendedor == null) 
        {
            vendedor = new Vendedor(lineas.get(0)[0], docVendedor, "Desconocido", "Desconocido");
            agregarVendedor(vendedor);
        }
        Venta venta = new Venta(vendedor);
        for (int i = 1; i < lineas.size(); i++) 
        {
            String[] datos = lineas.get(i);
            if (datos.length >= 2) 
            {
                String idProd = datos[0];
                int cantidad = Integer.parseInt(datos[1]);
                Producto producto = buscarProducto(idProd);
                
                if (producto == null) 
                {
                    producto = new Producto(idProd, "Prod_" + idProd, 0.0);
                    agregarProducto(producto);
                }
                venta.agregarDetalle(new DetalleVenta(producto, cantidad, 0.0));
            }
        }
        ventas.add(venta);
    }

    public void exportarReporteVendedores(String rutaDirectorio) throws IOException 
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Map<Vendedor, Double> totales = new HashMap<>();
        for (Vendedor v : vendedores) totales.put(v, 0.0);
        for (Venta v : ventas) 
        {
            totales.put(v.getVendedor(), totales.getOrDefault(v.getVendedor(), 0.0) + v.getTotalVenta());
        }
        List<Map.Entry<Vendedor, Double>> listaOrdenada = new ArrayList<>(totales.entrySet());
        listaOrdenada.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Persistencia.exportarVendedoresCSV(listaOrdenada, rutaDirectorio, timestamp);
        Persistencia.exportarVendedoresJSON(listaOrdenada, rutaDirectorio, timestamp);
    }

    public void exportarReporteProductos(String rutaDirectorio) throws IOException
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Map<Producto, Integer> cantidades = new HashMap<>();
        for (Producto p : productos) cantidades.put(p, 0);
        for (Venta v : ventas) 
        {
            for (DetalleVenta dv : v.getDetalles()) 
            {
                cantidades.put(dv.getProducto(), cantidades.getOrDefault(dv.getProducto(), 0) + dv.getCantidad());
            }
        }
        List<Map.Entry<Producto, Integer>> listaOrdenada = new ArrayList<>(cantidades.entrySet());
        listaOrdenada.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        Persistencia.exportarProductosCSV(listaOrdenada, rutaDirectorio, timestamp);
        Persistencia.exportarProductosJSON(listaOrdenada, rutaDirectorio, timestamp);
    }
}
    

