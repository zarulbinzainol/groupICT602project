package com.example.projectict602;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CameraProgress extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 200;
    private FusedLocationProviderClient locationClient;
    private Location currentLocation;
    private FirebaseUser currentUser;

    private ImageView imagePreview;
    private TextView statusText;
    private ProgressBar spinner;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (bitmap != null) {
                        imagePreview.setImageBitmap(bitmap);
                        statusText.setText("Uploading...");
                        uploadImageToDatabase(bitmap);
                    }
                } else {
                    Toast.makeText(this, "Cancelled or failed to capture image", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera); // Ensure camera.xml has image_preview, status_text, loading_spinner

        imagePreview = findViewById(R.id.image_preview);
        statusText = findViewById(R.id.status_text);
        spinner = findViewById(R.id.loading_spinner);

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION);
        } else {
            capturePhoto();
        }
    }

    private void capturePhoto() {
        statusText.setText("Getting your location...");
        spinner.setVisibility(View.VISIBLE);

        try {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    statusText.setText("Launching camera...");
                    spinner.setVisibility(View.GONE);

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(cameraIntent);
                } else {
                    statusText.setText("Unable to get location.");
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(e -> {
                spinner.setVisibility(View.GONE);
                Toast.makeText(this, "Location fetch failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
        } catch (SecurityException e) {
            spinner.setVisibility(View.GONE);
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void uploadImageToDatabase(Bitmap bitmap) {
        if (currentUser == null || bitmap == null) return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

        // Get location name from lat/lng using Geocoder
        String locationName = "Unknown Location";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                locationName = addr.getLocality() + ", " + addr.getCountryName();
            }
        } catch (Exception ignored) {}

        Map<String, Object> photoData = new HashMap<>();
        photoData.put("timestamp", timestamp);
        photoData.put("latitude", latitude);
        photoData.put("longitude", longitude);
        photoData.put("locationName", locationName);
        photoData.put("imageData", encodedImage);

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://ict602project-fb860-default-rtdb.firebaseio.com/")
                .getReference("UserProgress").child(currentUser.getUid());

        String key = dbRef.push().getKey();
        if (key != null) {
            dbRef.child(key).setValue(photoData)
                    .addOnSuccessListener(unused -> {
                        statusText.setText("Upload successful!");
                        Toast.makeText(this, "Progress saved", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        statusText.setText("Upload failed.");
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean cameraGranted = false;
            boolean locationGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    cameraGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (cameraGranted && locationGranted) {
                capturePhoto();
            } else {
                Toast.makeText(this, "Camera and Location permissions are required.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
