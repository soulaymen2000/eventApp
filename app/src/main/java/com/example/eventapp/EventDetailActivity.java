package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {
    private ImageView eventDetailImage;
    private TextView eventDetailTitle, eventDetailDate, eventDetailDescription, eventDetailCapacity;
    private Button btnReserve;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String eventId;
    private Event currentEvent;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupReservationButton();
        loadEventData();
    }

    private void initializeViews() {
        eventDetailImage = findViewById(R.id.eventDetailImage);
        eventDetailTitle = findViewById(R.id.eventDetailTitle);
        eventDetailDate = findViewById(R.id.eventDetailDate);
        eventDetailDescription = findViewById(R.id.eventDetailDescription);
        eventDetailCapacity = findViewById(R.id.eventDetailCapacity);
        btnReserve = findViewById(R.id.btnReserve);

        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            showErrorAndFinish("Erreur : ID de l'événement manquant");
        }
    }

    private void setupReservationButton() {
        btnReserve.setOnClickListener(v -> handleReservation());
    }

    private void loadEventData() {
        databaseRef.child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseHelper.EventDTO eventDTO = dataSnapshot.getValue(FirebaseHelper.EventDTO.class);
                if (eventDTO == null) {
                    showErrorAndFinish("Événement non trouvé");
                    return;
                }

                try {
                    Date date = dateFormat.parse(eventDTO.getDate());
                    currentEvent = new Event(
                            eventDTO.getTitle(),
                            date,
                            eventDTO.getLocation(),
                            eventDTO.getDescription(),
                            eventDTO.getCapacity(),
                            eventDTO.getImageUrl()
                    );
                    currentEvent.setId(dataSnapshot.getKey());
                    updateUI();
                    checkExistingReservation();
                } catch (Exception e) {
                    showErrorAndFinish("Erreur de parsing de date");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showErrorAndFinish(databaseError.getMessage());
            }
        });
    }

    private void updateUI() {
        if (currentEvent.getImageUrl() != null && !currentEvent.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentEvent.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(eventDetailImage);
        } else {
            eventDetailImage.setImageResource(R.drawable.error_image);
        }

        eventDetailTitle.setText(currentEvent.getTitle());
        eventDetailDate.setText(dateFormat.format(currentEvent.getDate()));
        eventDetailDescription.setText(currentEvent.getDescription());
        eventDetailCapacity.setText("Places disponibles: " + currentEvent.getCapacity());

        if (currentEvent.getCapacity() <= 0) {
            btnReserve.setEnabled(false);
            btnReserve.setText("Complet");
        }
    }

    private void handleReservation() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            return;
        }

        if (currentEvent.getCapacity() <= 0) {
            Toast.makeText(this, "Plus de places disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("reservations").child(userId).child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(EventDetailActivity.this, "Vous avez déjà réservé cet événement", Toast.LENGTH_SHORT).show();
                } else {
                    createReservation(userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EventDetailActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createReservation(String userId) {
        databaseRef.child("reservations").child(userId).child(eventId).setValue(true)
                .addOnSuccessListener(aVoid -> updateEventCapacity())
                .addOnFailureListener(e -> Toast.makeText(EventDetailActivity.this,
                        "Échec de la réservation: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateEventCapacity() {
        databaseRef.child("events").child(eventId).child("capacity")
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer capacity = mutableData.getValue(Integer.class);
                        if (capacity != null && capacity > 0) {
                            mutableData.setValue(capacity - 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            Toast.makeText(EventDetailActivity.this,
                                    "Erreur de mise à jour: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (committed) {
                            currentEvent.setCapacity(currentEvent.getCapacity() - 1);
                            updateUI();
                            Toast.makeText(EventDetailActivity.this,
                                    "Réservation réussie!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkExistingReservation() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            databaseRef.child("reservations").child(userId).child(eventId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                btnReserve.setEnabled(false);
                                btnReserve.setText("Déjà réservé");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Handle error
                        }
                    });
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}