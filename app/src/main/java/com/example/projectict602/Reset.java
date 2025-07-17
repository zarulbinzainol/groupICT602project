package com.example.projectict602;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Reset extends AppCompatActivity {

    EditText emailInput;
    Button resetButton, backToLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset);

        // Bind views
        emailInput = findViewById(R.id.reset_email);
        resetButton = findViewById(R.id.reset_button);
        backToLogin = findViewById(R.id.back_to_login);

        // Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        // Reset button logic
        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Reset email sent to your email", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Back to login logic
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}
