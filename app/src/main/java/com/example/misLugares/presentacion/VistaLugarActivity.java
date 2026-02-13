package com.example.misLugares.presentacion;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

public class VistaLugarActivity extends AppCompatActivity {
    private LugaresBDAdapter lugares;
    private CasosUsoLugar usoLugar;
    private int pos;
    private Lugar lugar;
    final static int RESULTADO_GALERIA = 2;
    final static int RESULTADO_FOTO = 3;
    private ImageView foto;
    private Uri uriUltimaFoto;
    final static int RESULTADO_EDITAR = 1;
    private int id = -1;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_lugar);
        Bundle extras = getIntent().getExtras();
        pos = extras.getInt("pos", 0);
        lugares = ((Aplicacion) getApplication()).lugares;
        id = lugares.getAdaptador().idPosition(pos);
        usoLugar = new CasosUsoLugar(this, lugares);
        lugar = lugares.elementoPos(pos);
        foto = findViewById(R.id.foto);
        actualizaVistas();
    }

    public void actualizaVistas() {
        TextView nombre = findViewById(R.id.nombre);
        nombre.setText(lugar.getNombre());
        ImageView logo_tipo = findViewById(R.id.logo_tipo);
        logo_tipo.setImageResource(lugar.getTipo().getRecurso());
        TextView tipo = findViewById(R.id.tipo);
        tipo.setText(lugar.getTipo().getTexto());
        if (lugar.getDireccion().isEmpty()) {
            findViewById(R.id.direccion).setVisibility(View.GONE);
            findViewById(R.id.logo_direccion).setVisibility(View.GONE);
        } else {
            findViewById(R.id.direccion).setVisibility(View.VISIBLE);
            TextView direccion = findViewById(R.id.direccion);
            direccion.setText(lugar.getDireccion());
        }
        if (lugar.getTelefono() == 0) {
            findViewById(R.id.telefono).setVisibility(View.GONE);
            findViewById(R.id.logo_telefono).setVisibility(View.GONE);
        } else {
            findViewById(R.id.telefono).setVisibility(View.VISIBLE);
            TextView telefono = findViewById(R.id.telefono);
            telefono.setText(Integer.toString(lugar.getTelefono()));
        }
        if (lugar.getUrl().isEmpty()) {
            findViewById(R.id.url).setVisibility(View.GONE);
            findViewById(R.id.logo_url).setVisibility(View.GONE);
        } else {
            findViewById(R.id.url).setVisibility(View.VISIBLE);
            TextView url = findViewById(R.id.url);
            url.setText(lugar.getUrl());
        }
        if (lugar.getComentario().isEmpty()) {
            findViewById(R.id.comentario).setVisibility(View.GONE);
            findViewById(R.id.logo_comentario).setVisibility(View.GONE);
        } else {
            findViewById(R.id.comentario).setVisibility(View.VISIBLE);
            TextView comentario = findViewById(R.id.comentario);
            comentario.setText(lugar.getComentario());
        }

        TextView fecha = findViewById(R.id.fecha);
        fecha.setText(DateFormat.getDateInstance().format(new Date(lugar.getFecha())));
        TextView hora = findViewById(R.id.hora);
        hora.setText(DateFormat.getTimeInstance().format(new Date(lugar.getFecha())));
        RatingBar valoracion = findViewById(R.id.valoracion);
        valoracion.setOnRatingBarChangeListener(null);
        valoracion.setRating(lugar.getValoracion());
        valoracion.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float valor, boolean fromUser) {
                lugar.setValoracion(valor);
                pos = lugares.actualizaPosLugar(pos, lugar);
            }
        });
        usoLugar.visualizarFoto(lugar, foto);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vista_lugar, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.accion_compartir) {
            usoLugar.compartit(lugar);
            return true;
        } else if (id == R.id.accion_llegar) {
            usoLugar.verMapa(lugar);
            return true;
        } else if (id == R.id.accion_trazar_ruta) {
            // Trazar ruta con línea de color
            trazarRuta();
            return true;
        } else if (id == R.id.accion_editar) {
            usoLugar.editar(pos, RESULTADO_EDITAR);
            return true;
        } else if (id == R.id.accion_borrar) {
            new AlertDialog.Builder(this)
                    .setTitle("Borrado de lugar")
                    .setMessage("¿Estás seguro que quieres eliminar este lugar?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            usoLugar.borrarPos(pos);
                        }
                    })
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Trazar la ruta desde la ubicación actual hasta este lugar.
     */
    private void trazarRuta() {
        GeoPunto posicionActual = ((Aplicacion) getApplication()).posicionActual;
        if (posicionActual == null || posicionActual.equals(GeoPunto.SIN_POSICION)) {
            Toast.makeText(this, "No se pudo obtener tu ubicación actual. " +
                    "Asegúrate de tener activado el GPS.", Toast.LENGTH_LONG).show();
            return;
        }
        if (lugar.getPosicion() == null || lugar.getPosicion().equals(GeoPunto.SIN_POSICION)) {
            Toast.makeText(this, "Este lugar no tiene coordenadas válidas",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, RutaActivity.class);
        intent.putExtra("pos", pos);
        startActivity(intent);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULTADO_EDITAR) {
            lugar = lugares.elemento(id);
            pos = lugares.getAdaptador().posicionId(id);
            actualizaVistas();
        } else if (requestCode == RESULTADO_GALERIA) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // Para API >= 19 guardamos permiso persistente del URI
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        try {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }

                    // Guardamos la foto en el lugar y la mostramos
                    usoLugar.ponerFoto(pos, uri.toString(), foto);
                }
            } else {
                Toast.makeText(this, "Foto no cargada", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RESULTADO_FOTO) {
            if (resultCode == Activity.RESULT_OK && uriUltimaFoto!=null) {
                lugar.setFoto(uriUltimaFoto.toString());
                usoLugar.ponerFoto(pos, lugar.getFoto(), foto);
            } else {
                Toast.makeText(this, "Error en captura", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void verMapa(View view) {
        usoLugar.verMapa(lugar);
    }

    private static final int SOLICITUD_PERMISO_CALL_PHONE = 0;
    public void llamarTelefono(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            usoLugar.llamarTelefono(lugar);
        } else {
            solicitarPermiso(Manifest.permission.CALL_PHONE, "Sin el permiso llamar telefono " +
                    "no puedo realizar llamadas", SOLICITUD_PERMISO_CALL_PHONE, this);
        }
    }

    public void verPgWeb(View view) {
        usoLugar.verPgWeb(lugar);
    }

    public void ponerDeGaleria(View view) {
        usoLugar.ponerDeGaleria(RESULTADO_GALERIA);
    }

    public void tomarFoto(View view) {
        uriUltimaFoto = usoLugar.tomarFoto(RESULTADO_FOTO);
    }

    public void eliminarFoto(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Borrado de foto")
                .setMessage("¿Estás seguro que quieres eliminar la foto?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        usoLugar.ponerFoto(pos, "", foto);
                    }
                })
                .show();
    }

    public static void solicitarPermiso(final String permiso, String justificacion, final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad, permiso)) {
            new AlertDialog.Builder(actividad).setTitle("Solicitud de permiso").setMessage(justificacion)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_CALL_PHONE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                usoLugar.llamarTelefono(lugar);
            } else {
                Toast.makeText(this, "Sin el permiso, no puedo realizar la acción", Toast.LENGTH_SHORT).show();
            }
        }
    }
}