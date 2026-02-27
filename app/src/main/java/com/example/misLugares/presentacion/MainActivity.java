package com.example.misLugares.presentacion;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.example.misLugares.AdaptadorLugares;
import com.example.misLugares.Aplicacion;
import com.example.misLugares.CasosUsoLocalizacion;
import com.example.misLugares.LugaresBDAdapter;
import com.example.misLugares.R;
import com.example.misLugares.casos_uso.CasosUsoActividades;
import com.example.misLugares.casos_uso.CasosUsoLugar;
import com.example.misLugares.datos.LugaresBD;
import com.example.misLugares.datos.RepositorioLugares;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.misLugares.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private Button bAcercaDe;
    private Button bSalir;
    //private RepositorioLugares lugares;
    private LugaresBDAdapter lugares;
    public AdaptadorLugaresBD adaptador;
    private CasosUsoLugar usoLugar;
    private CasosUsoActividades usoActividades;
    private ActivityMainBinding binding;
    private RecyclerView recyclerView;
   // public AdaptadorLugares adaptador;
    private static final int SOLICITUD_PERMISO_LOCALIZACION = 1;
    private CasosUsoLocalizacion usoLocalizacion;
    static final int RESULTADO_PREFERENCIAS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/
            usoLugar.nuevo();
        });

        usoActividades = new CasosUsoActividades(this);
        /*bAcercaDe = findViewById(R.id.button3);
        bAcercaDe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lanzarAcercaDe(null);
                usoActividades.lanzarAcercaDe();
            }
        });

        //bSalir = findViewById(R.id.button4);
        bSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/
        
        lugares = ((Aplicacion) getApplication()).lugares;
        usoLugar = new CasosUsoLugar(this, lugares);
        //usoLugar.mostrar(pos);

        adaptador = ((Aplicacion) getApplication()).adaptador;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adaptador);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        int margin = (int) (getResources().getDisplayMetrics().density * 8);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(margin, margin, margin, margin);
            }
        });
        adaptador.setOnItemClickListener(new View.OnClickListener() {
           @Override public void onClick(View v) {
               int pos = (Integer)(v.getTag());
               usoLugar.mostrar(pos);
           }
        });

        usoLocalizacion = new CasosUsoLocalizacion(this, SOLICITUD_PERMISO_LOCALIZACION);
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
        //startActivity(i);
        startActivityForResult(i,RESULTADO_PREFERENCIAS);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            lanzarPreferencias(null);
            return true;
        }
        if (id == R.id.acercaDe) {
            //lanzarAcercaDe(null);
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
            adaptador.setCursor(lugares.extraeCursorCompleto());
            adaptador.notifyDataSetChanged();
        }
    }
}