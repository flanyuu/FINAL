package com.example.misLugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.R;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;

import java.util.ArrayList;
import java.util.List;

/**
 * Actividad para elegir punto A, punto B y opcionalmente más paradas antes de trazar una ruta.
 * Usada en Lugares Recomendados (mínimo punto A y punto B).
 */
public class RutaPuntosActivity extends AppCompatActivity {

    private List<GeoPunto> opcionesPuntos;
    private List<String> etiquetasPuntos;
    private Spinner spinnerPuntoA;
    private Spinner spinnerPuntoB;
    private LinearLayout containerParadas;
    private final List<Spinner> spinnersParadas = new ArrayList<>();
    private int posDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta_puntos);

        posDestino = getIntent().getIntExtra("pos_destino", 0);
        construirOpciones();

        spinnerPuntoA = findViewById(R.id.spinnerPuntoA);
        spinnerPuntoB = findViewById(R.id.spinnerPuntoB);
        containerParadas = findViewById(R.id.containerParadas);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, etiquetasPuntos);
        spinnerPuntoA.setAdapter(adapter);
        spinnerPuntoB.setAdapter(adapter);
        spinnerPuntoA.setSelection(0);
        spinnerPuntoB.setSelection(Math.min(posDestino + 1, etiquetasPuntos.size() - 1));

        findViewById(R.id.btnAnadirParada).setOnClickListener(v -> anadirParada());
        findViewById(R.id.btnTrazarRuta).setOnClickListener(v -> trazarRuta());
    }

    private void construirOpciones() {
        Aplicacion app = (Aplicacion) getApplication();
        opcionesPuntos = new ArrayList<>();
        etiquetasPuntos = new ArrayList<>();

        etiquetasPuntos.add("Mi ubicación");
        opcionesPuntos.add(app.posicionActual != null ? app.posicionActual : GeoPunto.SIN_POSICION);

        int n = app.lugares.tamaño();
        for (int i = 0; i < n; i++) {
            Lugar l = app.lugares.elementoPos(i);
            etiquetasPuntos.add(l.getNombre());
            opcionesPuntos.add(l.getPosicion());
        }
    }

    private void anadirParada() {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, etiquetasPuntos);
        s.setAdapter(adapter);

        TextView label = new TextView(this);
        label.setText("Parada " + (spinnersParadas.size() + 1));
        label.setPadding(0, 16, 0, 4);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.addView(label);
        row.addView(s, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48));

        containerParadas.addView(row);
        spinnersParadas.add(s);
    }

    private void trazarRuta() {
        List<GeoPunto> orden = new ArrayList<>();

        int idxA = spinnerPuntoA.getSelectedItemPosition();
        int idxB = spinnerPuntoB.getSelectedItemPosition();
        orden.add(opcionesPuntos.get(idxA));
        for (Spinner s : spinnersParadas) {
            int idx = s.getSelectedItemPosition();
            orden.add(opcionesPuntos.get(idx));
        }
        orden.add(opcionesPuntos.get(idxB));

        if (orden.size() < 2) {
            Toast.makeText(this, "Se necesitan al menos punto A y punto B", Toast.LENGTH_SHORT).show();
            return;
        }

        for (GeoPunto p : orden) {
            if (p == null || p.equals(GeoPunto.SIN_POSICION)) {
                Toast.makeText(this, "Todos los puntos deben tener coordenadas válidas", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        double[] lats = new double[orden.size()];
        double[] lngs = new double[orden.size()];
        for (int i = 0; i < orden.size(); i++) {
            lats[i] = orden.get(i).getLatitud();
            lngs[i] = orden.get(i).getLongitud();
        }

        Intent intent = new Intent(this, RutaActivity.class);
        intent.putExtra(RutaActivity.EXTRA_WAYPOINTS_LAT, lats);
        intent.putExtra(RutaActivity.EXTRA_WAYPOINTS_LNG, lngs);
        startActivity(intent);
        finish();
    }
}
