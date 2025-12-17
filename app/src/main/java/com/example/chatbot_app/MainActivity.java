package com.example.chatbot_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements 
    AttachmentHandler.AttachmentCallback, 
    VoiceInputHandler.VoiceInputCallback {

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputEditText userInput;
    private FloatingActionButton sendButton;
    private ImageButton attachButton;
    private ImageButton voiceButton;
    private ImageButton menuButton;
    private LinearLayout chatContainer;
    private NestedScrollView scrollView;
    private MaterialCardView welcomeCard;
    private CircularProgressIndicator typingIndicator;
    private TextView statusText;
    private TextView modelInfoText;
    private ExtendedFloatingActionButton quickActionsFab;
    
    // Suggestion chips
    private Chip chipSuggestion1, chipSuggestion2, chipSuggestion3, chipSuggestion4;
    private Chip chipHelp, chipExamples;
    
    // Handlers
    private AttachmentHandler attachmentHandler;
    private VoiceInputHandler voiceInputHandler;
    
    // API Configuration
    private String apiKey = "SderZMSzs5pN6rcYptXBGOZ1llgZbqjZz3KIR8Fe";
    private String cohereUrl = "https://api.cohere.ai/v1/chat";
    private String[] availableModels = {"command-r", "command-r-08-2024", "command", "command-light", "command-nightly"};
    
    // Threading
    private ExecutorService executorService;
    private Handler mainHandler;
    
    // Settings
    private SharedPreferences settings;
    private static final String PREFS_NAME = "ChatbotSettings";
    
    // State
    private boolean isVoiceRecording = false;
    private int messageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up status bar
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_main);

        // Initialize components
        initializeComponents();
        initializeUI();
        setupEventListeners();
        loadSettings();
        
        // Show welcome message
        showWelcomeMessage();
    }
    
    private void initializeComponents() {
        // Initialize threading
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize settings
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize handlers
        attachmentHandler = new AttachmentHandler(this, this);
        voiceInputHandler = new VoiceInputHandler(this, this);
    }
    
    private void initializeUI() {
        // Find all UI components
        toolbar = findViewById(R.id.topAppBar);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
        attachButton = findViewById(R.id.attachButton);
        voiceButton = findViewById(R.id.voiceButton);
        menuButton = findViewById(R.id.menuButton);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView = findViewById(R.id.scrollView);
        welcomeCard = findViewById(R.id.welcomeCard);
        typingIndicator = findViewById(R.id.typingIndicator);
        statusText = findViewById(R.id.statusText);
        modelInfoText = findViewById(R.id.modelInfoText);
        quickActionsFab = findViewById(R.id.quickActionsFab);
        
        // Suggestion chips
        chipSuggestion1 = findViewById(R.id.chipSuggestion1);
        chipSuggestion2 = findViewById(R.id.chipSuggestion2);
        chipSuggestion3 = findViewById(R.id.chipSuggestion3);
        chipSuggestion4 = findViewById(R.id.chipSuggestion4);
        chipHelp = findViewById(R.id.chipHelp);
        chipExamples = findViewById(R.id.chipExamples);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
    }
    
    private void setupEventListeners() {
        // Send button
        sendButton.setOnClickListener(v -> sendMessage());
        
        // Attachment button
        attachButton.setOnClickListener(v -> attachmentHandler.showAttachmentOptions());
        
        // Voice button
        voiceButton.setOnClickListener(v -> toggleVoiceInput());
        
        // Menu button
        menuButton.setOnClickListener(v -> showOptionsMenu());
        
        // Quick actions FAB
        quickActionsFab.setOnClickListener(v -> showQuickActions());
        
        // Enter key in input field
        userInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
        
        // Suggestion chips
        setupSuggestionChips();
    }
    
    private void setupSuggestionChips() {
        chipSuggestion1.setOnClickListener(v -> {
            userInput.setText("Tell me a joke");
            sendMessage();
        });
        
        chipSuggestion2.setOnClickListener(v -> {
            userInput.setText("Explain quantum physics");
            sendMessage();
        });
        
        chipSuggestion3.setOnClickListener(v -> {
            userInput.setText("Write a poem");
            sendMessage();
        });
        
        chipSuggestion4.setOnClickListener(v -> {
            userInput.setText("Help with coding");
            sendMessage();
        });
        
        chipHelp.setOnClickListener(v -> {
            userInput.setText("What can you help me with?");
            sendMessage();
        });
        
        chipExamples.setOnClickListener(v -> showExamplesDialog());
    }
    
    private void loadSettings() {
        // Load custom API key if available
        String customApiKey = SettingsActivity.getCustomApiKey(settings);
        if (!customApiKey.isEmpty()) {
            apiKey = customApiKey;
        }
        
        // Apply text size setting
        float textSize = SettingsActivity.getTextSize(settings);
        userInput.setTextSize(textSize);
    }
    
    private void showWelcomeMessage() {
        if (messageCount == 0) {
            welcomeCard.setVisibility(View.VISIBLE);
        }
    }

    // Voice Input Methods
    private void toggleVoiceInput() {
        if (!SettingsActivity.isVoiceInputEnabled(settings)) {
            Toast.makeText(this, "Voice input is disabled in settings", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isVoiceRecording) {
            voiceInputHandler.stopVoiceInput();
        } else {
            voiceInputHandler.startVoiceInput();
        }
    }
    
    @Override
    public void onVoiceInputStarted() {
        runOnUiThread(() -> {
            isVoiceRecording = true;
            voiceButton.setImageResource(R.drawable.ic_mic_off);
            voiceButton.setColorFilter(getColor(R.color.error));
            statusText.setText("Listening...");
            showSnackbar("Speak now...", Snackbar.LENGTH_INDEFINITE);
        });
    }
    
    @Override
    public void onVoiceInputResult(String text) {
        runOnUiThread(() -> {
            userInput.setText(text);
            if (SettingsActivity.isAutoSendEnabled(settings)) {
                sendMessage();
            }
        });
    }
    
    @Override
    public void onVoiceInputError(String error) {
        runOnUiThread(() -> {
            showSnackbar("Voice input error: " + error, Snackbar.LENGTH_LONG);
        });
    }
    
    @Override
    public void onVoiceInputStopped() {
        runOnUiThread(() -> {
            isVoiceRecording = false;
            voiceButton.setImageResource(R.drawable.ic_mic);
            voiceButton.setColorFilter(getColor(R.color.on_surface_variant));
            statusText.setText("Online");
        });
    }
    
    @Override
    public void onVolumeChanged(float volume) {
        // Could animate microphone icon based on volume
    }
    
    // Attachment Methods
    @Override
    public void onFileSelected(String fileName, String fileType, String fileContent) {
        runOnUiThread(() -> {
            String message = "📎 File attached: " + fileName + "\n\nPlease analyze this " + fileType + " file.";
            userInput.setText(message);
            showSnackbar("File attached: " + fileName, Snackbar.LENGTH_SHORT);
        });
    }
    
    @Override
    public void onImageSelected(String fileName, Bitmap bitmap, String base64) {
        runOnUiThread(() -> {
            String message = "🖼️ Image attached: " + fileName + "\n\nPlease describe this image.";
            userInput.setText(message);
            showSnackbar("Image attached: " + fileName, Snackbar.LENGTH_SHORT);
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            showSnackbar("Error: " + error, Snackbar.LENGTH_LONG);
        });
    }
    
    // Menu and Dialog Methods
    private void showOptionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options")
               .setItems(new String[]{"Settings", "Clear Chat", "About"}, (dialog, which) -> {
                   switch (which) {
                       case 0:
                           startActivity(new Intent(this, SettingsActivity.class));
                           break;
                       case 1:
                           clearChat();
                           break;
                       case 2:
                           showAboutDialog();
                           break;
                   }
               })
               .show();
    }
    
    private void showQuickActions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quick Actions")
               .setItems(new String[]{"Clear Chat", "Export Chat", "Voice Settings", "Attachment Options"}, 
                   (dialog, which) -> {
                       switch (which) {
                           case 0:
                               clearChat();
                               break;
                           case 1:
                               exportChat();
                               break;
                           case 2:
                               startActivity(new Intent(this, SettingsActivity.class));
                               break;
                           case 3:
                               attachmentHandler.showAttachmentOptions();
                               break;
                       }
                   })
               .show();
    }
    
    private void showExamplesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Example Prompts")
               .setMessage("• Tell me a joke\n• Explain quantum physics\n• Write a poem about nature\n• Help me debug this code\n• Summarize the latest tech news\n• Create a workout plan\n• Translate text to Spanish\n• Generate creative writing ideas")
               .setPositiveButton("OK", null)
               .show();
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About AI ChatBot")
               .setMessage("AI ChatBot v1.0\n\nPowered by Cohere AI\n\nFeatures:\n• Voice input with speech-to-text\n• File and image attachments\n• Customizable settings\n• Beautiful Material Design 3 UI\n• Smart model fallback system")
               .setPositiveButton("OK", null)
               .show();
    }
    
    private void clearChat() {
        chatContainer.removeAllViews();
        messageCount = 0;
        welcomeCard.setVisibility(View.VISIBLE);
        quickActionsFab.setVisibility(View.GONE);
        showSnackbar("Chat cleared", Snackbar.LENGTH_SHORT);
    }
    
    private void exportChat() {
        showSnackbar("Export feature coming soon!", Snackbar.LENGTH_SHORT);
    }
    
    private void showSnackbar(String message, int duration) {
        Snackbar.make(findViewById(R.id.snackbarContainer), message, duration).show();
    }
    
    private void hideWelcomeCardIfNeeded() {
        if (messageCount > 0) {
            welcomeCard.setVisibility(View.GONE);
            quickActionsFab.setVisibility(View.VISIBLE);
        }
    }
    
    // Core Messaging Methods
    private void sendMessage() {
        String message = userInput.getText().toString().trim();
        if (message.isEmpty()) {
            showSnackbar("Please enter a message", Snackbar.LENGTH_SHORT);
            return;
        }
        
        // Hide welcome card after first message
        if (messageCount == 0) {
            welcomeCard.setVisibility(View.GONE);
            quickActionsFab.setVisibility(View.VISIBLE);
        }
        
        // Add user message to chat
        addMessageToChat(message, true);
        
        // Clear input and show typing indicator
        userInput.setText("");
        userInput.clearFocus();
        showTypingIndicator();
        
        // Send to API in background thread
        executorService.execute(() -> {
            String response = callCohereAPI(message);
            
            // Update UI on main thread
            mainHandler.post(() -> {
                hideTypingIndicator();
                addMessageToChat(response, false);
                scrollToBottom();
            });
        });
        
        messageCount++;
    }
    
    private void addMessageToChat(String message, boolean isUser) {
        // Clean the message (remove prefixes)
        String cleanMessage = message.replaceFirst("^(You|Bot):\\s*", "");
        
        // Create message card
        MaterialCardView messageCard = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(isUser ? 64 : 16, 8, isUser ? 16 : 64, 8);
        messageCard.setLayoutParams(cardParams);
        
        // Set card styling
        messageCard.setCardBackgroundColor(getColor(isUser ? R.color.user_message_bg : R.color.bot_message_bg));
        messageCard.setRadius(16f);
        messageCard.setCardElevation(4f);
        messageCard.setStrokeColor(getColor(R.color.outline_variant));
        messageCard.setStrokeWidth(1);
        
        // Create content layout
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(20, 16, 20, 16);
        
        // Message text
        TextView messageText = new TextView(this);
        messageText.setText(cleanMessage);
        messageText.setTextColor(getColor(isUser ? R.color.user_message_text : R.color.bot_message_text));
        messageText.setTextSize(16f);
        messageText.setLineSpacing(4f, 1.1f);
        
        // Timestamp
        TextView timestampText = new TextView(this);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timestampText.setText(sdf.format(new Date()));
        timestampText.setTextColor(getColor(R.color.on_surface_variant));
        timestampText.setTextSize(12f);
        timestampText.setAlpha(0.7f);
        
        LinearLayout.LayoutParams timestampParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timestampParams.setMargins(0, 8, 0, 0);
        timestampParams.gravity = isUser ? android.view.Gravity.END : android.view.Gravity.START;
        timestampText.setLayoutParams(timestampParams);
        
        // Add views to layout
        contentLayout.addView(messageText);
        contentLayout.addView(timestampText);
        messageCard.addView(contentLayout);
        
        // Add to chat container
        chatContainer.addView(messageCard);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
    
    private String callCohereAPI(String userPrompt) {
        // Try different models in case one is deprecated
        for (String model : availableModels) {
            try {
                String result = tryAPICallWithModel(userPrompt, model);
                if (!result.startsWith("API Error 404") && !result.startsWith("Exception")) {
                    // Update model info on success
                    mainHandler.post(() -> {
                        if (modelInfoText != null) {
                            modelInfoText.setText("Model: " + model + " • Ready to help");
                        }
                    });
                    return result; // Success with this model
                }
            } catch (Exception e) {
                continue; // Try next model
            }
        }
        
        return "I apologize, but I'm currently unable to process your request. This could be due to:\n\n" +
               "• Network connectivity issues\n" +
               "• API service maintenance\n" +
               "• Model availability\n\n" +
               "Please check your internet connection and try again in a moment.";
    }
    
    private void showTypingIndicator() {
        mainHandler.post(() -> {
            if (typingIndicator != null) {
                typingIndicator.setVisibility(View.VISIBLE);
            }
            if (statusText != null) {
                statusText.setText("Typing...");
            }
            if (modelInfoText != null) {
                modelInfoText.setText("Processing your request...");
            }
        });
    }
    
    private void hideTypingIndicator() {
        mainHandler.post(() -> {
            if (typingIndicator != null) {
                typingIndicator.setVisibility(View.GONE);
            }
            if (statusText != null) {
                statusText.setText("Online");
            }
            if (modelInfoText != null) {
                modelInfoText.setText("Model: " + availableModels[0] + " • Ready to help");
            }
        });
    }
    
    private String tryAPICallWithModel(String userPrompt, String model) {
        try {
            URL url = new URL(cohereUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(30000); // 30 seconds
            
            // Create request JSON
            JSONObject data = new JSONObject();
            data.put("model", model);
            data.put("message", userPrompt);
            data.put("max_tokens", 1000);
            data.put("temperature", 0.7);
            
            // Send request
            OutputStream os = connection.getOutputStream();
            byte[] input = data.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();
            
            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("text")) {
                    return jsonResponse.getString("text").trim();
                } else if (jsonResponse.has("message")) {
                    return jsonResponse.getString("message").trim();
                } else {
                    return "Received response but couldn't parse it properly.";
                }
            } else {
                // Read error response
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                return "API Error " + responseCode + ": " + errorResponse.toString();
            }

        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (voiceInputHandler != null) {
            voiceInputHandler.destroy();
        }
    }
}
