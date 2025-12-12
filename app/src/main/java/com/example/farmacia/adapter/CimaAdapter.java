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
import com.example.farmacia.model.cima.CimaMedicamento;

import java.util.List;

public class CimaAdapter extends RecyclerView.Adapter<CimaAdapter.CimaViewHolder> {

    private List<CimaMedicamento> listaMedicamentos;
    private Context context;

    public CimaAdapter(Context context, List<CimaMedicamento> listaMedicamentos) {
        this.context = context;
        this.listaMedicamentos = listaMedicamentos;
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

        public CimaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvCimaNombre);
            tvLaboratorio = itemView.findViewById(R.id.tvCimaLab);
            tvNRegistro = itemView.findViewById(R.id.tvCimaReg);
            btnVerProspecto = itemView.findViewById(R.id.btnCimaProspecto);
        }
    }
}
