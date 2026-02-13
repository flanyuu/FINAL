package com.example.misLugares.presentacion;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.CasosUsoLocalizacion;
import com.example.misLugares.R;
import com.example.misLugares.casos_uso.CasosUsoActividades;
import com.example.misLugares.datos.RutasBD;
import com.example.misLugares.modelo.Ruta;
import com.example.misLugares.modelo.WaypointRuta;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class RutasActivity extends AppCompatActivity {

    private RutasBD rutasBD;
    private AdaptadorRutas adaptadorRutas;
    private RecyclerView recyclerView;
    private CasosUsoActividades usoActividades;
    private static final int SOLICITUD_PERMISO_LOCALIZACION = 2;
    private CasosUsoLocalizacion usoLocalizacion;
    private static final int REQUEST_CREAR_RUTA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.rutas);
        }

        rutasBD = new RutasBD(this);
        usoActividades = new CasosUsoActividades(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, CrearRutaActivity.class);
            startActivityForResult(i, REQUEST_CREAR_RUTA);
        });

        adaptadorRutas = new AdaptadorRutas();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptadorRutas);
        adaptadorRutas.setOnItemClickListener(v -> {
            int pos = (Integer) v.getTag();
            Ruta ruta = adaptadorRutas.getRutaAt(pos);
            if (ruta == null || ruta.getWaypoints() == null || ruta.getWaypoints().size() < 2) return;
            double[] lats = new double[ruta.getWaypoints().size()];
            double[] lngs = new double[ruta.getWaypoints().size()];
            for (int i = 0; i < ruta.getWaypoints().size(); i++) {
                WaypointRuta w = ruta.getWaypoints().get(i);
                lats[i] = w.getLatitud();
                lngs[i] = w.getLongitud();
            }
            Intent intent = new Intent(this, RutaActivity.class);
            intent.putExtra(RutaActivity.EXTRA_RUTA_ID, ruta.getId());
            intent.putExtra(RutaActivity.EXTRA_WAYPOINTS_LAT, lats);
            intent.putExtra(RutaActivity.EXTRA_WAYPOINTS_LNG, lngs);
            startActivity(intent);
        });

        adaptadorRutas.setOnItemLongClickListener(v -> {
            int pos = (Integer) v.getTag();
            Ruta ruta = adaptadorRutas.getRutaAt(pos);
            if (ruta == null) return false;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.eliminar_ruta)
                    .setMessage(getString(R.string.eliminar_ruta_confirmar, ruta.getNombre()))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                        rutasBD.eliminarRuta(ruta.getId());
                        refrescarLista();
                        Toast.makeText(this, R.string.ruta_eliminada, Toast.LENGTH_SHORT).show();
                    })
                    .show();
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RutasActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        usoLocalizacion = new CasosUsoLocalizacion(this, SOLICITUD_PERMISO_LOCALIZACION);
        refrescarLista();
    }

    private void refrescarLista() {
        List<Ruta> rutas = rutasBD.obtenerTodasLasRutas();
        adaptadorRutas.setRutas(rutas);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREAR_RUTA && resultCode == RESULT_OK) {
            refrescarLista();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, PreferenciasActivity.class));
            return true;
        }
        if (id == R.id.acercaDe) {
            usoActividades.lanzarAcercaDe();
            return true;
        }
        if (id == R.id.menu_mapa) {
            startActivity(new Intent(this, MapaActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION && grantResults.length == 1 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            usoLocalizacion.permisoConcedido();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        usoLocalizacion.activar();
        refrescarLista();
    }

    @Override
    protected void onPause() {
        super.onPause();
        usoLocalizacion.desactivar();
    }
}
