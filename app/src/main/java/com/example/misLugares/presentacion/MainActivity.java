package com.example.misLugares.presentacion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.CasosUsoLocalizacion;
import com.example.misLugares.LugaresBDAdapter;
import com.example.misLugares.R;
import com.example.misLugares.casos_uso.CasosUsoActividades;
import com.example.misLugares.casos_uso.CasosUsoLugar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private LugaresBDAdapter lugares;
    public AdaptadorLugaresBD adaptador;
    private CasosUsoLugar usoLugar;
    private CasosUsoActividades usoActividades;
    private RecyclerView recyclerView;
    private static final int SOLICITUD_PERMISO_LOCALIZACION = 1;
    private CasosUsoLocalizacion usoLocalizacion;
    static final int RESULTADO_PREFERENCIAS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Habilitar botón de retroceso para volver al menú principal
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> usoLugar.nuevo());

        usoActividades = new CasosUsoActividades(this);

        lugares = ((Aplicacion) getApplication()).lugares;
        usoLugar = new CasosUsoLugar(this, lugares);

        adaptador = ((Aplicacion) getApplication()).adaptador;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);
        adaptador.setOnItemClickListener(new View.OnClickListener() {
           @Override public void onClick(View v) {
               int pos = (Integer)(v.getTag());
               usoLugar.mostrar(pos);
           }
        });

        usoLocalizacion = new CasosUsoLocalizacion(this, SOLICITUD_PERMISO_LOCALIZACION);

        // Botón Atrás del sistema: volver al menú en lugar de cerrar la app
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    public void lanzarAcercaDe(View view) {
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }
    public void lanzarVistaLugar(View view){
        final EditText entrada = new EditText(this);
        entrada.setText("0");
        new AlertDialog.Builder(this)
                .setTitle("Selección de lugar")
                .setMessage("indica su id")
                .setView(entrada)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int wichButton) {
                        int id = Integer.parseInt(entrada.getText().toString());
                        usoLugar.mostrar(id);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    public void Salir(View view) {
        finish();
    }
    public void lanzarPreferencias(View view) {
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivityForResult(i, RESULTADO_PREFERENCIAS);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        if (id == R.id.action_settings) {
            lanzarPreferencias(null);
            return true;
        }
        if (id == R.id.acercaDe) {
            usoActividades.lanzarAcercaDe();
            return true;
        }
        if (id == R.id.menu_buscar) {
            lanzarVistaLugar(null);
            return true;
        }
        if (id == R.id.menu_mapa) {
            Intent intent = new Intent(this, MapaActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            usoLocalizacion.permisoConcedido();
    }
    @Override protected void onResume() {
        super.onResume();
        usoLocalizacion.activar();
    }
    @Override protected void onPause() {
        super.onPause();
        usoLocalizacion.desactivar();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULTADO_PREFERENCIAS) {
            adaptador.setCursor(lugares.extraeCursor());
            adaptador.notifyDataSetChanged();
        }
    }

}