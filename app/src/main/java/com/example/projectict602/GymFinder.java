package com.example.projectict602;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GymFinder extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final String API_KEY = "mykey";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private ListView gymListView;
    private ArrayAdapter<String> adapter;
    private final List<GymItem> gymList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gym_finder);

        gymListView = findViewById(R.id.gym_list);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gym_map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        findViewById(R.id.profile_back).setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableLocation();
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fetchLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void fetchLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = location;
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                fetchNearbyGyms(userLatLng);
            }
        });
    }

    private void fetchNearbyGyms(LatLng location) {
        new Thread(() -> {
            try {
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                        location.latitude + "," + location.longitude +
                        "&radius=2000&type=gym&key=" + API_KEY;

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONArray gyms = json.getJSONArray("results");

                gymList.clear();
                for (int i = 0; i < gyms.length(); i++) {
                    JSONObject g = gyms.getJSONObject(i);
                    String name = g.getString("name");
                    JSONObject loc = g.getJSONObject("geometry").getJSONObject("location");
                    double lat = loc.getDouble("lat");
                    double lng = loc.getDouble("lng");

                    LatLng gymLatLng = new LatLng(lat, lng);
                    Location gymLoc = new Location("");
                    gymLoc.setLatitude(lat);
                    gymLoc.setLongitude(lng);
                    float distance = userLocation.distanceTo(gymLoc) / 1000f;

                    gymList.add(new GymItem(name, gymLatLng, distance));
                }

                runOnUiThread(() -> displayGyms());

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error loading gyms", Toast.LENGTH_SHORT).show());
                Log.e("GymFinder", "API error", e);
            }
        }).start();
    }

    private void displayGyms() {
        mMap.clear();
        List<String> names = new ArrayList<>();

        gymList.sort(Comparator.comparingDouble(g -> g.distance));
        for (GymItem g : gymList) {
            names.add(g.name + String.format(" (%.1f km)", g.distance));
            mMap.addMarker(new MarkerOptions()
                    .position(g.latLng)
                    .title(g.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        gymListView.setAdapter(adapter);

        gymListView.setOnItemClickListener((parent, view, position, id) -> {
            LatLng latLng = gymList.get(position).latLng;
            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latLng.latitude + "," + latLng.longitude);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        } else {
            Toast.makeText(this, "Location permission is required to find nearby gyms", Toast.LENGTH_LONG).show();
        }
    }

    static class GymItem {
        String name;
        LatLng latLng;
        float distance;

        GymItem(String name, LatLng latLng, float distance) {
            this.name = name;
            this.latLng = latLng;
            this.distance = distance;
        }
    }
}
