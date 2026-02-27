package com.example.misLugares.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Ruta de sendero (predefinida en BD). Tiene id cuando viene de la BD para poder actualizarla.
 */
public class Ruta {
    private long id;
    private String nombre;
    private double distanciaKm;
    private List<Integer> lugarIds;
    private float valoracion;

    public Ruta(String nombre, double distanciaKm, List<Integer> lugarIds) {
        this.id = -1;
        this.nombre = nombre;
        this.distanciaKm = distanciaKm;
        this.lugarIds = lugarIds != null ? new ArrayList<>(lugarIds) : new ArrayList<>();
        this.valoracion = 0f;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre : ""; }
    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }
    public List<Integer> getLugarIds() { return lugarIds; }
    public float getValoracion() { return valoracion; }
    public void setValoracion(float valoracion) { this.valoracion = valoracion; }

    /** Clave estable para persistir valoración (ids ordenados unidos por _). */
    public String rutaKey() {
        List<Integer> ids = new ArrayList<>(lugarIds);
        java.util.Collections.sort(ids);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append('_');
            sb.append(ids.get(i));
        }
        return sb.toString();
    }
}
