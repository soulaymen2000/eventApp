package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import com.example.eventapp.databinding.ActivitySignInBinding;

public class SignInActivity extends BaseActivity {
    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.signUpText.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            finish();
        });

        binding.loginButton.setOnClickListener(v -> {
            String email = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (validateInput(email, password)) {
                handleSignIn(email, password);
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            binding.usernameInput.setError("Veuillez entrer votre email");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.usernameInput.setError("Email invalide");
            return false;
        }
        if (password.isEmpty()) {
            binding.passwordInput.setError("Veuillez entrer votre mot de passe");
            return false;
        }
        return true;
    }

    private void handleSignIn(String email, String password) {
        showLoading("Connexion en cours...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = mAuth.getCurrentUser().getUid();
                    databaseRef.child("users").child(userId).child("isAdmin")
                            .get().addOnCompleteListener(task -> {
                                hideLoading();
                                if (task.isSuccessful() && Boolean.TRUE.equals(task.getResult().getValue(Boolean.class))) {
                                    Intent intent = new Intent(SignInActivity.this, CreateEventActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    showToast("Échec de la connexion : " + e.getMessage());
                });
    }
}