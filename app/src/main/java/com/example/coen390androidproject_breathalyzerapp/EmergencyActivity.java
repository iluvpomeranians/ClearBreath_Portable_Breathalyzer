package com.example.coen390androidproject_breathalyzerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.encoders.json.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class EmergencyActivity extends AppCompatActivity implements Marker.OnMarkerClickListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private MapView mapView;
    private Button buttonFindNearby, buttonFindHospitals, buttonCall, buttonSMS, buttonWhatsApp;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean pendingTaxiDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Emergency/Help Page");
        }

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(EmergencyActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Initialize the osmdroid configuration
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(46.8131, -71.2075)); // Quebec location

        buttonFindNearby = findViewById(R.id.button_find_nearby);
        buttonFindHospitals = findViewById(R.id.button_find_hospitals);
        buttonCall = findViewById(R.id.button_call);
        buttonSMS = findViewById(R.id.button_sms);
        buttonWhatsApp = findViewById(R.id.button_whatsapp);

        buttonFindNearby.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                findPlaces("police", "Montreal");
            }
        });

        buttonFindHospitals.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                findPlaces("hospital", "Montreal");
            }
        });

        buttonCall.setOnClickListener(v -> {
            makePhoneCall("711");
            pendingTaxiDialog = true;
        });

        buttonSMS.setOnClickListener(v -> {
            sendSMS("711", "I been drinkin too much... can u help a brother much? Just a place to stay, ill be on the wayyy");
            pendingTaxiDialog = true;
        });

        buttonWhatsApp.setOnClickListener(v -> {
            openWhatsApp("911", "I have drank too much and I need help now!");
            pendingTaxiDialog = true;
        });

        SettingsUtils.applySettings(this, buttonFindNearby, buttonFindHospitals, buttonCall, buttonSMS, buttonWhatsApp);
    }

    private void findPlaces(String placeType, String city) {
        // Fetch the user's location and show places on the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            fetchNearbyPlaces(location.getLatitude(), location.getLongitude(), placeType, city);
                        }
                    });
        }
    }

    private void fetchNearbyPlaces(double latitude, double longitude, String placeType, String city) {
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + placeType + "+" + city + "&limit=10&lat=" + latitude + "&lon=" + longitude;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    mapView.getOverlays().clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject place = response.getJSONObject(i);
                            String name = place.getString("display_name");
                            double lat = place.getDouble("lat");
                            double lon = place.getDouble("lon");

                            Marker marker = new Marker(mapView);
                            marker.setPosition(new GeoPoint(lat, lon));
                            marker.setTitle(name);
                            marker.setOnMarkerClickListener((m, mv) -> {
                                m.showInfoWindow();
                                new Handler().postDelayed(this::showTaxiDialog, 5000); // Show taxi dialog after 5 seconds
                                return true;
                            });
                            mapView.getOverlays().add(marker);

                            if (i == 0) {
                                mapView.getController().setCenter(new GeoPoint(lat, lon));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Toast.makeText(EmergencyActivity.this, "Error fetching nearby places", Toast.LENGTH_SHORT).show());

        queue.add(jsonArrayRequest);
    }

    private void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 2);
        } else {
            startActivity(intent);
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }

    private void openWhatsApp(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + message));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingTaxiDialog) {
            showTaxiDialog();
            pendingTaxiDialog = false;
        }
    }

    private void showTaxiDialog() {
        TaxiDialogFragment dialogFragment = new TaxiDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "taxiDialog");
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        new Handler().postDelayed(this::showTaxiDialog, 5000);
        return true;
    }
}
