package com.example.farmacia;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.adapter.ChatAdapter;
import com.example.farmacia.dao.PastilleroDAO;
import com.example.farmacia.model.ChatMessage;
import com.example.farmacia.model.Medicamento;
import com.example.farmacia.network.GeminiApiClient;
import com.example.farmacia.network.GeminiRequest;
import com.example.farmacia.network.GeminiResponse;
import com.example.farmacia.network.GeminiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IAActivity extends AppCompatActivity {

    private RecyclerView rvChat;

    private View panelInput;
    private EditText etInput;
    private Button btnSend;

    private Button btnPickMed;

    private View panelOptions;
    private Button btnOptAlergias;
    private Button btnOptAlimentosAlcohol;
    private Button btnOptInteracciones;

    private View panelSatisfaction;
    private Button btnYes;
    private Button btnNo;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private PastilleroDAO pastilleroDAO;

    private final List<Medicamento> meds = new ArrayList<>();
    private Medicamento selectedMed = null;

    private int currentUserId = -1;

    private enum UiState { NEED_MED, MENU, WAITING_AI, SATISFACTION }
    private UiState state = UiState.NEED_MED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ia);

        rvChat = findViewById(R.id.rvChat);

        panelInput = findViewById(R.id.panelInput);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);

        btnPickMed = findViewById(R.id.btnPickMed);

        panelOptions = findViewById(R.id.panelOptions);
        btnOptAlergias = findViewById(R.id.btnOptAlergias);
        btnOptAlimentosAlcohol = findViewById(R.id.btnOptAlimentosAlcohol);
        btnOptInteracciones = findViewById(R.id.btnOptInteracciones);

        panelSatisfaction = findViewById(R.id.panelSatisfaction);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);

        adapter = new ChatAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(adapter);

        panelInput.setVisibility(View.GONE);
        btnSend.setEnabled(false);
        etInput.setEnabled(false);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        pastilleroDAO = new PastilleroDAO(this);

        btnPickMed.setEnabled(false);

        addBot("Hola 👋 Soy tu asistente. Para empezar, selecciona un medicamento del pastillero.");
        addBot("⚠️ Aviso importante: Soy un asistente que te ayuda a entender tu medicación, pero no puedo sustituir a un médico real. " +
                "Si tienes síntomas, dudas importantes o no te sientes bien, pide ayuda a un profesional sanitario o a alguien de confianza.");


        loadUserMeds();

        btnPickMed.setOnClickListener(v -> openPickMedicationDialog());

        btnOptAlergias.setOnClickListener(v -> onOptionAlergias());
        btnOptAlimentosAlcohol.setOnClickListener(v -> onOptionAlimentosAlcohol());
        btnOptInteracciones.setOnClickListener(v -> onOptionInteracciones());

        btnYes.setOnClickListener(v -> {
            addUser("Sí");
            addBot("¡Gracias! 😊 Elige otra opción cuando quieras.");
            showMenu();
        });

        btnNo.setOnClickListener(v -> {
            addUser("No");
            addBot("Entendido. Prueba otra opción o cambia de medicamento.");
            showMenu();
        });

        showNeedMed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserMeds();
    }

    private void showNeedMed() {
        state = UiState.NEED_MED;
        panelOptions.setVisibility(View.VISIBLE);
        panelSatisfaction.setVisibility(View.GONE);
        btnOptAlergias.setEnabled(false);
        btnOptAlimentosAlcohol.setEnabled(false);
        btnOptInteracciones.setEnabled(false);
    }

    private void showMenu() {
        if (selectedMed == null) {
            showNeedMed();
            return;
        }
        state = UiState.MENU;
        panelOptions.setVisibility(View.VISIBLE);
        panelSatisfaction.setVisibility(View.GONE);
        btnOptAlergias.setEnabled(true);
        btnOptAlimentosAlcohol.setEnabled(true);
        btnOptInteracciones.setEnabled(true);
        btnPickMed.setEnabled(true);
    }

    private void showWaitingAI() {
        state = UiState.WAITING_AI;
        panelOptions.setVisibility(View.GONE);
        panelSatisfaction.setVisibility(View.GONE);
        btnPickMed.setEnabled(false);
    }

    private void showSatisfaction() {
        state = UiState.SATISFACTION;
        panelOptions.setVisibility(View.GONE);
        panelSatisfaction.setVisibility(View.VISIBLE);
        btnPickMed.setEnabled(true);
    }

    private void addUser(String text) {
        messages.add(new ChatMessage(ChatMessage.SENDER_USER, text));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private int addBot(String text) {
        messages.add(new ChatMessage(ChatMessage.SENDER_BOT, text));
        int idx = messages.size() - 1;
        adapter.notifyItemInserted(idx);
        scrollToBottom();
        return idx;
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) rvChat.scrollToPosition(messages.size() - 1);
    }

    private void loadUserMeds() {
        btnPickMed.setEnabled(false);

        if (currentUserId == -1) {
            addBot("No tengo el usuario actual (USER_ID).");
            return;
        }

        dbExecutor.execute(() -> {
            try {
                pastilleroDAO.open();
                List<Medicamento> list = pastilleroDAO.obtenerMedicamentosPorUsuario(currentUserId);
                pastilleroDAO.close();

                runOnUiThread(() -> {
                    meds.clear();
                    if (list != null) meds.addAll(list);

                    if (meds.isEmpty()) {
                        addBot("Tu pastillero está vacío.");
                        btnPickMed.setEnabled(true);
                        showNeedMed();
                    } else {
                        btnPickMed.setEnabled(true);
                        showNeedMed();
                    }
                });
            } catch (Exception e) {
                try { pastilleroDAO.close(); } catch (Exception ignored) {}
                runOnUiThread(() -> {
                    addBot("Error cargando pastillero: " + e.getMessage());
                    btnPickMed.setEnabled(true);
                    showNeedMed();
                });
            }
        });
    }

    private void openPickMedicationDialog() {
        if (state == UiState.WAITING_AI) return;

        if (meds.isEmpty()) {
            addBot("No hay medicamentos cargados del pastillero.");
            return;
        }

        String[] names = new String[meds.size()];
        for (int i = 0; i < meds.size(); i++) names[i] = meds.get(i).getNombre();

        new AlertDialog.Builder(this)
                .setTitle("Selecciona un medicamento")
                .setItems(names, (dialog, which) -> {
                    selectedMed = meds.get(which);
                    addBot("✅ Medicamento seleccionado: " + selectedMed.getNombre());
                    showMenu();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean ensureMainMedSelected() {
        if (selectedMed == null) {
            addBot("Primero selecciona un medicamento del pastillero.");
            showNeedMed();
            return false;
        }
        return true;
    }

    private void onOptionAlergias() {
        if (!ensureMainMedSelected()) return;

        addUser("Alergias / contraindicaciones");
        sendPromptToAI(buildPromptAlergias(selectedMed.getNombre()));
    }

    private void onOptionAlimentosAlcohol() {
        if (!ensureMainMedSelected()) return;

        addUser("Compatibilidad con alimentos o alcohol");
        sendPromptToAI(buildPromptAlimentosAlcohol(selectedMed.getNombre()));
    }

    private void onOptionInteracciones() {
        if (!ensureMainMedSelected()) return;

        if (meds.size() < 2) {
            addBot("Necesitas al menos 2 medicamentos en tu pastillero para comprobar interacciones.");
            return;
        }

        List<Medicamento> candidates = new ArrayList<>();
        for (Medicamento m : meds) {
            if (m.getId() != selectedMed.getId()) candidates.add(m);
        }

        if (candidates.isEmpty()) {
            addBot("No hay otro medicamento disponible distinto al seleccionado.");
            return;
        }

        String[] names = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) names[i] = candidates.get(i).getNombre();

        new AlertDialog.Builder(this)
                .setTitle("Selecciona el otro medicamento")
                .setItems(names, (dialog, which) -> {
                    Medicamento other = candidates.get(which);
                    addUser("Compatibilidad con otro medicamento");
                    addBot("Comparando: " + selectedMed.getNombre() + " + " + other.getNombre());
                    sendPromptToAI(buildPromptInteracciones(selectedMed.getNombre(), other.getNombre()));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String buildPromptAlergias(String medName) {
        return "Eres un médico cercano y amable. Responde en español.\n\n" +
                "Medicamento: " + medName + "\n\n" +
                "Da la respuesta directa, sencilla, sin tecnicismos.\n" +
                "Habla solo de alergias posibles, efectos secundarios importantes y señales para pedir ayuda.\n" +
                "No uses asteriscos dobles ** porque no se ven en el chat.\n" +
                "Usa 4 a 6 viñetas cortas y claras.";
    }

    private String buildPromptAlimentosAlcohol(String medName) {
        return "Eres un médico cercano y amable. Responde en español.\n\n" +
                "Medicamento: " + medName + "\n\n" +
                "Da la respuesta directa, sencilla, sin tecnicismos.\n" +
                "Habla solo de alimentos que puedan dar problema (si los hay) y alcohol (si se puede o no) y consejos simples.\n" +
                "Si no hay alimentos problemáticos, di: 'No hay alimentos importantes que den problema'.\n" +
                "No uses asteriscos dobles ** porque no se ven en el chat.\n" +
                "Usa 4 a 6 viñetas cortas y claras.";
    }

    private String buildPromptInteracciones(String med1, String med2) {
        return "Eres un médico cercano y amable. Responde en español.\n\n" +
                "Medicamento 1: " + med1 + "\n" +
                "Medicamento 2: " + med2 + "\n\n" +
                "Da la respuesta directa, sencilla, sin tecnicismos.\n" +
                "Habla solo de si se pueden tomar juntos o no, el riesgo (bajo/medio/alto) y qué hacer de forma simple.\n" +
                "Si no hay problema conocido, di: 'No se conocen problemas importantes si se toman juntos'.\n" +
                "No uses asteriscos dobles ** porque no se ven en el chat.\n" +
                "Usa 4 a 6 viñetas cortas y claras.";
    }

    private void sendPromptToAI(String prompt) {
        int typingIndex = addBot("Escribiendo...");

        GeminiService service = GeminiApiClient.getGeminiService();

        GeminiRequest request = new GeminiRequest(prompt);

        Call<GeminiResponse> call = service.generateContent("gemini-2.5-flash", request);

        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        int code = response.code();
                        String detail = "";
                        try { if (response.errorBody() != null) detail = response.errorBody().string(); } catch (Exception ignored) {}

                        if (code == 429) {
                            messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT,
                                    "Estoy recibiendo muchas consultas seguidas. Espera 10 segundos y prueba otra vez."));
                            adapter.notifyItemChanged(typingIndex);
                            scrollToBottom();

                            rvChat.postDelayed(() -> IAActivity.this.showMenu(), 10000);
                            return;
                        }

                        messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT,
                                "Error: HTTP " + code + (detail.isEmpty() ? "" : "\n" + detail)));
                        adapter.notifyItemChanged(typingIndex);
                        scrollToBottom();
                        addBot("Vuelve a elegir una opción.");
                        showMenu();
                        return;
                    }


                    String text = response.body().getFirstTextSafe();
                    if (text == null || text.trim().isEmpty()) {
                        messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT,
                                "No he podido obtener una respuesta clara."));
                        adapter.notifyItemChanged(typingIndex);
                        showMenu();
                        return;
                    }

                    messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT, text));
                    adapter.notifyItemChanged(typingIndex);
                    scrollToBottom();
                    showSatisfaction();
                });
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT,
                            "Error de conexión. Revisa tu internet o espera un momento."));
                    adapter.notifyItemChanged(typingIndex);
                    scrollToBottom();
                    showMenu();
                });
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
        try { pastilleroDAO.close(); } catch (Exception ignored) {}
    }
}
