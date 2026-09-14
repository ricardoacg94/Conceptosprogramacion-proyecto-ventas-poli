/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author serpe
 */
public class Producto {
    
    private String idProducto, nombre;
    private double valorUnidad;
    
    public Producto(String idProducto, String nombre, double valorUnidad) 
    {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.valorUnidad = valorUnidad;
    }
    
    public String getIdProducto()
    {
        return idProducto;
    }
    public String getNombre()
    {
        return nombre;
    }
    public double getValorUnidad()
    {
        return valorUnidad; 
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }
    public void setValorUnidad(double valorUnidad) 
    {
        this.valorUnidad = valorUnidad;
    }

}
