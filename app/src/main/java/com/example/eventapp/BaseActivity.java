package com.example.eventapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth;
    protected FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    protected DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
    }

    protected void showLoading(String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    protected void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Interface pour récupérer les événements
    public interface OnEventFetch {
        void onFetch(List<Event> events);
        void onError(String error);
    }

    // Interface pour vérifier si un utilisateur est admin
    public interface OnAdminCheck {
        void onResult(boolean isAdmin);
        void onError(String error);
    }

    // Récupérer tous les événements
    protected void getAllEvents(OnEventFetch callback) {
        databaseRef.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Event> events = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String title = snapshot.child("title").getValue(String.class);
                        String dateStr = snapshot.child("date").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        String description = snapshot.child("description").getValue(String.class);
                        Integer capacity = snapshot.child("capacity").getValue(Integer.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                        if (title != null && dateStr != null && location != null && description != null && capacity != null && imageUrl != null) {
                            Date date = dateFormat.parse(dateStr);
                            Event event = new Event(title, date, location, description, capacity, imageUrl);
                            event.setId(snapshot.getKey());
                            events.add(event);
                        }
                    } catch (Exception e) {
                        // Continue processing other events if one fails
                        e.printStackTrace();
                    }
                }
                callback.onFetch(events);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Vérifier si un utilisateur est admin
    protected void checkIfAdmin(String uid, OnAdminCheck callback) {
        databaseRef.child("users").child(uid).child("isAdmin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isAdmin = dataSnapshot.getValue(Boolean.class);
                callback.onResult(isAdmin != null && isAdmin);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Ajouter un événement
    protected void addEvent(Event event) {
        try {
            String dateStr = dateFormat.format(event.getDate());
            HashMap<String, Object> eventData = new HashMap<>();
            eventData.put("title", event.getTitle());
            eventData.put("date", dateStr);
            eventData.put("location", event.getLocation());
            eventData.put("description", event.getDescription());
            eventData.put("capacity", event.getCapacity());
            eventData.put("imageUrl", event.getImageUrl());

            String eventId = databaseRef.child("events").push().getKey();
            if (eventId != null) {
                databaseRef.child("events").child(eventId).setValue(eventData)
                        .addOnSuccessListener(aVoid -> showToast("Événement créé avec succès"))
                        .addOnFailureListener(e -> showToast("Échec de la création : " + e.getMessage()));
            }
        } catch (Exception e) {
            showToast("Erreur lors de la création de l'événement : " + e.getMessage());
        }
    }
}