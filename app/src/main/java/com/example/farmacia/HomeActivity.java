package com.example.farmacia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmacia.dao.CaregiverDAO;
import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.dao.UserDAO;
import com.example.farmacia.model.User;
import com.example.farmacia.util.AlarmScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity implements ListBottomSheetFragment.OnOptionClickListener {

    // Views
    private TextView tvWelcome;
    private ImageButton btnLogoutIcon;
    private MaterialButton btnHomeCalendar, btnHomePillbox, btnHomeSearch, btnHomeIA, btnHomeManageCaregivers, btnHomeManagePatients;

    // User Data
    private String userName;
    private int userId;

    // DAOs and utils
    private UserDAO userDAO;
    private CaregiverDAO caregiverDAO;
    private PillboxDAO pillboxDAO;
    private String currentMenuTag;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeDaos();
        initializeViews();
        getUserData();
        setupListeners();

        checkExpirations();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        btnHomeCalendar = findViewById(R.id.btnHomeCalendar);
        btnHomePillbox = findViewById(R.id.btnHomePillbox);
        btnHomeSearch = findViewById(R.id.btnHomeSearch);
        btnHomeIA = findViewById(R.id.btnHomeIA);
        btnHomeManageCaregivers = findViewById(R.id.btnHomeManageCaregivers);
        btnHomeManagePatients = findViewById(R.id.btnHomeManagePatients);
    }

    private void initializeDaos() {
        userDAO = new UserDAO(this);
        userDAO.open();
        caregiverDAO = new CaregiverDAO(this);
        caregiverDAO.open();
        pillboxDAO = new PillboxDAO(this);
        pillboxDAO.open();
    }

    private void getUserData() {
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("USER_NAME");
            userId = intent.getIntExtra("USER_ID", -1);
            if (userName != null) tvWelcome.setText("Bienvenido, " + userName);
        }
    }

    private void setupListeners() {
        btnHomeCalendar.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, CalendarActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        btnHomePillbox.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, PillboxActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        btnHomeSearch.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, SearchMedicinesActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        btnHomeIA.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IAActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        btnHomeManageCaregivers.setOnClickListener(v -> showAccessOptions());
        btnHomeManagePatients.setOnClickListener(v -> showSelectPatientDialog());
        btnLogoutIcon.setOnClickListener(v -> logout());
    }

    private void logout() {
        executor.execute(() -> {
            AlarmScheduler.cancelAllAlarmsForUser(this, userId);

            SharedPreferences prefs = getSharedPreferences("FarmacIAPrefs", MODE_PRIVATE);
            prefs.edit().remove("LAST_USER_ID").apply();

            runOnUiThread(() -> {
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
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
        // This method should be implemented or removed if not used
    }

    private void showAccessOptions() {
        currentMenuTag = "accessOptions";
        ArrayList<String> options = new ArrayList<>(Arrays.asList("Dar permiso a nuevo cuidador", "Quitar permiso a cuidador existente"));
        ListBottomSheetFragment bottomSheet = ListBottomSheetFragment.newInstance("Gestión de Acceso", options);
        bottomSheet.show(getSupportFragmentManager(), currentMenuTag);
    }

    private void showShareAccessDialog() {
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
        executor.shutdown();
    }
}
