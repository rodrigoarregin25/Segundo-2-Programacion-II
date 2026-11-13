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
 * Cliente de cine. Se lo identifica por su DNI.

 */

public class Cliente implements Serializable {
    private int dni; 
    private String nombre; 
    private String apellido; 
    private String email; 
    private String contrasenia; 
    
    
    

/**
 * Que hace: crea un cliente validando sus datos
 * Parametros: dni (int), nombre (String), apellido (String), email (String),
 *             contrasenia (String)
 * Lanza: IllegalArgumentException si el dni es menor o igual a 0
 *        o si el nombre, apellido, email o contrasenia son null o estan vacios
 */


public Cliente(int dni, String nombre, String apellido, String email, String contrasenia){
    
    //validaciones
    

    
    if(dni <=0){
    throw new IllegalArgumentException("El numero de DNI debe ser mayor a cero");

}
    
    if(nombre == null || nombre.trim().equals("") ){
        throw new IllegalArgumentException("El nombre no puede estar vacio");
        
        
    }
    
    if(apellido == null || apellido.trim().equals("")){
        throw new IllegalArgumentException("El apellido no puede estar vacio");
    }
    
    if(email == null || email.trim().equals("")){ 
        throw new IllegalArgumentException("El email no puede estar vacio");
    }
    
    if(contrasenia == null || contrasenia.trim().equals("")){ 
        throw new IllegalArgumentException("La contraseña no puede estar vacia");
    }
    
    
    
    
    this.dni = dni;
    this.nombre = nombre; 
    this.apellido = apellido;
    this.email = email; 
    this.contrasenia = contrasenia; 
    
    }

/**
 * Que hace: devuelve el dni del cliente
 * Retorna: el dni (int)
 */
public int getDni (){
    return dni;
}
/**
* Que hace: devuelve el nombre del cliente
* Retorna: el nombre (String)
*/
public String getNombre (){
    return nombre;
}

/**
* Que hace: devuelve el apellido del cliente
* Retorna: el apellido (String)
*/
public String getApellido (){
    return apellido;
    
}

/**
* Que hace: devuelve el email del cliente
* Retorna: el email (String)
*/
public String getEmail (){
    return email;
}

/**
 * Que hace: devuelve la contraseña del cliente
 * Retorna: la contraseña (String)
 */

public String getContrasenia () {
    return contrasenia;
}


    /**
     * Que hace: dice si dos clientes son el mismo (mismo dni)
     * Parametros: otroObjeto (Object) el objeto a comparar con este cliente
     * Retorna: true si tienen el mismo dni, false si no
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

}
        