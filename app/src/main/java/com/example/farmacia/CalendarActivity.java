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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView rvDaySchedule;
    private int userId;
    private TextView tvSelectedDay;
    private Button[] dayButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        userId = getIntent().getIntExtra("USER_ID", -1);

        findViewById(R.id.btnCalendarBack).setOnClickListener(v -> finish());
        tvSelectedDay = findViewById(R.id.tvSelectedDay);
        rvDaySchedule = findViewById(R.id.rvDaySchedule);
        rvDaySchedule.setLayoutManager(new LinearLayoutManager(this));

        setupDayButtons();

        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
    }

    private void setupDayButtons() {
        int[] ids = {R.id.btnLu, R.id.btnMa, R.id.btnMi, R.id.btnJu, R.id.btnVi, R.id.btnSa, R.id.btnDo};
        dayButtons = new Button[ids.length];
        for (int i = 0; i < ids.length; i++) {
            dayButtons[i] = findViewById(ids[i]);
            final int index = i;
        }
    }

        for (Button btn : dayButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btn.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        }
        dayButtons[index].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_blue)));
        dayButtons[index].setTextColor(Color.WHITE);

    }



                }
            }
        }

    }

    }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toma, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        }

        @Override

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View v) {
                super(v);
            }
        }
    }
}
