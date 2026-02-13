package com.example.misLugares.modelo;

/**
 * Un punto de una ruta (orden, coordenadas y nombre opcional).
 */
public class WaypointRuta {
    private int orden;
    private double latitud;
    private double longitud;
    private String nombre;

    public WaypointRuta(int orden, double latitud, double longitud, String nombre) {
        this.orden = orden;
        this.latitud = latitud;
        this.longitud = longitud;
        this.nombre = nombre != null ? nombre : "";
    }

    public int getOrden() { return orden; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getNombre() { return nombre; }
}
