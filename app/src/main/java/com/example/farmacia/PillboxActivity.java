package com.example.farmacia;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.adapter.MedicamentoAdapter;
import com.example.farmacia.dao.PastilleroDAO;
import com.example.farmacia.model.Medicamento;
import com.example.farmacia.receiver.AlarmReceiver;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PillboxActivity extends AppCompatActivity {

    private RecyclerView rvMedications;
    private PastilleroDAO pastilleroDAO;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pillbox);

        pedirPermisosNotificaciones();

        ImageButton btnPillboxBack = findViewById(R.id.btnPillboxBack);
        btnPillboxBack.setOnClickListener(v -> finish());

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

    private void pedirPermisosNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void cargarMedicamentos() {
        List<Medicamento> lista = pastilleroDAO.obtenerMedicamentosPorUsuario(userId);
        MedicamentoAdapter adapter = new MedicamentoAdapter(lista, this::mostrarOpcionesMedicamento);
        rvMedications.setAdapter(adapter);
    }

    private void mostrarOpcionesMedicamento(final Medicamento medicamento) {
        String[] opciones = {"Añadir Fecha de Caducidad", "Configurar Dosis/Horario", "Eliminar del Pastillero"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(medicamento.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0: mostrarDatePicker(medicamento); break;
                        case 1: configurarDosis(medicamento); break;
                        case 2: confirmarEliminacion(medicamento); break;
                    }
                })
                .show();
    }

    private void configurarDosis(final Medicamento medicamento) {
        String[] tipoDosis = {"Dosis Diaria", "Días Específicos"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Frecuencia de toma")
                .setItems(tipoDosis, (dialog, which) -> {
                    if (which == 0) {
                        seleccionarHoras(medicamento, "Todos los días", null);
                    } else {
                        seleccionarDias(medicamento);
                    }
                })
                .show();
    }

    private void seleccionarDias(final Medicamento medicamento) {
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        int[] diasCalendar = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
        boolean[] seleccionados = new boolean[dias.length];
        ArrayList<Integer> elegidosIndex = new ArrayList<>();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Selecciona los días")
                .setMultiChoiceItems(dias, seleccionados, (dialog, which, isChecked) -> {
                    if (isChecked) elegidosIndex.add(which);
                    else elegidosIndex.remove(Integer.valueOf(which));
                })
                .setPositiveButton("Siguiente", (dialog, which) -> {
                    if (elegidosIndex.isEmpty()) {
                        Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Collections.sort(elegidosIndex);
                    StringBuilder sb = new StringBuilder();
                    ArrayList<Integer> diasFinales = new ArrayList<>();
                    for (int i : elegidosIndex) {
                        sb.append(dias[i].substring(0, 2)).append(",");
                        diasFinales.add(diasCalendar[i]);
                    }
                    String diasStr = sb.substring(0, sb.length() - 1);
                    seleccionarHoras(medicamento, diasStr, diasFinales);
                })
                .setNegativeButton("Atrás", null)
                .show();
    }

    private void seleccionarHoras(final Medicamento medicamento, String frecuenciaLabel, ArrayList<Integer> diasCalendar) {
        ArrayList<String> horasLista = new ArrayList<>();
        ArrayList<Integer[]> horasMinutos = new ArrayList<>();
        mostrarTimePicker(medicamento, frecuenciaLabel, diasCalendar, horasLista, horasMinutos);
    }

    private void mostrarTimePicker(Medicamento med, String freqLabel, ArrayList<Integer> dias, ArrayList<String> horasStr, ArrayList<Integer[]> horasMin) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            horasStr.add(horaFormateada);
            horasMin.add(new Integer[]{hourOfDay, minute});
            
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Hora añadida: " + horaFormateada)
                    .setMessage("¿Quieres añadir otra hora de toma?")
                    .setPositiveButton("Sí", (dialog, which) -> mostrarTimePicker(med, freqLabel, dias, horasStr, horasMin))
                    .setNegativeButton("No, terminar", (dialog, which) -> {
                        guardarDosisYAlarmas(med, freqLabel, dias, horasStr, horasMin);
                    })
                    .show();
            
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void guardarDosisYAlarmas(Medicamento med, String freqLabel, ArrayList<Integer> dias, ArrayList<String> horasStr, ArrayList<Integer[]> horasMin) {
        Collections.sort(horasStr);
        String dosisFinal = freqLabel + " a las " + horasStr.toString().replace("[", "").replace("]", "");
        
        pastilleroDAO.actualizarPastillero(userId, med.getId(), null, dosisFinal);
        
        programarAlarmas(med.getNombre(), dias, horasMin);
        
        cargarMedicamentos();
        Toast.makeText(this, "Horario y alarmas configuradas", Toast.LENGTH_SHORT).show();
    }

    private void programarAlarmas(String medNombre, ArrayList<Integer> dias, ArrayList<Integer[]> horasMin) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        for (Integer[] hm : horasMin) {
            if (dias == null) { // Todos los días
                scheduleAlarm(alarmManager, medNombre, -1, hm[0], hm[1]);
            } else { // Días específicos
                for (Integer diaSemana : dias) {
                    scheduleAlarm(alarmManager, medNombre, diaSemana, hm[0], hm[1]);
                }
            }
        }
    }

    private void scheduleAlarm(AlarmManager am, String medNombre, int diaSemana, int hora, int min) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("MED_NAME", medNombre);
        
        // ID único para cada alarma basado en nombre y tiempo para no sobreescribir
        int requestCode = (medNombre + diaSemana + hora + min).hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);

        if (diaSemana != -1) {
            calendar.set(Calendar.DAY_OF_WEEK, diaSemana);
        }

        // Si la hora ya pasó hoy, programar para mañana o la próxima semana
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            if (diaSemana == -1) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }
        }

        if (diaSemana == -1) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
        }
    }

    private void mostrarDatePicker(final Medicamento medicamento) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String nuevaFecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                    pastilleroDAO.actualizarPastillero(userId, medicamento.getId(), nuevaFecha, null);
                    cargarMedicamentos();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void confirmarEliminacion(final Medicamento medicamento) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar " + medicamento.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    pastilleroDAO.eliminarMedicamentoDeUsuario(userId, medicamento.getId());
                    cargarMedicamentos();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pastilleroDAO != null) pastilleroDAO.close();
    }
}
