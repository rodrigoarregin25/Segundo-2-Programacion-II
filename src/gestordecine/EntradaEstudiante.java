/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestordecine;

/**
 * Entrada de estudiante. Tiene un descuento del 20% sobre el precio base.
 */

public class EntradaEstudiante extends Entrada{
    
    private static final double DESCUENTO = 0.20;

    /**
     * Que hace: crea una entrada de estudiante para una butaca con su precio base
     * Parametros: butaca (Butaca), precioBase (double)
     * Lanza: IllegalArgumentException si la butaca es nula o el precio base es menor o igual a 0
     */
    
 public EntradaEstudiante (Butaca butaca, double precioBase){
     super(butaca, precioBase);
 }
   /**
     * Que hace: calcula el precio final aplicando el descuento de estudiante
     * Retorna: el precio base menos el 20% (double)
     */
 @Override 
 public double calcularPrecioFinal (){
     return getPrecioBase() * (1 - DESCUENTO);
 }
    
}
