/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author serpe
 */
public class DetalleVenta {

    private Producto producto;
    private int cantidad;
    private double valorUnitario, impuesto;
    
    public DetalleVenta(Producto producto, int cantidad, double impuesto) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.valorUnitario = producto.getValorUnidad();
        this.impuesto = impuesto;
    }
    
    public double getValorTotal()
    { 
        return (cantidad * valorUnitario) + impuesto;
    }
    public Producto getProducto() 
    {
        return producto; 
    }
    public int getCantidad()
    {
        return cantidad;
    }
    public double getValorUnitario()
    {
        return valorUnitario;
    }    
}
