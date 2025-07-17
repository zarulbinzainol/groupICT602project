package com.example.projectict602;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Exercise extends AppCompatActivity {

    TextView titleText, timerText, dateText, timeText;
    Button finishBtn, cancelBtn;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    int currentStep = 0;
    CountDownTimer countDownTimer;
    MediaPlayer completeSound;

    final String[][] routine = {
            {"Side Lunge", "30"},
            {"Rest", "15"},
            {"High Knees", "30"},
            {"Rest", "15"},
            {"Squats", "30"},
            {"Rest", "15"},
            {"Burpee", "30"},
            {"Rest", "15"},
            {"Pushup", "30"},
            {"Rest", "15"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise);

        // Bind views
        titleText = findViewById(R.id.exercise_title);
        timerText = findViewById(R.id.exercise_timer);
        finishBtn = findViewById(R.id.finish_button);
        cancelBtn = findViewById(R.id.cancel_button);
        progressBar = findViewById(R.id.exercise_progress);
        dateText = findViewById(R.id.text_date);
        timeText = findViewById(R.id.text_time);

        // Auth & DB
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance("https://ict602project-fb860-default-rtdb.firebaseio.com/")
                .getReference("Users")
                .child(mAuth.getCurrentUser().getUid());

        // Set date & time
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        dateText.setText(dateFormat.format(now));
        timeText.setText(timeFormat.format(now));

        // Setup
        completeSound = MediaPlayer.create(this, R.raw.finish1);

        finishBtn.setEnabled(false);
        finishBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);

        startNextStep();

        cancelBtn.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            Toast.makeText(this, "Exercise cancelled.", Toast.LENGTH_SHORT).show();
            finish();
        });

        finishBtn.setOnClickListener(v -> {
            finishBtn.setEnabled(false);
            completeSound.start();
            Toast.makeText(this, "Great Job!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                startActivity(new Intent(Exercise.this, Dashboard.class));
                finish();
            }, 3000); // 3-second delay
        });
    }

    private void startNextStep() {
        if (currentStep >= routine.length) {
            titleText.setText("YOU DID IT!");
            timerText.setText("🎉");
            progressBar.setProgress(0);
            finishBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.GONE);
            finishBtn.setEnabled(true);
            return;
        }

        String name = routine[currentStep][0];
        int duration = Integer.parseInt(routine[currentStep][1]);

        titleText.setText(name);
        progressBar.setMax(duration);
        progressBar.setProgress(duration);

        countDownTimer = new CountDownTimer(duration * 1000L, 1000) {
            int secondsLeft = duration;

            @Override
            public void onTick(long millisUntilFinished) {
                secondsLeft--;
                timerText.setText(String.format(Locale.getDefault(), "%ds", secondsLeft));
                progressBar.setProgress(secondsLeft);
            }

            @Override
            public void onFinish() {
                currentStep++;
                startNextStep();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (completeSound != null) completeSound.release();
    }
}
