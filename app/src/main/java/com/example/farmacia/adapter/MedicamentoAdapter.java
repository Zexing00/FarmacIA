package com.example.farmacia.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.R;
import com.example.farmacia.model.Medicamento;

import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> listaMedicamentos;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Medicamento medicamento);
    }

    public MedicamentoAdapter(List<Medicamento> listaMedicamentos, OnItemClickListener listener) {
        this.listaMedicamentos = listaMedicamentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = listaMedicamentos.get(position);
        holder.bind(medicamento, listener, context);
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos.size();
    }

    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvProspecto;
        Button btnVerProspectoPDF;
        TextView tvDosis;
        TextView tvCaducidad;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMedName);
            tvProspecto = itemView.findViewById(R.id.tvMedProspect);
            btnVerProspectoPDF = itemView.findViewById(R.id.btnVerProspectoPDF);
            tvDosis = itemView.findViewById(R.id.tvMedDosis);
            tvCaducidad = itemView.findViewById(R.id.tvMedCaducidad);
        }

        public void bind(final Medicamento medicamento, final OnItemClickListener listener, final Context context) {
            tvNombre.setText(medicamento.getNombre());
            
            // Lógica para mostrar botón PDF o texto plano
            tvProspecto.setVisibility(View.GONE);
            btnVerProspectoPDF.setVisibility(View.GONE);

            final String prospectoInfo = medicamento.getProspecto();
            
            if (prospectoInfo != null && prospectoInfo.contains("http")) {
                btnVerProspectoPDF.setVisibility(View.VISIBLE);
                btnVerProspectoPDF.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(prospectoInfo));
                            context.startActivity(browserIntent);
                        } catch (Exception e) {
                            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                if (prospectoInfo != null && !prospectoInfo.isEmpty()) {
                     tvProspecto.setVisibility(View.VISIBLE);
                     tvProspecto.setText(prospectoInfo);
                }
            }

            // Mostrar Dosis
            if (medicamento.getDosisSemanal() != null && !medicamento.getDosisSemanal().isEmpty()) {
                tvDosis.setText("Dosis: " + medicamento.getDosisSemanal());
            } else {
                tvDosis.setText("Dosis: No definida");
            }

            // Mostrar Caducidad
            if (medicamento.getFechaCaducidad() != null && !medicamento.getFechaCaducidad().isEmpty()) {
                tvCaducidad.setText("Caducidad: " + medicamento.getFechaCaducidad());
            } else {
                tvCaducidad.setText("Caducidad: --/--");
            }

            // Click listener en todo el item para abrir el menú de opciones
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(medicamento);
                }
            });
        }
    }
}
