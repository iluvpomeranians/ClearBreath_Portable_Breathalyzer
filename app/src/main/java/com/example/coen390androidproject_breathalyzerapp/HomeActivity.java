package com.example.coen390androidproject_breathalyzerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Handler;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.juky.squircleview.views.SquircleButton;


public class HomeActivity extends AppCompatActivity implements BluetoothService.BluetoothDataListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private SharedPreferences sharedPreferences;
    private TextView bacDisplay;
    private ProgressBar progressBar;
    private TextView textViewBlow;
    private SquircleButton buttonStartRecording, btnAccountHistory;
    private TextView bacMlDisplay;
    private TextView timeUntilSoberDisplay;
    private CircularProgressBar circularProgressBar;
    private SquircleButton btnInstructions;
    private SquircleButton btnStartRecording;
    private SquircleButton btnCancelRecording;
    private SquircleButton btnBluetooth;
    private SquircleButton btnPairDevices;
    private TextView bluetoothStatusDisplay;
    private int currentUserId = -1;
    private BluetoothService bluetoothService;
    private boolean isBound = false;
    private static final String TAG = "HomeActivity";
    private NavigationView navigationView, navigationViewUI;
    private OnBackPressedCallback onBackPressedCallback;
    private boolean isBluetoothOn = false;
    private Handler handler = new Handler();
    private int progressStatus = 0;
    private boolean isRecording = false;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final int REQUEST_CODE_NOTIFICATIONS = 102;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Clear Breath - Home");
        }

        dbHelper = new DBHelper(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_account) {
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_manage_account) {
                Intent intent = new Intent(HomeActivity.this, ManageAccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_bac_data) {
                Intent intent = new Intent(HomeActivity.this, BACDataActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            }
            return false;
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        progressBar = findViewById(R.id.progressBar);
        textViewBlow = findViewById(R.id.textView_blow);
        buttonStartRecording = findViewById(R.id.btn_start_recording);
        btnCancelRecording = findViewById(R.id.btn_cancel_recording);
        btnAccountHistory = findViewById(R.id.button_account_history);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        bacDisplay = findViewById(R.id.bac_display);
        bacMlDisplay = findViewById(R.id.bac_ml_display);
        timeUntilSoberDisplay = findViewById(R.id.time_until_sober_display);
        btnInstructions = findViewById(R.id.btn_more_info);
        btnBluetooth = findViewById(R.id.btn_bluetooth);
        btnPairDevices = findViewById(R.id.btn_pairdevices);
        bluetoothStatusDisplay = findViewById(R.id.bluetooth_status_display);

        SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay, buttonStartRecording, btnBluetooth, btnInstructions, btnCancelRecording,  btnAccountHistory, btnPairDevices, bluetoothStatusDisplay, textViewBlow);

        buttonStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                textViewBlow.setVisibility(View.VISIBLE);
                buttonStartRecording.setEnabled(false);
                btnCancelRecording.setVisibility(View.VISIBLE);
                isRecording = true;
                startProgressBar();
            }
        });
        btnCancelRecording.setOnClickListener(v -> cancelRecording());

        btnAccountHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AccountHistoryActivity.class);
            intent.putExtra("currentUserId", currentUserId);
            startActivity(intent);
        });

        btnInstructions.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoreInfoActivity.class);
            startActivity(intent);
        });

        btnBluetooth.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
            setupBluetoothService();
            updateBluetoothStatus();
        });

        btnPairDevices.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            } else {
                showDeviceListDialog();
            }
        });

        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        Log.d(TAG, "onCreate");
        updateUI(currentUserId);

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    moveTaskToBack(true);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        updateMenuItems();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATIONS);
            }
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                    sendTokenToServer(token);
                });

        if (!isBound) {
            Intent btserviceIntent = new Intent(this, BluetoothService.class);
            bindService(btserviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        updateBluetoothStatus();

    }

    private void sendTokenToServer(String token) {
        String url = "https://clearbreath-14b67f8b2024.herokuapp.com/send-notification";
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject payload = new JSONObject();
        try {
            payload.put("token", token);
            payload.put("title", "Hello from Clearbreath");
            payload.put("body", "Have you updated to the latest version of Clearbreath?");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Token sent successfully: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Failed to send token", error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void cancelRecording() {
        isRecording = false;
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        textViewBlow.setVisibility(View.GONE);
        buttonStartRecording.setEnabled(true);
        btnCancelRecording.setVisibility(View.GONE);
        Toast.makeText(this, "Recording cancelled", Toast.LENGTH_SHORT).show();
    }

    private void startProgressBar() {
        // Reset progress status
        progressStatus = 0;
        progressBar.setProgress(progressStatus);

        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        // Sleep for 150 milliseconds to simulate the progress
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler.post(new Runnable() {
                    public void run() {
                        textViewBlow.setVisibility(View.GONE);
                        btnCancelRecording.setVisibility(View.GONE);
                        buttonStartRecording.setEnabled(true);
                    }
                });
            }
        }).start();
    }
    private void updateMenuItems() {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("loggedIn", false);

        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
        updateUI(currentUserId);
    }


    private void showDeviceListDialog() {
        DeviceListDialogFragment dialogFragment = new DeviceListDialogFragment();
        dialogFragment.setDeviceListListener(device -> {
            if (isBound && bluetoothService != null) {
                if (bluetoothService.isDeviceConnected(device)) {
                    Toast.makeText(HomeActivity.this, "Device is already paired and connected", Toast.LENGTH_SHORT).show();
                } else {
                    bluetoothService.pairDevice(this, device, bluetoothStatusDisplay);
                }
            }
        });
        dialogFragment.show(getSupportFragmentManager(), "deviceListDialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bacDisplay != null && bacMlDisplay != null && timeUntilSoberDisplay != null && btnStartRecording != null && btnInstructions != null) {
            SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay, btnStartRecording, btnInstructions);
        } else {
            Log.e(TAG, "One or more UI elements are null");
        }
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        updateMenuItems();
        updateBluetoothStatus();
        Log.d(TAG, "onResume");

        if (!isBound) {
            Intent serviceIntent = new Intent(this, BluetoothService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (bluetoothService != null) {
            bluetoothService.setBluetoothDataListener(this);
            updateBluetoothStatus();
        }
    }

    private void scheduleSoberNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SoberNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long soberTimeMillis = System.currentTimeMillis() + (long) (calculateSoberTimeInMillis());
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, soberTimeMillis, pendingIntent);
        }
    }

    private long calculateSoberTimeInMillis() {
        double bac = 0.1; // Example BAC value, replace with actual value from your sensor
        double soberTimeHours = bac / 0.015; // On average, BAC decreases by 0.015% per hour
        return (long) (soberTimeHours * 3600 * 1000); // Convert hours to milliseconds
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
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                setupBluetoothService();
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with showing notifications
            } else {
                // Permission denied, handle appropriately
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBluetoothService() {
        if (isBound && bluetoothService != null) {
            bluetoothService.setupBluetooth(this, bluetoothStatusDisplay);
        } else {
            Toast.makeText(this, "Bluetooth service not connected", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDataReceived(String data) {
        processReceivedData(data);
    }

    private void processReceivedData(String data) {
        try {
            data = data.replace("BAC:", "").trim();
            if (data.isEmpty()) {
                return;
            }

            double bac = Double.parseDouble(data);
            int bacProgress = (int) (bac * 1000);

            int progressBarColor;
            if (bac < 0.02) {
                progressBarColor = Color.GREEN;
            } else if (bac < 0.05) {
                progressBarColor = Color.YELLOW;
            } else if (bac < 0.08) {
                progressBarColor = Color.rgb(255, 165, 0);
            } else {
                progressBarColor = Color.RED;
            }

            bacDisplay.setText(String.format("BAC: %.3f%%", bac));
            circularProgressBar.setProgressWithAnimation(bacProgress, 1000L);
            circularProgressBar.setProgressBarColor(progressBarColor);

            double bacMl = bac * 1000;
            bacMlDisplay.setText(String.format("BAC in mL: %.3f mL", bacMl));

            double hoursUntilSober = bac / 0.015;
            timeUntilSoberDisplay.setText(String.format("Time Until Sober: %.1f hours", hoursUntilSober));

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
            // Save BAC data for the current user
            dbHelper.insertBACRecord(currentUserId, timestamp, bac);

        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid BAC data received", e);
        }
    }

    private void updateBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        isBluetoothOn = bluetoothAdapter != null && bluetoothAdapter.isEnabled();

        if (isBound && bluetoothService != null) {
            String connectedDeviceName = bluetoothService.getConnectedDeviceName();
            Log.d(TAG, "Connected device name in updateBluetoothStatus: " + connectedDeviceName);
            if (connectedDeviceName != null) {
                bluetoothStatusDisplay.setText("Status: Connected to " + connectedDeviceName);
                bluetoothStatusDisplay.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                bluetoothStatusDisplay.setText("Status: Not connected");
                bluetoothStatusDisplay.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } else {
            bluetoothStatusDisplay.setText("Status: Not connected");
            bluetoothStatusDisplay.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        toggleBluetooth();
    }

    @SuppressLint("MissingPermission")
    private void toggleBluetooth() {
        String status = bluetoothStatusDisplay.getText().toString();

        if (status.contains("Not connected")) {
            btnBluetooth.setText("Bluetooth Off");
        } else {
            btnBluetooth.setText("Bluetooth On");
        }

    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            bluetoothService.setBluetoothDataListener(HomeActivity.this);
            isBound = true;
            updateBluetoothStatus();

            if (allPermissionsGranted()) {
                bluetoothService.setupBluetooth(HomeActivity.this, bluetoothStatusDisplay);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            bluetoothService = null;
            updateBluetoothStatus();
        }
    };

    private void scheduleSoberNotification(double currentBAC) {
        long timeUntilSober = (long) (currentBAC / 0.015 * 3600000); // Convert to milliseconds

        // ATTENTION
        double bac = 2.3;

        // Made bac 2.3 for now. Double.parseDouble(data)

        try {
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

    private void updateUI(int currentUserId) {
        MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
        if (currentUserId == -1) {
            accountMenuItem.setTitle("Account");
        } else {
            Cursor cursor = dbHelper.getAccount(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
                accountMenuItem.setTitle(username);
                cursor.close();
            } else {
                accountMenuItem.setTitle("Account");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (onBackPressedCallback != null && onBackPressedCallback.isEnabled()) {
                onBackPressedCallback.handleOnBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}