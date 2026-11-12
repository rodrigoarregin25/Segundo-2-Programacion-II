/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gimen
 */

import java.io.Serializable; 

public class Cliente implements Serializable {
    private int dni; 
    private String nombre; 
    private String apellido; 
    private String email; 
    
    


public Cliente(int dni, String nombre, String apellido, String email){
    this.dni = dni;
    this.nombre = nombre; 
    this.apellido = apellido;
    this.email = email; 
    }


public int getDni (){
    return dni;
}

public String getNombre (){
    return nombre;
}

public String getApellido (){
    return apellido;
    
}

public String getEmail (){
    return email;
}
    
}
        