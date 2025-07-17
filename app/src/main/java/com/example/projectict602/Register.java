package com.example.projectict602;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.Locale;

public class Register extends AppCompatActivity {

    EditText email, password, fullname, nickname, dob;
    Button registerBtn, backToLogin;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Bind Views
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        fullname = findViewById(R.id.register_fullname);
        nickname = findViewById(R.id.register_nickname);
        dob = findViewById(R.id.register_dob);
        registerBtn = findViewById(R.id.register_button);
        backToLogin = findViewById(R.id.back_to_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ict602project-fb860-default-rtdb.firebaseio.com/");
        dbRef = database.getReference("Users");

        // Date Picker Dialog for DOB
        dob.setFocusable(false);
        dob.setClickable(true);
        dob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    Register.this,
                    (view, year, month, dayOfMonth) -> {
                        String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        dob.setText(formatted);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });

        // Register Button Logic
        registerBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();
            String name = fullname.getText().toString().trim();
            String nick = nickname.getText().toString().trim();
            String birth = dob.getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty() || name.isEmpty() || nick.isEmpty() || birth.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send verification email
                            user.sendEmailVerification()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                            // Save user data to database
                            String uid = user.getUid();
                            Usermodel userData = new Usermodel(userEmail, name, nick, birth);
                            dbRef.child(uid).setValue(userData)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, Login.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FIREBASE_DB", "Error saving data", e);
                                        Toast.makeText(this, "Database Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE_AUTH", "Registration Failed", e);
                        Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Back to Login Button
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}
