/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author gimen
 */
package gestordecine;

import java.io.Serializable;

/**
 * Cliente de cine. Se lo identifica por su DNI. Tambien puede ser
 * administrador, segun su rol.
 */
public class Cliente implements Serializable {

    // Numero de version para poder leer los archivos .dat guardados antes
    private static final long serialVersionUID = 8961938315294995730L;
    private int dni;
    private String nombre;
    private String apellido;
    private String email;
    private String contrasenia;
    // Los clientes guardados antes de existir los roles tienen null aca,
    // por eso getRol() devuelve CLIENTE cuando es null
    private Rol rol;

    /**
     * Que hace: crea un cliente validando sus datos. Siempre con rol CLIENTE.
     * Parametros: dni (int), nombre (String), apellido (String), email
     * (String), contrasenia (String) Lanza: IllegalArgumentException si el dni
     * es menor o igual a 0 o si el nombre, apellido, email o contrasenia son
     * null o estan vacios
     */
    public Cliente(int dni, String nombre, String apellido, String email, String contrasenia) {

        //validaciones
        if (dni <= 0) {
            throw new IllegalArgumentException("El numero de DNI debe ser mayor a cero");
        }

        if (nombre == null || nombre.trim().equals("")) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }

        if (apellido == null || apellido.trim().equals("")) {
            throw new IllegalArgumentException("El apellido no puede estar vacio");
        }

        if (email == null || email.trim().equals("")) {
            throw new IllegalArgumentException("El email no puede estar vacio");
        }

        if (contrasenia == null || contrasenia.trim().equals("")) {
            throw new IllegalArgumentException("La contraseña no puede estar vacia");
        }

        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
        this.rol = Rol.CLIENTE;
    }

    /**
     * Que hace: crea un cliente con un rol elegido (por ejemplo un
     * administrador). Parametros: los mismos que el otro constructor, mas rol
     * (Rol) Lanza: IllegalArgumentException si el rol es null o algun dato es
     * invalido
     */
    public Cliente(int dni, String nombre, String apellido, String email,
            String contrasenia, Rol rol) {

        this(dni, nombre, apellido, email, contrasenia);

        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser null");
        }

        this.rol = rol;
    }

    /**
     * Que hace: devuelve el dni del cliente Retorna: el dni (int)
     */
    public int getDni() {
        return dni;
    }

    /**
     * Que hace: devuelve el nombre del cliente Retorna: el nombre (String)
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Que hace: devuelve el apellido del cliente Retorna: el apellido (String)
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Que hace: devuelve el email del cliente Retorna: el email (String)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Que hace: devuelve la contraseña del cliente Retorna: la contraseña
     * (String)
     */
    public String getContrasenia() {
        return contrasenia;
    }

    /**
     * Que hace: devuelve el rol del usuario. Si el cliente es de los datos
     * viejos (sin rol guardado), se considera CLIENTE. Retorna: el rol (Rol)
     */
    public Rol getRol() {
        if (rol == null) {
            return Rol.CLIENTE;
        }
        return rol;
    }

    /**
     * Que hace: dice si el usuario es administrador Retorna: true si su rol es
     * ADMINISTRADOR, false si no
     */
    public boolean esAdministrador() {
        return getRol() == Rol.ADMINISTRADOR;
    }

    /**
     * Que hace: dice si el usuario es un cliente comun Retorna: true si su rol
     * es CLIENTE, false si no
     */
    public boolean esCliente() {
        return getRol() == Rol.CLIENTE;
    }

    /**
     * Que hace: dice si dos clientes son el mismo (mismo dni) Parametros:
     * otroObjeto (Object) el objeto a comparar con este cliente Retorna: true
     * si tienen el mismo dni, false si no
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
     * Que hace: calcula el hash del cliente, usando el mismo campo que equals
     * Retorna: el hash (int)
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
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }

        this.rol = nuevoRol;
    }

}
