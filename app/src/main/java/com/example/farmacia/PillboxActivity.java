package com.example.farmacia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.adapter.MedicationAdapter;
import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.model.Medication;

import java.util.List;

public class PillboxActivity extends AppCompatActivity {

    private RecyclerView rvMedications;
    private PillboxDAO pillboxDAO;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pillbox);

        // Go back button configuration
        ImageButton btnPillboxBack = findViewById(R.id.btnPillboxBack);
        btnPillboxBack.setOnClickListener(v -> finish());

        rvMedications = findViewById(R.id.rvMedications);
        rvMedications.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve user ID
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getIntExtra("USER_ID", -1);
        }

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pillboxDAO = new PillboxDAO(this);
        pillboxDAO.open();

        loadMedications();
    }

    private void loadMedications() {
        List<Medication> medicationList = pillboxDAO.getMedicationsByUserId(userId);
        
        if (medicationList.isEmpty()) {
            Toast.makeText(this, "No tienes medicamentos en tu pastillero", Toast.LENGTH_SHORT).show();
        }

        MedicationAdapter adapter = new MedicationAdapter(medicationList, this::showMedicationOptions, userId);
        rvMedications.setAdapter(adapter);
    }

    private void showMedicationOptions(final Medication medication) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gestionar " + medication.getName());
        
        String[] options = {"Añadir Fecha de Caducidad", "Añadir Dosis Semanal", "Eliminar del Pastillero"};
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showDateDialog(medication);
                    break;
                case 1:
                    showDoseDialog(medication);
                    break;
                case 2:
                    confirmDeletion(medication);
                    break;
            }
        });
        builder.show();
    }

    private void showDateDialog(final Medication medication) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fecha de Caducidad (YYYY-MM-DD)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_DATETIME);
        if (medication.getExpiryDate() != null) {
            input.setText(medication.getExpiryDate());
        }
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newDate = input.getText().toString();
            medication.setExpiryDate(newDate);
            pillboxDAO.updatePillbox(userId, medication.getId(), newDate, null);
            loadMedications(); // Reload list
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showDoseDialog(final Medication medication) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dosis Semanal (ej. 3 veces al día)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (medication.getWeeklyDose() != null) {
            input.setText(medication.getWeeklyDose());
        }
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newDose = input.getText().toString();
            medication.setWeeklyDose(newDose);
            pillboxDAO.updatePillbox(userId, medication.getId(), null, newDose);
            loadMedications(); // Reload list
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void confirmDeletion(final Medication medication) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Medicamento")
                .setMessage("¿Estás seguro de que quieres eliminar " + medication.getName() + " de tu pastillero?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    pillboxDAO.removeMedicationFromUser(userId, medication.getId());
                    Toast.makeText(PillboxActivity.this, "Medicamento eliminado", Toast.LENGTH_SHORT).show();
                    loadMedications(); // Reload list
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pillboxDAO != null) {
            pillboxDAO.close();
        }
    }
}
