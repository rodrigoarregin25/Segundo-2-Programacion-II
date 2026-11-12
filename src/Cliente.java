/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gimen
 */

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
        