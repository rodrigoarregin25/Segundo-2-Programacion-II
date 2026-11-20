/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestordecine;

/**
 * Entrada de jubilados. Tiene un descuento del 45% sobre el precio base.
 */

public class EntradaJubilados extends Entrada {
    
    private static final double DESCUENTO = 0.45;
    
 /**
 * Que hace: crea una entrada de jubilado para una butaca con su precio base
 * Parametros: butaca (Butaca), precioBase (double)
 * Lanza: IllegalArgumentException si la butaca es nula o el precio base es menor o igual a 0
 */
public EntradaJubilados (Butaca butaca, double precioBase){
    super(butaca, precioBase);
        
    }
    
    
       /**
* Que hace: calcula el precio final aplicando el descuento de jubilados
* Retorna: el precio base menos el 45% (double)
*/
 @Override 
 public double calcularPrecioFinal(){
     return getPrecioBase() * (1 - DESCUENTO);
 }
/**
* Que hace: devuelve el nombre del tipo de entrada
* Retorna: "Entrada Jubilados" (String)
*/
@Override
public String getTipo() {
    return "Entrada Jubilados";
}
    
}
