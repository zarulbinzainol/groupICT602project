package com.example.projectict602;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Locale;

public class ProgressGallery extends AppCompatActivity {

    LinearLayout galleryLayout;
    Button backButton;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_gallery);

        galleryLayout = findViewById(R.id.gallery_layout);
        backButton = findViewById(R.id.profile_back);
        user = FirebaseAuth.getInstance().getCurrentUser();

        backButton.setOnClickListener(v -> finish());

        if (user == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://ict602project-fb860-default-rtdb.firebaseio.com/")
                .getReference("UserProgress").child(user.getUid());

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String imageData = snap.child("imageData").getValue(String.class);
                    String timestamp = snap.child("timestamp").getValue(String.class);
                    String locationName = snap.child("locationName").getValue(String.class);

                    if (imageData != null && timestamp != null && locationName != null) {
                        addImageBlock(imageData, timestamp, locationName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProgressGallery.this, "Failed to load progress", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addImageBlock(String base64, String timestamp, String locationName) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, 48);

        // Decode Base64 to Bitmap
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        ImageView image = new ImageView(this);
        image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        image.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView text = new TextView(this);
        text.setText(String.format(Locale.getDefault(), "🕒 %s\n📍 %s", timestamp, locationName));
        text.setPadding(0, 8, 0, 0);

        container.addView(image);
        container.addView(text);
        galleryLayout.addView(container);
    }
}
