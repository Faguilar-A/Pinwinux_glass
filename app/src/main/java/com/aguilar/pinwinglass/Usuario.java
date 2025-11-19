package com.aguilar.pinwinglass;

//Firebase trabaja mejor con objetos (POJOs) que con datos sueltos
public class Usuario {
    private String nombre;
    private String correo;
    private int edad;
    // No guardamos contraseña en texto plano en la nube por seguridad

    public Usuario() {} // Constructor vacío obligatorio para Firebase

    public Usuario(String nombre, String correo, int edad) {
        this.nombre = nombre;
        this.correo = correo;
        this.edad = edad;
    }

    // Getters obligatorios
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public int getEdad() { return edad; }
}
