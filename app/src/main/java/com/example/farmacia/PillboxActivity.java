package com.example.farmacia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.adapter.MedicamentoAdapter;
import com.example.farmacia.dao.PastilleroDAO;
import com.example.farmacia.model.Medicamento;

import java.util.List;

public class PillboxActivity extends AppCompatActivity {

    private RecyclerView rvMedications;
    private PastilleroDAO pastilleroDAO;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pillbox);

        rvMedications = findViewById(R.id.rvMedications);
        rvMedications.setLayoutManager(new LinearLayoutManager(this));

        // Recuperar ID de usuario
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getIntExtra("USER_ID", -1);
        }

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pastilleroDAO = new PastilleroDAO(this);
        pastilleroDAO.open();

        cargarMedicamentos();
    }

    private void cargarMedicamentos() {
        List<Medicamento> lista = pastilleroDAO.obtenerMedicamentosPorUsuario(userId);
        
        if (lista.isEmpty()) {
            Toast.makeText(this, "No tienes medicamentos en tu pastillero", Toast.LENGTH_SHORT).show();
        }

        MedicamentoAdapter adapter = new MedicamentoAdapter(lista);
        rvMedications.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pastilleroDAO != null) {
            pastilleroDAO.close();
        }
    }
}
