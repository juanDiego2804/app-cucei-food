package com.example.cuceifood;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class LocalesAdapter extends RecyclerView.Adapter<LocalesAdapter.LocalViewHolder> {

    private List<Map<String, Object>> locales;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> local);
    }

    public LocalesAdapter(List<Map<String, Object>> locales, OnItemClickListener listener) {
        this.locales = locales;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local, parent, false);
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalViewHolder holder, int position) {
        Map<String, Object> local = locales.get(position);
        holder.bind(local, listener);
    }

    @Override
    public int getItemCount() {
        return locales.size();
    }

    public void updateData(List<Map<String, Object>> newLocales) {
        locales = newLocales;
        notifyDataSetChanged();
    }

    static class LocalViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textTipoComida, textRangoPrecios;

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombreLocal);
            textTipoComida = itemView.findViewById(R.id.textTipoComida);
            textRangoPrecios = itemView.findViewById(R.id.textRangoPrecios);
        }

        public void bind(Map<String, Object> local, OnItemClickListener listener) {
            textNombre.setText(local.get("nombre").toString());
            textTipoComida.setText("Tipo: " + local.get("tipo_comida").toString());

            int rango = Integer.parseInt(local.get("rango_precios").toString());
            String rangoTexto = rango == 1 ? "Económico" : rango == 2 ? "Regular" : "Caro";
            textRangoPrecios.setText("Precio: " + rangoTexto);

            itemView.setOnClickListener(v -> listener.onItemClick(local));
        }
    }
}