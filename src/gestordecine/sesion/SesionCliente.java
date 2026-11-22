
package gestordecine.sesion;

import gestordecine.Cliente;

/**
 * Administra la sesión del cliente dentro de CINEMA.
 *
 * Permite iniciar sesión, consultar el cliente autenticado
 * y cerrar sesión sin modificar los datos persistidos.
 *
 * @author Rodrigo Arregin
 */
public final class SesionCliente {

    private static Cliente clienteActual;

    /**
     * Impide crear instancias de esta clase.
     */
    private SesionCliente() {
    }

    /**
     * Establece el cliente que inició sesión.
     *
     * Este método debe utilizarse únicamente después de
     * validar las credenciales del cliente.
     *
     * @param cliente Cliente que inició sesión correctamente.
     * @throws IllegalArgumentException Si el cliente es nulo.
     */
    public static void iniciarSesion(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser nulo"
            );
        }

        clienteActual = cliente;
    }

    /**
     * Obtiene el cliente que tiene una sesión activa.
     *
     * @return Cliente autenticado o null si no hay sesión.
     */
    public static Cliente getClienteActual() {
        return clienteActual;
    }

    /**
     * Indica si existe una sesión iniciada.
     *
     * @return true si hay un cliente autenticado; false en caso contrario.
     */
    public static boolean haySesion() {
        return clienteActual != null;
    }

    /**
     * Finaliza la sesión actual.
     *
     * No elimina el cliente ni sus reservas guardadas.
     */
    public static void cerrarSesion() {
        clienteActual = null;
    }
}
