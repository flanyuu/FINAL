package com.example.misLugares.casos_uso;

import android.content.Context;

import com.example.misLugares.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper para obtener rutas usando la API de GraphHopper.
 * Las rutas se dibujan en el mapa OSM desde la actividad que llama.
 */
public class RutasHelper {

    public interface RutasCallback {
        void onRuta(List<GeoPoint> puntos);
        void onError(String mensaje);
    }

    public static void trazarRuta(Context context, GeoPoint origen, GeoPoint destino, RutasCallback callback) {
        String apiKey = BuildConfig.GRAPHHOPPER_API_KEY;
        String url = "https://graphhopper.com/api/1/route?"
                + "point=" + origen.getLatitude() + "," + origen.getLongitude()
                + "&point=" + destino.getLatitude() + "," + destino.getLongitude()
                + "&points_encoded=false"
                + "&key=" + apiKey;

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String err = "Error HTTP: " + responseCode;
                    if (callback != null) {
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(() -> callback.onError(err));
                    }
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);
                reader.close();
                conn.disconnect();

                List<GeoPoint> puntos = parseGraphHopperResponse(json.toString());
                if (callback != null && puntos != null) {
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(() -> callback.onRuta(puntos));
                } else if (callback != null) {
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(() -> callback.onError("No se encontraron rutas"));
                }
            } catch (Exception e) {
                if (callback != null) {
                    String msg = e.getMessage() != null ? e.getMessage() : "Error de conexión";
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(() -> callback.onError(msg));
                }
            }
        }).start();
    }

    static List<GeoPoint> parseGraphHopperResponse(String jsonString) {
        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONArray paths = jsonObj.optJSONArray("paths");
            if (paths == null || paths.length() == 0) return null;

            JSONObject path = paths.getJSONObject(0);
            Object pointsObj = path.get("points");
            List<GeoPoint> puntos = new ArrayList<>();

            if (pointsObj instanceof JSONObject) {
                JSONObject pointsJson = (JSONObject) pointsObj;
                JSONArray coords = pointsJson.getJSONArray("coordinates");
                for (int i = 0; i < coords.length(); i++) {
                    JSONArray c = coords.getJSONArray(i);
                    double lon = c.getDouble(0);
                    double lat = c.getDouble(1);
                    puntos.add(new GeoPoint(lat, lon));
                }
            }
            return puntos;
        } catch (Exception e) {
            return null;
        }
    }
}
