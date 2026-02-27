package com.example.misLugares.presentacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.R;
import com.example.misLugares.modelo.Lugar;

import java.util.ArrayList;
import java.util.List;

/** Lista de paradas (lugar ids) para editar una ruta: mostrar nombre y quitar. */
public class AdaptadorParadasEditable extends RecyclerView.Adapter<AdaptadorParadasEditable.ViewHolder> {

    private final List<Integer> paradaIds = new ArrayList<>();
    private final com.example.misLugares.LugaresBDAdapter lugares;

    public AdaptadorParadasEditable(com.example.misLugares.LugaresBDAdapter lugares) {
        this.lugares = lugares;
    }

    public void setParadaIds(List<Integer> ids) {
        paradaIds.clear();
        if (ids != null) paradaIds.addAll(ids);
        notifyDataSetChanged();
    }

    /** Devuelve la lista actual de ids (orden de paradas). */
    public List<Integer> getParadaIds() {
        return new ArrayList<>(paradaIds);
    }

    public void añadirParada(int lugarId) {
        if (paradaIds.contains(lugarId)) return;
        paradaIds.add(lugarId);
        notifyItemInserted(paradaIds.size() - 1);
    }

    public void quitarEn(int position) {
        if (position < 0 || position >= paradaIds.size()) return;
        paradaIds.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parada_editable, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int id = paradaIds.get(position);
        Lugar l = lugares.elemento(id);
        holder.nombreParada.setText(l != null && l.getNombre() != null ? l.getNombre() : ("ID " + id));
        holder.btnQuitar.setOnClickListener(v -> quitarEn(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return paradaIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreParada;
        ImageButton btnQuitar;

        ViewHolder(View itemView) {
            super(itemView);
            nombreParada = itemView.findViewById(R.id.nombreParada);
            btnQuitar = itemView.findViewById(R.id.btnQuitarParada);
        }
    }
}
