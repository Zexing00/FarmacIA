package com.example.prueba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText editUser, editPass;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUser = findViewById(R.id.editUsernameLogin);
        editPass = findViewById(R.id.editPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);

        SharedPreferences prefs = getSharedPreferences("LoginData", MODE_PRIVATE);

        btnLogin.setOnClickListener(v -> {

            String user = editUser.getText().toString();
            String pass = editPass.getText().toString();

            String savedUser = prefs.getString("USER", "");
            String savedPass = prefs.getString("PASS", "");

            if (user.equals(savedUser) && pass.equals(savedPass)) {

                // Guardar que la sesión está iniciada
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("LOGGED_IN", true);
                editor.apply();

                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();

            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
