package com.example.onemed;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class add_record_activity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_FILES = 101;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 102;

    private MaterialButton uploadBtn, saveBtn, clearAllBtn;
    private RecyclerView rvFiles;
    private FilesAdapter adapter;
    private List<Uri> selectedFileUris = new ArrayList<>();

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "upload_prefs";
    private static final String KEY_FILES = "uploaded_files";

    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_record_activity); // your layout

        // initialize UI
        uploadBtn = findViewById(R.id.upload_btn);
        saveBtn = findViewById(R.id.save);
        clearAllBtn = findViewById(R.id.clear_all);
        rvFiles = findViewById(R.id.file_uploaded_container);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        setupRecyclerView();
        loadSavedFiles();

        uploadBtn.setOnClickListener(v -> {
            if (checkStoragePermission()) pickFilesFromGallery();
            else requestStoragePermission();
        });

        saveBtn.setOnClickListener(v -> uploadFilesAndClear());

        clearAllBtn.setOnClickListener(v -> showClearAllConfirmationDialog());
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        ActivityCompat.requestPermissions(this, new String[]{perm}, REQUEST_CODE_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFilesFromGallery();
            } else {
                Toast.makeText(this, "Permission denied to access files", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickFilesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FILES && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        selectedFileUris.add(uri);
                    }
                } else if (data.getData() != null) {
                    selectedFileUris.add(data.getData());
                }
                saveFilesToPrefs();
                adapter.notifyDataSetChanged();
                rvFiles.setVisibility(View.VISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveFilesToPrefs() {
        Set<String> uris = new HashSet<>();
        for (Uri uri : selectedFileUris) uris.add(uri.toString());
        prefs.edit().putStringSet(KEY_FILES, uris).apply();
    }

    private void loadSavedFiles() {
        Set<String> uris = prefs.getStringSet(KEY_FILES, new HashSet<>());
        for (String s : uris) selectedFileUris.add(Uri.parse(s));
        adapter.notifyDataSetChanged();
        rvFiles.setVisibility(selectedFileUris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void clearAllSelections() {
        selectedFileUris.clear();
        prefs.edit().remove(KEY_FILES).apply();
        adapter.notifyDataSetChanged();
        rvFiles.setVisibility(View.GONE);
    }

    private void uploadFilesAndClear() {
        for (Uri uri : selectedFileUris) {
            StorageReference fileRef = storageRef.child("uploads/" + uri.getLastPathSegment());
            fileRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> Log.d("Upload", "Success: " + uri))
                    .addOnFailureListener(e -> Log.e("Upload", "Error: ", e));
        }
        clearAllSelections();
        Toast.makeText(this, "Files uploaded and cleared", Toast.LENGTH_SHORT).show();
    }
    private void showClearAllConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Clear All")
                .setMessage("Are you sure?");

        builder.setPositiveButton("Yes", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            final Button yesBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            yesBtn.setEnabled(false);

            new CountDownTimer(2000, 1000) {
                @Override public void onTick(long millisUntilFinished) {
                    yesBtn.setText("Yes (" + (millisUntilFinished / 1000 + 1) + ")");
                }
                @Override public void onFinish() {
                    yesBtn.setText("Yes");
                    yesBtn.setEnabled(true);
                }
            }.start();

            yesBtn.setOnClickListener(v -> {
                clearAllSelections();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
    public void hideRecyclerView() {
        rvFiles.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new FilesAdapter(this, selectedFileUris);
        rvFiles.setLayoutManager(new LinearLayoutManager(this));
        rvFiles.setAdapter(adapter);
        rvFiles.setVisibility(View.VISIBLE);
    }
    private void uploadFileWithMetadata(Uri fileUri, String title, String description) {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String fileName = UUID.randomUUID().toString(); // unique file name

        // 1. Reference in Cloud Storage
        StorageReference fileRef = FirebaseStorage.getInstance()
                .getReference("uploads/" + userId + "/" + fileName);

        // 2. Upload file
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 3. Get Download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                        // 4. Prepare metadata
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("title", title);
                        metadata.put("description", description);
                        metadata.put("fileUrl", downloadUri.toString());
                        metadata.put("timestamp", FieldValue.serverTimestamp());

                        // 5. Store in Firestore
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .collection("uploads")
                                .add(metadata)
                                .addOnSuccessListener(docRef ->
                                        Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );

                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                    );

                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}




