package com.example.farmacia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.adapter.CimaAdapter;
import com.example.farmacia.model.cima.CimaResponse;
import com.example.farmacia.network.ApiClient;
import com.example.farmacia.network.CimaApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMedicinesActivity extends AppCompatActivity {

    private EditText etSearchQuery;
    private ImageButton btnSearch;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private CimaApiService cimaApiService;
    private int userId;
    private ImageButton btnSearchBack;

    // --- Variables para la búsqueda en tiempo real (debouncing) ---
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500; // 500ms de espera
    private static final int MIN_QUERY_LENGTH = 3; // Mínimo 3 caracteres para buscar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_medicines);

        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnSearch = findViewById(R.id.btnSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        progressBar = findViewById(R.id.progressBar);
        btnSearchBack = findViewById(R.id.btnSearchBack);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getIntExtra("USER_ID", -1);
        }

        cimaApiService = ApiClient.getCimaApiService();

        // Botón de búsqueda manual (sigue funcionando)
        btnSearch.setOnClickListener(v -> performSearch());

        // Botón para volver atrás
        btnSearchBack.setOnClickListener(v -> finish());

        // Búsqueda al pulsar "Intro" en el teclado
        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // --- Configuración de la búsqueda en tiempo real ---
        setupRealtimeSearch();
    }

    private void setupRealtimeSearch() {
        searchRunnable = this::performSearch; // La tarea a ejecutar es la búsqueda

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cada vez que el texto cambia, cancelamos la búsqueda anterior
                searchHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= MIN_QUERY_LENGTH) {
                    // Si el texto es suficientemente largo, programamos una nueva búsqueda
                    searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
                } else {
                    // Si es muy corto, limpiamos los resultados
                    rvSearchResults.setAdapter(null);
                }
            }
        });
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();
        if (query.isEmpty()) {
            // Ya no mostramos Toast para no ser molestos, simplemente no buscamos
            return;
        }

        // Cancelamos cualquier búsqueda automática pendiente para que no se pisen
        searchHandler.removeCallbacks(searchRunnable);

        progressBar.setVisibility(View.VISIBLE);
        if (rvSearchResults.getAdapter() == null) {
            rvSearchResults.setAdapter(null); // Limpiamos para que se vea el ProgressBar
        }

        cimaApiService.searchMedicationsByName(query).enqueue(new Callback<CimaResponse>() {
            @Override
            public void onResponse(Call<CimaResponse> call, Response<CimaResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    CimaResponse cimaResponse = response.body();
                    if (cimaResponse.getResults() != null && !cimaResponse.getResults().isEmpty()) {
                        CimaAdapter adapter = new CimaAdapter(SearchMedicinesActivity.this, cimaResponse.getResults(), userId);
                        rvSearchResults.setAdapter(adapter);
                    } else {
                        // Si la búsqueda no da resultados, lo indicamos
                        rvSearchResults.setAdapter(null);
                        Toast.makeText(SearchMedicinesActivity.this, "No se encontraron medicamentos", Toast.LENGTH_SHORT).show();
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
