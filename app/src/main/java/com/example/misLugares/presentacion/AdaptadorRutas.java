package com.example.misLugares.presentacion;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.misLugares.R;
import com.example.misLugares.modelo.Ruta;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para la lista de rutas guardadas.
 */
public class AdaptadorRutas extends RecyclerView.Adapter<AdaptadorRutas.ViewHolder> {

    private List<Ruta> rutas = new ArrayList<>();
    private View.OnClickListener onItemClickListener;
    private View.OnLongClickListener onItemLongClickListener;

    public void setRutas(List<Ruta> rutas) {
        this.rutas = rutas != null ? rutas : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Ruta getRutaAt(int position) {
        if (position < 0 || position >= rutas.size()) return null;
        return rutas.get(position);
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(View.OnLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.elemento_lista_ruta, parent, false);
        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(onItemClickListener);
        v.setOnLongClickListener(onItemLongClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ruta r = rutas.get(position);
        holder.nombre.setText(r.getNombre());
        int n = r.getWaypoints() != null ? r.getWaypoints().size() : 0;
        holder.paradas.setText(holder.itemView.getContext().getString(R.string.paradas_count, n));
        String tipo = r.getTipoTransporte();
        if ("foot".equals(tipo)) holder.tipoTransporte.setText(R.string.modo_a_pie);
        else if ("bike".equals(tipo)) holder.tipoTransporte.setText(R.string.modo_bici);
        else holder.tipoTransporte.setText(R.string.modo_coche);
        holder.valoracion.setRating(r.getValoracion());
        Drawable progressDrawable = holder.valoracion.getProgressDrawable();
        if (progressDrawable instanceof LayerDrawable) {
            Drawable layer0 = ((LayerDrawable) progressDrawable).getDrawable(0);
            if (layer0 != null) {
                layer0.setTint(ContextCompat.getColor(holder.itemView.getContext(), R.color.rating_star_empty));
            }
        }
        if (r.getDistanciaKm() > 0) {
            holder.distancia.setText(holder.itemView.getContext().getString(R.string.distancia_km, r.getDistanciaKm()));
        } else {
            holder.distancia.setText(holder.itemView.getContext().getString(R.string.distancia_desconocida));
        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return rutas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        TextView paradas;
        TextView tipoTransporte;
        RatingBar valoracion;
        TextView distancia;

        ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre_ruta);
            paradas = itemView.findViewById(R.id.paradas_ruta);
            tipoTransporte = itemView.findViewById(R.id.tipo_transporte_ruta);
            valoracion = itemView.findViewById(R.id.valoracion_ruta);
            distancia = itemView.findViewById(R.id.distancia_ruta);
        }
    }
}
