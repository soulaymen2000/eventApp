package com.example.eventapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {
    private ImageView eventDetailImage;
    private TextView eventDetailTitle, eventDetailDate, eventDetailDescription;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventDetailImage = findViewById(R.id.eventDetailImage);
        eventDetailTitle = findViewById(R.id.eventDetailTitle);
        eventDetailDate = findViewById(R.id.eventDetailDate);
        eventDetailDescription = findViewById(R.id.eventDetailDescription);

        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            Toast.makeText(this, "Erreur : ID de l'événement manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseDatabase.getInstance().getReference("events").child(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FirebaseHelper.EventDTO eventDTO = dataSnapshot.getValue(FirebaseHelper.EventDTO.class);
                        if (eventDTO == null) {
                            Toast.makeText(EventDetailActivity.this, "Erreur : Événement non trouvé", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        try {
                            Date date = dateFormat.parse(eventDTO.getDate());
                            Event event = new Event(
                                    eventDTO.getTitle(),
                                    date,
                                    eventDTO.getLocation(),
                                    eventDTO.getDescription(),
                                    eventDTO.getCapacity(),
                                    eventDTO.getImageUrl()
                            );
                            event.setId(dataSnapshot.getKey());
                            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                                Glide.with(EventDetailActivity.this)
                                        .load(event.getImageUrl())
                                        .placeholder(R.drawable.placeholder_image)
                                        .error(R.drawable.error_image)
                                        .into(eventDetailImage);
                            } else {
                                eventDetailImage.setImageResource(R.drawable.error_image);
                            }
                            eventDetailTitle.setText(event.getTitle());
                            eventDetailDate.setText(dateFormat.format(event.getDate()));
                            eventDetailDescription.setText(event.getDescription());
                        } catch (Exception e) {
                            Toast.makeText(EventDetailActivity.this, "Erreur de parsing de date : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EventDetailActivity.this, "Erreur : " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}