package com.example.chatbot_app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AttachmentHandler {
    
    private AppCompatActivity activity;
    private AttachmentCallback callback;
    private ActivityResultLauncher<Intent> documentPickerLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    
    private static final int MAX_IMAGE_SIZE = 1024 * 1024; // 1MB
    
    public interface AttachmentCallback {
        void onFileSelected(String fileName, String fileType, String fileContent);
        void onImageSelected(String fileName, Bitmap bitmap, String base64);
        void onError(String error);
    }
    
    public AttachmentHandler(AppCompatActivity activity, AttachmentCallback callback) {
        this.activity = activity;
        this.callback = callback;
        initializeLaunchers();
    }
    
    private void initializeLaunchers() {
        // Document picker launcher
        documentPickerLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleDocumentResult(result.getData());
                }
            }
        );
        
        // Image picker launcher
        imagePickerLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleImageResult(result.getData());
                }
            }
        );
        
        // Camera launcher
        cameraLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleCameraResult(result.getData());
                }
            }
        );
        
        // Permission launcher
        permissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    callback.onError("Camera permission is required to take photos");
                }
            }
        );
    }
    
    public void showAttachmentOptions() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(activity);
        bottomSheet.setContentView(R.layout.bottom_sheet_attachment);
        
        MaterialButton btnDocument = bottomSheet.findViewById(R.id.btnDocument);
        MaterialButton btnImage = bottomSheet.findViewById(R.id.btnImage);
        MaterialButton btnCamera = bottomSheet.findViewById(R.id.btnCamera);
        MaterialButton btnCancel = bottomSheet.findViewById(R.id.btnCancel);
        
        if (btnDocument != null) {
            btnDocument.setOnClickListener(v -> {
                bottomSheet.dismiss();
                openDocumentPicker();
            });
        }
        
        if (btnImage != null) {
            btnImage.setOnClickListener(v -> {
                bottomSheet.dismiss();
                openImagePicker();
            });
        }
        
        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> {
                bottomSheet.dismiss();
                checkCameraPermissionAndOpen();
            });
        }
        
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> bottomSheet.dismiss());
        }
        
        bottomSheet.show();
    }
    
    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
            "text/plain", "text/csv", "application/pdf", 
            "application/msword", "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        });
        
        try {
            documentPickerLauncher.launch(Intent.createChooser(intent, "Select Document"));
        } catch (Exception e) {
            callback.onError("Unable to open document picker: " + e.getMessage());
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
        } catch (Exception e) {
            callback.onError("Unable to open image picker: " + e.getMessage());
        }
    }
    
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            callback.onError("Camera not available on this device");
        }
    }
    
    private void handleDocumentResult(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            callback.onError("No file selected");
            return;
        }
        
        try {
            String fileName = getFileName(uri);
            String mimeType = activity.getContentResolver().getType(uri);
            
            // Read file content
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                callback.onError("Unable to read file");
                return;
            }
            
            // Convert to base64 for small files
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data_bytes = new byte[1024];
            int nRead;
            long totalSize = 0;
            
            while ((nRead = inputStream.read(data_bytes, 0, data_bytes.length)) != -1) {
                totalSize += nRead;
                if (totalSize > MAX_IMAGE_SIZE) {
                    callback.onError("File too large. Maximum size is 1MB");
                    inputStream.close();
                    return;
                }
                buffer.write(data_bytes, 0, nRead);
            }
            
            inputStream.close();
            String base64Content = Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT);
            
            callback.onFileSelected(fileName, mimeType, base64Content);
            
        } catch (IOException e) {
            callback.onError("Error reading file: " + e.getMessage());
        }
    }
    
    private void handleImageResult(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            callback.onError("No image selected");
            return;
        }
        
        try {
            String fileName = getFileName(uri);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            
            // Resize if too large
            bitmap = resizeBitmap(bitmap, 800, 600);
            
            // Convert to base64
            String base64 = bitmapToBase64(bitmap);
            
            callback.onImageSelected(fileName, bitmap, base64);
            
        } catch (IOException e) {
            callback.onError("Error processing image: " + e.getMessage());
        }
    }
    
    private void handleCameraResult(Intent data) {
        if (data.getExtras() == null) {
            callback.onError("No image captured");
            return;
        }
        
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        if (bitmap == null) {
            callback.onError("Failed to capture image");
            return;
        }
        
        // Resize if needed
        bitmap = resizeBitmap(bitmap, 800, 600);
        
        // Convert to base64
        String base64 = bitmapToBase64(bitmap);
        
        callback.onImageSelected("camera_image.jpg", bitmap, base64);
    }
    
    private String getFileName(Uri uri) {
        String fileName = "unknown_file";
        
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        
        return fileName != null ? fileName : "unknown_file";
    }
    
    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }
        
        float aspectRatio = (float) width / height;
        
        if (width > height) {
            width = maxWidth;
            height = (int) (width / aspectRatio);
        } else {
            height = maxHeight;
            width = (int) (height * aspectRatio);
        }
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
