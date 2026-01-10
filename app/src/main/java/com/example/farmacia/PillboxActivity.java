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

import com.example.farmacia.adapter.MedicationAdapter;
import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.model.Medication;
import com.example.farmacia.receiver.AlarmReceiver;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PillboxActivity extends AppCompatActivity {

    private RecyclerView rvMedications;
    private PillboxDAO pillboxDAO;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pillbox);

        requestNotificationPermissions();

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

        pillboxDAO = new PillboxDAO(this);
        pillboxDAO.open();

        loadMedications();
    }

    private void requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void loadMedications() {
        List<Medication> medicationList = pillboxDAO.getMedicationsByUserId(userId);
        MedicationAdapter adapter = new MedicationAdapter(medicationList, this::showMedicationOptions);
        rvMedications.setAdapter(adapter);
    }

    private void showMedicationOptions(final Medication medication) {
        String[] options = {"Añadir Fecha de Caducidad", "Configurar Dosis/Horario", "Eliminar del Pastillero"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(medication.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: showDatePicker(medication); break;
                        case 1: configureDose(medication); break;
                        case 2: confirmDeletion(medication); break;
                    }
                })
                .show();
    }

    private void configureDose(final Medication medication) {
        String[] doseTypes = {"Dosis Diaria", "Días Específicos"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Frecuencia de toma")
                .setItems(doseTypes, (dialog, which) -> {
                    if (which == 0) {
                        selectHours(medication, "Todos los días", null);
                    } else {
                        selectDays(medication);
                    }
                })
                .show();
    }

    private void selectDays(final Medication medication) {
        final String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        final String[] daysAbrev = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
        int[] calendarDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
        boolean[] selected = new boolean[days.length];
        ArrayList<Integer> chosenIndices = new ArrayList<>();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Selecciona los días")
                .setMultiChoiceItems(days, selected, (dialog, which, isChecked) -> {
                    if (isChecked) chosenIndices.add(which);
                    else chosenIndices.remove(Integer.valueOf(which));
                })
                .setPositiveButton("Siguiente", (dialog, which) -> {
                    if (chosenIndices.isEmpty()) {
                        Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Collections.sort(chosenIndices);
                    ArrayList<String> chosenDaysAbbr = new ArrayList<>();
                    ArrayList<Integer> finalDays = new ArrayList<>();
                    for (int i : chosenIndices) {
                        chosenDaysAbbr.add(daysAbrev[i]);
                        finalDays.add(calendarDays[i]);
                    }
                    String daysStr = String.join(",", chosenDaysAbbr);
                    selectHours(medication, daysStr, finalDays);
                })
                .setNegativeButton("Atrás", null)
                .show();
    }

    private void selectHours(final Medication medication, String frequencyLabel, ArrayList<Integer> calendarDays) {
        ArrayList<String> hoursList = new ArrayList<>();
        ArrayList<Integer[]> hoursMinutesList = new ArrayList<>();
        showTimePicker(medication, frequencyLabel, calendarDays, hoursList, hoursMinutesList);
    }

    private void showTimePicker(Medication med, String freqLabel, ArrayList<Integer> days, ArrayList<String> hoursStr, ArrayList<Integer[]> hoursMin) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            hoursStr.add(formattedTime);
            hoursMin.add(new Integer[]{hourOfDay, minute});

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Hora añadida: " + formattedTime)
                    .setMessage("¿Quieres añadir otra hora de toma?")
                    .setPositiveButton("Sí", (dialog, which) -> showTimePicker(med, freqLabel, days, hoursStr, hoursMin))
                    .setNegativeButton("No, terminar", (dialog, which) -> {
                        saveDoseAndAlarms(med, freqLabel, days, hoursStr, hoursMin);
                    })
                    .show();

        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void saveDoseAndAlarms(Medication med, String freqLabel, ArrayList<Integer> days, ArrayList<String> hoursStr, ArrayList<Integer[]> hoursMin) {
        Collections.sort(hoursStr);
        String finalDose = freqLabel + " a las " + String.join(", ", hoursStr);

        pillboxDAO.updatePillbox(userId, med.getId(), null, finalDose);

        scheduleAlarms(med.getName(), days, hoursMin);

        loadMedications();
        Toast.makeText(this, "Horario y alarmas configuradas", Toast.LENGTH_SHORT).show();
    }

    private void scheduleAlarms(String medName, ArrayList<Integer> days, ArrayList<Integer[]> hoursMin) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for (Integer[] hm : hoursMin) {
            if (days == null) { // Everyday
                scheduleAlarm(alarmManager, medName, -1, hm[0], hm[1]);
            } else { // Specific days
                for (Integer dayOfWeek : days) {
                    scheduleAlarm(alarmManager, medName, dayOfWeek, hm[0], hm[1]);
                }
            }
        }
    }

    private void scheduleAlarm(AlarmManager am, String medName, int dayOfWeek, int hour, int minute) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("MED_NAME", medName);

        int requestCode = (medName + dayOfWeek + hour + minute).hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (dayOfWeek != -1) {
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        }

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            if (dayOfWeek == -1) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }
        }

        if (dayOfWeek == -1) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
        }
    }

    private void showDatePicker(final Medication medication) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String newDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                    pillboxDAO.updatePillbox(userId, medication.getId(), newDate, null);
                    loadMedications();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void confirmDeletion(final Medication medication) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar " + medication.getName() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    pillboxDAO.removeMedicationFromUser(userId, medication.getId());
                    loadMedications();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pillboxDAO != null) pillboxDAO.close();
    }
}
