package com.example.misLugares.casos_uso;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.LugaresBDAdapter;
import com.example.misLugares.datos.RepositorioLugares;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.presentacion.EdicionLugarActivity;
import com.example.misLugares.presentacion.VistaLugarActivity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class CasosUsoLugar {
    private Activity actividad;
    //private RepositorioLugares lugares;
    private LugaresBDAdapter lugares;
    public CasosUsoLugar(Activity actividad, RepositorioLugares lugares) {
        this.actividad = actividad;
        this.lugares = (LugaresBDAdapter) lugares;
    }
    public void mostrar(int pos) {
        Intent i = new Intent(actividad, VistaLugarActivity.class);
        i.putExtra("pos",pos);
        actividad.startActivity(i);
    }
    public void borrar(int id) {
        lugares.borrar(id);
        lugares.getAdaptador().setCursor(lugares.extraeCursorCompleto());
        lugares.getAdaptador().notifyDataSetChanged();
        actividad.finish();
    }
    public void editar(int pos, int codidoSolicitud) {
        Intent i = new Intent(actividad, EdicionLugarActivity.class);
        i.putExtra("pos", pos);
        actividad.startActivityForResult(i, codidoSolicitud);
    }
    public void guardar(int id, Lugar nuevoLugar) {
        lugares.actualiza(id, nuevoLugar);
    }
    public void compartit(Lugar lugar) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, lugar.getNombre() + " - " + lugar.getUrl());
        actividad.startActivity(i);
    }
    public void llamarTelefono(Lugar lugar) {
        //actividad.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + lugar.getTelefono())));
        actividad.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + lugar.getTelefono())));
    }
    public void verPgWeb(Lugar lugar){
        actividad.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(lugar.getUrl())));
    }
    public final void verMapa(Lugar lugar) {
        double lat = lugar.getPosicion().getLatitud();
        double lon = lugar.getPosicion().getLongitud();
        Uri uri = lugar.getPosicion() != GeoPunto.SIN_POSICION
                ? Uri.parse("geo:" + lat + "," + lon)
                : Uri.parse("geo:0,0?q=" + lugar.getDireccion());
        actividad.startActivity(new Intent("android.intent.action.VIEW", uri));
    }
    public void ponerDeGaleria(int codigoSolicitud) {
        /*String action;
        if (Build.VERSION.SDK_INT >= 19) {
            action = Intent.ACTION_OPEN_DOCUMENT;
        } else {
            action = Intent.ACTION_PICK;
        }
        Intent intent = new Intent(action, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        actividad.startActivityForResult(intent, codigoSolicitud);*/
        Intent intent;

        if (Build.VERSION.SDK_INT >= 19) {
            // Usar Storage Access Framework
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            // Compatibilidad con versiones antiguas
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        intent.setType("image/*");
        actividad.startActivityForResult(intent, codigoSolicitud);
    }
    public void ponerFoto(int pos, String uri, ImageView imageView) {
        Lugar lugar = lugares.elementoPos(pos);
        lugar.setFoto(uri);
        visualizarFoto(lugar, imageView);
        lugares.actualizaPosLugar(pos, lugar);
    }
    public void visualizarFoto(Lugar lugar, ImageView imageView) {
        if (lugar.getFoto() != null && !lugar.getFoto().isEmpty()) {
            imageView.setImageURI(Uri.parse(lugar.getFoto()));
        } else {
            imageView.setImageBitmap(null);
        }
    }
    public Uri tomarFoto(int codidoSolicitud) {
        try {
            Uri uriU1timaFoto;
            File file = File.createTempFile(
                    "img_" + (System.currentTimeMillis()/1000), ".jpg" ,
                    actividad.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
            if (Build.VERSION.SDK_INT >= 24) {
                uriU1timaFoto = FileProvider.getUriForFile(
                        actividad, "com.example.misLugares.fileProvider", file
                );
            } else {
                uriU1timaFoto = Uri.fromFile(file);
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriU1timaFoto);
            actividad.startActivityForResult(intent, codidoSolicitud);
            return uriU1timaFoto;
        } catch (IOException ex) {
            Toast.makeText(actividad, "Error al crear fichero de imagen",
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }
    public void nuevo() {
        Intent i = new Intent(actividad, EdicionLugarActivity.class);
        i.putExtra("_id", -1);
        actividad.startActivity(i);
    }
    public void borrarPos(int pos) {
        int id = lugares.getAdaptador().idPosition(pos);
        borrar(id);
    }

}

