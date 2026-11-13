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
    
    

/**
 * Qué hace?: crea un cliente con sus datos
 * Parametros: dni (int), nombre (String), apellido (String), email (String)
 */


public Cliente(int dni, String nombre, String apellido, String email){
    
    //validaciones
    
    
    /**
 * Que hace: crea un cliente validando sus datos
 * Parametros: dni (int), nombre (String), apellido (String), email (String)
 * Lanza: IllegalArgumentException si el dni es menor o igual a 0
 *        o si el nombre, apellido o email son null o estan vacios
 */
    
    if (dni <=0){
    throw new IllegalArgumentException("El numero de DNI debe ser mayor a cero");

}
    
    if(nombre == null || nombre.trim().equals("") ){
        throw new IllegalArgumentException("El nombre no puede estar vacio");
        
        
    }
    
    if(apellido == null || apellido.trim().equals("")){
        throw new IllegalArgumentException("El apellido no puede estar vacio");
    }
    
    if(email == null || email.trim().equals("")){ //preguntar a Rodrigo si dejamos email o no. depende interfaz
        throw new IllegalArgumentException("El email no puede estar vacio");
    }
    
    
    
    
    this.dni = dni;
    this.nombre = nombre; 
    this.apellido = apellido;
    this.email = email; 
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

}
        