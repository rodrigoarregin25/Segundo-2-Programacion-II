package gestordecine;

/**
 * Excepción personalizada que indica que se intenta registrar un elemento que
 * ya existe en el sistema.
 *
 * Extiende IllegalArgumentException para representar un error relacionado con
 * datos duplicados.
 */
public class RegistroDuplicadoException extends IllegalArgumentException {

    /**
     * Crea una excepción con un mensaje descriptivo del error.
     *
     * @param mensaje Descripción del elemento duplicado.
     */
    public RegistroDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
