package com.example.farmacia;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmacia.dao.UserDAO;
import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private MaterialButton btnRegisterUser;
    private MaterialButton btnGoToLogin;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etNewUsername);
        etPassword = findViewById(R.id.etNewPassword);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        userDAO = new UserDAO(this);
        userDAO.open();

        btnRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userDAO.userExists(username)) {
                    Toast.makeText(RegisterActivity.this, "El nombre de usuario ya existe", Toast.LENGTH_SHORT).show();
                    return;
                }

                long result = userDAO.registerUser(username, password, false);
                if (result != -1) {
                    Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the login screen
                } else {
                    Toast.makeText(RegisterActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener for the new button to go back to login
        btnGoToLogin.setOnClickListener(v -> {
            finish(); // Simply close this activity to go back
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) {
            userDAO.close();
        }
    }
}
