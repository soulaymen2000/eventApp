package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import com.example.eventapp.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;

public class SignUpActivity extends BaseActivity {
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.signInText.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        });

        binding.signUpButton.setOnClickListener(v -> {
            String username = binding.usernameInput.getText().toString().trim();
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (validateInput(username, email, password)) {
                handleSignUp(username, email, password);
            }
        });
    }

    private boolean validateInput(String username, String email, String password) {
        if (username.isEmpty()) {
            binding.usernameInput.setError("Veuillez entrer un nom d'utilisateur");
            return false;
        }
        if (email.isEmpty()) {
            binding.emailInput.setError("Veuillez entrer un email");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError("Adresse email invalide");
            return false;
        }
        if (password.isEmpty()) {
            binding.passwordInput.setError("Veuillez entrer un mot de passe");
            return false;
        }
        if (password.length() < 6) {
            binding.passwordInput.setError("Le mot de passe doit comporter au moins 6 caractères");
            return false;
        }
        return true;
    }

    private void handleSignUp(String username, String email, String password) {
        showLoading("Création du compte...");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    DatabaseReference userRef = databaseRef.child("users").child(user.getUid());

                    HashMap<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("createdAt", ServerValue.TIMESTAMP);
                    userData.put("isAdmin", false);

                    userRef.setValue(userData)
                            .addOnSuccessListener(aVoid -> {
                                hideLoading();
                                showToast("Compte créé avec succès");
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                hideLoading();
                                showToast("Échec de l'enregistrement des données : " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    showToast("Échec de l'inscription : " + e.getMessage());
                });
    }
}