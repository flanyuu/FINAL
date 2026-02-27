package com.example.misLugares.presentacion;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.AdaptadorLugares;
import com.example.misLugares.R;
import com.example.misLugares.datos.LugaresBD;
import com.example.misLugares.datos.RepositorioLugares;
import com.example.misLugares.modelo.Lugar;

public class AdaptadorLugaresBD extends AdaptadorLugares {
    protected Cursor cursor;
    public AdaptadorLugaresBD(RepositorioLugares lugares, Cursor cursor) {
        super(lugares);
        this.cursor = cursor;
    }
    public Cursor getCursor() {
        return cursor;
    }
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
    public Lugar lugarPosition(int posicion) {
        cursor.moveToPosition(posicion);
        return LugaresBD.extraeLugar(cursor);
    }
    public int idPosition(int posicion) {
        cursor.moveToPosition(posicion);
        if (cursor.getCount() > 0)
            return cursor.getInt(0);
        else
            return -1;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_lista, parent, false);
        v.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Lugar lugar = lugarPosition(posicion);
        holder.personaliza(lugar);
        holder.itemView.setTag(new Integer(posicion));
    }
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
    public void setOnItemClickListener(View.OnClickListener onClickListener) {
    }
    public int posicionId(int id) {
        int pos = 0;
        while (pos<getItemCount() && idPosition(pos)!=id) pos++;
        if (pos >= getItemCount()) return -1;
        else return pos;
    }
}

