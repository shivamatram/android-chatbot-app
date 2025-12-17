package com.example.chatbot_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceInputHandler {
    
    private AppCompatActivity activity;
    private VoiceInputCallback callback;
    private SpeechRecognizer speechRecognizer;
    private ActivityResultLauncher<String> permissionLauncher;
    private boolean isListening = false;
    
    public interface VoiceInputCallback {
        void onVoiceInputStarted();
        void onVoiceInputResult(String text);
        void onVoiceInputError(String error);
        void onVoiceInputStopped();
        void onVolumeChanged(float volume);
    }
    
    public VoiceInputHandler(AppCompatActivity activity, VoiceInputCallback callback) {
        this.activity = activity;
        this.callback = callback;
        initializePermissionLauncher();
        initializeSpeechRecognizer();
    }
    
    private void initializePermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startVoiceInput();
                } else {
                    callback.onVoiceInputError("Microphone permission is required for voice input");
                }
            }
        );
    }
    
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    callback.onVoiceInputStarted();
                }
                
                @Override
                public void onBeginningOfSpeech() {
                    // Speech input detected
                }
                
                @Override
                public void onRmsChanged(float rmsdB) {
                    // Volume level changed
                    float volume = Math.max(0, Math.min(1, (rmsdB + 10) / 20));
                    callback.onVolumeChanged(volume);
                }
                
                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Audio buffer received
                }
                
                @Override
                public void onEndOfSpeech() {
                    // End of speech detected
                }
                
                @Override
                public void onError(int error) {
                    isListening = false;
                    String errorMessage = getErrorMessage(error);
                    callback.onVoiceInputError(errorMessage);
                    callback.onVoiceInputStopped();
                }
                
                @Override
                public void onResults(Bundle results) {
                    isListening = false;
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        callback.onVoiceInputResult(recognizedText);
                    } else {
                        callback.onVoiceInputError("No speech recognized");
                    }
                    callback.onVoiceInputStopped();
                }
                
                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Partial results available
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String partialText = matches.get(0);
                        // Could show partial results in real-time
                    }
                }
                
                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Speech recognition event
                }
            });
        } else {
            callback.onVoiceInputError("Speech recognition not available on this device");
        }
    }
    
    public void startVoiceInput() {
        if (speechRecognizer == null) {
            callback.onVoiceInputError("Speech recognizer not initialized");
            return;
        }
        
        if (isListening) {
            stopVoiceInput();
            return;
        }
        
        // Check microphone permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }
        
        // Create speech recognition intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        
        // Start listening
        try {
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            callback.onVoiceInputError("Failed to start voice recognition: " + e.getMessage());
        }
    }
    
    public void stopVoiceInput() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            callback.onVoiceInputStopped();
        }
    }
    
    public boolean isListening() {
        return isListening;
    }
    
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
    
    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech input recognized";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input detected";
            default:
                return "Unknown error occurred";
        }
    }
    
    // Static method to check if speech recognition is available
    public static boolean isSpeechRecognitionAvailable(AppCompatActivity activity) {
        return SpeechRecognizer.isRecognitionAvailable(activity);
    }
    
    // Static method to check microphone permission
    public static boolean hasMicrophonePermission(AppCompatActivity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) 
            == PackageManager.PERMISSION_GRANTED;
    }
}
