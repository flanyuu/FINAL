package com.example.misLugares.presentacion;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.R;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.Ruta;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LugaresDeRutaActivity extends AppCompatActivity {

    public static final String KEY_NOMBRE_RUTA = "nombreRuta";
    public static final String KEY_DISTANCIA_KM = "distanciaKm";
    public static final String KEY_VALORACION_RUTA = "valoracionRuta";
    public static final String KEY_LUGAR_IDS = "lugarIds";
    public static final String KEY_RUTA_ID = "rutaId";

    private static final String PREFS_VALORACION = "valoracion_rutas";

    private Ruta rutaActual;
    private RatingBar ratingBarRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lugares_de_ruta);

        String nombreRuta = getIntent().getStringExtra(KEY_NOMBRE_RUTA);
        double distanciaKm = getIntent().getDoubleExtra(KEY_DISTANCIA_KM, 0);
        ArrayList<Integer> lugarIds = getIntent().getIntegerArrayListExtra(KEY_LUGAR_IDS);
        if (lugarIds == null) lugarIds = new ArrayList<>();
        long rutaId = getIntent().getLongExtra(KEY_RUTA_ID, -1);

        rutaActual = new Ruta(nombreRuta != null ? nombreRuta : "Ruta", distanciaKm, lugarIds);
        if (rutaId >= 0) rutaActual.setId(rutaId);
        float valoracionGuardada = getSharedPreferences(PREFS_VALORACION, MODE_PRIVATE)
                .getFloat("ruta_val_" + rutaActual.rutaKey(), getIntent().getFloatExtra(KEY_VALORACION_RUTA, 0f));
        rutaActual.setValoracion(valoracionGuardada);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(nombreRuta != null ? nombreRuta : "Ruta");
            getSupportActionBar().setSubtitle(String.format(Locale.US, "%.2f km", distanciaKm));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ratingBarRuta = findViewById(R.id.ratingBarRuta);
        if (ratingBarRuta != null) {
            ratingBarRuta.setRating(rutaActual.getValoracion());
            ratingBarRuta.setOnRatingBarChangeListener((rb, valor, fromUser) -> {
                if (fromUser) {
                    rutaActual.setValoracion(valor);
                    getSharedPreferences(PREFS_VALORACION, MODE_PRIVATE)
                            .edit()
                            .putFloat("ruta_val_" + rutaActual.rutaKey(), valor)
                            .apply();
                    Toast.makeText(this, "Valoración guardada", Toast.LENGTH_SHORT).show();
                }
            });
            ratingBarRuta.post(() -> ratingBarRuta.setRating(rutaActual.getValoracion()));
        }

        MapView mapView = findViewById(R.id.mapaRuta);
        if (mapView != null) {
            final ArrayList<Integer> idsParaMapa = lugarIds;
            mapView.post(() -> dibujarRutaEnMapa(idsParaMapa));
        }

        RecyclerView recycler = findViewById(R.id.recyclerLugaresRuta);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        AdaptadorLugaresDeRuta adaptador = new AdaptadorLugaresDeRuta(((Aplicacion) getApplication()).lugares);
        adaptador.setLugarIds(lugarIds);
        recycler.setAdapter(adaptador);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (rutaActual != null && rutaActual.getId() >= 0) {
            getMenuInflater().inflate(R.menu.menu_lugares_de_ruta, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_editar_ruta && rutaActual != null && rutaActual.getId() >= 0) {
            Intent i = new Intent(this, EditarRutaActivity.class);
            i.putExtra(EditarRutaActivity.KEY_RUTA_ID, rutaActual.getId());
            i.putExtra(EditarRutaActivity.KEY_NOMBRE_RUTA, rutaActual.getNombre());
            i.putIntegerArrayListExtra(EditarRutaActivity.KEY_LUGAR_IDS, new ArrayList<>(rutaActual.getLugarIds()));
            startActivityForResult(i, 200);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rutaActual != null && ratingBarRuta != null) {
            float val = getSharedPreferences(PREFS_VALORACION, MODE_PRIVATE)
                    .getFloat("ruta_val_" + rutaActual.rutaKey(), rutaActual.getValoracion());
            rutaActual.setValoracion(val);
            ratingBarRuta.setRating(val);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && rutaActual != null && rutaActual.getId() >= 0) {
            com.example.misLugares.modelo.Ruta actualizada = ((Aplicacion) getApplication()).lugares.obtenerRutaSendero(rutaActual.getId());
            if (actualizada != null) {
                rutaActual.setNombre(actualizada.getNombre());
                rutaActual.setDistanciaKm(actualizada.getDistanciaKm());
                rutaActual.getLugarIds().clear();
                rutaActual.getLugarIds().addAll(actualizada.getLugarIds());
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(rutaActual.getNombre());
                    getSupportActionBar().setSubtitle(String.format(Locale.US, "%.2f km", rutaActual.getDistanciaKm()));
                }
                AdaptadorLugaresDeRuta ad = (AdaptadorLugaresDeRuta) ((RecyclerView) findViewById(R.id.recyclerLugaresRuta)).getAdapter();
                if (ad != null) ad.setLugarIds(rutaActual.getLugarIds());
                dibujarRutaEnMapa(new ArrayList<>(rutaActual.getLugarIds()));
            }
        }
    }

    private void dibujarRutaEnMapa(ArrayList<Integer> lugarIds) {
        MapView mapView = findViewById(R.id.mapaRuta);
        if (mapView == null || lugarIds == null || lugarIds.isEmpty()) return;
        com.example.misLugares.LugaresBDAdapter lugares = ((Aplicacion) getApplication()).lugares;
        List<GeoPoint> paradas = new ArrayList<>();
        List<String> nombresParadas = new ArrayList<>();
        for (int id : lugarIds) {
            try {
                Lugar l = lugares.elemento(id);
                if (l != null && l.getPosicion() != null && !l.getPosicion().equals(GeoPunto.SIN_POSICION)) {
                    paradas.add(new GeoPoint(l.getPosicion().getLatitud(), l.getPosicion().getLongitud()));
                    String nombre = l.getNombre();
                    nombresParadas.add(nombre != null && !nombre.isEmpty() ? nombre : "Parada");
                }
            } catch (Exception ignored) { }
        }
        if (paradas.isEmpty()) return;
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().clear();

        double distanciaKm = calcularDistanciaKm(paradas);
        rutaActual.setDistanciaKm(distanciaKm);
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(String.format(Locale.US, "%.2f km", distanciaKm));

        List<GeoPoint> puntosRuta = lineaCurvaEntreParadas(paradas);
        dibujarPolylineYMarcadores(mapView, puntosRuta, paradas, nombresParadas);
    }

    private static double calcularDistanciaKm(List<GeoPoint> paradas) {
        if (paradas == null || paradas.size() < 2) return 0;
        double metros = 0;
        for (int i = 0; i < paradas.size() - 1; i++) {
            GeoPoint a = paradas.get(i), b = paradas.get(i + 1);
            GeoPunto pA = new GeoPunto(a.getLongitude(), a.getLatitude());
            GeoPunto pB = new GeoPunto(b.getLongitude(), b.getLatitude());
            metros += pA.distancia(pB);
        }
        return Math.round(metros / 10.0) / 100.0;
    }

    private void dibujarPolylineYMarcadores(MapView mapView, List<GeoPoint> puntosRuta, List<GeoPoint> paradas, List<String> nombresParadas) {
        Polyline line = new Polyline();
        line.setPoints(puntosRuta);
        line.getOutlinePaint().setColor(Color.parseColor("#5BC8C8"));
        line.getOutlinePaint().setStrokeWidth(10f);
        mapView.getOverlays().add(0, line);

        if (paradas != null && nombresParadas != null) {
            for (int i = 0; i < paradas.size(); i++) {
                Marker m = new Marker(mapView);
                m.setPosition(paradas.get(i));
                String titulo = i < nombresParadas.size() ? nombresParadas.get(i) : null;
                m.setTitle(titulo != null && !titulo.isEmpty() ? titulo : ("Parada " + (i + 1)));
                mapView.getOverlays().add(m);
            }
        }

        zoomToRuta(mapView, puntosRuta);
        mapView.invalidate();
    }

    private void zoomToRuta(MapView mapView, List<GeoPoint> puntos) {
        if (puntos.isEmpty()) return;
        double north = puntos.get(0).getLatitude(), south = north, east = puntos.get(0).getLongitude(), west = east;
        for (GeoPoint p : puntos) {
            north = Math.max(north, p.getLatitude());
            south = Math.min(south, p.getLatitude());
            east = Math.max(east, p.getLongitude());
            west = Math.min(west, p.getLongitude());
        }
        BoundingBox box = new BoundingBox(north, east, south, west);
        mapView.zoomToBoundingBox(box.increaseByScale(1.3f), true);
    }

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

    private static GeoPoint catmullRom(GeoPoint p0, GeoPoint p1, GeoPoint p2, GeoPoint p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double lat = 0.5 * (2 * p1.getLatitude() + (-p0.getLatitude() + p2.getLatitude()) * t
                + (2 * p0.getLatitude() - 5 * p1.getLatitude() + 4 * p2.getLatitude() - p3.getLatitude()) * t2
                + (-p0.getLatitude() + 3 * p1.getLatitude() - 3 * p2.getLatitude() + p3.getLatitude()) * t3);
        double lon = 0.5 * (2 * p1.getLongitude() + (-p0.getLongitude() + p2.getLongitude()) * t
                + (2 * p0.getLongitude() - 5 * p1.getLongitude() + 4 * p2.getLongitude() - p3.getLongitude()) * t2
                + (-p0.getLongitude() + 3 * p1.getLongitude() - 3 * p2.getLongitude() + p3.getLongitude()) * t3);
        return new GeoPoint(lat, lon);
    }

    private static List<GeoPoint> lineaCurvaEntreParadas(List<GeoPoint> paradas) {
        if (paradas == null || paradas.size() < 2) return paradas;
        if (paradas.size() == 2) {
            List<GeoPoint> out = new ArrayList<>();
            out.add(paradas.get(0));
            out.addAll(lineaRecta(paradas.get(0), paradas.get(1), 25));
            out.add(paradas.get(1));
            return out;
        }
        List<GeoPoint> out = new ArrayList<>();
        int segmentosPorTramo = 20;
        for (int i = 0; i < paradas.size() - 1; i++) {
            GeoPoint p1 = paradas.get(i);
            GeoPoint p2 = paradas.get(i + 1);
            GeoPoint p0 = (i > 0) ? paradas.get(i - 1) : new GeoPoint(
                    p1.getLatitude() - (p2.getLatitude() - p1.getLatitude()),
                    p1.getLongitude() - (p2.getLongitude() - p1.getLongitude()));
            GeoPoint p3 = (i + 2 < paradas.size()) ? paradas.get(i + 2) : new GeoPoint(
                    p2.getLatitude() + (p2.getLatitude() - p1.getLatitude()),
                    p2.getLongitude() + (p2.getLongitude() - p1.getLongitude()));
            for (int k = 0; k < segmentosPorTramo; k++) {
                double t = (double) k / segmentosPorTramo;
                out.add(catmullRom(p0, p1, p2, p3, t));
            }
        }
        out.add(paradas.get(paradas.size() - 1));
        return out;
    }

    private static List<GeoPoint> lineaRectaEntreParadas(List<GeoPoint> paradas) {
        if (paradas == null || paradas.size() < 2) return paradas;
        List<GeoPoint> out = new ArrayList<>();
        for (int i = 0; i < paradas.size() - 1; i++) {
            out.add(paradas.get(i));
            out.addAll(lineaRecta(paradas.get(i), paradas.get(i + 1), 25));
        }
        out.add(paradas.get(paradas.size() - 1));
        return out;
    }
}
