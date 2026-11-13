/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestordecine;

/**
 * Entrada de estudiante. Tiene un descuento del 45% sobre el precio base.
 */

public class EntradaJubilados extends Entrada {
    
    private static final double DESCUENTO = 0.45;
    
      /**
* Que hace: crea una entrada de Jubilados con su precio base
* Parametros: precioBase (double)
* Lanza: IllegalArgumentException si el precio base es menor o igual a 0
*/ 
    
    
    public EntradaJubilados (double precioBase){
        super(precioBase);
        
    }
    
    
       /**
* Que hace: calcula el precio final aplicando el descuento de estudiante
* Retorna: el precio base menos el 45% (double)
*/
 @Override 
 public double calcularPrecioFinal(){
     return getPrecioBase() * (1 - DESCUENTO);
 }
    
}
