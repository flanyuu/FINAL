package com.example.misLugares.datos;

import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.Ruta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Genera rutas a partir de lugares: cada ruta es una secuencia de lugares
 * con distancia máxima 1 km entre consecutivos. Solo se devuelven rutas
 * que tengan al menos un lugar cercano al usuario.
 */
public class GeneradorRutas {

    /** Distancia máxima entre lugares consecutivos para formar una ruta (km). 5 km permite rutas de senderismo con 5 puntos clave. */
    public static final double MAX_KM_ENTRE_LUGARES = 5.0;
    public static final double MAX_KM_RUTA_CERCANA_USUARIO = 30.0;

    public static List<Ruta> generar(List<Integer> ids, List<Lugar> lugares, GeoPunto posicionUsuario) {
        List<Ruta> resultado = new ArrayList<>();
        if (lugares == null || lugares.isEmpty() || ids == null || ids.size() != lugares.size()) return resultado;

        int n = lugares.size();
        double[][] distancias = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double d = lugares.get(i).getPosicion().distancia(lugares.get(j).getPosicion());
                distancias[i][j] = distancias[j][i] = d / 1000.0;
            }
        }

        Set<Integer> usados = new HashSet<>();
        for (int semilla = 0; semilla < n; semilla++) {
            if (usados.contains(semilla)) continue;
            List<Integer> componente = new ArrayList<>();
            dfs(semilla, n, distancias, usados, componente);
            if (componente.size() < 2) continue;

            ordenarPorProximidad(componente, lugares);
            // Máximo 5 lugares por ruta
            int maxLugares = Math.min(5, componente.size());
            double distanciaTotal = 0;
            for (int i = 0; i < maxLugares - 1; i++) {
                int a = componente.get(i), b = componente.get(i + 1);
                distanciaTotal += distancias[a][b];
            }
            String nombre = lugares.get(componente.get(0)).getNombre();
            if (nombre == null || nombre.isEmpty()) nombre = "Ruta " + (resultado.size() + 1);

            List<Integer> idsRuta = new ArrayList<>();
            for (int i = 0; i < maxLugares; i++) idsRuta.add(ids.get(componente.get(i)));

            boolean cercanaAlUsuario = false;
            if (posicionUsuario != null && !posicionUsuario.equals(GeoPunto.SIN_POSICION)) {
                double umbralMetros = MAX_KM_RUTA_CERCANA_USUARIO * 1000;
                for (int i = 0; i < maxLugares; i++) {
                    int idx = componente.get(i);
                    if (lugares.get(idx).getPosicion().distancia(posicionUsuario) <= umbralMetros) {
                        cercanaAlUsuario = true;
                        break;
                    }
                }
            } else {
                cercanaAlUsuario = true;
            }
            if (cercanaAlUsuario)
                resultado.add(new Ruta(nombre, Math.round(distanciaTotal * 100) / 100.0, idsRuta));
        }
        return resultado;
    }

    private static void dfs(int v, int n, double[][] distancias, Set<Integer> usados, List<Integer> componente) {
        usados.add(v);
        componente.add(v);
        for (int w = 0; w < n; w++) {
            if (w != v && !usados.contains(w) && distancias[v][w] <= MAX_KM_ENTRE_LUGARES && distancias[v][w] > 0)
                dfs(w, n, distancias, usados, componente);
        }
    }

    private static void ordenarPorProximidad(List<Integer> componente, List<Lugar> lugares) {
        if (componente.size() <= 2) return;
        List<Integer> ordenado = new ArrayList<>();
        ordenado.add(componente.get(0));
        Set<Integer> restantes = new HashSet<>(componente);
        restantes.remove(componente.get(0));
        while (!restantes.isEmpty()) {
            int ultimo = ordenado.get(ordenado.size() - 1);
            int mejor = -1;
            double menorDist = Double.MAX_VALUE;
            for (int w : restantes) {
                double d = lugares.get(ultimo).getPosicion().distancia(lugares.get(w).getPosicion());
                if (d < menorDist) { menorDist = d; mejor = w; }
            }
            if (mejor == -1) break;
            ordenado.add(mejor);
            restantes.remove(mejor);
        }
        componente.clear();
        componente.addAll(ordenado);
    }
}
