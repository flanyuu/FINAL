package com.example.misLugares.presentacion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.BuildConfig;
import com.example.misLugares.R;
import com.example.misLugares.datos.RutasBD;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.Ruta;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;
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
    public static final String EXTRA_WAYPOINTS_LAT = "waypoints_lat";
    public static final String EXTRA_WAYPOINTS_LNG = "waypoints_lng";
    public static final String EXTRA_RUTA_ID = "ruta_id";

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private int rutaId = -1;
    private RutasBD rutasBD;
    private View panelRutaInfo;
    private TextView textoDistanciaKm;
    private RatingBar valoracionRuta;
    private ChipGroup chipGroupModo;
    private GeoPunto origen;
    private GeoPunto destino;
    private Lugar lugarDestino;
    private List<GeoPoint> waypointsList;
    private String modoActual = "driving";
    private Polyline polylineActual;
    private static final String GRAPHHOPPER_BASE = "https://graphhopper.com/api/1/route";

    private static String getGraphHopperKey() {
        String key = BuildConfig.GRAPHHOPPER_API_KEY;
        return (key != null && !key.isEmpty()) ? key : "TU_GRAPHHOPPER_API_KEY";
    }

    /** Misma lógica que en la lista de rutas: estrellas vacías visibles con color más oscuro. */
    private void aplicarTintEstrellasVacias(RatingBar ratingBar) {
        if (ratingBar == null) return;
        Drawable progressDrawable = ratingBar.getProgressDrawable();
        if (progressDrawable instanceof LayerDrawable) {
            Drawable layer0 = ((LayerDrawable) progressDrawable).getDrawable(0);
            if (layer0 != null) {
                layer0.setTint(ContextCompat.getColor(this, R.color.rating_star_empty));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);

        panelRutaInfo = findViewById(R.id.panel_ruta_info);
        textoDistanciaKm = findViewById(R.id.distancia_ruta_km);
        valoracionRuta = findViewById(R.id.valoracion_ruta_detalle);
        aplicarTintEstrellasVacias(valoracionRuta);
        chipGroupModo = findViewById(R.id.chip_group_modo);
        chipGroupModo.check(R.id.chip_coche);
        rutasBD = new RutasBD(this);

        mapView = findViewById(R.id.mapa);
        mapView.getController().setZoom(14.0);
        mapView.setMultiTouchControls(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(myLocationOverlay);
        }

        chipGroupModo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID || mapView == null) return;
            if (checkedId == R.id.chip_coche) modoActual = "driving";
            else if (checkedId == R.id.chip_a_pie) modoActual = "walking";
            else if (checkedId == R.id.chip_bici) modoActual = "bicycling";
            else return;
            solicitarRuta();
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            rutaId = extras.getInt(EXTRA_RUTA_ID, -1);
            double[] lats = extras.getDoubleArray(EXTRA_WAYPOINTS_LAT);
            double[] lngs = extras.getDoubleArray(EXTRA_WAYPOINTS_LNG);
            if (lats != null && lngs != null && lats.length >= 2 && lats.length == lngs.length) {
                waypointsList = new ArrayList<>();
                for (int i = 0; i < lats.length; i++) {
                    waypointsList.add(new GeoPoint(lats[i], lngs[i]));
                }
                origen = null;
                destino = null;
                lugarDestino = null;
                panelRutaInfo.setVisibility(View.VISIBLE);
                valoracionRuta.setVisibility(rutaId > 0 ? View.VISIBLE : View.GONE);
                if (rutaId > 0) {
                    Ruta r = rutasBD.obtenerRuta(rutaId);
                    if (r != null) {
                        valoracionRuta.setRating(r.getValoracion());
                        if (r.getDistanciaKm() > 0) {
                            textoDistanciaKm.setText(getString(R.string.distancia_km, r.getDistanciaKm()));
                        }
                        String tipo = r.getTipoTransporte();
                        if ("foot".equals(tipo)) {
                            modoActual = "walking";
                            chipGroupModo.check(R.id.chip_a_pie);
                        } else if ("bike".equals(tipo)) {
                            modoActual = "bicycling";
                            chipGroupModo.check(R.id.chip_bici);
                        } else {
                            modoActual = "driving";
                            chipGroupModo.check(R.id.chip_coche);
                        }
                    }
                    valoracionRuta.setOnRatingBarChangeListener((ratingBar, valor, fromUser) -> {
                        if (fromUser && rutaId > 0) {
                            rutasBD.actualizarValoracion(rutaId, valor);
                        }
                    });
                }
            } else {
                int pos = extras.getInt("pos", 0);
                lugarDestino = ((Aplicacion) getApplication()).lugares.elementoPos(pos);
                destino = lugarDestino.getPosicion();
                origen = ((Aplicacion) getApplication()).posicionActual;
                waypointsList = null;
                Log.d(TAG, "Lugar destino: " + lugarDestino.getNombre());
                Log.d(TAG, "Origen: " + origen.getLatitud() + ", " + origen.getLongitud());
                Log.d(TAG, "Destino: " + destino.getLatitud() + ", " + destino.getLongitud());
            }
        }

        if (waypointsList != null && waypointsList.size() >= 2) {
            for (int i = 0; i < waypointsList.size(); i++) {
                GeoPoint p = waypointsList.get(i);
                Marker marker = new Marker(mapView);
                marker.setPosition(p);
                marker.setTitle(i == 0 ? "Punto A" : (i == waypointsList.size() - 1) ? "Punto B" : "Parada " + i);
                mapView.getOverlays().add(marker);
            }
            mapView.getController().setCenter(waypointsList.get(0));
            mapView.getController().setZoom(12.0);
            solicitarRuta();
            Toast.makeText(this, "Trazando ruta...", Toast.LENGTH_SHORT).show();
            return;
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

        GeoPoint origenPt = new GeoPoint(origen.getLatitud(), origen.getLongitud());
        GeoPoint destinoPt = new GeoPoint(destino.getLatitud(), destino.getLongitud());

        Marker markerOrigen = new Marker(mapView);
        markerOrigen.setPosition(origenPt);
        markerOrigen.setTitle("Mi ubicación");
        mapView.getOverlays().add(markerOrigen);

        Marker markerDestino = new Marker(mapView);
        markerDestino.setPosition(destinoPt);
        markerDestino.setTitle(lugarDestino.getNombre());
        markerDestino.setSnippet(lugarDestino.getDireccion());
        mapView.getOverlays().add(markerDestino);

        mapView.getController().setCenter(origenPt);
        mapView.getController().setZoom(12.0);

        panelRutaInfo.setVisibility(View.VISIBLE);
        valoracionRuta.setVisibility(View.GONE);
        solicitarRuta();
        Toast.makeText(this, "Trazando ruta...", Toast.LENGTH_SHORT).show();
    }

    private boolean isGraphHopperKeyConfigured() {
        String key = BuildConfig.GRAPHHOPPER_API_KEY;
        return key != null && !key.isEmpty() && !key.equals("TU_GRAPHHOPPER_API_KEY");
    }

    /** Pide la ruta a la API según el modo actual (waypoints o origen-destino). */
    private void solicitarRuta() {
        if (mapView == null) return;
        if (!isGraphHopperKeyConfigured()) {
            Toast.makeText(this, R.string.error_ruta_api_key, Toast.LENGTH_LONG).show();
            return;
        }
        if (polylineActual != null) {
            mapView.getOverlays().remove(polylineActual);
            mapView.invalidate();
            polylineActual = null;
        }
        if (waypointsList != null && waypointsList.size() >= 2) {
            String url = buildUrlConWaypoints(waypointsList, modoActual);
            Log.d(TAG, "Solicitando ruta (waypoints), modo: " + modoActual);
            new ObtenerRutaTask().execute(url);
        } else if (origen != null && destino != null && !origen.equals(GeoPunto.SIN_POSICION) && !destino.equals(GeoPunto.SIN_POSICION)) {
            GeoPoint o = new GeoPoint(origen.getLatitud(), origen.getLongitud());
            GeoPoint d = new GeoPoint(destino.getLatitud(), destino.getLongitud());
            trazarRuta(o, d, modoActual);
        }
    }

    /** Convierte modo interno (driving/walking/bicycling) a perfil GraphHopper (car/foot/bike). */
    private static String toGraphHopperProfile(String mode) {
        if ("walking".equals(mode)) return "foot";
        if ("bicycling".equals(mode)) return "bike";
        return "car";
    }

    private String buildUrlConWaypoints(List<GeoPoint> points, String mode) {
        StringBuilder url = new StringBuilder(GRAPHHOPPER_BASE).append("?");
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) url.append("&");
            url.append("point=").append(points.get(i).getLatitude()).append(",").append(points.get(i).getLongitude());
        }
        url.append("&profile=").append(toGraphHopperProfile(mode));
        url.append("&points_encoded=false");
        url.append("&key=").append(getGraphHopperKey());
        return url.toString();
    }

    private void trazarRuta(GeoPoint origenPt, GeoPoint destinoPt, String mode) {
        String url = GRAPHHOPPER_BASE + "?point=" + origenPt.getLatitude() + "," + origenPt.getLongitude()
                + "&point=" + destinoPt.getLatitude() + "," + destinoPt.getLongitude()
                + "&profile=" + toGraphHopperProfile(mode)
                + "&points_encoded=false"
                + "&key=" + getGraphHopperKey();
        Log.d(TAG, "URL GraphHopper: " + url);
        new ObtenerRutaTask().execute(url);
    }

    private static class RutaResultado {
        final List<GeoPoint> puntos;
        final int distanceMeters;
        RutaResultado(List<GeoPoint> puntos, int distanceMeters) {
            this.puntos = puntos;
            this.distanceMeters = distanceMeters;
        }
    }

    private class ObtenerRutaTask extends AsyncTask<String, Void, RutaResultado> {
        private String errorMessage = "";

        @Override
        protected RutaResultado doInBackground(String... urls) {
            try {
                Log.d(TAG, "Iniciando petición a GraphHopper API...");
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Código de respuesta: " + responseCode);

                InputStream in = (responseCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream()
                        : conn.getErrorStream();
                if (in == null) {
                    errorMessage = (responseCode == 401) ? "ERROR_401_KEY" : ("Error HTTP: " + responseCode);
                    Log.e(TAG, errorMessage);
                    conn.disconnect();
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();
                conn.disconnect();

                String jsonString = json.toString();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (responseCode == 401) {
                        errorMessage = "ERROR_401_KEY";
                    } else {
                        try {
                            JSONObject errObj = new JSONObject(jsonString);
                            errorMessage = errObj.optString("message", "Error HTTP: " + responseCode);
                        } catch (Exception ignored) {
                            errorMessage = "Error HTTP: " + responseCode;
                        }
                    }
                    Log.e(TAG, errorMessage);
                    return null;
                }
                Log.d(TAG, "Respuesta JSON (primeros 500 chars): " + jsonString.substring(0, Math.min(500, jsonString.length())));

                JSONObject jsonObj = new JSONObject(jsonString);

                if (jsonObj.has("message")) {
                    errorMessage = jsonObj.optString("message", "Error GraphHopper");
                    Log.e(TAG, errorMessage);
                    return null;
                }

                JSONArray paths = jsonObj.optJSONArray("paths");
                if (paths == null || paths.length() == 0) {
                    errorMessage = "No se encontraron rutas";
                    Log.e(TAG, errorMessage);
                    return null;
                }

                JSONObject path = paths.getJSONObject(0);
                double distanceMeters = path.optDouble("distance", 0);

                List<GeoPoint> puntos = new ArrayList<>();
                JSONObject pointsObj = path.optJSONObject("points");
                if (pointsObj != null && pointsObj.has("coordinates")) {
                    JSONArray coords = pointsObj.getJSONArray("coordinates");
                    for (int i = 0; i < coords.length(); i++) {
                        JSONArray pair = coords.getJSONArray(i);
                        double lon = pair.getDouble(0);
                        double lat = pair.getDouble(1);
                        puntos.add(new GeoPoint(lat, lon));
                    }
                }

                if (puntos.isEmpty()) {
                    errorMessage = "Ruta sin geometría";
                    Log.e(TAG, errorMessage);
                    return null;
                }

                Log.d(TAG, "GraphHopper: " + puntos.size() + " puntos, " + distanceMeters + " m");
                return new RutaResultado(puntos, (int) Math.round(distanceMeters));
            } catch (Exception e) {
                errorMessage = "Excepción: " + e.getMessage();
                Log.e(TAG, "Error al obtener ruta", e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(RutaResultado resultado) {
            if (resultado != null && resultado.puntos != null && resultado.puntos.size() > 0 && mapView != null) {
                if (polylineActual != null) {
                    mapView.getOverlays().remove(polylineActual);
                }
                polylineActual = new Polyline();
                polylineActual.setPoints(resultado.puntos);
                polylineActual.setColor(Color.BLUE);
                polylineActual.setWidth(12f);
                mapView.getOverlays().add(polylineActual);
                mapView.invalidate();

                double km = resultado.distanceMeters / 1000.0;
                if (panelRutaInfo != null && panelRutaInfo.getVisibility() == View.VISIBLE && textoDistanciaKm != null) {
                    textoDistanciaKm.setText(getString(R.string.distancia_km, km));
                }
                if (rutaId > 0 && rutasBD != null) {
                    rutasBD.actualizarDistancia(rutaId, km);
                }

                Toast.makeText(RutaActivity.this, "Ruta trazada correctamente", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Ruta dibujada exitosamente con " + resultado.puntos.size() + " puntos, " + km + " km");
            } else {
                String mensaje;
                if ("ERROR_401_KEY".equals(errorMessage)) {
                    mensaje = getString(R.string.error_ruta_api_key);
                } else {
                    mensaje = "No se pudo trazar la ruta";
                    if (!errorMessage.isEmpty()) {
                        mensaje += ": " + errorMessage;
                    }
                }
                Toast.makeText(RutaActivity.this, mensaje, Toast.LENGTH_LONG).show();
                Log.e(TAG, mensaje);
            }
        }
    }
}
