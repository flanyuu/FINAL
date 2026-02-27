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
import com.example.misLugares.BuildConfig;
import com.example.misLugares.R;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RutaActivity extends FragmentActivity {
    private static final String TAG = "RutaActivity";
    private MapView mapa;
    private GeoPunto origen;
    private GeoPunto destino;
    private Lugar lugarDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);

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

        mapa = findViewById(R.id.mapa);
        mapa.setMultiTouchControls(true);
        IMapController controller = mapa.getController();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(
                    new GpsMyLocationProvider(this), mapa);
            myLocationOverlay.enableMyLocation();
            mapa.getOverlays().add(myLocationOverlay);
        }

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

        GeoPoint origenPoint = new GeoPoint(origen.getLatitud(), origen.getLongitud());
        GeoPoint destinoPoint = new GeoPoint(destino.getLatitud(), destino.getLongitud());

        Marker markerOrigen = new Marker(mapa);
        markerOrigen.setPosition(origenPoint);
        markerOrigen.setTitle("Mi ubicación");
        markerOrigen.setIcon(defaultMarker(Color.GREEN));
        mapa.getOverlays().add(markerOrigen);

        Marker markerDestino = new Marker(mapa);
        markerDestino.setPosition(destinoPoint);
        markerDestino.setTitle(lugarDestino.getNombre());
        markerDestino.setSnippet(lugarDestino.getDireccion());
        markerDestino.setIcon(defaultMarker(Color.RED));
        mapa.getOverlays().add(markerDestino);

        BoundingBox bounds = new BoundingBox(
                Math.max(origen.getLatitud(), destino.getLatitud()),
                Math.max(origen.getLongitud(), destino.getLongitud()),
                Math.min(origen.getLatitud(), destino.getLatitud()),
                Math.min(origen.getLongitud(), destino.getLongitud())
        );
        mapa.zoomToBoundingBox(bounds.increaseByScale(1.5f), true);

        trazarRuta(origenPoint, destinoPoint);
        Toast.makeText(this, "Trazando ruta...", Toast.LENGTH_SHORT).show();
    }

    /** Genera puntos en línea recta entre dos puntos (solo si el ruteo falla). */
    private static List<GeoPoint> lineaRecta(GeoPoint desde, GeoPoint hasta, int numSegmentos) {
        List<GeoPoint> out = new ArrayList<>();
        for (int i = 1; i <= numSegmentos; i++) {
            double t = (double) i / numSegmentos;
            double lat = desde.getLatitude() + t * (hasta.getLatitude() - desde.getLatitude());
            double lon = desde.getLongitude() + t * (hasta.getLongitude() - desde.getLongitude());
            out.add(new GeoPoint(lat, lon));
        }
        return out;
    }

    private android.graphics.drawable.Drawable defaultMarker(int color) {
        android.graphics.drawable.ShapeDrawable shape = new android.graphics.drawable.ShapeDrawable(new android.graphics.drawable.shapes.OvalShape());
        shape.getPaint().setColor(color);
        shape.setIntrinsicWidth(24);
        shape.setIntrinsicHeight(24);
        return shape;
    }

    private void trazarRuta(GeoPoint origen, GeoPoint destino) {
        String apiKey = BuildConfig.GRAPHHOPPER_API_KEY;
        String url = "https://graphhopper.com/api/1/route?"
                + "point=" + origen.getLatitude() + "," + origen.getLongitude()
                + "&point=" + destino.getLatitude() + "," + destino.getLongitude()
                + "&points_encoded=false"
                + "&key=" + apiKey;

        Log.d(TAG, "URL GraphHopper: " + url.replace(apiKey, "***"));
        new ObtenerRutaTask(origen, destino).execute(url);
    }

    private class ObtenerRutaTask extends AsyncTask<String, Void, List<GeoPoint>> {
        private final GeoPoint origenPoint;
        private final GeoPoint destinoPoint;
        private String errorMessage = "";

        ObtenerRutaTask(GeoPoint origenPoint, GeoPoint destinoPoint) {
            this.origenPoint = origenPoint;
            this.destinoPoint = destinoPoint;
        }

        @Override
        protected List<GeoPoint> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Código de respuesta: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    InputStream err = conn.getErrorStream();
                    if (err != null) {
                        BufferedReader r = new BufferedReader(new InputStreamReader(err));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) sb.append(line);
                        errorMessage = sb.toString();
                    } else {
                        errorMessage = "Error HTTP: " + responseCode;
                    }
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);
                reader.close();
                conn.disconnect();

                JSONObject jsonObj = new JSONObject(json.toString());
                JSONArray paths = jsonObj.optJSONArray("paths");
                if (paths == null || paths.length() == 0) {
                    if (jsonObj.has("message")) {
                        errorMessage = jsonObj.getString("message");
                    } else {
                        errorMessage = "No se encontraron rutas";
                    }
                    return null;
                }

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
                if (puntos.isEmpty()) return null;
                Log.d(TAG, "Puntos GraphHopper: " + puntos.size());
                return puntos;
            } catch (Exception e) {
                errorMessage = "Excepción: " + e.getMessage();
                Log.e(TAG, "Error al obtener ruta", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<GeoPoint> puntos) {
            if (puntos == null || puntos.isEmpty()) {
                puntos = lineaRecta(origenPoint, destinoPoint, 50);
                if (puntos.isEmpty()) puntos = null;
                if (puntos != null)
                    Log.d(TAG, "Ruteo falló. Dibujando línea recta solo como fallback.");
            }
            if (puntos != null && !puntos.isEmpty() && mapa != null) {
                Polyline line = new Polyline();
                line.setPoints(puntos);
                line.getOutlinePaint().setColor(Color.BLUE);
                line.getOutlinePaint().setStrokeWidth(12f);
                mapa.getOverlays().add(0, line);
                mapa.invalidate();

                // Ajustar zoom para que la ruta completa se vea con un margen justo
                double north = puntos.get(0).getLatitude();
                double south = north;
                double east = puntos.get(0).getLongitude();
                double west = east;
                for (GeoPoint p : puntos) {
                    north = Math.max(north, p.getLatitude());
                    south = Math.min(south, p.getLatitude());
                    east = Math.max(east, p.getLongitude());
                    west = Math.min(west, p.getLongitude());
                }
                BoundingBox routeBounds = new BoundingBox(north, east, south, west);
                mapa.zoomToBoundingBox(routeBounds.increaseByScale(1.2f), true);

                Toast.makeText(RutaActivity.this, "Ruta trazada correctamente", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Ruta dibujada con " + puntos.size() + " puntos");
            } else {
                String mensaje = "No se pudo trazar la ruta";
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    mensaje += ": " + errorMessage;
                }
                Toast.makeText(RutaActivity.this, mensaje, Toast.LENGTH_LONG).show();
                Log.e(TAG, mensaje);
            }
        }
    }
}
