package com.example.farmacia;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.adapter.CimaAdapter;
import com.example.farmacia.model.cima.CimaResponse;
import com.example.farmacia.network.ApiClient;
import com.example.farmacia.network.CimaService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMedicinesActivity extends AppCompatActivity {

    private EditText etSearchQuery;
    private ImageButton btnSearch; // Cambiado de Button a ImageButton
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private CimaService cimaService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_medicines);

        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnSearch = findViewById(R.id.btnSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        progressBar = findViewById(R.id.progressBar);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        // Recuperar ID de usuario
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getIntExtra("USER_ID", -1);
        }

        cimaService = ApiClient.getCimaService();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        etSearchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Introduce un nombre para buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvSearchResults.setAdapter(null);

        cimaService.buscarMedicamentos(query).enqueue(new Callback<CimaResponse>() {
            @Override
            public void onResponse(Call<CimaResponse> call, Response<CimaResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    CimaResponse cimaResponse = response.body();
                    if (cimaResponse.getResultados() != null && !cimaResponse.getResultados().isEmpty()) {
                        CimaAdapter adapter = new CimaAdapter(SearchMedicinesActivity.this, cimaResponse.getResultados(), userId);
                        rvSearchResults.setAdapter(adapter);
                    } else {
                        Toast.makeText(SearchMedicinesActivity.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SearchMedicinesActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CimaResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchMedicinesActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
