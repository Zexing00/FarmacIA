package com.example.farmacia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
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

        MedicamentoAdapter adapter = new MedicamentoAdapter(lista, new MedicamentoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Medicamento medicamento) {
                mostrarOpcionesMedicamento(medicamento);
            }
        });
        rvMedications.setAdapter(adapter);
    }

    private void mostrarOpcionesMedicamento(final Medicamento medicamento) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gestionar " + medicamento.getNombre());
        
        String[] opciones = {"Añadir Fecha de Caducidad", "Añadir Dosis Semanal", "Eliminar del Pastillero"};
        
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mostrarDialogoFecha(medicamento);
                        break;
                    case 1:
                        mostrarDialogoDosis(medicamento);
                        break;
                    case 2:
                        confirmarEliminacion(medicamento);
                        break;
                }
            }
        });
        builder.show();
    }

    private void mostrarDialogoFecha(final Medicamento medicamento) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fecha de Caducidad (YYYY-MM-DD)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_DATETIME);
        if (medicamento.getFechaCaducidad() != null) {
            input.setText(medicamento.getFechaCaducidad());
        }
        builder.setView(input);

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevaFecha = input.getText().toString();
                medicamento.setFechaCaducidad(nuevaFecha);
                pastilleroDAO.actualizarPastillero(userId, medicamento.getId(), nuevaFecha, null);
                cargarMedicamentos(); // Recargar lista
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoDosis(final Medicamento medicamento) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dosis Semanal (ej. 3 veces al día)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (medicamento.getDosisSemanal() != null) {
            input.setText(medicamento.getDosisSemanal());
        }
        builder.setView(input);

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevaDosis = input.getText().toString();
                medicamento.setDosisSemanal(nuevaDosis);
                pastilleroDAO.actualizarPastillero(userId, medicamento.getId(), null, nuevaDosis);
                cargarMedicamentos(); // Recargar lista
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void confirmarEliminacion(final Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Medicamento")
                .setMessage("¿Estás seguro de que quieres eliminar " + medicamento.getNombre() + " de tu pastillero?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pastilleroDAO.eliminarMedicamentoDeUsuario(userId, medicamento.getId());
                        Toast.makeText(PillboxActivity.this, "Medicamento eliminado", Toast.LENGTH_SHORT).show();
                        cargarMedicamentos(); // Recargar lista
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pastilleroDAO != null) {
            pastilleroDAO.close();
        }
    }
}
