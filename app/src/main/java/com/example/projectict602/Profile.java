package com.example.projectict602;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Profile extends AppCompatActivity {

    TextView nameText, emailText, ageText, bmiText, idealBmiText, idealWeightText;
    EditText heightInput, weightInput;
    Button updateBtn, backBtn;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String uid;

    final float IDEAL_BMI = 24.9f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Bind views
        nameText = findViewById(R.id.profile_name);
        emailText = findViewById(R.id.profile_email);
        ageText = findViewById(R.id.profile_age);
        bmiText = findViewById(R.id.profile_bmi);
        idealBmiText = findViewById(R.id.profile_target_bmi);
        idealWeightText = findViewById(R.id.profile_target_weight);
        heightInput = findViewById(R.id.profile_height);
        weightInput = findViewById(R.id.profile_weight);
        updateBtn = findViewById(R.id.update_button);
        backBtn = findViewById(R.id.profile_back);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            uid = user.getUid();
            dbRef = FirebaseDatabase.getInstance("https://ict602project-fb860-default-rtdb.firebaseio.com/")
                    .getReference("Users").child(uid);

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fullname = snapshot.child("fullname").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String dob = snapshot.child("dateOfBirth").getValue(String.class);
                    String heightVal = snapshot.child("height").getValue(String.class);
                    String weightVal = snapshot.child("weight").getValue(String.class);

                    nameText.setText("Full Name: " + (fullname != null ? fullname : "-"));
                    emailText.setText("Email: " + (email != null ? email : "-"));

                    if (dob != null) {
                        int age = calculateAge(dob);
                        ageText.setText("Age: " + age + " years");
                    }

                    if (heightVal != null) heightInput.setText(heightVal);
                    if (weightVal != null) weightInput.setText(weightVal);

                    updateBmiDisplay();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Profile.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Auto update BMI on change
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBmiDisplay();
                saveHeightWeightToFirebase();
            }
        };

        heightInput.addTextChangedListener(watcher);
        weightInput.addTextChangedListener(watcher);

        updateBtn.setOnClickListener(v -> {
            String height = heightInput.getText().toString().trim();
            String weight = weightInput.getText().toString().trim();

            if (height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(Profile.this, "Please enter height and weight", Toast.LENGTH_SHORT).show();
                return;
            }

            dbRef.child("height").setValue(height);
            dbRef.child("weight").setValue(weight);
            Toast.makeText(Profile.this, "Profile updated!", Toast.LENGTH_SHORT).show();

            updateBmiDisplay();
        });

        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, Dashboard.class));
            finish();
        });
    }

    private void updateBmiDisplay() {
        String heightStr = heightInput.getText().toString();
        String weightStr = weightInput.getText().toString();

        if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
            try {
                float height = Float.parseFloat(heightStr) / 100; // convert cm to meters
                float weight = Float.parseFloat(weightStr);

                float bmi = weight / (height * height);
                float idealWeight = IDEAL_BMI * height * height;

                bmiText.setText(String.format(Locale.getDefault(), "Current BMI: %.1f", bmi));
                idealBmiText.setText(String.format(Locale.getDefault(), "Ideal BMI: %.1f", IDEAL_BMI));
                idealWeightText.setText(String.format(Locale.getDefault(), "Ideal Weight: %.1f kg", idealWeight));
            } catch (Exception e) {
                bmiText.setText("Current BMI: -");
                idealBmiText.setText("Ideal BMI: -");
                idealWeightText.setText("Ideal Weight: -");
            }
        } else {
            bmiText.setText("Current BMI: -");
            idealBmiText.setText("Ideal BMI: -");
            idealWeightText.setText("Ideal Weight: -");
        }
    }

    private void saveHeightWeightToFirebase() {
        String height = heightInput.getText().toString().trim();
        String weight = weightInput.getText().toString().trim();

        if (!height.isEmpty()) dbRef.child("height").setValue(height);
        if (!weight.isEmpty()) dbRef.child("weight").setValue(weight);
    }

    private int calculateAge(String dobString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar dob = Calendar.getInstance();
            Date date = sdf.parse(dobString);
            if (date != null) dob.setTime(date);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            return 0;
        }
    }
}
