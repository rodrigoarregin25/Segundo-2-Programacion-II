package gestordecine;

/**
 * Representa una entrada para jubilados. Aplica un descuento del 45% sobre el
 * precio base.
 */
public class EntradaJubilados extends Entrada {

    private static final double DESCUENTO = 0.45;

    /**
     * Crea una entrada de jubilado asociada a una butaca.
     *
     * @param butaca Butaca correspondiente a la entrada.
     * @param precioBase Precio base de la entrada.
     * @throws IllegalArgumentException Si la butaca es nula o el precio base es
     * menor o igual a cero.
     */
    public EntradaJubilados(Butaca butaca, double precioBase) {
        super(butaca, precioBase);
    }

    /**
     * Calcula el precio final aplicando un descuento del 45%.
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
     * @return El texto "Entrada Jubilados".
     */
    @Override
    public String getTipo() {
        return "Entrada Jubilados";
    }
}
