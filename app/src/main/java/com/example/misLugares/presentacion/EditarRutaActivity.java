package com.example.misLugares.presentacion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.R;
import com.example.misLugares.modelo.Lugar;

import java.util.ArrayList;
import java.util.List;

/**
 * Edita nombre y paradas de una ruta de sendero (solo rutas con id de BD).
 * Guardar actualiza la tabla rutas y ruta_paradas.
 */
public class EditarRutaActivity extends AppCompatActivity {

    public static final String KEY_RUTA_ID = "rutaId";
    public static final String KEY_NOMBRE_RUTA = "nombreRuta";
    public static final String KEY_LUGAR_IDS = "lugarIds";

    private long rutaId;
    private com.google.android.material.textfield.TextInputEditText editNombre;
    private AdaptadorParadasEditable adaptadorParadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_ruta);

        rutaId = getIntent().getLongExtra(KEY_RUTA_ID, -1);
        String nombre = getIntent().getStringExtra(KEY_NOMBRE_RUTA);
        ArrayList<Integer> lugarIds = getIntent().getIntegerArrayListExtra(KEY_LUGAR_IDS);
        if (lugarIds == null) lugarIds = new ArrayList<>();

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar ruta");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        editNombre = findViewById(R.id.editNombreRuta);
        if (nombre != null) editNombre.setText(nombre);

        com.example.misLugares.LugaresBDAdapter lugares = ((Aplicacion) getApplication()).lugares;
        adaptadorParadas = new AdaptadorParadasEditable(lugares);
        adaptadorParadas.setParadaIds(lugarIds);

        RecyclerView recyclerParadas = findViewById(R.id.recyclerParadas);
        recyclerParadas.setLayoutManager(new LinearLayoutManager(this));
        recyclerParadas.setAdapter(adaptadorParadas);

        findViewById(R.id.btnAnadirParada).setOnClickListener(v -> mostrarDialogoAñadirParada(lugares));
        findViewById(R.id.btnGuardarRuta).setOnClickListener(v -> guardar());
    }

    private void mostrarDialogoAñadirParada(com.example.misLugares.LugaresBDAdapter lugares) {
        List<Integer> idsSendero = new ArrayList<>();
        List<Lugar> lugaresSendero = new ArrayList<>();
        lugares.listarSenderoConPosicionYIds(idsSendero, lugaresSendero);
        if (idsSendero.isEmpty()) {
            Toast.makeText(this, "No hay lugares de sendero disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Integer> yaEnRuta = adaptadorParadas.getParadaIds();
        List<String> nombresList = new ArrayList<>();
        List<Integer> idsList = new ArrayList<>();
        for (int i = 0; i < idsSendero.size(); i++) {
            if (yaEnRuta.contains(idsSendero.get(i))) continue;
            Lugar l = lugaresSendero.get(i);
            nombresList.add(l != null && l.getNombre() != null ? l.getNombre() : ("Lugar " + idsSendero.get(i)));
            idsList.add(idsSendero.get(i));
        }
        if (nombresList.isEmpty()) {
            Toast.makeText(this, "Todos los lugares de sendero ya están en la ruta", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] nombresArr = nombresList.toArray(new String[0]);
        final int[] idsArr = new int[idsList.size()];
        for (int i = 0; i < idsList.size(); i++) idsArr[i] = idsList.get(i);
        new AlertDialog.Builder(this)
                .setTitle("Añadir parada")
                .setItems(nombresArr, (dialog, which) -> {
                    if (which >= 0 && which < idsArr.length) adaptadorParadas.añadirParada(idsArr[which]);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void guardar() {
        String nombre = editNombre.getText() != null ? editNombre.getText().toString().trim() : "";
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre para la ruta", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Integer> paradaIds = adaptadorParadas.getParadaIds();
        if (paradaIds.isEmpty()) {
            Toast.makeText(this, "Añade al menos una parada", Toast.LENGTH_SHORT).show();
            return;
        }
        ((Aplicacion) getApplication()).lugares.actualizarRutaSendero(rutaId, nombre, paradaIds);
        Toast.makeText(this, "Ruta guardada", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
