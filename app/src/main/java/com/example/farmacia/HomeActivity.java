package com.example.farmacia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnPillbox;
    private Button btnMedications;
    private String userName;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnPillbox = findViewById(R.id.btnPillbox);
        btnMedications = findViewById(R.id.btnMedications);

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
                Toast.makeText(HomeActivity.this, "Abriendo Pastillero...", Toast.LENGTH_SHORT).show();
                // TODO: Navegar a la actividad de Pastillero
            }
        });

        btnMedications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Abriendo Medicamentos...", Toast.LENGTH_SHORT).show();
                // TODO: Navegar a la actividad de Medicamentos
            }
        });
    }
}
