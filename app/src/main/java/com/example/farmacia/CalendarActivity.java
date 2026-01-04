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

import com.example.farmacia.dao.PastilleroDAO;
import com.example.farmacia.model.Medicamento;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView rvDaySchedule;
    private PastilleroDAO pastilleroDAO;
    private int userId;
    private TextView tvSelectedDay;
    private final String[] diasSemanaLong = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private final String[] diasAbrev = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
    private Button[] dayButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        userId = getIntent().getIntExtra("USER_ID", -1);
        pastilleroDAO = new PastilleroDAO(this);
        pastilleroDAO.open();

        findViewById(R.id.btnCalendarBack).setOnClickListener(v -> finish());
        tvSelectedDay = findViewById(R.id.tvSelectedDay);
        rvDaySchedule = findViewById(R.id.rvDaySchedule);
        rvDaySchedule.setLayoutManager(new LinearLayoutManager(this));

        setupDayButtons();

        // Seleccionar día actual
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
        seleccionarDia(index);
    }

    private void setupDayButtons() {
        int[] ids = {R.id.btnLu, R.id.btnMa, R.id.btnMi, R.id.btnJu, R.id.btnVi, R.id.btnSa, R.id.btnDo};
        dayButtons = new Button[ids.length];
        for (int i = 0; i < ids.length; i++) {
            dayButtons[i] = findViewById(ids[i]);
            final int index = i;
            dayButtons[i].setOnClickListener(v -> seleccionarDia(index));
        }
    }

    private void seleccionarDia(int index) {
        // Resetear estilos de botones
        for (Button btn : dayButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btn.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        }
        // Resaltar seleccionado
        dayButtons[index].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_blue)));
        dayButtons[index].setTextColor(Color.WHITE);

        tvSelectedDay.setText("Tomas para el " + diasSemanaLong[index]);
        cargarTomasDia(diasAbrev[index]);
    }

    private void cargarTomasDia(String diaAbrev) {
        List<Medicamento> todos = pastilleroDAO.obtenerMedicamentosPorUsuario(userId);
        List<Toma> tomasHoy = new ArrayList<>();

        for (Medicamento m : todos) {
            String dosis = m.getDosisSemanal();
            if (dosis == null || !dosis.contains("a las ")) continue;

            if (dosis.contains("Todos los días") || dosis.contains(diaAbrev)) {
                String horasStr = dosis.split("a las ")[1];
                String[] horasArr = horasStr.split(", ");
                for (String h : horasArr) {
                    tomasHoy.add(new Toma(h.trim(), m.getNombre()));
                }
            }
        }

        Collections.sort(tomasHoy, (t1, t2) -> t1.hora.compareTo(t2.hora));
        rvDaySchedule.setAdapter(new TomaAdapter(tomasHoy));
    }

    private static class Toma {
        String hora, nombre;
        Toma(String h, String n) { this.hora = h; this.nombre = n; }
    }

    private static class TomaAdapter extends RecyclerView.Adapter<TomaAdapter.ViewHolder> {
        private final List<Toma> tomas;
        TomaAdapter(List<Toma> tomas) { this.tomas = tomas; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toma, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Toma t = tomas.get(position);
            holder.tvHora.setText(t.hora);
            holder.tvNombre.setText(t.nombre);
        }

        @Override
        public int getItemCount() { return tomas.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHora, tvNombre;
            ViewHolder(View v) {
                super(v);
                tvHora = v.findViewById(R.id.tvTomaHora);
                tvNombre = v.findViewById(R.id.tvTomaNombre);
            }
        }
    }
}
