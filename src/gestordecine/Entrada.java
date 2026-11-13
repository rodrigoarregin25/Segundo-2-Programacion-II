/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestordecine;
import java.io.Serializable; 

/**
 * Entrada del cine. Cada tipo de entrada calcula su precio final a su manera.
 */
public abstract class Entrada  implements Serializable {
    private double precioBase;
    
 /**
     * Que hace: crea una entrada con su precio base
     * Parametros: precioBase (double)
     * Lanza: IllegalArgumentException si el precio base es menor o igual a 0
     */
    
    public Entrada (double precioBase){
        if (precioBase <= 0){
throw new IllegalArgumentException("El precio base debe ser mayor a cero");
        }
    
    this.precioBase = precioBase;
    
    }
  /**
     * Que hace: calcula el precio final de la entrada segun su tipo
     * Retorna: el precio final (double)
     */
public abstract double calcularPrecioFinal(); 

/**
 * Que hace: devuelve el precio base de la entrada
 * Retorna: el precio base (double)
 */

public double getPrecioBase (){
    return precioBase; 
}

}

