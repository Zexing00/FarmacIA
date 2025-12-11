package com.example.farmacia;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmacia.dao.UsuarioDAO;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnRegister;
    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etNewUsername);
        etPassword = findViewById(R.id.etNewPassword);
        btnRegister = findViewById(R.id.btnRegisterUser);

        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (usuarioDAO.existeUsuario(username)) {
                    Toast.makeText(RegisterActivity.this, "El nombre de usuario ya existe", Toast.LENGTH_SHORT).show();
                    return;
                }

                long result = usuarioDAO.registrarUsuario(username, password, false);
                if (result != -1) {
                    Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Vuelve a la pantalla de login
                } else {
                    Toast.makeText(RegisterActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }
}
