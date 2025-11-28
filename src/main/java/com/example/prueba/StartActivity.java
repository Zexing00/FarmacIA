package com.example.prueba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        SharedPreferences prefs = getSharedPreferences("LoginData", MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean("LOGGED_IN", false);

        if (loggedIn) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
            return;
        }

        Button btnLogin = findViewById(R.id.btnStartLogin);
        Button btnRegister = findViewById(R.id.btnStartRegister);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(StartActivity.this, LoginActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(StartActivity.this, RegisterActivity.class)));
    }
}
