package com.example.farmacia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmacia.dao.CuidadorDAO;
import com.example.farmacia.dao.PastilleroDAO;
import com.example.farmacia.dao.UsuarioDAO;
import com.example.farmacia.model.Medicamento;
import com.example.farmacia.model.Usuario;

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
    
    private UsuarioDAO usuarioDAO;
    private CuidadorDAO cuidadorDAO;
    private PastilleroDAO pastilleroDAO;

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
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        cuidadorDAO = new CuidadorDAO(this);
        cuidadorDAO.open();
        pastilleroDAO = new PastilleroDAO(this);
        pastilleroDAO.open();

        // Recuperar datos del intent
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("USER_NAME");
            userId = intent.getIntExtra("USER_ID", -1);
            if (userName != null) {
                tvWelcome.setText("Bienvenido, " + userName);
            }
        }

        btnPillbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PillboxActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        btnMedications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SearchMedicinesActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        btnShareAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarOpcionesAcceso();
            }
        });

        btnManagePatients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoSeleccionarPaciente();
            }
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
        for (Medicamento m : medicamentos) {
            String fechaStr = m.getFechaCaducidad();
            if (fechaStr != null && !fechaStr.isEmpty()) {
                try {
                    Date fechaCad = sdf.parse(fechaStr);
                    if (fechaCad != null && fechaCad.before(fechaLimite)) {
                        aviso.append("- ").append(m.getNombre()).append("\n");
                    }
                } catch (ParseException ignored) {}
            }
        }

        if (aviso.length() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Alerta de Caducidad")
                    .setMessage("Los siguientes medicamentos caducan en menos de una semana:\n\n" + aviso.toString())
                    .setPositiveButton("Entendido", null)
                    .show();
        }
    }

    private void mostrarOpcionesAcceso() {
        String[] opciones = {"Dar permiso a nuevo cuidador", "Quitar permiso a cuidador existente"};
        new AlertDialog.Builder(this)
                .setTitle("Gestión de Acceso")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) mostrarDialogoCompartirAcceso();
                    else mostrarDialogoQuitarAcceso();
                }).show();
    }

    private void mostrarDialogoCompartirAcceso() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dar permiso a un cuidador");
        builder.setMessage("Introduce el nombre de usuario de tu cuidador:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String cuidadorNombre = input.getText().toString().trim();
                if (!cuidadorNombre.isEmpty()) {
                    int cuidadorId = usuarioDAO.obtenerIdPorNombre(cuidadorNombre);
                    
                    if (cuidadorId == -1) {
                        Toast.makeText(HomeActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    } else if (cuidadorId == userId) {
                        Toast.makeText(HomeActivity.this, "No puedes añadirte a ti mismo", Toast.LENGTH_SHORT).show();
                    } else {
                        boolean exito = cuidadorDAO.agregarCuidador(userId, cuidadorId);
                        if (exito) {
                            Toast.makeText(HomeActivity.this, "Permiso concedido a " + cuidadorNombre, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HomeActivity.this, "Este usuario ya tiene permiso", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoQuitarAcceso() {
        List<Usuario> cuidadores = cuidadorDAO.obtenerCuidadoresPorPaciente(userId);
        if (cuidadores.isEmpty()) {
            Toast.makeText(this, "No tienes cuidadores asignados", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nombres = new String[cuidadores.size()];
        for (int i = 0; i < cuidadores.size(); i++) nombres[i] = cuidadores.get(i).getNombreUsuario();

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar cuidador para quitar acceso")
                .setItems(nombres, (dialog, which) -> {
                    Usuario c = cuidadores.get(which);
                    cuidadorDAO.eliminarCuidador(userId, c.getId());
                    Toast.makeText(this, "Acceso revocado a " + c.getNombreUsuario(), Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void mostrarDialogoSeleccionarPaciente() {
        final List<Usuario> pacientes = cuidadorDAO.obtenerPacientesAsignados(userId);
        if (pacientes.isEmpty()) {
            Toast.makeText(this, "No tienes pacientes asignados", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Paciente");

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);
        for (Usuario p : pacientes) adapter.add(p.getNombreUsuario());

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Usuario pacienteSeleccionado = pacientes.get(which);
                
                Intent intent = new Intent(HomeActivity.this, PillboxActivity.class);
                intent.putExtra("USER_ID", pacienteSeleccionado.getId());
                intent.putExtra("ES_CUIDADOR", true);
                startActivity(intent);
            }
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
