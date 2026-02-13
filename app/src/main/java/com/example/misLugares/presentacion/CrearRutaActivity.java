package com.example.misLugares.presentacion;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.R;
import com.example.misLugares.datos.RutasBD;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.WaypointRuta;

import java.util.ArrayList;
import java.util.List;

/**
 * Crear y guardar una ruta con al menos punto A y punto B.
 */
public class CrearRutaActivity extends AppCompatActivity {

    private List<GeoPunto> opcionesPuntos = new ArrayList<>();
    private List<String> etiquetasPuntos = new ArrayList<>();
    private Spinner spinnerPuntoA;
    private Spinner spinnerPuntoB;
    private LinearLayout containerParadas;
    private final List<Spinner> spinnersParadas = new ArrayList<>();
    private EditText etNombreRuta;
    private Spinner spinnerTipoTransporte;
    private RutasBD rutasBD;
    private static final String[] TIPOS_TRANSPORTE_LABLES = {"Coche", "A pie", "Bici"};
    private static final String[] TIPOS_TRANSPORTE_VALORES = {"car", "foot", "bike"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ruta);

        rutasBD = new RutasBD(this);
        construirOpciones();

        etNombreRuta = findViewById(R.id.etNombreRuta);
        spinnerTipoTransporte = findViewById(R.id.spinnerTipoTransporte);
        spinnerPuntoA = findViewById(R.id.spinnerPuntoA);
        spinnerPuntoB = findViewById(R.id.spinnerPuntoB);
        containerParadas = findViewById(R.id.containerParadas);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, etiquetasPuntos);
        spinnerPuntoA.setAdapter(adapter);
        spinnerPuntoB.setAdapter(adapter);
        spinnerPuntoA.setSelection(0);
        if (etiquetasPuntos.size() > 1) spinnerPuntoB.setSelection(1);

        ArrayAdapter<String> adapterTransporte = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TIPOS_TRANSPORTE_LABLES);
        spinnerTipoTransporte.setAdapter(adapterTransporte);
        spinnerTipoTransporte.setSelection(0);

        findViewById(R.id.btnAnadirParada).setOnClickListener(v -> anadirParada());
        findViewById(R.id.btnGuardarRuta).setOnClickListener(v -> guardarRuta());
    }

    private void construirOpciones() {
        Aplicacion app = (Aplicacion) getApplication();
        opcionesPuntos.clear();
        etiquetasPuntos.clear();

        etiquetasPuntos.add("Mi ubicación");
        opcionesPuntos.add(app.posicionActual != null ? app.posicionActual : GeoPunto.SIN_POSICION);

        int n = app.lugares.tamaño();
        for (int i = 0; i < n; i++) {
            Lugar l = app.lugares.elementoPos(i);
            etiquetasPuntos.add(l.getNombre());
            opcionesPuntos.add(l.getPosicion() != null ? l.getPosicion() : GeoPunto.SIN_POSICION);
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

    private void guardarRuta() {
        String nombre = etNombreRuta.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre para la ruta", Toast.LENGTH_SHORT).show();
            return;
        }

        int idxA = spinnerPuntoA.getSelectedItemPosition();
        int idxB = spinnerPuntoB.getSelectedItemPosition();
        List<WaypointRuta> orden = new ArrayList<>();
        orden.add(toWaypoint(0, idxA));
        for (Spinner s : spinnersParadas) {
            int idx = s.getSelectedItemPosition();
            orden.add(toWaypoint(orden.size(), idx));
        }
        orden.add(toWaypoint(orden.size(), idxB));

        if (orden.size() < 2) {
            Toast.makeText(this, "Se necesitan al menos punto A y punto B", Toast.LENGTH_SHORT).show();
            return;
        }

        for (WaypointRuta w : orden) {
            GeoPunto p = new GeoPunto(w.getLongitud(), w.getLatitud());
            if (p.equals(GeoPunto.SIN_POSICION)) {
                Toast.makeText(this, "Todos los puntos deben tener coordenadas válidas", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String tipoTransporte = TIPOS_TRANSPORTE_VALORES[spinnerTipoTransporte.getSelectedItemPosition()];
        long id = rutasBD.insertarRuta(nombre, orden, tipoTransporte);
        if (id >= 0) {
            Toast.makeText(this, "Ruta guardada", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar la ruta", Toast.LENGTH_SHORT).show();
        }
    }

    private WaypointRuta toWaypoint(int orden, int index) {
        if (index < 0 || index >= opcionesPuntos.size()) return new WaypointRuta(orden, 0, 0, "");
        GeoPunto g = opcionesPuntos.get(index);
        String nom = index < etiquetasPuntos.size() ? etiquetasPuntos.get(index) : "";
        return new WaypointRuta(orden, g.getLatitud(), g.getLongitud(), nom);
    }
}
