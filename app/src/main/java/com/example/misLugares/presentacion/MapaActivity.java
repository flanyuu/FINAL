package com.example.misLugares.presentacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.LugaresBDAdapter;
import com.example.misLugares.R;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mapa;
    private LugaresBDAdapter lugares;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);
        lugares = ((Aplicacion) getApplication()).lugares;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
    }
    @Override public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
        }
        if (lugares.tamaño() > 0) {
            GeoPunto p = lugares.elementoPos(0).getPosicion();
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(p.getLatitud(), p.getLongitud()), 12));
        }
        for (int n=0; n<lugares.tamaño(); n++) {
            Lugar lugar = lugares.elementoPos(n);
            GeoPunto p = lugar.getPosicion();
            if (p != null && p.getLatitud() != 0) {
                Bitmap iGrande = BitmapFactory.decodeResource(getResources(), lugar.getTipo().getRecurso());
                Bitmap icono = Bitmap.createScaledBitmap(iGrande, iGrande.getWidth() / 7, iGrande.getHeight() / 7, false);
                mapa.addMarker(new MarkerOptions().position(new LatLng(p.getLatitud(), p.getLongitud()))
                        .title(lugar.getNombre()).snippet(lugar.getDireccion()).icon(BitmapDescriptorFactory.fromBitmap(icono)));
            }
        }
        mapa.setOnInfoWindowClickListener(this);
    }
    @Override public void onInfoWindowClick(Marker marker) {
        for (int pos = 0; pos < lugares.tamaño(); pos++) {
            if (lugares.elementoPos(pos).getNombre().equals(marker.getTitle())){
                Intent intent = new Intent(this, VistaLugarActivity.class);
                intent.putExtra("pos", pos);
                startActivity(intent);
                break;
            }
        }
    }
}
