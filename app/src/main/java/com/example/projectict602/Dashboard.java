package com.example.projectict602;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class Dashboard extends AppCompatActivity {

    TextView welcomeText, encourageText;
    Button logoutBtn, btnProfile, btnBMI, btnCamera,btnExercise, btnGymFinder, btnViewProgress;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Bind views
        welcomeText = findViewById(R.id.welcome_text);
        encourageText = findViewById(R.id.encourage_text);
        logoutBtn = findViewById(R.id.logout_button);
        btnProfile = findViewById(R.id.btn_profile);
        btnBMI = findViewById(R.id.btn_bmi);
        btnCamera = findViewById(R.id.btn_camera);
        btnGymFinder = findViewById(R.id.btn_gym_finder);
        btnExercise = findViewById(R.id.btn_exercise);
        btnViewProgress = findViewById(R.id.btn_view_progress);

        // Firebase auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            dbRef = FirebaseDatabase.getInstance("https://ict602project-fb860-default-rtdb.firebaseio.com/")
                    .getReference("Users").child(uid);

            dbRef.child("nickname").get().addOnSuccessListener(snapshot -> {
                String nickname = snapshot.getValue(String.class);
                if (nickname != null && !nickname.isEmpty()) {
                    String formattedNickname = nickname.substring(0, 1).toUpperCase() + nickname.substring(1);
                    welcomeText.setText(getString(R.string.welcome_user, formattedNickname));

                }
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to load nickname", Toast.LENGTH_SHORT).show()
            );
        }

        // Logout logic
        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(Dashboard.this, Login.class));
            finish();
        });

        // Navigate to User Profile
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, Profile.class));
        });

        // Dummy feature buttons
        btnBMI.setOnClickListener(v ->{
            startActivity(new Intent(Dashboard.this, Calc.class));
        });


        btnGymFinder = findViewById(R.id.btn_gym_finder);
        btnGymFinder.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, GymFinder.class));
        });

        btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, CameraProgress.class));
        });

        Button btnViewProgress = findViewById(R.id.btn_view_progress);
        btnViewProgress.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, ProgressGallery.class));
        });



        btnExercise.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, Exercise.class));
        });

        // Encouragement quote
        String[] encouragements = {
                "You’re stronger than you think!", "Push yourself today!", "Every rep counts!",
                "No excuses, just results!", "Feel the burn and smile!", "Let’s crush it today!",
                "One more set!", "Champions train, losers complain.", "Fuel your ambition!",
                "Progress, not perfection.", "Train insane or remain the same.", "Sweat now, shine later.",
                "Discipline equals freedom.", "Strength starts in the mind.", "Believe and achieve.",
                "Be proud, but never satisfied.", "Lift heavy, love harder.", "Consistency is key.",
                "You're doing amazing!", "Keep grinding!",
                "The pain you feel today will be strength tomorrow.", "Your only limit is you.",
                "One step at a time.", "Never give up!", "Focus. Breathe. Conquer."
        };
        int randomIndex = (int) (Math.random() * encouragements.length);
        encourageText.setText(encouragements[randomIndex]);
    }
}
