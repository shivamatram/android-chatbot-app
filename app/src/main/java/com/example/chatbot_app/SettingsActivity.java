package com.example.chatbot_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private SwitchMaterial darkModeSwitch;
    private SwitchMaterial notificationsSwitch;
    private SwitchMaterial voiceInputSwitch;
    private SwitchMaterial autoSendSwitch;
    private Slider responseSpeedSlider;
    private Slider textSizeSlider;
    private TextInputEditText customApiKeyInput;
    private MaterialButton saveButton;
    private MaterialButton resetButton;
    
    // SharedPreferences
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "ChatbotSettings";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Initialize preferences
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize UI components
        initializeViews();
        setupToolbar();
        loadSettings();
        setupListeners();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        voiceInputSwitch = findViewById(R.id.voiceInputSwitch);
        autoSendSwitch = findViewById(R.id.autoSendSwitch);
        responseSpeedSlider = findViewById(R.id.responseSpeedSlider);
        textSizeSlider = findViewById(R.id.textSizeSlider);
        customApiKeyInput = findViewById(R.id.customApiKeyInput);
        saveButton = findViewById(R.id.saveButton);
        resetButton = findViewById(R.id.resetButton);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }
    
    private void loadSettings() {
        // Load saved settings
        darkModeSwitch.setChecked(preferences.getBoolean("dark_mode", false));
        notificationsSwitch.setChecked(preferences.getBoolean("notifications", true));
        voiceInputSwitch.setChecked(preferences.getBoolean("voice_input", true));
        autoSendSwitch.setChecked(preferences.getBoolean("auto_send", false));
        responseSpeedSlider.setValue(preferences.getFloat("response_speed", 1.0f));
        textSizeSlider.setValue(preferences.getFloat("text_size", 16.0f));
        
        String savedApiKey = preferences.getString("custom_api_key", "");
        if (!savedApiKey.isEmpty()) {
            customApiKeyInput.setText(savedApiKey);
        }
    }
    
    private void setupListeners() {
        // Dark mode toggle
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Notifications toggle
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Voice input toggle
        voiceInputSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Voice input " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Auto send toggle
        autoSendSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Auto send " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Response speed slider
        responseSpeedSlider.addOnChangeListener((slider, value, fromUser) -> {
            String speedText = value == 0.5f ? "Slow" : value == 1.0f ? "Normal" : "Fast";
            Toast.makeText(this, "Response speed: " + speedText, Toast.LENGTH_SHORT).show();
        });
        
        // Text size slider
        textSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            Toast.makeText(this, "Text size: " + (int)value + "sp", Toast.LENGTH_SHORT).show();
        });
        
        // Save button
        saveButton.setOnClickListener(v -> saveSettings());
        
        // Reset button
        resetButton.setOnClickListener(v -> resetSettings());
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putBoolean("dark_mode", darkModeSwitch.isChecked());
        editor.putBoolean("notifications", notificationsSwitch.isChecked());
        editor.putBoolean("voice_input", voiceInputSwitch.isChecked());
        editor.putBoolean("auto_send", autoSendSwitch.isChecked());
        editor.putFloat("response_speed", responseSpeedSlider.getValue());
        editor.putFloat("text_size", textSizeSlider.getValue());
        
        String apiKey = customApiKeyInput.getText().toString().trim();
        if (!apiKey.isEmpty()) {
            editor.putString("custom_api_key", apiKey);
        }
        
        editor.apply();
        
        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
    }
    
    private void resetSettings() {
        // Reset to default values
        darkModeSwitch.setChecked(false);
        notificationsSwitch.setChecked(true);
        voiceInputSwitch.setChecked(true);
        autoSendSwitch.setChecked(false);
        responseSpeedSlider.setValue(1.0f);
        textSizeSlider.setValue(16.0f);
        customApiKeyInput.setText("");
        
        // Clear saved preferences
        preferences.edit().clear().apply();
        
        // Reset theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Public methods to get settings values
    public static boolean isDarkModeEnabled(SharedPreferences prefs) {
        return prefs.getBoolean("dark_mode", false);
    }
    
    public static boolean areNotificationsEnabled(SharedPreferences prefs) {
        return prefs.getBoolean("notifications", true);
    }
    
    public static boolean isVoiceInputEnabled(SharedPreferences prefs) {
        return prefs.getBoolean("voice_input", true);
    }
    
    public static boolean isAutoSendEnabled(SharedPreferences prefs) {
        return prefs.getBoolean("auto_send", false);
    }
    
    public static float getResponseSpeed(SharedPreferences prefs) {
        return prefs.getFloat("response_speed", 1.0f);
    }
    
    public static float getTextSize(SharedPreferences prefs) {
        return prefs.getFloat("text_size", 16.0f);
    }
    
    public static String getCustomApiKey(SharedPreferences prefs) {
        return prefs.getString("custom_api_key", "");
    }
}
