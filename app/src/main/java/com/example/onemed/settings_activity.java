package com.example.onemed;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class settings_activity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private LinearLayout securityContainer;
    private Switch themeSwitch, notificationSwitch;
    private EditText oldPasswordField, newPasswordField, newEmailField;
    private MaterialButton changePasswordBtn, changeEmailBtn, securitySettingsBtn, signOutBtn;
    private ImageView back_btn;
    private static final String PREFS_NAME = "app_prefs";
    private static final String THEME_KEY = "is_dark_theme";

    private boolean isSecurityVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mAuth = FirebaseAuth.getInstance();

        // Init views
        securityContainer = findViewById(R.id.security_container);
        securityContainer.setVisibility(View.GONE); // Hidden by default

        themeSwitch = findViewById(R.id.switch_theme);
        //notificationSwitch = findViewById(R.id.switch_notifications);

        oldPasswordField = findViewById(R.id.old_password);
        newPasswordField = findViewById(R.id.new_password);
        newEmailField = findViewById(R.id.new_email);

        changePasswordBtn = findViewById(R.id.change_password);
        changeEmailBtn = findViewById(R.id.change_email);
        securitySettingsBtn = findViewById(R.id.security_toggle);
        back_btn = findViewById(R.id.back_btn);
        signOutBtn = findViewById(R.id.sign_out);

        // Set initial state of the switch
        themeSwitch.setChecked(isDarkThemeEnabled());

// Toggle theme on switch change
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setDarkThemeEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Dark mode enabled" : "Light mode enabled", Toast.LENGTH_SHORT).show();
            recreate(); // Recreate activity to apply theme instantly
        });


        /*notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Notifications ON" : "Notifications OFF", Toast.LENGTH_SHORT).show();
        }); */

        signOutBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(settings_activity.this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        if (auth.getCurrentUser() != null) {
                            auth.signOut();
                            Intent intent = new Intent(settings_activity.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(settings_activity.this, "You're not signed in.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        });

        // Toggle security section
        securitySettingsBtn.setOnClickListener(v -> {
            isSecurityVisible = !isSecurityVisible;
            securityContainer.setVisibility(isSecurityVisible ? View.VISIBLE : View.GONE);
        });

        changePasswordBtn.setOnClickListener(v -> promptPasswordReauthAndChange());
        changeEmailBtn.setOnClickListener(v -> promptEmailReauthAndChange());

        back_btn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void promptPasswordReauthAndChange() {
        String oldPass = oldPasswordField.getText().toString().trim();
        String newPass = newPasswordField.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(this, "Fill in both password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPass).addOnCompleteListener(passTask -> {
                    if (passTask.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        oldPasswordField.setText("");
                        newPasswordField.setText("");
                    } else {
                        Log.e("FirebaseAuth", "Failed to update password", passTask.getException()); // 👈 Log added
                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("FirebaseAuth", "Re-authentication failed", task.getException()); // 👈 Log added
                Toast.makeText(this, "Re-authentication failed. Check current password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void promptEmailReauthAndChange() {
        String newEmail = newEmailField.getText().toString().trim();

        if (newEmail.isEmpty()) {
            Toast.makeText(this, "Enter new email", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Re-authenticate");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_authenticate, null);
        EditText emailField = view.findViewById(R.id.auth_email);
        EditText passwordField = view.findViewById(R.id.auth_password);
        emailField.setText(user.getEmail());
        emailField.setEnabled(false);

        builder.setView(view);
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String password = passwordField.getText().toString().trim();

                if (password.isEmpty()) {
                    Toast.makeText(this, "Enter password to confirm", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

                user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        user.updateEmail(newEmail).addOnCompleteListener(emailTask -> {
                            if (emailTask.isSuccessful()) {
                                Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                                newEmailField.setText("");
                                dialog.dismiss();
                            } else {
                                Log.e("FirebaseAuth", "Failed to update email", emailTask.getException()); // 👈 Log added
                                Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e("FirebaseAuth", "Re-authentication failed", authTask.getException()); // 👈 Log added
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }
    private void setDarkThemeEnabled(boolean enabled) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(THEME_KEY, enabled)
                .apply();

        AppCompatDelegate.setDefaultNightMode(
                enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private boolean isDarkThemeEnabled() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(THEME_KEY, false);
    }
}
