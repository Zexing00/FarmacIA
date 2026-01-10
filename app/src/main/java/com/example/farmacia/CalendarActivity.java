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
    private final String[] weekDaysLong = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private final String[] daysAbrev = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"}; // Se mantiene para la lógica de BD
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

        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
        if (index < 0) index = 6;
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
        for (Button btn : dayButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btn.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        }
        dayButtons[index].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_blue)));
        dayButtons[index].setTextColor(Color.WHITE);

        tvSelectedDay.setText("Takes for " + weekDaysLong[index]);
        loadTakesForDay(daysAbrev[index]);
    }

    private void loadTakesForDay(String dayAbrev) {
        List<Medication> allMedications = pillboxDAO.getMedicationsByUserId(userId);
        List<Take> takesToday = new ArrayList<>();

        for (Medication m : allMedications) {
            String weeklyDose = m.getWeeklyDose();
            if (weeklyDose == null || !weeklyDose.contains("a las ")) continue;

            if (weeklyDose.contains("Todos los días") || weeklyDose.contains(dayAbrev)) {
                String hoursStr = weeklyDose.split("a las ")[1];
                String[] hoursArr = hoursStr.split(", ");
                for (String h : hoursArr) {
                    takesToday.add(new Take(h.trim(), m.getName()));
                }
            }
        }

        Collections.sort(takesToday, (t1, t2) -> t1.hour.compareTo(t2.hour));
        rvDaySchedule.setAdapter(new TakeAdapter(takesToday));
    }

    private static class Take {
        String hour, name;
        Take(String h, String n) { this.hour = h; this.name = n; }
    }

    private static class TakeAdapter extends RecyclerView.Adapter<TakeAdapter.ViewHolder> {
        private final List<Take> takes;
        TakeAdapter(List<Take> takes) { this.takes = takes; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toma, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Take t = takes.get(position);
            holder.tvHour.setText(t.hour);
            holder.tvName.setText(t.name);
        }

        @Override
        public int getItemCount() { return takes.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHour, tvName;
            ViewHolder(View v) {
                super(v);
                tvHour = v.findViewById(R.id.tvTomaHora);
                tvName = v.findViewById(R.id.tvTomaNombre);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pillboxDAO != null) {
            pillboxDAO.close();
        }
    }
}
