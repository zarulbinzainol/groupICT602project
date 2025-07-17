package com.example.projectict602;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText email, password;
    Button loginBtn, toRegisterBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.login);

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_button);
        toRegisterBtn = findViewById(R.id.goto_register);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> {
            String user = email.getText().toString();
            String pass = password.getText().toString();

            mAuth.signInWithEmailAndPassword(user, pass)
                    .addOnSuccessListener(authResult -> {
                        startActivity(new Intent(this, Dashboard.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                    );
        });

        toRegisterBtn.setOnClickListener(v ->
                startActivity(new Intent(this, Register.class))
        );

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, Reset.class));
        });


    }
}
