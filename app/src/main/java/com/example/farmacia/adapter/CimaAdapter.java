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

import com.example.farmacia.IAActivity;
import com.example.farmacia.R;
import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.model.cima.CimaMedication;

import java.util.List;

public class CimaAdapter extends RecyclerView.Adapter<CimaAdapter.CimaViewHolder> {

    private List<CimaMedication> medicationList;
    private Context context;
    private int userId;
    private PillboxDAO pillboxDAO;

    public CimaAdapter(Context context, List<CimaMedication> medicationList, int userId) {
        this.context = context;
        this.medicationList = medicationList;
        this.userId = userId;
        this.pillboxDAO = new PillboxDAO(context);
    }

    @NonNull
    @Override
    public CimaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento_cima, parent, false);
        return new CimaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CimaViewHolder holder, int position) {
        CimaMedication medication = medicationList.get(position);
        holder.tvName.setText(medication.getName());
        holder.tvLaboratory.setText(medication.getLaboratory());
        holder.tvRegistryNumber.setText("Reg: " + medication.getRegistryNumber());

        String leafletUrl = medication.getLeafletUrl();
        if (leafletUrl != null && !leafletUrl.isEmpty()) {
            holder.btnViewLeaflet.setVisibility(View.VISIBLE);
            holder.btnViewLeaflet.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(leafletUrl));
                context.startActivity(browserIntent);
            });
        } else {
            holder.btnViewLeaflet.setVisibility(View.GONE);
        }

        // "Add to Pillbox" button setup
        holder.btnAddToPillbox.setOnClickListener(v -> {
            pillboxDAO.open();
            boolean success = pillboxDAO.addMedicationToUser(userId, medication.getName(), leafletUrl);
            pillboxDAO.close();

            if (success) {
                Toast.makeText(context, "Añadido al pastillero", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Ya está en tu pastillero o ha ocurrido un error", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Smart Summary button ---
        holder.btnSmartSummary.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), IAActivity.class);
            i.putExtra("USER_ID", userId);
            i.putExtra("IA_MODE", "RESUMEN");
            i.putExtra("MED_NAME", medication.getName());
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class CimaViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvLaboratory;
        TextView tvRegistryNumber;
        Button btnViewLeaflet;
        Button btnAddToPillbox;
        Button btnSmartSummary;

        public CimaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCimaNombre);
            tvLaboratory = itemView.findViewById(R.id.tvCimaLab);
            tvRegistryNumber = itemView.findViewById(R.id.tvCimaReg);
            btnViewLeaflet = itemView.findViewById(R.id.btnCimaProspecto);
            btnAddToPillbox = itemView.findViewById(R.id.btnAddToPillbox);
            btnSmartSummary = itemView.findViewById(R.id.btnResumenInteligente);
        }
    }
}
