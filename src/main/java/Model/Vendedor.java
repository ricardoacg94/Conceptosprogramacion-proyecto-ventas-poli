/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author serpe
 */
public class Vendedor {
    private String nombres, apellidos, tipoDocumento, documento;
    
    public Vendedor(String tipoDocumento, String documento, String nombres, String apellidos) 
    {
        this.tipoDocumento = tipoDocumento;
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }
    
    public String getDocumento() 
    {
        return documento; 
    }
    public String getNombres() 
    { 
        return nombres; 
    }
    public String getApellidos()
    {
        return apellidos;
    }
    public String getTipoDocumento()
    {
        return tipoDocumento;
    }
    
    public void setNombres(String nombres) 
    {
        this.nombres = nombres;
    }
    public void setApellidos(String apellidos) 
    {
        this.apellidos = apellidos;
    }
    public void setTipoDocumento(String tipoDocumento)
    {
        this.tipoDocumento = tipoDocumento;
    }
}
