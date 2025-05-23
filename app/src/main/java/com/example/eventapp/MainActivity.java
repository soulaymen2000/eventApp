package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements BaseActivity.OnEventFetch {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private Button btnLogin, btnRegister, btnAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        adapter = new EventAdapter(new ArrayList<>(), event -> {
            if (event.getId() == null) {
                Toast.makeText(MainActivity.this, "Erreur : ID de l'événement manquant", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnAdmin = findViewById(R.id.btnAdmin);

        // Update button visibility based on authentication state
        updateButtonVisibility();

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        btnAdmin.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SignInActivity.class));
                return;
            }

            checkIfAdmin(mAuth.getCurrentUser().getUid(), new OnAdminCheck() {
                @Override
                public void onResult(boolean isAdmin) {
                    if (isAdmin) {
                        startActivity(new Intent(MainActivity.this, CreateEventActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Accès réservé aux administrateurs", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(MainActivity.this, "Erreur : " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Fetch events regardless of authentication state
        getAllEvents(this);
    }

    private void updateButtonVisibility() {
        if (mAuth.getCurrentUser() == null) {
            // Unauthenticated: Show login and register buttons, hide admin button
            btnLogin.setVisibility(Button.VISIBLE);
            btnRegister.setVisibility(Button.VISIBLE);
            btnAdmin.setVisibility(Button.GONE);
        } else {
            // Authenticated: Hide login and register buttons, show admin button
            btnLogin.setVisibility(Button.GONE);
            btnRegister.setVisibility(Button.GONE);
            btnAdmin.setVisibility(Button.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update button visibility when returning to activity (e.g., after login)
        updateButtonVisibility();
        // Refresh events
        getAllEvents(this);
    }

    @Override
    public void onFetch(List<Event> events) {
        if (events == null || events.isEmpty()) {
            Toast.makeText(this, "Aucun événement récupéré", Toast.LENGTH_SHORT).show();
            // Create new adapter with original listener
            adapter = new EventAdapter(new ArrayList<>(), event -> {
                if (event.getId() == null) {
                    Toast.makeText(MainActivity.this, "Erreur : ID de l'événement manquant", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra("event_id", event.getId());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
            return;
        }

        // Update existing adapter with new data
        adapter = new EventAdapter(events, event -> {
            if (event.getId() == null) {
                Toast.makeText(MainActivity.this, "Erreur : ID de l'événement manquant", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, "Erreur : " + error, Toast.LENGTH_SHORT).show();
        // Reset adapter with empty list and original listener
        adapter = new EventAdapter(new ArrayList<>(), event -> {
            if (event.getId() == null) {
                Toast.makeText(MainActivity.this, "Erreur : ID de l'événement manquant", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }
}
