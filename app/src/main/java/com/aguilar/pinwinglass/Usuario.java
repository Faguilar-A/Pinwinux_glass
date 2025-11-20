package com.aguilar.pinwinglass;

//Firebase trabaja mejor con objetos (POJOs) que con datos sueltos
public class Usuario {
    private String nombre;
    private String correo;
    private int edad;
    private String password;


    public Usuario() {} // Constructor vacío obligatorio para Firebase para que pueda leer los datos

    // Constructor completo para crear nuevos usuarios
    public Usuario(String nombre, String correo, int edad, String password) {
        this.nombre = nombre;
        this.correo = correo;
        this.edad = edad;
        this.password = password;
    }

    // Getters (Firebase los usa para leer los valores)
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public int getEdad() { return edad; }
    public String getPassword() { return password; }
}
