package com.example.misLugares.presentacion;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.R;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para la lista de lugares de una ruta.
 * Muestra nombre, dirección, valoración y distancia (mismo estilo que "mis lugares guardados").
 * Solo lectura: al hacer clic se abre VistaLugarActivity en modo solo lectura.
 */
public class AdaptadorLugaresDeRuta extends RecyclerView.Adapter<AdaptadorLugaresDeRuta.ViewHolder> {

    private final List<Integer> lugarIds = new ArrayList<>();
    private final com.example.misLugares.LugaresBDAdapter lugares;

    public AdaptadorLugaresDeRuta(com.example.misLugares.LugaresBDAdapter lugares) {
        this.lugares = lugares;
    }

    public void setLugarIds(List<Integer> ids) {
        lugarIds.clear();
        if (ids != null) lugarIds.addAll(ids);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_lista, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int id = lugarIds.get(position);
        Lugar lugar = lugares.elemento(id);
        holder.personaliza(lugar);
        holder.itemView.setOnClickListener(v -> {
            int pos = lugares.getAdaptador().posicionId(id);
            if (pos >= 0) {
                Intent i = new Intent(v.getContext(), VistaLugarActivity.class);
                i.putExtra("pos", pos);
                i.putExtra("solo_lectura", true);
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lugarIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, direccion, distancia;
        ImageView foto;
        RatingBar valoracion;

        ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            direccion = itemView.findViewById(R.id.direccion);
            foto = itemView.findViewById(R.id.foto);
            valoracion = itemView.findViewById(R.id.valoracion);
            distancia = itemView.findViewById(R.id.distancia);
        }

        void personaliza(Lugar lugar) {
            nombre.setText(lugar.getNombre());
            direccion.setText(lugar.getDireccion() != null ? lugar.getDireccion() : "");
            valoracion.setRating(lugar.getValoracion());
            valoracion.setIsIndicator(true);
            int idDrawable = R.drawable.otros;
            switch (lugar.getTipo()) {
                case RESTAURANTE: idDrawable = R.drawable.restaurante; break;
                case BAR: idDrawable = R.drawable.bar; break;
                case COPAS: idDrawable = R.drawable.copas; break;
                case ESPECTACULO: idDrawable = R.drawable.espectaculos; break;
                case HOTEL: idDrawable = R.drawable.hotel; break;
                case COMPRAS: idDrawable = R.drawable.compras; break;
                case EDUCACION: idDrawable = R.drawable.educacion; break;
                case DEPORTE: idDrawable = R.drawable.deporte; break;
                case NATURALEZA: idDrawable = R.drawable.naturaleza; break;
                case GASOLINERA: idDrawable = R.drawable.gasolinera; break;
            }
            foto.setImageResource(idDrawable);
            GeoPunto pos = ((Aplicacion) itemView.getContext().getApplicationContext()).posicionActual;
            if (pos == null || pos.equals(GeoPunto.SIN_POSICION) || lugar.getPosicion() == null || lugar.getPosicion().equals(GeoPunto.SIN_POSICION)) {
                distancia.setText("... Km");
            } else {
                int d = (int) pos.distancia(lugar.getPosicion());
                if (d < 2000) distancia.setText(d + " m");
                else distancia.setText(d / 1000 + " Km");
            }
        }
    }
}
