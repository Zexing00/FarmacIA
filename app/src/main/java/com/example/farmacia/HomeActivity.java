package com.example.farmacia;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity implements ListBottomSheetFragment.OnOptionClickListener {

    private TextView tvWelcome;
    private Button btnCalendar, btnPillbox, btnMedications, btnShareAccess, btnManagePatients, btnIA;

    private String userName;
    private int userId;

    private UserDAO userDAO;
    private CaregiverDAO caregiverDAO;
    private PillboxDAO pillboxDAO;
    private String currentMenuTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnPillbox = findViewById(R.id.btnPillbox);
        btnMedications = findViewById(R.id.btnMedications);
        btnShareAccess = findViewById(R.id.btnShareAccess);
        btnManagePatients = findViewById(R.id.btnManagePatients);
        btnIA = findViewById(R.id.btnIA);

        userDAO = new UserDAO(this);
        userDAO.open();
        caregiverDAO = new CaregiverDAO(this);
        caregiverDAO.open();
        pillboxDAO = new PillboxDAO(this);
        pillboxDAO.open();

        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("USER_NAME");
            userId = intent.getIntExtra("USER_ID", -1);
            if (userName != null) tvWelcome.setText("Bienvenido, " + userName);
        }

        btnCalendar.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, CalendarActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        btnPillbox.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, PillboxActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        btnMedications.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, SearchMedicinesActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        btnShareAccess.setOnClickListener(v -> showAccessOptions());
        btnManagePatients.setOnClickListener(v -> showSelectPatientDialog());

        btnIA.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IAActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        checkExpirations();
    }

    @Override
    public void onOptionClick(String option, int position) {
        if (currentMenuTag == null) return;

        switch (currentMenuTag) {
            case "accessOptions":
                if (position == 0) showShareAccessDialog();
                else showRemoveAccessDialog();
                break;
            case "removeAccess":
                List<User> caregivers = caregiverDAO.getCaregiversByPatient(userId);
                caregiverDAO.removeCaregiver(userId, caregivers.get(position).getId());
                Toast.makeText(this, "Acceso revocado", Toast.LENGTH_SHORT).show();
                break;
            case "selectPatient":
                List<User> patients = caregiverDAO.getAssignedPatients(userId);
                Intent i = new Intent(HomeActivity.this, PillboxActivity.class);
                i.putExtra("USER_ID", patients.get(position).getId());
                i.putExtra("IS_CAREGIVER", true);
                startActivity(i);
                break;
        }
        currentMenuTag = null; // Reset tag
    }

    private void checkExpirations() {
        // ... (existing code)
    }

    private void showAccessOptions() {
        currentMenuTag = "accessOptions";
        ArrayList<String> options = new ArrayList<>(Arrays.asList("Dar permiso a nuevo cuidador", "Quitar permiso a cuidador existente"));
        ListBottomSheetFragment bottomSheet = ListBottomSheetFragment.newInstance("Gestión de Acceso", options);
        bottomSheet.show(getSupportFragmentManager(), currentMenuTag);
    }

    private void showShareAccessDialog() {
        // This one remains an AlertDialog because it needs a text input
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Nuevo Cuidador");
        builder.setMessage("Introduce el nombre de usuario:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                int cId = userDAO.getUserIdByName(name);
                if (cId == -1) Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                else if (cId == userId) Toast.makeText(this, "No puedes añadirte a ti mismo", Toast.LENGTH_SHORT).show();
                else {
                    if (caregiverDAO.addCaregiver(userId, cId)) Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Ya tiene permiso", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showRemoveAccessDialog() {
        List<User> caregivers = caregiverDAO.getCaregiversByPatient(userId);
        if (caregivers.isEmpty()) {
            Toast.makeText(this, "No tienes cuidadores", Toast.LENGTH_SHORT).show();
            return;
        }
        currentMenuTag = "removeAccess";
        ArrayList<String> names = caregivers.stream().map(User::getUsername).collect(Collectors.toCollection(ArrayList::new));
        ListBottomSheetFragment bottomSheet = ListBottomSheetFragment.newInstance("Quitar Acceso", names);
        bottomSheet.show(getSupportFragmentManager(), currentMenuTag);
    }

    private void showSelectPatientDialog() {
        List<User> patients = caregiverDAO.getAssignedPatients(userId);
        if (patients.isEmpty()) {
            Toast.makeText(this, "No tienes pacientes", Toast.LENGTH_SHORT).show();
            return;
        }
        currentMenuTag = "selectPatient";
        ArrayList<String> names = patients.stream().map(User::getUsername).collect(Collectors.toCollection(ArrayList::new));
        ListBottomSheetFragment bottomSheet = ListBottomSheetFragment.newInstance("Seleccionar Paciente", names);
        bottomSheet.show(getSupportFragmentManager(), currentMenuTag);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) userDAO.close();
        if (caregiverDAO != null) caregiverDAO.close();
        if (pillboxDAO != null) pillboxDAO.close();
    }
}