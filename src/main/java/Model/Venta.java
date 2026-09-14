/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author serpe
 */
public class Venta {

    private Vendedor vendedor;
    private List<DetalleVenta> detalles;
    
    public Venta(Vendedor vendedor)
    {
        this.vendedor = vendedor;
        this.detalles = new ArrayList<>();
    }
    public void agregarDetalle(DetalleVenta detalle) 
    {
        this.detalles.add(detalle);
    }
    public Vendedor getVendedor()
    {
        return vendedor;
    }
    public List<DetalleVenta> getDetalles()
    {
        return detalles;
    }
    public double getTotalVenta()
    {
        return detalles.stream().mapToDouble(DetalleVenta::getValorTotal).sum(); 
    }
}
    
