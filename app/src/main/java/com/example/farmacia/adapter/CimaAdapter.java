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

import com.example.farmacia.IAActivity; // <-- AÑADIDO
import com.example.farmacia.R;
import com.example.farmacia.dao.PastilleroDAO;
import com.example.farmacia.model.cima.CimaMedicamento;

import java.util.List;

public class CimaAdapter extends RecyclerView.Adapter<CimaAdapter.CimaViewHolder> {

    private List<CimaMedicamento> listaMedicamentos;
    private Context context;
    private int userId;
    private PastilleroDAO pastilleroDAO;

    public CimaAdapter(Context context, List<CimaMedicamento> listaMedicamentos, int userId) {
        this.context = context;
        this.listaMedicamentos = listaMedicamentos;
        this.userId = userId;
        this.pastilleroDAO = new PastilleroDAO(context);
    }

    @NonNull
    @Override
    public CimaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento_cima, parent, false);
        return new CimaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CimaViewHolder holder, int position) {
        CimaMedicamento medicamento = listaMedicamentos.get(position);
        holder.tvNombre.setText(medicamento.getNombre());
        holder.tvLaboratorio.setText(medicamento.getLabtitular());
        holder.tvNRegistro.setText("Reg: " + medicamento.getNregistro());

        String urlProspecto = medicamento.getUrlProspecto();
        if (urlProspecto != null) {
            holder.btnVerProspecto.setVisibility(View.VISIBLE);
            holder.btnVerProspecto.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlProspecto));
                context.startActivity(browserIntent);
            });
        } else {
            holder.btnVerProspecto.setVisibility(View.GONE);
        }

        // Configurar botón "Añadir al Pastillero"
        holder.btnAddToPillbox.setOnClickListener(v -> {
            pastilleroDAO.open();
            boolean exito = pastilleroDAO.agregarMedicamentoUsuario(userId, medicamento.getNombre(), urlProspecto);
            pastilleroDAO.close();

            if (exito) {
                Toast.makeText(context, "Añadido al pastillero", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Ya está en tu pastillero o error", Toast.LENGTH_SHORT).show();
            }
        });

        // --- AÑADIDO: botón Resumen inteligente ---
        holder.btnResumenInteligente.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), IAActivity.class);
            i.putExtra("USER_ID", userId);
            i.putExtra("IA_MODE", "RESUMEN");
            i.putExtra("MED_NAME", medicamento.getNombre());
            v.getContext().startActivity(i);
        });
        // --- FIN AÑADIDO ---
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos.size();
    }

    public static class CimaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvLaboratorio;
        TextView tvNRegistro;
        Button btnVerProspecto;
        Button btnAddToPillbox;

        Button btnResumenInteligente; // <-- AÑADIDO

        public CimaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvCimaNombre);
            tvLaboratorio = itemView.findViewById(R.id.tvCimaLab);
            tvNRegistro = itemView.findViewById(R.id.tvCimaReg);
            btnVerProspecto = itemView.findViewById(R.id.btnCimaProspecto);
            btnAddToPillbox = itemView.findViewById(R.id.btnAddToPillbox);

            btnResumenInteligente = itemView.findViewById(R.id.btnResumenInteligente); // <-- AÑADIDO
        }
    }
}
