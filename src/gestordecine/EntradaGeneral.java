/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestordecine;

/**
 * Entrada general. Paga el precio completo, sin descuento.
 */

public class EntradaGeneral extends Entrada {
    
    /**
     * Que hace: crea una entrada general con su precio base
     * Parametros: precioBase (double)
     * Lanza: IllegalArgumentException si el precio base es menor o igual a 0
     */

    public EntradaGeneral(double precioBase) {
        super(precioBase);
    }
    
      /**
     * Que hace: calcula el precio final de la entrada general
     * Retorna: el precio base, sin descuento (double)
     */
    @Override
    public double calcularPrecioFinal() {
        return getPrecioBase();
    }

}