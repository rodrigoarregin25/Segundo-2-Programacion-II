
package gestordecine;

import java.io.Serializable;

/**
 * Representa un usuario registrado en CINEMA.
 *
 * Cada usuario se identifica mediante su DNI y puede tener
 * el rol CLIENTE o ADMINISTRADOR.
 */
public class Cliente implements Serializable {

    // Mantener este valor para conservar la compatibilidad con cine_app.dat.
    private static final long serialVersionUID = 8961938315294995730L;

    private int dni;
    private String nombre;
    private String apellido;
    private String email;
    private String contrasenia;

    // Los usuarios antiguos pueden tener el rol en null.
    // En ese caso, se consideran clientes.
    private Rol rol;

    /**
     * Crea un usuario con rol CLIENTE y valida sus datos.
     *
     * @param dni Documento de identidad del usuario.
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario.
     * @param email Correo electrónico del usuario.
     * @param contrasenia Contraseña de acceso.
     * @throws IllegalArgumentException Si el DNI es inválido, algún campo
     * obligatorio está vacío o el correo no tiene un formato válido.
     */
    public Cliente(int dni, String nombre, String apellido,
                   String email, String contrasenia) {

        if (dni <= 0) {
            throw new IllegalArgumentException(
                    "El numero de DNI debe ser mayor a cero"
            );
        }

        if (nombre == null || nombre.trim().equals("")) {
            throw new IllegalArgumentException(
                    "El nombre no puede estar vacio"
            );
        }

        if (apellido == null || apellido.trim().equals("")) {
            throw new IllegalArgumentException(
                    "El apellido no puede estar vacio"
            );
        }

        if (email == null || email.trim().equals("")) {
            throw new IllegalArgumentException(
                    "El email no puede estar vacio"
            );
        }

        if (!esEmailValido(email)) {
            throw new IllegalArgumentException(
                    "El email no es valido (ejemplo: nombre@mail.com)"
            );
        }

        if (contrasenia == null || contrasenia.trim().equals("")) {
            throw new IllegalArgumentException(
                    "La contraseña no puede estar vacia"
            );
        }

        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email.trim();
        this.contrasenia = contrasenia;
        this.rol = Rol.CLIENTE;
    }

    /**
     * Crea un usuario con un rol específico.
     *
     * @param dni Documento de identidad del usuario.
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario.
     * @param email Correo electrónico del usuario.
     * @param contrasenia Contraseña de acceso.
     * @param rol Rol que se asignará al usuario.
     * @throws IllegalArgumentException Si algún dato es inválido
     * o el rol es nulo.
     */
    public Cliente(int dni, String nombre, String apellido,
                   String email, String contrasenia, Rol rol) {

        this(dni, nombre, apellido, email, contrasenia);

        if (rol == null) {
            throw new IllegalArgumentException(
                    "El rol no puede ser null"
            );
        }

        this.rol = rol;
    }

    /**
     * Comprueba que un correo electrónico tenga un formato válido.
     *
     * Verifica que no tenga espacios, que exista una sola arroba
     * y que el dominio contenga un punto con texto a ambos lados.
     *
     * @param email Correo electrónico que se desea validar.
     * @return true si tiene un formato válido, false en caso contrario.
     */
    private static boolean esEmailValido(String email) {

        String texto = email.trim();

        if (texto.contains(" ")) {
            return false;
        }

        // Debe tener una única arroba y texto antes de ella.
        int arroba = texto.indexOf('@');

        if (arroba <= 0 || arroba != texto.lastIndexOf('@')) {
            return false;
        }

        // El dominio debe tener un punto con texto a ambos lados.
        String dominio = texto.substring(arroba + 1);
        int punto = dominio.lastIndexOf('.');

        if (punto <= 0 || punto == dominio.length() - 1) {
            return false;
        }

        if (dominio.startsWith(".") || dominio.contains("..")) {
            return false;
        }

        return true;
    }

    /**
     * Obtiene el DNI del usuario.
     *
     * @return DNI del usuario.
     */
    public int getDni() {
        return dni;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return Nombre del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el apellido del usuario.
     *
     * @return Apellido del usuario.
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return Correo electrónico del usuario.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Obtiene la contraseña del usuario.
     *
     * @return Contraseña del usuario.
     */
    public String getContrasenia() {
        return contrasenia;
    }

    /**
     * Obtiene el rol asignado al usuario.
     *
     * Los usuarios guardados antes de incorporar roles
     * se consideran CLIENTE cuando su rol es null.
     *
     * @return Rol del usuario.
     */
    public Rol getRol() {

        if (rol == null) {
            return Rol.CLIENTE;
        }

        return rol;
    }

    /**
     * Comprueba si el usuario es administrador.
     *
     * @return true si tiene rol ADMINISTRADOR, false en caso contrario.
     */
    public boolean esAdministrador() {
        return getRol() == Rol.ADMINISTRADOR;
    }

    /**
     * Comprueba si el usuario es cliente.
     *
     * @return true si tiene rol CLIENTE, false en caso contrario.
     */
    public boolean esCliente() {
        return getRol() == Rol.CLIENTE;
    }

    /**
     * Compara dos usuarios utilizando su DNI.
     *
     * @param otroObjeto Objeto que se desea comparar.
     * @return true si ambos usuarios tienen el mismo DNI.
     */
    @Override
    public boolean equals(Object otroObjeto) {

        if (this == otroObjeto) {
            return true;
        }

        if (otroObjeto == null || getClass() != otroObjeto.getClass()) {
            return false;
        }

        Cliente otroCliente = (Cliente) otroObjeto;

        return dni == otroCliente.dni;
    }

    /**
     * Calcula el código hash del usuario a partir de su DNI.
     *
     * @return Código hash del usuario.
     */
    @Override
    public int hashCode() {
        return dni;
    }

    /**
     * Modifica el rol asignado al usuario.
     *
     * @param nuevoRol Nuevo rol del usuario.
     * @throws IllegalArgumentException Si el rol es nulo.
     */
    public void setRol(Rol nuevoRol) {

        if (nuevoRol == null) {
            throw new IllegalArgumentException(
                    "El rol no puede ser nulo"
            );
        }

        this.rol = nuevoRol;
    }
}