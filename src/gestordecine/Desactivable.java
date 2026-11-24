package gestordecine;

/**
 * Define el comportamiento de los elementos que pueden desactivarse y
 * reactivarse sin ser eliminados del sistema.
 *
 * Permite conservar el historial y mantener las referencias de las reservas
 * existentes.
 */
public interface Desactivable {

    /**
     * Indica si el elemento se encuentra activo.
     *
     * @return true si está activo, false si está desactivado.
     */
    boolean isActiva();

    /**
     * Desactiva el elemento sin eliminarlo del sistema.
     */
    void desactivar();

    /**
     * Reactiva un elemento previamente desactivado.
     */
    void reactivar();
}
