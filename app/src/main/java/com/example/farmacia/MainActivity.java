package com.example.farmacia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmacia.dao.UserDAO;
import com.example.farmacia.model.Administrator;
import com.example.farmacia.model.User;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnGoToRegister;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        userDAO = new UserDAO(this);
        userDAO.open();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    User user = userDAO.login(username, password);
                    if (user != null) {
                        String userType = (user instanceof Administrator) ? "Administrador" : "Usuario";
                        Toast.makeText(MainActivity.this, "Bienvenido " + userType + ": " + user.getUsername(), Toast.LENGTH_LONG).show();
                        
                        // Start HomeActivity
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra("USER_NAME", user.getUsername());
                        intent.putExtra("USER_ID", user.getId());
                        startActivity(intent);
                        finish(); // Optional: Close MainActivity so the back button doesn't return to login
                    } else {
                        Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
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
