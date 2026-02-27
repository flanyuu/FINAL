package com.example.misLugares;

import android.content.Context;

import com.example.misLugares.datos.LugaresBD;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.presentacion.AdaptadorLugaresBD;

public class LugaresBDAdapter extends LugaresBD {
    private AdaptadorLugaresBD adaptador;
    public LugaresBDAdapter(Context contexto) {
        super(contexto);
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
    @Override public int tamaño() {
        return adaptador.getItemCount();
    }
    @Override public void actualiza(int id, Lugar lugar) {
        super.actualiza(id, lugar);
        adaptador.setCursor(extraeCursorCompleto());
        adaptador.notifyDataSetChanged();
    }
    public int actualizaPosLugar(int pos, Lugar lugar) {
        int id = adaptador.idPosition(pos);
        actualiza(id, lugar);
        return adaptador.posicionId(id);
    }
}
