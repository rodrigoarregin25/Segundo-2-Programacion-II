/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestordecine;

import java.io.Serializable;

/**
 * Entrada para una butaca de una funcion. Guarda el precio base con el que se
 * vendio. Cada tipo de entrada calcula su precio final.
 */
public abstract class Entrada implements Serializable {

    private Butaca butaca;
    private double precioBase;

    /**
     * Que hace: crea una entrada para una butaca con un precio base Parametros:
     * butaca (Butaca), precioBase (double) Lanza: IllegalArgumentException si
     * la butaca es nula o el precio es menor o igual a cero
     */
    public Entrada(Butaca butaca, double precioBase) {
        if (butaca == null) {
            throw new IllegalArgumentException("La butaca no puede ser nula");
        }
        if (!Double.isFinite(precioBase) || precioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser un número válido mayor a cero");
        }
        this.butaca = butaca;
        this.precioBase = precioBase;

    }

    /**
     * Que hace: calcula el precio final de la entrada segun su tipo Retorna: el
     * precio final (double)
     */
    public abstract double calcularPrecioFinal();

    /**
     * Que hace: devuelve el nombre del tipo de entrada Retorna: el tipo, por
     * ejemplo "General" (String)
     */
    public abstract String getTipo();

    /**
     * Que hace: devuelve la butaca de la entrada Retorna: la butaca (Butaca)
     */
    public Butaca getButaca() {
        return butaca;
    }

    /**
     * Que hace: devuelve el precio base de la entrada Retorna: el precio base
     * (double)
     */
    public double getPrecioBase() {
        return precioBase;
    }

}
