package com.example.farmacia;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.adapter.ChatAdapter;
import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.model.ChatMessage;
import com.example.farmacia.model.Medication;
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

    private static final String TAG = "IAActivity";

    private RecyclerView rvChat;

    private View panelInput;
    private EditText etInput;
    private ImageButton btnSend;

    private Button btnPickMed;

    private View panelOptions;
    private Button btnOptAllergies;
    private Button btnOptFoodAlcohol;
    private Button btnOptInteractions;

    private View panelSatisfaction;
    private Button btnYes;
    private Button btnNo;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private PillboxDAO pillboxDAO;

    private final List<Medication> meds = new ArrayList<>();
    private Medication selectedMed = null;

    private int currentUserId = -1;

    private String incomingMode = null;
    private String incomingMedName = null;
    private boolean isSummaryMode = false;

    private static final String SUMMARY_NOTICE =
            "⚠️ Aviso: Este resumen es orientativo y puede no ser tan exacto como el prospecto. " +
                    "Para información fiable consulta el prospecto oficial (CIMA) y, si tienes dudas o síntomas, habla con tu médico o farmacéutico.";


    private enum UiState { NEED_MED, MENU, WAITING_AI, SATISFACTION }
    private UiState state = UiState.NEED_MED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ia);

        ImageButton btnIaBack = findViewById(R.id.btnIaBack);
        btnIaBack.setOnClickListener(v -> finish());

        rvChat = findViewById(R.id.rvChat);

        panelInput = findViewById(R.id.panelInput);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);

        btnPickMed = findViewById(R.id.btnPickMed);

        panelOptions = findViewById(R.id.panelOptions);
        btnOptAllergies = findViewById(R.id.btnOptAlergias);
        btnOptFoodAlcohol = findViewById(R.id.btnOptAlimentosAlcohol);
        btnOptInteractions = findViewById(R.id.btnOptInteracciones);

        panelSatisfaction = findViewById(R.id.panelSatisfaction);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);

        adapter = new ChatAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(adapter);

        panelInput.setVisibility(View.GONE);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);

        incomingMode = getIntent().getStringExtra("IA_MODE");
        incomingMedName = getIntent().getStringExtra("MED_NAME");
        isSummaryMode = "RESUMEN".equals(incomingMode) && incomingMedName != null && !incomingMedName.trim().isEmpty();

        pillboxDAO = new PillboxDAO(this);

        btnPickMed.setEnabled(false);

        if (isSummaryMode) {
            btnPickMed.setVisibility(View.GONE);
            panelOptions.setVisibility(View.GONE);
            panelSatisfaction.setVisibility(View.GONE);
            panelInput.setVisibility(View.GONE);

            addBot("Resumen inteligente");
            addBot(SUMMARY_NOTICE);
            addUser("Hazme un resumen de " + incomingMedName);
            sendPromptToAI(buildSummaryPrompt(incomingMedName));
            return;
        }

        addBot("Hola 👋 Soy tu asistente. Para empezar, selecciona un medicamento del pastillero.");
        addBot("⚠️ Aviso importante: Soy un asistente que te ayuda a entender tu medicación, pero no puedo sustituir a un médico real. " +
                "Si tienes síntomas, dudas importantes o no te sientes bien, pide ayuda a un profesional sanitario o a alguien de confianza.");

        loadUserMeds();

        btnPickMed.setOnClickListener(v -> openPickMedicationDialog());

        btnOptAllergies.setOnClickListener(v -> onAllergiesOption());
        btnOptFoodAlcohol.setOnClickListener(v -> onFoodAlcoholOption());
        btnOptInteractions.setOnClickListener(v -> onInteractionsOption());

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
        if (!isSummaryMode) {
            loadUserMeds();
        }
    }

    private void showNeedMed() {
        state = UiState.NEED_MED;
        panelOptions.setVisibility(View.VISIBLE);
        panelSatisfaction.setVisibility(View.GONE);
        btnOptAllergies.setEnabled(false);
        btnOptFoodAlcohol.setEnabled(false);
        btnOptInteractions.setEnabled(false);
    }

    private void showMenu() {
        if (selectedMed == null) {
            showNeedMed();
            return;
        }
        state = UiState.MENU;
        panelOptions.setVisibility(View.VISIBLE);
        panelSatisfaction.setVisibility(View.GONE);
        btnOptAllergies.setEnabled(true);
        btnOptFoodAlcohol.setEnabled(true);
        btnOptInteractions.setEnabled(true);
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
                pillboxDAO.open();
                List<Medication> list = pillboxDAO.getMedicationsByUserId(currentUserId);
                pillboxDAO.close();

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
                try { pillboxDAO.close(); } catch (Exception ignored) {}
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
        for (int i = 0; i < meds.size(); i++) names[i] = meds.get(i).getName();

        new AlertDialog.Builder(this)
                .setTitle("Selecciona un medicamento")
                .setItems(names, (dialog, which) -> {
                    selectedMed = meds.get(which);
                    addBot("✅ Medicamento seleccionado: " + selectedMed.getName());
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

    private void onAllergiesOption() {
        if (!ensureMainMedSelected()) return;

        addUser("Alergias / posibles efectos secundarios");
        sendPromptToAI(buildAllergiesPrompt(selectedMed.getName()));
    }

    private void onFoodAlcoholOption() {
        if (!ensureMainMedSelected()) return;

        addUser("Compatibilidad con alimentos o alcohol");
        sendPromptToAI(buildFoodAlcoholPrompt(selectedMed.getName()));
    }

    private void onInteractionsOption() {
        if (!ensureMainMedSelected()) return;

        if (meds.size() < 2) {
            addBot("Necesitas al menos 2 medicamentos en tu pastillero para comprobar interacciones.");
            return;
        }

        List<Medication> candidates = new ArrayList<>();
        for (Medication m : meds) {
            if (m.getId() != selectedMed.getId()) candidates.add(m);
        }

        if (candidates.isEmpty()) {
            addBot("No hay otro medicamento disponible distinto al seleccionado.");
            return;
        }

        String[] names = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) names[i] = candidates.get(i).getName();

        new AlertDialog.Builder(this)
                .setTitle("Selecciona el otro medicamento")
                .setItems(names, (dialog, which) -> {
                    Medication other = candidates.get(which);
                    addUser("Compatibilidad con otro medicamento");
                    addBot("Comparando: " + selectedMed.getName() + " + " + other.getName());
                    sendPromptToAI(buildInteractionsPrompt(selectedMed.getName(), other.getName()));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String buildAllergiesPrompt(String medName) {
        return "Eres un asistente farmacéutico virtual, amable y cercano. Responde en español.\n" +
                "NUNCA te presentes como un médico ni des consejos médicos. Tu único rol es dar información del prospecto de forma sencilla.\n" +
                "SIEMPRE, al final de tu respuesta, recuerda al usuario que debe consultar a su médico o farmacéutico para cualquier duda de salud.\n\n" +
                "Medicamento: " + medName + "\n\n" +
                "Da la respuesta directa, sencilla, sin tecnicismos.\n" +
                "Habla solo de alergias posibles, efectos secundarios importantes y señales para pedir ayuda profesional.\n" +
                "No uses asteriscos dobles ** porque no se ven en el chat.\n" +
                "Usa 4 a 6 viñetas cortas y claras.";
    }

    private String buildFoodAlcoholPrompt(String medName) {
        return "Eres un asistente farmacéutico virtual, amable y cercano. Responde en español.\n" +
                "NUNCA te presentes como un médico ni des consejos médicos. Tu único rol es dar información del prospecto de forma sencilla.\n" +
                "SIEMPRE, al final de tu respuesta, recuerda al usuario que debe consultar a su médico o farmacéutico para cualquier duda de salud.\n\n" +
                "Medicamento: " + medName + "\n\n" +
                "Da la respuesta directa, sencilla, sin tecnicismos.\n" +
                "Habla solo de alimentos que puedan dar problema (si los hay) y alcohol (si se puede o no) y consejos simples.\n" +
                "Si no hay alimentos problemáticos, di: 'No hay alimentos importantes que den problema'.\n" +
                "No uses asteriscos dobles ** porque no se ven en el chat.\n" +
                "Usa 4 a 6 viñetas cortas y claras.";
    }

    private String buildInteractionsPrompt(String med1, String med2) {
        return "Eres un asistente farmacéutico virtual, amable y cercano. Responde en español.\n" +
                "NUNCA te presentes como un médico ni des consejos médicos. Tu único rol es dar información del prospecto de forma sencilla.\n" +
                "SIEMPRE, al final de tu respuesta, recuerda al usuario que debe consultar a su médico o farmacé-eutico para cualquier duda de salud.\n\n" +
                "Medicamento 1: " + med1 + "\n" +
                "Medicamento 2: " + med2 + "\n\n" +
                "Da la respuesta directa, sencilla, sin tecnicismos.\n" +
                "Habla solo de si se pueden tomar juntos o no, el riesgo (bajo/medio/alto) y qué hacer de forma simple.\n" +
                "Si no hay problema conocido, di: 'No se conocen problemas importantes si se toman juntos'.\n" +
                "No uses asteriscos dobles ** porque no se ven en el chat.\n" +
                "Usa 4 a 6 viñetas cortas y claras.";
    }

    private String buildSummaryPrompt(String medName) {
        return "Eres un asistente farmacéutico virtual. Explica de forma sencilla para personas mayores. Responde en español. " +
                "Explícalo de una manera objetiva como un profesional, sin tecnicismos y sin ser coloquial, como si estuvieras haciendo un resumen neutro.\n" +
                "NUNCA te presentes como un médico ni des consejos médicos. Tu único rol es resumir la información.\n" +
                "No uses asteriscos dobles ** ni negritas y no uses listas ni viñetas.\n\n" +
                "Medicamento: " + medName + "\n\n" +
                "Escribe exactamente dos párrafos cortos. En el primero explica para qué sirve y en qué casos se usa normalmente. " +
                "No des consejos de uso, solamente el resumen del medicamento";
    }

    private String getFriendlyErrorMessage(int code) {
        switch (code) {
            case 400:
                return "(Error 400) La petición no es válida. Revisa la pregunta.";
            case 404:
                return "(Error 404) No se ha encontrado el modelo de IA solicitado.";
            case 429:
                return "(Error 429) Has superado el límite de consultas. Por favor, espera un minuto antes de volver a intentarlo.";
            case 500:
            case 503:
                return "(Error " + code + ") El servicio de IA no está disponible en este momento. Inténtalo de nuevo más tarde.";
            default:
                return "(Error " + code + ") Ha ocurrido un problema inesperado con el servicio de IA.";
        }
    }

    private void sendPromptToAI(String prompt) {
        showWaitingAI();
        int typingIndex = addBot("Pensando... 🤖");

        String modelName = "gemini-flash-latest";

        GeminiService service = GeminiApiClient.getGeminiService();
        GeminiRequest request = new GeminiRequest(prompt);

        Call<GeminiResponse> call = service.generateContent(modelName, request);

        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        String errorMessage = getFriendlyErrorMessage(response.code());
                        messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT, errorMessage));
                        adapter.notifyItemChanged(typingIndex);
                        scrollToBottom();

                        if (!isSummaryMode) {
                            addBot("Vuelve a elegir una opción.");
                            showMenu();
                        }
                        return;
                    }

                    String text = response.body().getFirstTextSafe();
                    if (text == null || text.trim().isEmpty()) {
                        messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT,
                                "No he podido obtener una respuesta clara."));
                        adapter.notifyItemChanged(typingIndex);

                        if (!isSummaryMode) {
                            showMenu();
                        }
                        return;
                    }

                    messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT, text));
                    adapter.notifyItemChanged(typingIndex);
                    scrollToBottom();

                    if (!isSummaryMode) {
                        showSatisfaction();
                    }
                });
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    messages.set(typingIndex, new ChatMessage(ChatMessage.SENDER_BOT,
                            "Error de conexión. Revisa tu internet o espera un momento."));
                    adapter.notifyItemChanged(typingIndex);
                    scrollToBottom();

                    if (!isSummaryMode) {
                        showMenu();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
        try { pillboxDAO.close(); } catch (Exception ignored) {}
    }
}
