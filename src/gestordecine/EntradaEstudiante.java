package gestordecine;

/**
 * Representa una entrada de estudiante. Aplica un descuento del 20% sobre el
 * precio base.
 */
public class EntradaEstudiante extends Entrada {

    private static final double DESCUENTO = 0.20;

    /**
     * Crea una entrada de estudiante asociada a una butaca.
     *
     * @param butaca Butaca correspondiente a la entrada.
     * @param precioBase Precio base de la entrada.
     * @throws IllegalArgumentException Si la butaca es nula o el precio base es
     * menor o igual a cero.
     */
    public EntradaEstudiante(Butaca butaca, double precioBase) {
        super(butaca, precioBase);
    }

    /**
     * Calcula el precio final aplicando un descuento del 20%.
     *
     * @return Precio base con el descuento aplicado.
     */
    @Override
    public double calcularPrecioFinal() {
        return getPrecioBase() * (1 - DESCUENTO);
    }

    /**
     * Devuelve el nombre del tipo de entrada.
     *
     * @return El texto "Entrada Estudiante".
     */
    @Override
    public String getTipo() {
        return "Entrada Estudiante";
    }
}
