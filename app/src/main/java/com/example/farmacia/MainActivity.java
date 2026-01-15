package com.example.farmacia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.farmacia.dao.UserDAO;
import com.example.farmacia.model.User;
import com.example.farmacia.util.AlarmScheduler;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoToRegister;
    private UserDAO userDAO;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        userDAO = new UserDAO(this);
        userDAO.open();

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform DB operations in the background
        executor.execute(() -> {
            User user = userDAO.login(username, password);

            runOnUiThread(() -> {
                if (user != null) {
                    // --- Actions on successful login ---
                    // 1. Save user info to SharedPreferences for BootReceiver and other components
                    SharedPreferences prefs = getSharedPreferences("FarmacIAPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("LAST_USER_ID", user.getId());
                    editor.putString("USER_NAME_" + user.getId(), user.getUsername());
                    editor.apply();

                    // 2. Schedule all alarms for this user
                    AlarmScheduler.scheduleAllAlarmsForUser(this, user.getId(), user.getUsername());

                    // 3. Proceed to HomeActivity
                    Toast.makeText(MainActivity.this, "¡Hola, " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra("USER_NAME", user.getUsername());
                    intent.putExtra("USER_ID", user.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) userDAO.close();
        executor.shutdown();
    }
}
