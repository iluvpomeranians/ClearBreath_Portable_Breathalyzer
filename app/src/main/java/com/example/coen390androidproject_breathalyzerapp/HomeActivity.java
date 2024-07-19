package com.example.coen390androidproject_breathalyzerapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class HomeActivity extends AppCompatActivity implements BluetoothService.BluetoothDataListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView bacDisplay;
    private TextView bacMlDisplay;
    private TextView timeUntilSoberDisplay;
    private CircularProgressBar circularProgressBar;
    private Button btnGoingOut;
    private Button btnHealth;
    private Button btnBluetooth;
    private TextView bluetoothStatusDisplay;

    private BluetoothService bluetoothService;
    private boolean isBound = false;

    private static final String TAG = "HomeActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_account) {
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        circularProgressBar = findViewById(R.id.circularProgressBar);
        bacDisplay = findViewById(R.id.bac_display);
        bacMlDisplay = findViewById(R.id.bac_ml_display);
        timeUntilSoberDisplay = findViewById(R.id.time_until_sober_display);
        btnGoingOut = findViewById(R.id.btn_more_info);
        btnHealth = findViewById(R.id.btn_health);
        btnBluetooth = findViewById(R.id.btn_bluetooth);
        bluetoothStatusDisplay = findViewById(R.id.bluetooth_status_display);

        btnGoingOut.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoreInfoActivity.class);
            startActivity(intent);
        });

        btnBluetooth.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                if (isBound && bluetoothService != null) {
                    bluetoothService.setupBluetooth(this, bluetoothStatusDisplay);
                } else {
                    Toast.makeText(this, "Bluetooth service not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);  // Use startService instead of startForegroundService
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
        if (isBound && bluetoothService != null) {
            String connectedDeviceName = bluetoothService.getConnectedDeviceName();
            Log.d(TAG, "Connected device name in onResume: " + connectedDeviceName);
            if (connectedDeviceName != null) {
                bluetoothStatusDisplay.setText("Status: Connected to " + connectedDeviceName);
                bluetoothStatusDisplay.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                bluetoothStatusDisplay.setText("Status: Not connected");
                bluetoothStatusDisplay.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                if (isBound && bluetoothService != null) {
                    bluetoothService.setupBluetooth(this, bluetoothStatusDisplay);
                }
            }
        }
    }

    private void applySettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int textSize = sharedPreferences.getInt("text_size", 16); // Default text size 16
        String font = sharedPreferences.getString("font", "default");
        int toolbarColor = sharedPreferences.getInt("toolbar_color", Color.BLACK);

        // Apply text size to TextViews
        bacDisplay.setTextSize(textSize);
        bacMlDisplay.setTextSize(textSize);
        timeUntilSoberDisplay.setTextSize(textSize);

        // Apply font
        if (!font.equals("default")) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), font);
            bacDisplay.setTypeface(typeface);
            bacMlDisplay.setTypeface(typeface);
            timeUntilSoberDisplay.setTypeface(typeface);
        }

        // Apply toolbar color
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(toolbarColor);
    }

    @Override
    public void onDataReceived(String data) {
        processReceivedData(data);
    }

    private void processReceivedData(String data) {
        try {
            data = data.replace("BAC:", "").trim(); // Remove "BAC:" prefix
            double bac = Double.parseDouble(data);
            int bacProgress = (int) (bac * 1000); // Convert BAC to integer representation

            int progressBarColor;
            if (bac < 0.02) {
                progressBarColor = Color.GREEN;
            } else if (bac < 0.05) {
                progressBarColor = Color.YELLOW;
            } else if (bac < 0.08) {
                progressBarColor = Color.rgb(255, 165, 0); // Orange color
            } else {
                progressBarColor = Color.RED;
            }

            bacDisplay.setText(String.format("BAC: %.2f%%", bac));
            circularProgressBar.setProgressWithAnimation(bacProgress, 1000L); // Animation duration of 1 second
            circularProgressBar.setProgressBarColor(progressBarColor);

            double bacMl = bac * 1000; // Convert BAC to mL
            bacMlDisplay.setText(String.format("BAC in mL: %.2f mL", bacMl));

            double hoursUntilSober = bac / 0.015;
            timeUntilSoberDisplay.setText(String.format("Time Until Sober: %.1f hours", hoursUntilSober));

        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid BAC data received", e);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            bluetoothService.setBluetoothDataListener(HomeActivity.this);
            isBound = true;

            // Call setupBluetooth here if permissions are already granted
            if (allPermissionsGranted()) {
                bluetoothService.setupBluetooth(HomeActivity.this, bluetoothStatusDisplay);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}
