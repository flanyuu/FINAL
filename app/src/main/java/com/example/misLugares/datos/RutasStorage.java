package com.example.misLugares.datos;

import android.content.SharedPreferences;

import com.example.misLugares.modelo.Ruta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y carga la lista de rutas generadas para no regenerar cada vez.
 * Solo se vuelve a generar cuando cambia el número de lugares con posición.
 */
public class RutasStorage {

    private static final String PREF_NAME = "rutas_app";
    private static final String KEY_JSON = "rutas_guardadas";
    private static final String KEY_PLACE_COUNT = "rutas_place_count";

    public static void guardar(SharedPreferences prefs, List<Ruta> rutas, int lugaresConPosicionCount) {
        try {
            JSONArray arr = new JSONArray();
            for (Ruta r : rutas) {
                JSONObject o = new JSONObject();
                o.put("nombre", r.getNombre());
                o.put("distanciaKm", r.getDistanciaKm());
                o.put("valoracion", (double) r.getValoracion());
                JSONArray ids = new JSONArray();
                for (int id : r.getLugarIds()) ids.put(id);
                o.put("lugarIds", ids);
                arr.put(o);
            }
            prefs.edit()
                    .putString(KEY_JSON, arr.toString())
                    .putInt(KEY_PLACE_COUNT, lugaresConPosicionCount)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Ruta> cargar(SharedPreferences prefs) {
        String s = prefs.getString(KEY_JSON, null);
        if (s == null || s.isEmpty()) return null;
        try {
            JSONArray arr = new JSONArray(s);
            List<Ruta> out = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String nombre = o.optString("nombre", "Ruta");
                double distanciaKm = o.optDouble("distanciaKm", 0);
                float valoracion = (float) o.optDouble("valoracion", 0);
                List<Integer> ids = new ArrayList<>();
                JSONArray idsArr = o.getJSONArray("lugarIds");
                for (int j = 0; j < idsArr.length(); j++) ids.add(idsArr.getInt(j));
                Ruta r = new Ruta(nombre, distanciaKm, ids);
                r.setValoracion(valoracion);
                out.add(r);
            }
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getSavedPlaceCount(SharedPreferences prefs) {
        return prefs.getInt(KEY_PLACE_COUNT, -1);
    }
}
