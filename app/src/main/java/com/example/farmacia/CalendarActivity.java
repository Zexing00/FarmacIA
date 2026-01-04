package com.example.farmacia;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.model.Medication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView rvDaySchedule;
    private PillboxDAO pillboxDAO;
    private int userId;
    private TextView tvSelectedDay;
    private final String[] longWeekDays = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private final String[] shortWeekDays = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
    private Button[] dayButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        userId = getIntent().getIntExtra("USER_ID", -1);
        pillboxDAO = new PillboxDAO(this);
        pillboxDAO.open();

        findViewById(R.id.btnCalendarBack).setOnClickListener(v -> finish());
        tvSelectedDay = findViewById(R.id.tvSelectedDay);
        rvDaySchedule = findViewById(R.id.rvDaySchedule);
        rvDaySchedule.setLayoutManager(new LinearLayoutManager(this));

        setupDayButtons();

        // Select current day
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
        selectDay(index);
    }

    private void setupDayButtons() {
        int[] ids = {R.id.btnLu, R.id.btnMa, R.id.btnMi, R.id.btnJu, R.id.btnVi, R.id.btnSa, R.id.btnDo};
        dayButtons = new Button[ids.length];
        for (int i = 0; i < ids.length; i++) {
            dayButtons[i] = findViewById(ids[i]);
            final int index = i;
            dayButtons[i].setOnClickListener(v -> selectDay(index));
        }
    }

    private void selectDay(int index) {
        // Reset button styles
        for (Button btn : dayButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btn.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        }
        // Highlight selected
        dayButtons[index].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_blue)));
        dayButtons[index].setTextColor(Color.WHITE);

        tvSelectedDay.setText("Tomas para el " + longWeekDays[index]);
        loadDosesForDay(shortWeekDays[index]);
    }

    private void loadDosesForDay(String dayAbbrev) {
        List<Medication> allMedications = pillboxDAO.getMedicationsByUserId(userId);
        List<Dose> dosesForToday = new ArrayList<>();

        for (Medication m : allMedications) {
            String doseInfo = m.getWeeklyDose();
            if (doseInfo == null || !doseInfo.contains("a las ")) continue;

            if (doseInfo.contains("Todos los días") || doseInfo.contains(dayAbbrev)) {
                String hoursStr = doseInfo.split("a las ")[1];
                String[] hoursArr = hoursStr.split(", ");
                for (String h : hoursArr) {
                    dosesForToday.add(new Dose(h.trim(), m.getName()));
                }
            }
        }

        Collections.sort(dosesForToday, (d1, d2) -> d1.time.compareTo(d2.time));
        rvDaySchedule.setAdapter(new DoseAdapter(dosesForToday));
    }

    private static class Dose {
        String time, name;
        Dose(String t, String n) { this.time = t; this.name = n; }
    }

    private static class DoseAdapter extends RecyclerView.Adapter<DoseAdapter.ViewHolder> {
        private final List<Dose> doses;
        DoseAdapter(List<Dose> doses) { this.doses = doses; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toma, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Dose d = doses.get(position);
            holder.tvTime.setText(d.time);
            holder.tvName.setText(d.name);
        }

        @Override
        public int getItemCount() { return doses.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime, tvName;
            ViewHolder(View v) {
                super(v);
                tvTime = v.findViewById(R.id.tvTomaHora);
                tvName = v.findViewById(R.id.tvTomaNombre);
            }
        }
    }
}
