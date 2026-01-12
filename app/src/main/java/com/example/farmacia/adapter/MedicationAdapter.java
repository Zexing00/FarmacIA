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
import com.example.farmacia.model.Medication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private final List<Medication> medicationList;
    private final OnItemClickListener listener;
    private int userId = -1;

    public interface OnItemClickListener {
        void onItemClick(Medication medication);
    }

    public MedicationAdapter(List<Medication> medicationList, OnItemClickListener listener) {
        this.medicationList = medicationList;
        this.listener = listener;
    }

    public MedicationAdapter(List<Medication> medicationList, OnItemClickListener listener, int userId) {
        this.medicationList = medicationList;
        this.listener = listener;
        this.userId = userId;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.bind(medication, listener, userId);
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDose;
        TextView tvExpiry;
        Button btnVerProspecto;
        Button btnResumenInteligente;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedName);
            tvDose = itemView.findViewById(R.id.tvMedDosis);
            tvExpiry = itemView.findViewById(R.id.tvMedCaducidad);
            btnVerProspecto = itemView.findViewById(R.id.btnVerProspecto);
            btnResumenInteligente = itemView.findViewById(R.id.btnResumenInteligente);
        }

        public void bind(final Medication medication, final OnItemClickListener listener, final int userId) {
            Context context = itemView.getContext();

            tvName.setText(medication.getName());
            tvDose.setText("Dosis: " + (medication.getWeeklyDose() != null ? medication.getWeeklyDose() : "No definida"));

            String expiryDateStr = medication.getExpiryDate();
            tvExpiry.setText("Caducidad: " + (expiryDateStr != null ? expiryDateStr : "--/--"));
            tvExpiry.setTextColor(ContextCompat.getColor(context, R.color.primary_dark_blue));

            checkExpiryAlert(medication, context);

            itemView.setOnClickListener(v -> listener.onItemClick(medication));

            final String leafletInfo = medication.getLeaflet();
            if (leafletInfo != null && leafletInfo.startsWith("http")) {
                btnVerProspecto.setVisibility(View.VISIBLE);
                btnVerProspecto.setOnClickListener(v -> {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(leafletInfo));
                        context.startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnVerProspecto.setVisibility(View.GONE);
            }

            if (userId != -1) {
                btnResumenInteligente.setVisibility(View.VISIBLE);
                btnResumenInteligente.setOnClickListener(v -> {
                    Intent i = new Intent(v.getContext(), IAActivity.class);
                    i.putExtra("USER_ID", userId);
                    i.putExtra("IA_MODE", "RESUMEN");
                    i.putExtra("MED_NAME", medication.getName());
                    v.getContext().startActivity(i);
                });
            } else {
                btnResumenInteligente.setVisibility(View.GONE);
            }
        }

        private void checkExpiryAlert(Medication m, Context context) {
            String dateStr = m.getExpiryDate();
            if (dateStr == null || dateStr.isEmpty()) {
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date expiryDate = sdf.parse(dateStr);
                Calendar nextWeek = Calendar.getInstance();
                nextWeek.add(Calendar.DAY_OF_YEAR, 7);

                if (expiryDate != null && expiryDate.before(nextWeek.getTime())) {
                    tvExpiry.setTextColor(Color.RED);
                    tvExpiry.setText("⚠️ Caducidad: " + dateStr);
                }
            } catch (ParseException ignored) {
            }
        }
    }
}