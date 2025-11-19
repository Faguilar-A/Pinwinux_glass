package com.aguilar.pinwinglass;

public class Deteccion {
    private double distancia;
    private String fecha; // Firebase puede manejar Timestamp, pero usaremos String por simplicidad ahora

    public Deteccion() {}

    public Deteccion(double distancia, String fecha) {
        this.distancia = distancia;
        this.fecha = fecha;
    }

    public double getDistancia() { return distancia; }
    public String getFecha() { return fecha; }
}