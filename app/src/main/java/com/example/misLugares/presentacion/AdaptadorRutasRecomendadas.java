package com.example.misLugares.presentacion;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.R;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.Ruta;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorRutasRecomendadas extends RecyclerView.Adapter<AdaptadorRutasRecomendadas.ViewHolder> {

    private static final int INFINITE_MULTIPLIER = 1000;

    private final List<Ruta> rutas = new ArrayList<>();
    private OnRutaClickListener listener;
    private com.example.misLugares.LugaresBDAdapter lugares;
    private int cardWidthPx = 0;

    public interface OnRutaClickListener {
        void onRutaClick(Ruta ruta);
    }

    public void setCardWidthPx(int widthPx) {
        if (widthPx <= 0 || widthPx == cardWidthPx) return;
        cardWidthPx = widthPx;
        notifyDataSetChanged();
    }

    public void setLugares(com.example.misLugares.LugaresBDAdapter lugares) {
        this.lugares = lugares;
    }

    public void setOnRutaClickListener(OnRutaClickListener listener) {
        this.listener = listener;
    }

    public void setRutas(List<Ruta> nuevas) {
        rutas.clear();
        if (nuevas != null) rutas.addAll(nuevas);
        notifyDataSetChanged();
    }

    /** Tamaño real de rutas (para carrusel infinito). */
    public int getRealCount() {
        return rutas.isEmpty() ? 0 : rutas.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta_recomendada, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int realCount = getRealCount();
        if (realCount == 0) return;
        int index = position % realCount;
        Ruta r = rutas.get(index);

        if (cardWidthPx > 0 && holder.itemView.getLayoutParams() != null) {
            holder.itemView.getLayoutParams().width = cardWidthPx;
        }

        holder.nombre.setText(r.getNombre());
        holder.distancia.setText(String.format(java.util.Locale.US, "%.2f km", r.getDistanciaKm()));
        holder.ratingBar.setRating(r.getValoracion());

        if (lugares != null && r.getLugarIds() != null && !r.getLugarIds().isEmpty()) {
            try {
                int firstId = r.getLugarIds().get(0);
                Lugar first = lugares.elemento(firstId);
                if (first != null && first.getFoto() != null && !first.getFoto().isEmpty()) {
                    holder.imagen.setImageURI(Uri.parse(first.getFoto()));
                    holder.imagen.setBackgroundResource(0);
                    holder.imagen.setVisibility(View.VISIBLE);
                } else {
                    holder.imagen.setImageDrawable(null);
                    holder.imagen.setBackgroundColor(0xFFD8EEF0);
                    holder.imagen.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                holder.imagen.setImageDrawable(null);
                holder.imagen.setBackgroundColor(0xFFD8EEF0);
            }
        } else {
            holder.imagen.setImageDrawable(null);
            holder.imagen.setBackgroundColor(0xFFD8EEF0);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRutaClick(r);
        });
        holder.imagen.setOnClickListener(v -> {
            if (listener != null) listener.onRutaClick(r);
        });
        View contentInfo = holder.itemView.findViewById(R.id.contentInfo);
        if (contentInfo != null) contentInfo.setOnClickListener(v -> {
            if (listener != null) listener.onRutaClick(r);
        });
    }

    @Override
    public int getItemCount() {
        int n = getRealCount();
        if (n == 0) return 0;
        return n * INFINITE_MULTIPLIER;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, distancia;
        ImageView imagen;
        RatingBar ratingBar;

        ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombreRuta);
            distancia = itemView.findViewById(R.id.distanciaRuta);
            imagen = itemView.findViewById(R.id.imagenRuta);
            ratingBar = itemView.findViewById(R.id.ratingBarRutaCard);
        }
    }
}
