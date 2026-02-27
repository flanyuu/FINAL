package com.example.misLugares.presentacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.LugaresBDAdapter;
import com.example.misLugares.R;
import com.example.misLugares.casos_uso.CasosUsoLugar;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapaActivity extends FragmentActivity {

    private MapView mapa;
    private LugaresBDAdapter lugares;
    private CasosUsoLugar usoLugar;
    private String filtroExtra = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa_con_fab);

        lugares = ((Aplicacion) getApplication()).lugares;
        usoLugar = new CasosUsoLugar(this, lugares);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            filtroExtra = extras.getString("filtro", "");
        }
        boolean soloIniciosSendero = extras != null && extras.getBoolean("solo_inicios_sendero", false);

        mapa = findViewById(R.id.mapa);
        mapa.setMultiTouchControls(true);
        IMapController controller = mapa.getController();
        controller.setZoom(12.0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(
                    new GpsMyLocationProvider(this), mapa);
            myLocationOverlay.enableMyLocation();
            mapa.getOverlays().add(myLocationOverlay);
        }

        boolean centrado = false;
        if (soloIniciosSendero) {
            java.util.List<Integer> idsInicios = lugares.listarIdsIniciosSendero();
            for (int lugarId : idsInicios) {
                try {
                    Lugar lugar = lugares.elemento(lugarId);
                    if (lugar == null) continue;
                    GeoPunto p = lugar.getPosicion();
                    if (p == null || p.getLatitud() == 0) continue;
                    if (!centrado) {
                        controller.setCenter(new GeoPoint(p.getLatitud(), p.getLongitud()));
                        centrado = true;
                    }
                    Marker marker = new Marker(mapa);
                    marker.setPosition(new GeoPoint(p.getLatitud(), p.getLongitud()));
                    marker.setTitle(lugar.getNombre());
                    marker.setSnippet(lugar.getDireccion());
                    marker.setRelatedObject(lugarId);
                    Bitmap iGrande = BitmapFactory.decodeResource(
                            getResources(), lugar.getTipo().getRecurso());
                    Bitmap icono = Bitmap.createScaledBitmap(
                            iGrande, Math.max(1, iGrande.getWidth() / 7), Math.max(1, iGrande.getHeight() / 7), false);
                    marker.setIcon(new BitmapDrawable(getResources(), icono));
                    marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setOnMarkerClickListener((m, mv) -> {
                        Object obj = m.getRelatedObject();
                        if (obj instanceof Integer) {
                            int id = (Integer) obj;
                            int pos = lugares.getAdaptador().posicionId(id);
                            if (pos >= 0) {
                                Intent intent = new Intent(this, VistaLugarActivity.class);
                                intent.putExtra("pos", pos);
                                startActivity(intent);
                            }
                        }
                        return true;
                    });
                    mapa.getOverlays().add(marker);
                } catch (Exception ignored) { }
            }
        } else {
            for (int n = 0; n < lugares.tamaño(); n++) {
                Lugar lugar = lugares.elementoPos(n);
                GeoPunto p = lugar.getPosicion();

                if (!filtroExtra.isEmpty() && !lugar.getTipo().name().equals(filtroExtra)) continue;

                if (p != null && p.getLatitud() != 0 && !centrado) {
                    controller.setCenter(new GeoPoint(p.getLatitud(), p.getLongitud()));
                    centrado = true;
                }

                if (p != null && p.getLatitud() != 0) {
                    Marker marker = new Marker(mapa);
                    marker.setPosition(new GeoPoint(p.getLatitud(), p.getLongitud()));
                    marker.setTitle(lugar.getNombre());
                    marker.setSnippet(lugar.getDireccion());
                    Bitmap iGrande = BitmapFactory.decodeResource(
                            getResources(), lugar.getTipo().getRecurso());
                    Bitmap icono = Bitmap.createScaledBitmap(
                            iGrande, Math.max(1, iGrande.getWidth() / 7), Math.max(1, iGrande.getHeight() / 7), false);
                    marker.setIcon(new BitmapDrawable(getResources(), icono));
                    marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    final int pos = n;
                    marker.setOnMarkerClickListener((m, mv) -> {
                        Intent intent = new Intent(this, VistaLugarActivity.class);
                        intent.putExtra("pos", pos);
                        startActivity(intent);
                        return true;
                    });
                    mapa.getOverlays().add(marker);
                }
            }
        }

        FloatingActionButton fab = findViewById(R.id.fabNuevoLugar);
        fab.setOnClickListener(v -> usoLugar.nuevo());
    }
}
