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

    private List<Medication> medicationList;
    private OnItemClickListener listener;

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
        TextView tvLeaflet;
        Button btnViewLeafletPDF;
        TextView tvDose;
        TextView tvExpiry;
        Button btnSmartSummary;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedName);
            tvLeaflet = itemView.findViewById(R.id.tvMedProspect);
            btnViewLeafletPDF = itemView.findViewById(R.id.btnVerProspectoPDF);
            tvDose = itemView.findViewById(R.id.tvMedDosis);
            tvExpiry = itemView.findViewById(R.id.tvMedCaducidad);
            btnSmartSummary = itemView.findViewById(R.id.btnResumenInteligente);
        }

        public void bind(final Medication medication, final OnItemClickListener listener, final int userId) {
            Context context = itemView.getContext();

            tvName.setText(medication.getName());

            tvLeaflet.setVisibility(View.GONE);
            btnViewLeafletPDF.setVisibility(View.GONE);

            final String leafletInfo = medication.getLeaflet();

            if (leafletInfo != null && leafletInfo.contains("http")) {
                btnViewLeafletPDF.setVisibility(View.VISIBLE);
                btnViewLeafletPDF.setOnClickListener(v -> {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(leafletInfo));
                        context.startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (leafletInfo != null && !leafletInfo.isEmpty()) {
                tvLeaflet.setVisibility(View.VISIBLE);
                tvLeaflet.setText(leafletInfo);
            }

            tvDose.setText("Dosis: " + (medication.getWeeklyDose() != null ? medication.getWeeklyDose() : "No definida"));
            tvExpiry.setText("Caducidad: " + (medication.getExpiryDate() != null ? medication.getExpiryDate() : "--/--"));

            checkExpiryAlert(medication, context);

            itemView.setOnClickListener(v -> listener.onItemClick(medication));

            btnSmartSummary.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), IAActivity.class);
                i.putExtra("USER_ID", userId);
                i.putExtra("IA_MODE", "RESUMEN");
                i.putExtra("MED_NAME", medication.getName());
                v.getContext().startActivity(i);
            });
        }

        private void checkExpiryAlert(Medication m, Context context) {
            String dateStr = m.getExpiryDate();
            if (dateStr != null && !dateStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date expiryDate = sdf.parse(dateStr);
                    Calendar nextWeek = Calendar.getInstance();
                    nextWeek.add(Calendar.DAY_OF_YEAR, 7);

                    if (expiryDate != null && expiryDate.before(nextWeek.getTime())) {
                        tvExpiry.setTextColor(Color.RED);
                        tvExpiry.setText("⚠️ " + tvExpiry.getText());
                    } else {
                        tvExpiry.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    }
                } catch (ParseException ignored) {
                    tvExpiry.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }
            } else {
                tvExpiry.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }
        }
    }
}
