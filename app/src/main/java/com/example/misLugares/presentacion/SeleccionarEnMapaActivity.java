package com.example.misLugares.presentacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.BuildConfig;
import com.example.misLugares.R;
import com.example.misLugares.modelo.GeoPunto;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Permite al usuario tocar un punto en el mapa para elegir una ubicación.
 * Se usa reverse geocoding (GraphHopper) para obtener la dirección y devolverla al llamador.
 */
public class SeleccionarEnMapaActivity extends FragmentActivity {

    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LON = "lon";
    public static final String EXTRA_DIRECCION = "direccion";

    private MapView mapa;
    private Marker markerSeleccionado;
    private GeoPoint puntoSeleccionado;
    private String direccionObtenida = "";
    private View btnUsarUbicacion;
    private float touchStartX, touchStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_en_mapa);

        mapa = findViewById(R.id.mapa);
        mapa.setMultiTouchControls(true);
        IMapController controller = mapa.getController();
        controller.setZoom(15.0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            MyLocationNewOverlay myLocation = new MyLocationNewOverlay(
                    new GpsMyLocationProvider(this), mapa);
            myLocation.enableMyLocation();
            mapa.getOverlays().add(myLocation);
        }

        GeoPunto actual = ((Aplicacion) getApplication()).posicionActual;
        if (actual != null && !actual.equals(GeoPunto.SIN_POSICION)) {
            controller.setCenter(new GeoPoint(actual.getLatitud(), actual.getLongitud()));
        } else {
            controller.setCenter(new GeoPoint(25.6866, -100.3161));
        }

        mapa.getOverlays().add(new org.osmdroid.views.overlay.gestures.RotationGestureOverlay(mapa));

        mapa.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    return false;
                case android.view.MotionEvent.ACTION_UP:
                    float dx = event.getX() - touchStartX;
                    float dy = event.getY() - touchStartY;
                    if (dx * dx + dy * dy < 100) {
                        org.osmdroid.api.IGeoPoint igp = mapa.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                        if (igp != null) {
                            GeoPoint p = new GeoPoint(igp.getLatitude(), igp.getLongitude());
                            seleccionarPunto(p);
                            return true;
                        }
                    }
                    return false;
                default:
                    return false;
            }
        });

        btnUsarUbicacion = findViewById(R.id.btnUsarUbicacion);
        btnUsarUbicacion.setAlpha(0.5f);
        btnUsarUbicacion.setOnClickListener(v -> devolverResultado());
    }

    private void seleccionarPunto(GeoPoint p) {
        puntoSeleccionado = p;
        if (markerSeleccionado != null) {
            mapa.getOverlays().remove(markerSeleccionado);
        }
        markerSeleccionado = new Marker(mapa);
        markerSeleccionado.setPosition(p);
        markerSeleccionado.setTitle("Ubicación elegida");
        mapa.getOverlays().add(markerSeleccionado);
        mapa.invalidate();
        btnUsarUbicacion.setEnabled(true);
        btnUsarUbicacion.setAlpha(1f);
        Toast.makeText(this, "Obteniendo dirección...", Toast.LENGTH_SHORT).show();
        new ReverseGeocodeTask().execute(p.getLatitude(), p.getLongitude());
    }

    private void devolverResultado() {
        if (puntoSeleccionado == null) return;
        Intent data = new Intent();
        data.putExtra(EXTRA_LAT, puntoSeleccionado.getLatitude());
        data.putExtra(EXTRA_LON, puntoSeleccionado.getLongitude());
        data.putExtra(EXTRA_DIRECCION, direccionObtenida != null ? direccionObtenida : "");
        setResult(RESULT_OK, data);
        finish();
    }

    private class ReverseGeocodeTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            double lat = params[0];
            double lon = params[1];
            String key = BuildConfig.GRAPHHOPPER_API_KEY;
            String urlStr = "https://graphhopper.com/api/1/geocode?reverse=true&point=" + lat + "," + lon + "&limit=1&key=" + key;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();
                return buildAddressFromHits(sb.toString());
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String address) {
            direccionObtenida = address != null ? address : "";
            if (markerSeleccionado != null && !direccionObtenida.isEmpty()) {
                markerSeleccionado.setSnippet(direccionObtenida);
            }
        }
    }

    private String buildAddressFromHits(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray hits = root.optJSONArray("hits");
            if (hits == null || hits.length() == 0) return "";
            JSONObject hit = hits.getJSONObject(0);
            StringBuilder addr = new StringBuilder();
            if (hit.has("street")) addr.append(hit.optString("street", ""));
            if (hit.has("housenumber")) {
                if (addr.length() > 0) addr.append(" ");
                addr.append(hit.optString("housenumber", ""));
            }
            if (addr.length() == 0 && hit.has("name")) addr.append(hit.optString("name", ""));
            if (hit.has("city")) {
                if (addr.length() > 0) addr.append(", ");
                addr.append(hit.optString("city", ""));
            }
            if (hit.has("postcode")) {
                if (addr.length() > 0) addr.append(" ");
                addr.append(hit.optString("postcode", ""));
            }
            if (hit.has("country")) {
                if (addr.length() > 0) addr.append(", ");
                addr.append(hit.optString("country", ""));
            }
            return addr.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
