package com.example.misLugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.LugaresBDAdapter;
import com.example.misLugares.R;
import com.example.misLugares.casos_uso.CasosUsoLugar;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.TipoLugar;

public class EdicionLugarActivity extends AppCompatActivity {

    private LugaresBDAdapter lugares;
    private CasosUsoLugar usoLugar;
    private int pos;
    private Lugar lugar;
    private EditText nombre, direccion, telefono, url, comentario;
    private Spinner tipo;
    private int _id;
    private static final int REQUEST_SELECCIONAR_EN_MAPA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_lugar);

        Bundle extras = getIntent().getExtras();
        pos = extras.getInt("pos", 0);
        lugares  = ((Aplicacion) getApplication()).lugares;
        usoLugar = new CasosUsoLugar(this, lugares);

        _id = extras.getInt("_id", -1);
        if (_id != -1) {
            lugar = lugares.elemento(_id);
        } else {
            lugar = new Lugar();
            GeoPunto posActual = ((Aplicacion) getApplication()).posicionActual;
            if (posActual != null && !posActual.equals(GeoPunto.SIN_POSICION))
                lugar.setPosicion(new GeoPunto(posActual.getLongitud(), posActual.getLatitud()));
        }

        actualizaVistas();

        LinearLayout btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(v -> {
            if (_id != -1) usoLugar.borrar(_id);
            finish();
        });

        // ── Botón GUARDAR ────────────────────────────────────────────────────
        LinearLayout btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> {
            lugar.setNombre(nombre.getText().toString());
            lugar.setTipo(TipoLugar.values()[tipo.getSelectedItemPosition()]);
            lugar.setDireccion(direccion.getText().toString());
            String telStr = telefono.getText().toString().trim();
            lugar.setTelefono(telStr.isEmpty() ? 0 : Integer.parseInt(telStr));
            lugar.setUrl(url.getText().toString());
            lugar.setComentario(comentario.getText().toString());

            if (_id == -1) {
                _id = lugares.nuevo();
            }
            usoLugar.guardar(_id, lugar);
            finish();
        });

        ImageButton btnSeleccionarEnMapa = findViewById(R.id.btnSeleccionarEnMapa);
        btnSeleccionarEnMapa.setOnClickListener(v -> {
            Intent i = new Intent(this, SeleccionarEnMapaActivity.class);
            startActivityForResult(i, REQUEST_SELECCIONAR_EN_MAPA);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECCIONAR_EN_MAPA && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra(SeleccionarEnMapaActivity.EXTRA_LAT, 0);
            double lon = data.getDoubleExtra(SeleccionarEnMapaActivity.EXTRA_LON, 0);
            String addr = data.getStringExtra(SeleccionarEnMapaActivity.EXTRA_DIRECCION);
            if (addr != null && !addr.isEmpty()) direccion.setText(addr);
            lugar.setPosicion(new GeoPunto(lon, lat));
        }
    }

    private void actualizaVistas() {
        nombre    = findViewById(R.id.nombre);
        tipo      = findViewById(R.id.tipo);
        direccion = findViewById(R.id.direccion);
        telefono  = findViewById(R.id.telefono);
        url       = findViewById(R.id.url);
        comentario = findViewById(R.id.comentario);

        nombre.setText(lugar.getNombre());

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, TipoLugar.getNombres());
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipo.setAdapter(adaptador);
        tipo.setSelection(lugar.getTipo().ordinal());

        direccion.setText(lugar.getDireccion());
        telefono.setText(lugar.getTelefono() == 0 ? "" : Integer.toString(lugar.getTelefono()));
        url.setText(lugar.getUrl());
        comentario.setText(lugar.getComentario());
    }
}