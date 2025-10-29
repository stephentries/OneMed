package com.example.onemed;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            new android.os.Handler().postDelayed(() -> {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Request timed out. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }, 10000); // 10 seconds timeout

            String email = String.valueOf(editTextEmail.getText());
            String password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                Toast.makeText(Login.this, "Error: User not found after login", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = user.getUid();
                            String userEmail = user.getEmail();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            // Check if Firestore user doc exists
                            db.collection("users").document(uid).get()
                                    .addOnCompleteListener(taskDoc -> {
                                        if (taskDoc.isSuccessful()) {
                                            DocumentSnapshot document = taskDoc.getResult();
                                            if (document != null && !document.exists()) {
                                                // Create user document if missing
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("email", userEmail);
                                                userData.put("createdAt", FieldValue.serverTimestamp());

                                                db.collection("users").document(uid).set(userData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            goToMainActivity();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(Login.this, "Failed to create user data", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                // User doc exists, proceed
                                                goToMainActivity();
                                            }
                                        } else {
                                            Toast.makeText(Login.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(Login.this, "User does not exist. Please sign up!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), main_activity.class);
        startActivity(intent);
        finish();
    }
}
