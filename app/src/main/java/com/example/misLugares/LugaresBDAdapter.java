package com.example.misLugares;

import android.content.Context;
import android.database.Cursor;

import com.example.misLugares.datos.LugaresBD;
import com.example.misLugares.datos.RepositorioLugares;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.presentacion.AdaptadorLugaresBD;

public class LugaresBDAdapter implements RepositorioLugares {
    private final LugaresBD bd;
    private AdaptadorLugaresBD adaptador;

    public LugaresBDAdapter(Context contexto) {
        this(contexto, new LugaresBD(contexto));
    }

    public LugaresBDAdapter(Context contexto, LugaresBD bd) {
        this.bd = bd;
        this.adaptador = new AdaptadorLugaresBD(this, bd.extraeCursor());
    }

    public Lugar elementoPos(int pos) {
        return adaptador.lugarPosition(pos);
    }

    public AdaptadorLugaresBD getAdaptador() {
        return adaptador;
    }

    public void setAdaptador(AdaptadorLugaresBD adaptador) {
        this.adaptador = adaptador;
    }

    public Cursor extraeCursor() {
        return bd.extraeCursor();
    }

    @Override
    public int tamaño() {
        return adaptador.getItemCount();
    }

    @Override
    public Lugar elemento(int id) {
        return bd.elemento(id);
    }

    @Override
    public void añade(Lugar lugar) {
        bd.añade(lugar);
    }

    @Override
    public int nuevo() {
        return bd.nuevo();
    }

    @Override
    public void borrar(int id) {
        bd.borrar(id);
    }

    @Override
    public void actualiza(int id, Lugar lugar) {
        bd.actualiza(id, lugar);
        adaptador.setCursor(bd.extraeCursor());
        adaptador.notifyDataSetChanged();
    }

    public int actualizaPosLugar(int pos, Lugar lugar) {
        int id = adaptador.idPosition(pos);
        actualiza(id, lugar);
        return adaptador.posicionId(id);
    }
}
