package com.example.misLugares.presentacion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.R;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RutaActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "RutaActivity";
    private GoogleMap mapa;
    private GeoPunto origen;
    private GeoPunto destino;
    private Lugar lugarDestino;
    private static final String DIRECTIONS_API_KEY = "AIzaSyBD5Lj82jqqYX0bSuhXPTLUs_fS7PY2ylQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);

        // Obtener los datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int pos = extras.getInt("pos", 0);
            lugarDestino = ((Aplicacion) getApplication()).lugares.elementoPos(pos);
            destino = lugarDestino.getPosicion();
            origen = ((Aplicacion) getApplication()).posicionActual;

            Log.d(TAG, "Lugar destino: " + lugarDestino.getNombre());
            Log.d(TAG, "Origen: " + origen.getLatitud() + ", " + origen.getLongitud());
            Log.d(TAG, "Destino: " + destino.getLatitud() + ", " + destino.getLongitud());
        }

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
        }

        // Validar que tenemos las coordenadas necesarias
        if (origen == null || origen.equals(GeoPunto.SIN_POSICION)) {
            Toast.makeText(this, "No se pudo obtener tu ubicación actual",
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error: origen es nulo o sin posición");
            finish();
            return;
        }

        if (destino == null || destino.equals(GeoPunto.SIN_POSICION)) {
            Toast.makeText(this, "El lugar no tiene coordenadas válidas",
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error: destino es nulo o sin posición");
            finish();
            return;
        }

        // Agregar marcadores para origen y destino
        LatLng origenLatLng = new LatLng(origen.getLatitud(), origen.getLongitud());
        LatLng destinoLatLng = new LatLng(destino.getLatitud(), destino.getLongitud());

        // Marcador de origen (posición actual) - Verde
        mapa.addMarker(new MarkerOptions()
                .position(origenLatLng)
                .title("Mi ubicación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Marcador de destino - Rojo
        mapa.addMarker(new MarkerOptions()
                .position(destinoLatLng)
                .title(lugarDestino.getNombre())
                .snippet(lugarDestino.getDireccion())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Ajustar la cámara para mostrar ambos marcadores
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origenLatLng);
        builder.include(destinoLatLng);
        LatLngBounds bounds = builder.build();

        int padding = 150; // Padding en píxeles
        mapa.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        // Trazar la ruta
        trazarRuta(origenLatLng, destinoLatLng);

        Toast.makeText(this, "Trazando ruta...", Toast.LENGTH_SHORT).show();
    }

    private void trazarRuta(LatLng origen, LatLng destino) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origen.latitude + "," + origen.longitude +
                "&destination=" + destino.latitude + "," + destino.longitude +
                "&key=" + DIRECTIONS_API_KEY;

        Log.d(TAG, "URL de Directions API: " + url);
        new ObtenerRutaTask().execute(url);
    }

    private class ObtenerRutaTask extends AsyncTask<String, Void, List<LatLng>> {
        private String errorMessage = "";

        @Override
        protected List<LatLng> doInBackground(String... urls) {
            try {
                Log.d(TAG, "Iniciando petición a Directions API...");
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Código de respuesta: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    errorMessage = "Error HTTP: " + responseCode;
                    Log.e(TAG, errorMessage);
                    return null;
                }

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();
                conn.disconnect();

                String jsonString = json.toString();
                Log.d(TAG, "Respuesta JSON (primeros 500 chars): " + jsonString.substring(0, Math.min(500, jsonString.length())));

                JSONObject jsonObj = new JSONObject(jsonString);

                // Verificar el estado de la respuesta
                String status = jsonObj.getString("status");
                Log.d(TAG, "Estado de la API: " + status);

                if (!status.equals("OK")) {
                    errorMessage = "Estado de API: " + status;
                    if (jsonObj.has("error_message")) {
                        errorMessage += " - " + jsonObj.getString("error_message");
                    }
                    Log.e(TAG, errorMessage);
                    return null;
                }

                JSONArray routes = jsonObj.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject poly = route.getJSONObject("overview_polyline");
                    String polyline = poly.getString("points");

                    Log.d(TAG, "Polyline obtenida, longitud: " + polyline.length());
                    List<LatLng> puntos = decodePoly(polyline);
                    Log.d(TAG, "Puntos decodificados: " + puntos.size());

                    return puntos;
                } else {
                    errorMessage = "No se encontraron rutas";
                    Log.e(TAG, errorMessage);
                }
            } catch (Exception e) {
                errorMessage = "Excepción: " + e.getMessage();
                Log.e(TAG, "Error al obtener ruta", e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LatLng> puntos) {
            if (puntos != null && puntos.size() > 0 && mapa != null) {
                // Dibujar la ruta con una línea azul gruesa
                PolylineOptions lineOptions = new PolylineOptions()
                        .addAll(puntos)
                        .width(12)  // Grosor de la línea
                        .color(Color.BLUE)  // Color azul
                        .geodesic(true);  // Línea geodésica (sigue la curvatura de la tierra)

                mapa.addPolyline(lineOptions);

                Toast.makeText(RutaActivity.this, "Ruta trazada correctamente", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Ruta dibujada exitosamente con " + puntos.size() + " puntos");
            } else {
                String mensaje = "No se pudo trazar la ruta";
                if (!errorMessage.isEmpty()) {
                    mensaje += ": " + errorMessage;
                }
                Toast.makeText(RutaActivity.this, mensaje, Toast.LENGTH_LONG).show();
                Log.e(TAG, mensaje);
            }
        }

        /**
         * Decodifica una cadena polyline codificada en el formato de Google Maps
         */
        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                poly.add(new LatLng(lat / 1E5, lng / 1E5));
            }
            return poly;
        }
    }
}