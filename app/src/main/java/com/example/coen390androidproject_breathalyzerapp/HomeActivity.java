package com.example.coen390androidproject_breathalyzerapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView bacDisplay;
    private TextView bacMlDisplay;
    private TextView timeUntilSoberDisplay;
    private CircularProgressBar circularProgressBar;
    private Button btnGoingOut;
    private Button btnHealth;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private boolean isSober = true;

    private static final String DEVICE_NAME = "ESP32_Sensor";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
    private final String DEVICE_ADDRESS = "00:11:22:33:44:55"; // Replace with your device's address

    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ClearBreath Portable Breathalyzer");
        }

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
            }
            else if (id == R.id.nav_settings) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        /*navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });*/

        circularProgressBar = findViewById(R.id.circularProgressBar);
        bacDisplay = findViewById(R.id.bac_display);
        bacMlDisplay = findViewById(R.id.bac_ml_display);
        timeUntilSoberDisplay = findViewById(R.id.time_until_sober_display);
        btnGoingOut = findViewById(R.id.btn_more_info);
        btnHealth = findViewById(R.id.btn_health);


        // Call applySettings after initializing all views
        applySettings();

        btnGoingOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Going Out button click
                Intent intent = new Intent(HomeActivity.this, MoreInfoActivity.class);
                startActivity(intent);
            }
        });
        /*
        // Check and request permissions if needed
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            setupBluetooth();
        }

        simulateReceivingData("0.01"); // Simulated BAC value
        */
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    //@Override
    protected void OnResume()
    {
        super.onResume();
        applySettings();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }
            startActivityForResult(enableBtIntent, 1);
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            beginListenForData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; // This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                try {
                    int bytesAvailable = inputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        inputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;

                                handler.post(() -> processReceivedData(data));
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException ex) {
                    stopWorker = true;
                }
            }
        });

        workerThread.start();
    }

    private void processReceivedData(String data) {
        try {
            double bac = Double.parseDouble(data.trim());
            int bacProgress = (int) (bac * 1000); // Convert BAC to integer representation

            // Set color based on BAC level
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

            bacDisplay.setText("BAC: " + String.format("%.2f", bac) + "%");
            circularProgressBar.setProgressWithAnimation(bacProgress, 1000L); // Animation duration of 1 second
            circularProgressBar.setProgressBarColor(progressBarColor);

            // Display BAC in terms of mL
            double bacMl = bac * 1000; // Convert BAC to mL
            bacMlDisplay.setText("BAC in mL: " + String.format("%.2f", bacMl) + " mL");

            // Estimate time until sobriety (assuming 0.015% BAC reduction per hour)
            double hoursUntilSober = bac / 0.015;
            timeUntilSoberDisplay.setText("Time Until Sober: " + String.format("%.1f", hoursUntilSober) + " hours");

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void simulateReceivingData(String data) {
        processReceivedData(data);
    }

    private void applySettings() {
        // Retrieve preferences and apply them
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
}