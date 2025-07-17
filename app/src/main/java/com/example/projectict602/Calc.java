package com.example.projectict602;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class Calc extends AppCompatActivity {

    EditText inputHeight, inputWeight;
    Button calcBtn, backBtn;
    TextView resultText;
    ProgressBar bmiMeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calc);

        inputHeight = findViewById(R.id.input_height);
        inputWeight = findViewById(R.id.input_weight);
        calcBtn = findViewById(R.id.calc_button);
        backBtn = findViewById(R.id.profile_back);
        resultText = findViewById(R.id.result_text);
        bmiMeter = findViewById(R.id.bmi_meter);

        calcBtn.setOnClickListener(v -> calculateBMI());

        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(Calc.this, Dashboard.class));
            finish();
        });
    }

    private void calculateBMI() {
        String heightStr = inputHeight.getText().toString().trim();
        String weightStr = inputWeight.getText().toString().trim();

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter both height and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float height = Float.parseFloat(heightStr) / 100; // cm to m
            float weight = Float.parseFloat(weightStr);
            float bmi = weight / (height * height);

            String status;
            if (bmi < 18.5) {
                status = "Underweight";
            } else if (bmi < 25) {
                status = "Normal";
            } else if (bmi < 30) {
                status = "Overweight";
            } else {
                status = "Obesity";
            }

            float minWeight = 18.5f * height * height;
            float maxWeight = 25.0f * height * height;

            String result = String.format(Locale.getDefault(),
                    "BMI = %.1f (%s)\nHealthy BMI range: 18.5 - 25\nHealthy weight for height: %.1f kg - %.1f kg",
                    bmi, status, minWeight, maxWeight);
            resultText.setText(result);

            // Set BMI meter progress (scaled by 10 to range 0–400)
            int progress = (int) (Math.max(10f, Math.min(bmi, 40f)) * 10);
            bmiMeter.setProgress(progress);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number input", Toast.LENGTH_SHORT).show();
        }
    }
}
