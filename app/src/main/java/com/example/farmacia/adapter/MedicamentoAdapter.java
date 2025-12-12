package com.example.farmacia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.R;
import com.example.farmacia.model.Medicamento;

import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> listaMedicamentos;

    public MedicamentoAdapter(List<Medicamento> listaMedicamentos) {
        this.listaMedicamentos = listaMedicamentos;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = listaMedicamentos.get(position);
        holder.tvNombre.setText(medicamento.getNombre());
        holder.tvProspecto.setText(medicamento.getProspecto());
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos.size();
    }

    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvProspecto;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMedName);
            tvProspecto = itemView.findViewById(R.id.tvMedProspect);
        }
    }
}
