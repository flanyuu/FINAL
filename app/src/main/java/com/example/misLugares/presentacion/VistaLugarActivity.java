package com.example.misLugares.presentacion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.LugaresBDAdapter;
import com.example.misLugares.R;
import com.example.misLugares.casos_uso.CasosUsoLugar;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;

import java.text.DateFormat;
import java.util.Date;

import android.Manifest;

public class VistaLugarActivity extends AppCompatActivity {

    private LugaresBDAdapter lugares;
    private CasosUsoLugar usoLugar;
    private int pos;
    private Lugar lugar;
    private ImageView foto;
    private Uri uriUltimaFoto;
    private int id = -1;
    private boolean soloLectura;

    final static int RESULTADO_GALERIA = 2;
    final static int RESULTADO_FOTO    = 3;
    final static int RESULTADO_EDITAR  = 1;
    private static final int SOLICITUD_PERMISO_CALL_PHONE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_lugar);

        Bundle extras = getIntent().getExtras();
        pos     = extras.getInt("pos", 0);
        lugares = ((Aplicacion) getApplication()).lugares;
        id      = lugares.getAdaptador().idPosition(pos);
        usoLugar = new CasosUsoLugar(this, lugares);
        lugar   = lugares.elementoPos(pos);
        foto    = findViewById(R.id.foto);

        actualizaVistas();

        // ── Botones de acción en el layout ────────────────────────────────────

        // Llegar (Google Maps externo)
        LinearLayout btnLlegar = findViewById(R.id.btnLlegar);
        btnLlegar.setOnClickListener(v -> usoLugar.verMapa(lugar));

        // Trazar ruta (RutaActivity)
        LinearLayout btnRuta = findViewById(R.id.btnRuta);
        btnRuta.setOnClickListener(v -> trazarRuta());

        // Compartir
        LinearLayout btnCompartir = findViewById(R.id.btnCompartir);
        btnCompartir.setOnClickListener(v -> usoLugar.compartit(lugar));

        // Editar
        LinearLayout btnEditar = findViewById(R.id.btnEditar);
        // Borrar
        LinearLayout btnBorrar = findViewById(R.id.btnBorrar);
        boolean soloLectura = getIntent().getBooleanExtra("solo_lectura", false);
        this.soloLectura = soloLectura;
        if (soloLectura) {
            btnEditar.setVisibility(View.GONE);
            btnBorrar.setVisibility(View.GONE);
        } else {
            btnEditar.setVisibility(View.VISIBLE);
            btnBorrar.setVisibility(View.VISIBLE);
        }
        btnEditar.setOnClickListener(v -> usoLugar.editar(pos, RESULTADO_EDITAR));

        btnBorrar.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Borrar lugar")
                        .setMessage("¿Seguro que quieres eliminar este lugar?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar", (d, i2) -> usoLugar.borrarPos(pos))
                        .show()
        );
    }

    public void actualizaVistas() {
        TextView nombre = findViewById(R.id.nombre);
        nombre.setText(lugar.getNombre());

        ImageView logo_tipo = findViewById(R.id.logo_tipo);
        logo_tipo.setImageResource(lugar.getTipo().getRecurso());

        TextView tipo = findViewById(R.id.tipo);
        tipo.setText(lugar.getTipo().getTexto());

        // Dirección
        View rowDireccion = findViewById(R.id.rowDireccion);
        if (lugar.getDireccion() == null || lugar.getDireccion().isEmpty()) {
            rowDireccion.setVisibility(View.GONE);
        } else {
            rowDireccion.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.direccion)).setText(lugar.getDireccion());
        }

        // Teléfono
        View rowTelefono = findViewById(R.id.rowTelefono);
        if (lugar.getTelefono() == 0) {
            rowTelefono.setVisibility(View.GONE);
        } else {
            rowTelefono.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.telefono)).setText(
                    Integer.toString(lugar.getTelefono()));
        }

        // URL
        View rowUrl = findViewById(R.id.rowUrl);
        if (lugar.getUrl() == null || lugar.getUrl().isEmpty()) {
            rowUrl.setVisibility(View.GONE);
        } else {
            rowUrl.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.url)).setText(lugar.getUrl());
        }

        // Comentario
        View rowComentario = findViewById(R.id.rowComentario);
        if (lugar.getComentario() == null || lugar.getComentario().isEmpty()) {
            rowComentario.setVisibility(View.GONE);
        } else {
            rowComentario.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.comentario)).setText(lugar.getComentario());
        }

        // Fecha y hora
        ((TextView) findViewById(R.id.fecha)).setText(
                DateFormat.getDateInstance().format(new Date(lugar.getFecha())));
        ((TextView) findViewById(R.id.hora)).setText(
                DateFormat.getTimeInstance().format(new Date(lugar.getFecha())));

        // RatingBar
        RatingBar valoracion = findViewById(R.id.valoracion);
        valoracion.setOnRatingBarChangeListener(null);
        valoracion.setRating(lugar.getValoracion());
        if (soloLectura) {
            valoracion.setIsIndicator(true);
        } else {
            valoracion.setIsIndicator(false);
            valoracion.setOnRatingBarChangeListener((rb, valor, fromUser) -> {
                lugar.setValoracion(valor);
                pos = lugares.actualizaPosLugar(pos, lugar);
            });
        }

        // Foto
        usoLugar.visualizarFoto(lugar, foto);
    }

    // ── Acciones de foto ─────────────────────────────────────────────────────

    public void verMapa(View view)        { usoLugar.verMapa(lugar); }
    public void ponerDeGaleria(View view) { usoLugar.ponerDeGaleria(RESULTADO_GALERIA); }
    public void tomarFoto(View view)      { uriUltimaFoto = usoLugar.tomarFoto(RESULTADO_FOTO); }

    public void eliminarFoto(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Borrar foto")
                .setMessage("¿Seguro que quieres eliminar la foto?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d, i2) ->
                        usoLugar.ponerFoto(pos, "", foto))
                .show();
    }

    public void llamarTelefono(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            usoLugar.llamarTelefono(lugar);
        } else {
            solicitarPermiso(Manifest.permission.CALL_PHONE,
                    "Sin el permiso no puedo realizar llamadas.",
                    SOLICITUD_PERMISO_CALL_PHONE, this);
        }
    }

    public void verPgWeb(View view) { usoLugar.verPgWeb(lugar); }

    private void trazarRuta() {
        GeoPunto pos2 = ((Aplicacion) getApplication()).posicionActual;
        if (pos2 == null || pos2.equals(GeoPunto.SIN_POSICION)) {
            Toast.makeText(this, "Activa el GPS para trazar la ruta.", Toast.LENGTH_LONG).show();
            return;
        }
        if (lugar.getPosicion() == null || lugar.getPosicion().equals(GeoPunto.SIN_POSICION)) {
            Toast.makeText(this, "Este lugar no tiene coordenadas.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, RutaActivity.class);
        intent.putExtra("pos", pos);
        startActivity(intent);
    }

    // ── Resultados ────────────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULTADO_EDITAR) {
            lugar = lugares.elemento(id);
            pos   = lugares.getAdaptador().posicionId(id);
            actualizaVistas();
        } else if (requestCode == RESULTADO_GALERIA) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri,
                                    data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                        } catch (SecurityException e) { e.printStackTrace(); }
                    }
                    usoLugar.ponerFoto(pos, uri.toString(), foto);
                }
            }
        } else if (requestCode == RESULTADO_FOTO) {
            if (resultCode == Activity.RESULT_OK && uriUltimaFoto != null) {
                lugar.setFoto(uriUltimaFoto.toString());
                usoLugar.ponerFoto(pos, lugar.getFoto(), foto);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_CALL_PHONE
                && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            usoLugar.llamarTelefono(lugar);
        }
    }

    public static void solicitarPermiso(final String permiso, String justificacion,
                                        final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad, permiso)) {
            new AlertDialog.Builder(actividad)
                    .setTitle("Permiso requerido")
                    .setMessage(justificacion)
                    .setPositiveButton("Ok", (d, w) ->
                            ActivityCompat.requestPermissions(actividad,
                                    new String[]{permiso}, requestCode))
                    .show();
        } else {
            ActivityCompat.requestPermissions(actividad,
                    new String[]{permiso}, requestCode);
        }
    }
}