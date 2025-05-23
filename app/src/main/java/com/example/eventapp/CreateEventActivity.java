package com.example.eventapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateEventActivity extends BaseActivity {
    private EditText etTitle, etDate, etLocation, etDescription, etCapacity, etImageUrl;
    private Button btnCreateEvent, btnLogout;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        etCapacity = findViewById(R.id.etCapacity);
        etImageUrl = findViewById(R.id.etImageUrl);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePicker());

        btnCreateEvent.setOnClickListener(v -> createEvent());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(CreateEventActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    etDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void createEvent() {
        String title = etTitle.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if (validateInput(title, dateStr, location, description, capacityStr, imageUrl)) {
            try {
                int capacity = Integer.parseInt(capacityStr);
                Date date = dateFormat.parse(dateStr);

                Event event = new Event(title, date, location, description, capacity, imageUrl);
                addEvent(event);

                // Clear form
                clearForm();

            } catch (Exception e) {
                showToast("Erreur lors de la création de l'événement: " + e.getMessage());
            }
        }
    }

    private boolean validateInput(String title, String dateStr, String location,
                                  String description, String capacityStr, String imageUrl) {
        if (title.isEmpty()) {
            etTitle.setError("Titre requis");
            return false;
        }
        if (dateStr.isEmpty()) {
            etDate.setError("Date requise");
            return false;
        }
        if (location.isEmpty()) {
            etLocation.setError("Lieu requis");
            return false;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description requise");
            return false;
        }
        if (capacityStr.isEmpty()) {
            etCapacity.setError("Capacité requise");
            return false;
        }
        if (imageUrl.isEmpty()) {
            etImageUrl.setError("URL de l'image requise");
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                etCapacity.setError("La capacité doit être positive");
                return false;
            }
        } catch (NumberFormatException e) {
            etCapacity.setError("Capacité invalide");
            return false;
        }

        return true;
    }

    private void clearForm() {
        etTitle.setText("");
        etDate.setText("");
        etLocation.setText("");
        etDescription.setText("");
        etCapacity.setText("");
        etImageUrl.setText("");
    }
}