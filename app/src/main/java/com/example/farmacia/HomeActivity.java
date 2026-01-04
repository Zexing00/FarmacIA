package com.example.farmacia;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.farmacia.dao.CuidadorDAO;
import com.example.farmacia.dao.PastilleroDAO;
import com.example.farmacia.dao.UsuarioDAO;
import com.example.farmacia.model.Medicamento;
import com.example.farmacia.model.Usuario;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnCalendar, btnPillbox, btnMedications, btnShareAccess, btnManagePatients, btnIA;
    
    private String userName;
    private int userId;
    
    private UsuarioDAO usuarioDAO;
    private CuidadorDAO cuidadorDAO;
    private PastilleroDAO pastilleroDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcome = findViewById(R.id.tvWelcome);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnPillbox = findViewById(R.id.btnPillbox);
        btnMedications = findViewById(R.id.btnMedications);
        btnShareAccess = findViewById(R.id.btnShareAccess);
        btnManagePatients = findViewById(R.id.btnManagePatients);
        btnIA = findViewById(R.id.btnIA);

        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        cuidadorDAO = new CuidadorDAO(this);
        cuidadorDAO.open();
        pastilleroDAO = new PastilleroDAO(this);
        pastilleroDAO.open();

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

        btnShareAccess.setOnClickListener(v -> mostrarOpcionesAcceso());
        btnManagePatients.setOnClickListener(v -> mostrarDialogoSeleccionarPaciente());
        
        btnIA.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IAActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        verificarCaducidades();
    }

    private void verificarCaducidades() {
        List<Medicamento> medicamentos = pastilleroDAO.obtenerMedicamentosPorUsuario(userId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar proximaSemana = Calendar.getInstance();
        proximaSemana.add(Calendar.DAY_OF_YEAR, 7);
        Date fechaLimite = proximaSemana.getTime();

        StringBuilder aviso = new StringBuilder();
        int contador = 0;
        for (Medicamento m : medicamentos) {
            String fechaStr = m.getFechaCaducidad();
            if (fechaStr != null && !fechaStr.isEmpty()) {
                try {
                    Date fechaCad = sdf.parse(fechaStr);
                    if (fechaCad != null && fechaCad.before(fechaLimite)) {
                        aviso.append(" • ").append(m.getNombre()).append("\n");
                        contador++;
                    }
                } catch (ParseException ignored) {}
            }
        }

        if (contador > 0) {
            SpannableString title = new SpannableString("⚠️ Alerta de Caducidad");
            title.setSpan(new ForegroundColorSpan(Color.parseColor("#D32F2F")), 0, title.length(), 0);

            new MaterialAlertDialogBuilder(this)
                    .setTitle(title)
                    .setMessage("Los siguientes medicamentos caducan pronto:\n\n" + aviso.toString())
                    .setPositiveButton("Entendido", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void mostrarOpcionesAcceso() {
        String[] opciones = {"Dar permiso a nuevo cuidador", "Quitar permiso a cuidador existente"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Gestión de Acceso")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) mostrarDialogoCompartirAcceso();
                    else mostrarDialogoQuitarAcceso();
                }).show();
    }

    private void mostrarDialogoCompartirAcceso() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Nuevo Cuidador");
        builder.setMessage("Introduce el nombre de usuario:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String nombre = input.getText().toString().trim();
            if (!nombre.isEmpty()) {
                int cId = usuarioDAO.obtenerIdPorNombre(nombre);
                if (cId == -1) Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                else if (cId == userId) Toast.makeText(this, "No puedes añadirte a ti mismo", Toast.LENGTH_SHORT).show();
                else {
                    if (cuidadorDAO.agregarCuidador(userId, cId)) Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Ya tiene permiso", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoQuitarAcceso() {
        List<Usuario> cuidadores = cuidadorDAO.obtenerCuidadoresPorPaciente(userId);
        if (cuidadores.isEmpty()) {
            Toast.makeText(this, "No tienes cuidadores", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] nombres = new String[cuidadores.size()];
        for (int i = 0; i < cuidadores.size(); i++) nombres[i] = cuidadores.get(i).getNombreUsuario();
        new MaterialAlertDialogBuilder(this)
                .setTitle("Quitar acceso")
                .setItems(nombres, (dialog, which) -> {
                    cuidadorDAO.eliminarCuidador(userId, cuidadores.get(which).getId());
                    Toast.makeText(this, "Acceso revocado", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void mostrarDialogoSeleccionarPaciente() {
        final List<Usuario> pacientes = cuidadorDAO.obtenerPacientesAsignados(userId);
        if (pacientes.isEmpty()) {
            Toast.makeText(this, "No tienes pacientes", Toast.LENGTH_SHORT).show();
            return;
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Seleccionar Paciente");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);
        for (Usuario p : pacientes) adapter.add(p.getNombreUsuario());
        builder.setAdapter(adapter, (dialog, which) -> {
            Intent i = new Intent(HomeActivity.this, PillboxActivity.class);
            i.putExtra("USER_ID", pacientes.get(which).getId());
            i.putExtra("ES_CUIDADOR", true);
            startActivity(i);
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) usuarioDAO.close();
        if (cuidadorDAO != null) cuidadorDAO.close();
        if (pastilleroDAO != null) pastilleroDAO.close();
    }
}
