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
     * Crea una entrada para una butaca con un precio base.
     *
     * @param butaca Butaca a la que corresponde la entrada.
     * @param precioBase Precio base con el que se vende la entrada, mayor a cero.
     * @throws IllegalArgumentException Si la butaca es nula.
     * @throws IllegalArgumentException Si el precio base no es un número válido
     * mayor a cero.
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
     * Calcula el precio final de la entrada según su tipo. Cada subclase lo
     * resuelve a su manera.
     *
     * @return El precio final de la entrada.
     */
    public abstract double calcularPrecioFinal();

    /**
     * Devuelve el nombre del tipo de entrada.
     *
     * @return El tipo de entrada, por ejemplo "General".
     */
    public abstract String getTipo();

    /**
     * Devuelve la butaca de la entrada.
     *
     * @return La butaca de la entrada.
     */
    public Butaca getButaca() {
        return butaca;
    }

    /**
     * Devuelve el precio base de la entrada.
     *
     * @return El precio base de la entrada.
     */
    public double getPrecioBase() {
        return precioBase;
    }

}
