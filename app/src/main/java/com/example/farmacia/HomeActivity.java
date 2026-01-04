package com.example.farmacia;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmacia.dao.CaregiverDAO;
import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.dao.UserDAO;
import com.example.farmacia.model.Medication;
import com.example.farmacia.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnPillbox;
    private Button btnMedications;
    private Button btnShareAccess;
    private Button btnManagePatients;
    
    private String userName;
    private int userId;
    
    private UserDAO userDAO;
    private CaregiverDAO caregiverDAO;
    private PillboxDAO pillboxDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnPillbox = findViewById(R.id.btnPillbox);
        btnMedications = findViewById(R.id.btnMedications);
        btnShareAccess = findViewById(R.id.btnShareAccess);
        btnManagePatients = findViewById(R.id.btnManagePatients);

        // DAOs initialization
        userDAO = new UserDAO(this);
        userDAO.open();
        caregiverDAO = new CaregiverDAO(this);
        caregiverDAO.open();
        pillboxDAO = new PillboxDAO(this);
        pillboxDAO.open();

        // Retrieve data from intent
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("USER_NAME");
            userId = intent.getIntExtra("USER_ID", -1);
            if (userName != null) {
                tvWelcome.setText("Bienvenido, " + userName);
            }
        }

        btnPillbox.setOnClickListener(v -> {
            Intent pillboxIntent = new Intent(HomeActivity.this, PillboxActivity.class);
            pillboxIntent.putExtra("USER_ID", userId);
            startActivity(pillboxIntent);
        });

        btnMedications.setOnClickListener(v -> {
            Intent searchIntent = new Intent(HomeActivity.this, SearchMedicinesActivity.class);
            searchIntent.putExtra("USER_ID", userId);
            startActivity(searchIntent);
        });

        btnShareAccess.setOnClickListener(v -> showAccessOptions());

        btnManagePatients.setOnClickListener(v -> showSelectPatientDialog());

        Button btnIA = findViewById(R.id.btnIA);
        btnIA.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IAActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        checkExpirations();
    }

    private void checkExpirations() {
        List<Medication> medications = pillboxDAO.getMedicationsByUserId(userId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar nextWeek = Calendar.getInstance();
        nextWeek.add(Calendar.DAY_OF_YEAR, 7);
        Date limitDate = nextWeek.getTime();

        StringBuilder notice = new StringBuilder();
        int counter = 0;
        for (Medication m : medications) {
            String dateStr = m.getExpiryDate();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    Date expiryDate = sdf.parse(dateStr);
                    if (expiryDate != null && expiryDate.before(limitDate)) {
                        notice.append(" • ").append(m.getName()).append("\n");
                        counter++;
                    }
                } catch (ParseException ignored) {}
            }
        }

        if (counter > 0) {
            SpannableString title = new SpannableString("⚠️ Alerta de Caducidad");
            title.setSpan(new ForegroundColorSpan(Color.parseColor("#D32F2F")), 0, title.length(), 0);

            new MaterialAlertDialogBuilder(this)
                    .setTitle(title)
                    .setMessage("Los siguientes medicamentos caducan pronto:\n\n" + notice.toString())
                    .setPositiveButton("Entendido", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void showAccessOptions() {
        String[] options = {"Dar permiso a nuevo cuidador", "Quitar permiso a cuidador existente"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Gestión de Acceso")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showShareAccessDialog();
                    else showRemoveAccessDialog();
                }).show();
    }

    private void showShareAccessDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Nuevo Cuidador");
        builder.setMessage("Introduce el nombre de usuario de tu cuidador:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String caregiverName = input.getText().toString().trim();
            if (!caregiverName.isEmpty()) {
                int caregiverId = userDAO.getUserIdByName(caregiverName);
                
                if (caregiverId == -1) {
                    Toast.makeText(HomeActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                } else if (caregiverId == userId) {
                    Toast.makeText(HomeActivity.this, "No puedes añadirte a ti mismo", Toast.LENGTH_SHORT).show();
                } else {
                    boolean success = caregiverDAO.addCaregiver(userId, caregiverId);
                    if (success) {
                        Toast.makeText(HomeActivity.this, "Permiso concedido a " + caregiverName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Este usuario ya tiene permiso", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showRemoveAccessDialog() {
        List<User> caregivers = caregiverDAO.getCaregiversByPatient(userId);
        if (caregivers.isEmpty()) {
            Toast.makeText(this, "No tienes cuidadores asignados", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[caregivers.size()];
        for (int i = 0; i < caregivers.size(); i++) names[i] = caregivers.get(i).getUsername();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Quitar acceso")
                .setItems(names, (dialog, which) -> {
                    User caregiver = caregivers.get(which);
                    caregiverDAO.removeCaregiver(userId, caregiver.getId());
                    Toast.makeText(this, "Acceso revocado a " + caregiver.getUsername(), Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void showSelectPatientDialog() {
        final List<User> patients = caregiverDAO.getAssignedPatients(userId);
        if (patients.isEmpty()) {
            Toast.makeText(this, "No tienes pacientes asignados", Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Seleccionar Paciente");

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);
        for (User p : patients) adapter.add(p.getUsername());

        builder.setAdapter(adapter, (dialog, which) -> {
            User selectedPatient = patients.get(which);
            
            Intent intent = new Intent(HomeActivity.this, PillboxActivity.class);
            intent.putExtra("USER_ID", selectedPatient.getId());
            intent.putExtra("IS_CAREGIVER", true);
            startActivity(intent);
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) userDAO.close();
        if (caregiverDAO != null) caregiverDAO.close();
        if (pillboxDAO != null) pillboxDAO.close();
    }
}
