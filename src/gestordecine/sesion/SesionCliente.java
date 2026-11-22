package gestordecine.sesion;

import gestordecine.Cliente;
import gestordecine.Rol;

/**
 * Administra la sesión del cliente dentro de CINEMA.
 *
 * Permite iniciar sesión, consultar el cliente autenticado y cerrar sesión sin
 * modificar los datos persistidos.
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
     * Establece el usuario que inició sesión.
     *
     * Solo permite iniciar sesión a usuarios activos y con credenciales
     * previamente validadas.
     *
     * @param cliente Usuario que desea iniciar sesión.
     * @throws IllegalArgumentException Si el usuario es nulo.
     * @throws IllegalStateException Si la cuenta está desactivada.
     */
    public static void iniciarSesion(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser nulo"
            );
        }

        if (!cliente.isActivo()) {
            throw new IllegalStateException(
                    "No se puede iniciar sesión con una cuenta desactivada"
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
     * Comprueba si existe una sesión activa y habilitada.
     *
     * @return true si existe un usuario autenticado y activo.
     */
    public static boolean haySesion() {
        return clienteActual != null && clienteActual.isActivo();
    }

    /**
     * Finaliza la sesión actual.
     *
     * No elimina el cliente ni sus reservas guardadas.
     */
    public static void cerrarSesion() {
        clienteActual = null;
    }

    /**
     * Que hace: devuelve el rol del usuario que tiene la sesion iniciada
     * Retorna: el rol (Rol), o null si no hay sesion
     */
    public static Rol getRolActual() {
        if (clienteActual == null) {
            return null;
        }
        return clienteActual.getRol();
    }

    /**
     * Que hace: dice si el usuario con sesion iniciada es administrador
     * Retorna: true si hay sesion y es administrador, false si no
     */
    public static boolean esAdministrador() {
        return haySesion() && clienteActual.esAdministrador();
    }

    /**
     * Que hace: dice si el usuario con sesion iniciada es un cliente comun
     * Retorna: true si hay sesion y es cliente, false si no
     */
    public static boolean esCliente() {
        return haySesion() && clienteActual.esCliente();
    }

    /**
     * Que hace: comprueba que quien tiene la sesion sea administrador. Se usa
     * antes de abrir o ejecutar algo de administracion. Lanza:
     * IllegalStateException si no hay sesion o no es administrador
     */
    public static void exigirAdministrador() {
        if (!esAdministrador()) {
            throw new IllegalStateException(
                    "Acceso restringido: se requieren permisos de administrador");
        }
    }
}
