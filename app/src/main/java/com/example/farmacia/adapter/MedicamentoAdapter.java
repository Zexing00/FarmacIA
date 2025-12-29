package com.example.farmacia.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.IAActivity;
import com.example.farmacia.R;
import com.example.farmacia.model.Medicamento;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> listaMedicamentos;
    private OnItemClickListener listener;

    private int userId = -1; // <-- AÑADIDO

    public interface OnItemClickListener {
        void onItemClick(Medicamento medicamento);
    }

    public MedicamentoAdapter(List<Medicamento> listaMedicamentos, OnItemClickListener listener) {
        this.listaMedicamentos = listaMedicamentos;
        this.listener = listener;
    }

    // <-- AÑADIDO: constructor con userId
    public MedicamentoAdapter(List<Medicamento> listaMedicamentos, OnItemClickListener listener, int userId) {
        this.listaMedicamentos = listaMedicamentos;
        this.listener = listener;
        this.userId = userId;
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
        holder.bind(medicamento, listener, userId);
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

        Button btnResumenInteligente; // <-- AÑADIDO

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMedName);
            tvProspecto = itemView.findViewById(R.id.tvMedProspect);
            btnVerProspectoPDF = itemView.findViewById(R.id.btnVerProspectoPDF);
            tvDosis = itemView.findViewById(R.id.tvMedDosis);
            tvCaducidad = itemView.findViewById(R.id.tvMedCaducidad);

            btnResumenInteligente = itemView.findViewById(R.id.btnResumenInteligente); // <-- AÑADIDO
        }

        public void bind(final Medicamento medicamento, final OnItemClickListener listener, final int userId) {
            Context context = itemView.getContext();

            tvNombre.setText(medicamento.getNombre());

            tvProspecto.setVisibility(View.GONE);
            btnVerProspectoPDF.setVisibility(View.GONE);

            final String prospectoInfo = medicamento.getProspecto();

            if (prospectoInfo != null && prospectoInfo.contains("http")) {
                btnVerProspectoPDF.setVisibility(View.VISIBLE);
                btnVerProspectoPDF.setOnClickListener(v -> {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(prospectoInfo));
                        context.startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (prospectoInfo != null && !prospectoInfo.isEmpty()) {
                tvProspecto.setVisibility(View.VISIBLE);
                tvProspecto.setText(prospectoInfo);
            }

            tvDosis.setText("Dosis: " + (medicamento.getDosisSemanal() != null ? medicamento.getDosisSemanal() : "No definida"));
            tvCaducidad.setText("Caducidad: " + (medicamento.getFechaCaducidad() != null ? medicamento.getFechaCaducidad() : "--/--"));

            verificarAlertaCaducidad(medicamento, context);

            itemView.setOnClickListener(v -> listener.onItemClick(medicamento));

            // --- AÑADIDO: Resumen inteligente ---
            btnResumenInteligente.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), IAActivity.class);
                i.putExtra("USER_ID", userId);
                i.putExtra("IA_MODE", "RESUMEN");
                i.putExtra("MED_NAME", medicamento.getNombre());
                v.getContext().startActivity(i);
            });
        }

        private void verificarAlertaCaducidad(Medicamento m, Context context) {
            String fechaStr = m.getFechaCaducidad();
            if (fechaStr != null && !fechaStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date fechaCad = sdf.parse(fechaStr);
                    Calendar proximaSemana = Calendar.getInstance();
                    proximaSemana.add(Calendar.DAY_OF_YEAR, 7);

                    if (fechaCad != null && fechaCad.before(proximaSemana.getTime())) {
                        tvCaducidad.setTextColor(Color.RED);
                        tvCaducidad.setText("⚠️ " + tvCaducidad.getText());
                    } else {
                        tvCaducidad.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    }
                } catch (ParseException ignored) {
                    tvCaducidad.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }
            } else {
                tvCaducidad.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }
        }
    }
}
