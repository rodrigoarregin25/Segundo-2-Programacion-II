package gestordecine;

/**
 * clases que no se borran, sino que se desactivan 
 *  Asi se conserva el historial y las reservas existentes no pierden
 * sus referencias.
 */
public interface Desactivable {

    /**
     * Que hace: dice si el elemento esta activo
     * Retorna: true si esta activo, false si esta desactivado
     */
    boolean isActiva();

    /**
     * Que hace: desactiva el elemento (no se borra)
     */
    void desactivar();

    /**
     * Que hace: vuelve a activar el elemento
     */
    void reactivar();
}