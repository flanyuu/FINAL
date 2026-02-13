package com.example.misLugares.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de una ruta guardada: nombre y lista de waypoints (mínimo 2: punto A y B).
 */
public class Ruta {
    private int id;
    private String nombre;
    private long fecha;
    private List<WaypointRuta> waypoints = new ArrayList<>();
    /** Distancia total en kilómetros (0 si aún no se ha calculado). */
    private double distanciaKm;
    /** Valoración de 0 a 5 estrellas, como en Mis Lugares. */
    private float valoracion;
    /** Tipo de transporte pensado para la ruta: "car", "foot", "bike". */
    private String tipoTransporte = "car";

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    public List<WaypointRuta> getWaypoints() { return waypoints; }
    public void setWaypoints(List<WaypointRuta> waypoints) { this.waypoints = waypoints != null ? waypoints : new ArrayList<>(); }
    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }
    public float getValoracion() { return valoracion; }
    public void setValoracion(float valoracion) { this.valoracion = valoracion; }
    public String getTipoTransporte() { return tipoTransporte != null ? tipoTransporte : "car"; }
    public void setTipoTransporte(String tipoTransporte) { this.tipoTransporte = tipoTransporte != null ? tipoTransporte : "car"; }
}
