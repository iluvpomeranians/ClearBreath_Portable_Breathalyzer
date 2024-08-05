package com.example.coen390androidproject_breathalyzerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Typeface;
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
import android.view.View;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
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
    private boolean isSimulating = false;
    int userAge;
    String userGender;
    double userBMI;
    private TextView textView_Calculating;
    private SquircleButton button_retake_recording, button_save_recording, button_cancel_recording;

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
    private SquircleButton btnInstructions, buttonEmergency;
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
    private boolean isCalculating = true;
    private boolean isFromNotification = false;
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_IS_FROM_NOTIFICATION = "isFromNotification";
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final int REQUEST_CODE_NOTIFICATIONS = 102;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private DBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Log.d(TAG, "onCreate: isFromNotification = " + isFromNotification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATIONS);
            }
        }

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
        updateMenuItems();

        textView_Calculating = findViewById(R.id.textView_Calculating);
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
        button_retake_recording = findViewById(R.id.button_retake_recording);
        button_save_recording = findViewById(R.id.button_save_recording);
        button_cancel_recording = findViewById(R.id.button_cancel_recording);
        buttonEmergency = findViewById(R.id.button_emergency);
        SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay, buttonStartRecording, btnBluetooth, btnInstructions, btnCancelRecording,
                btnAccountHistory, btnPairDevices, bluetoothStatusDisplay, textViewBlow, button_retake_recording, button_save_recording, button_cancel_recording, textView_Calculating);



        buttonStartRecording.setOnClickListener(v -> {startRecording();});
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
        button_retake_recording.setOnClickListener(v -> {
            if (isRecording) {
                cancelRecording();
            }
                else
                {
                    startRecording();
                }
        });
        button_save_recording.setOnClickListener(v -> {
                    if (isRecording) {
                        cancelRecording();
                    }
                    else
                    {
                        //TODO: Save recording
                    }
                });
        button_cancel_recording.setOnClickListener(v -> {cancelRecording();});


        buttonEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EmergencyActivity.class);
            startActivity(intent);
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

        if (sharedPreferences.getBoolean(KEY_IS_FROM_NOTIFICATION, false)) {
            // Reset the flag
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_FROM_NOTIFICATION, false);
            editor.apply();
            Log.d(TAG, "onCreate: isFromNotification reset to false");
        }

        onNewIntent(getIntent());

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
        btnCancelRecording.setVisibility(View.GONE);
        textView_Calculating.setVisibility(View.GONE);
        btnInstructions.setVisibility(View.VISIBLE);
        btnBluetooth.setVisibility(View.VISIBLE);
        btnPairDevices.setVisibility(View.VISIBLE);
        btnAccountHistory.setVisibility(View.VISIBLE);
        button_save_recording.setVisibility(View.GONE);
        button_retake_recording.setVisibility(View.GONE);
        button_cancel_recording.setVisibility(View.GONE);
        buttonStartRecording.setVisibility(View.VISIBLE);


        Toast.makeText(this, "Recording cancelled", Toast.LENGTH_SHORT).show();
    }
    private void startRecording()
        {
            progressBar.setVisibility(View.VISIBLE);
            textViewBlow.setVisibility(View.VISIBLE);
            btnCancelRecording.setVisibility(View.VISIBLE);
            textView_Calculating.setVisibility(View.GONE);
            btnInstructions.setVisibility(View.GONE);
            btnBluetooth.setVisibility(View.GONE);
            btnPairDevices.setVisibility(View.GONE);
            btnAccountHistory.setVisibility(View.GONE);
            button_retake_recording.setVisibility(View.GONE);
            button_save_recording.setVisibility(View.GONE);
            button_cancel_recording.setVisibility(View.GONE);
            buttonStartRecording.setVisibility(View.GONE);
            isRecording = true;
            startSimulation();
            startProgressBar();

        }


    private void startProgressBar() {
        // Reset progress status
        progressStatus = 0;
        progressBar.setProgress(progressStatus);


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressStatus < 100) {
                    if (!isRecording) {
                        return;
                    }
                    progressStatus += 1;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            circularProgressBar.setProgress(progressStatus);
                            if (progressStatus == 100) {
                                progressBar.setVisibility(View.GONE);
                                textViewBlow.setVisibility(View.GONE);
                                isRecording = false;
                                isCalculating = true;
                                btnCancelRecording.setVisibility(View.GONE);
                                if (isCalculating)
                                {
                                    startBACCalculation();
                                }

                                // Simulate the BAC value reading
                                //double bacValue = new Random().nextDouble() * 0.2; // Simulated BAC value
                                //updateBACUI(bacValue);
                            }
                        }
                    });
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }

                progressStatus = 0;

            }

        }).start();
    }
    private void startBACCalculation() {
        List<Double> bacValues = new ArrayList<>();
        handler.postDelayed(new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (count < 4) {
                    // Simulate BAC value reading
                    double bacValue = new Random().nextDouble() * 0.2; // Replace with actual BAC value retrieval logic
                    bacValues.add(bacValue);

                    count++;
                    handler.postDelayed(this, 1000); // Run this every 1 second for 7 seconds
                } else {
                    textView_Calculating.setVisibility(View.VISIBLE);
                    // Calculate the median BAC value
                    Collections.sort(bacValues);
                    double median;
                    if (bacValues.size() % 2 == 0) {
                        median = (bacValues.get(bacValues.size() / 2 - 1) + bacValues.get(bacValues.size() / 2)) / 2.0;
                    } else {
                        median = bacValues.get(bacValues.size() / 2);
                    }
                    // Update the UI with the median BAC value
                    if (currentUserId != -1) {
                        Cursor cursor = dbHelper.getAccount(currentUserId);
                        if (cursor != null && cursor.moveToFirst()) {
                            userAge = (cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE)));
                            userGender = (cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_GENDER)));
                            userBMI = (cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI)));
                            cursor.close();
                        }
                    }
                    if (currentUserId != -1) {
                        if (count<7)
                        {
                            count++;
                            handler.postDelayed(this, 1000);
                            textView_Calculating.setVisibility(View.VISIBLE);
                        }
                            else
                            {
                                double adjustedBAC = adjustBACForUserDetails(median, userAge, userGender, userBMI);
                                updateBACUI(adjustedBAC);
                                textView_Calculating.setVisibility(View.GONE);
                                button_retake_recording.setVisibility(View.VISIBLE);
                                button_save_recording.setVisibility(View.VISIBLE);
                                button_cancel_recording.setVisibility(View.VISIBLE);
                            }
                        }

                    else
                    {
                        if (count<7)
                        {
                            count++;
                            handler.postDelayed(this, 1000);
                            textView_Calculating.setVisibility(View.VISIBLE);
                        }
                            else
                            {

                                updateBACUI(median);
                                textView_Calculating.setVisibility(View.GONE);
                                button_retake_recording.setVisibility(View.VISIBLE);
                                button_save_recording.setVisibility(View.VISIBLE);
                                button_cancel_recording.setVisibility(View.VISIBLE);
                            }
                    }
                    stopSimulation();
                    isCalculating = false;
                }
            }
        }, 1);
    }

    private void updateBACUI(double bacValue) {
        double bacPercentage = bacValue;
        String bacText = String.format(Locale.getDefault(), "%.2f%%", bacPercentage);
        String bacMlText = String.format(Locale.getDefault(), "BAC in ml: %.2f mL", bacValue * 1000);
        String timeUntilSoberText = calculateTimeUntilSober(bacValue);

        bacDisplay.setText(bacText);
        bacMlDisplay.setText(bacMlText);
        timeUntilSoberDisplay.setText(timeUntilSoberText);
        circularProgressBar.setProgress((float) (bacPercentage / 0.2 * 100));
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
        applySettings();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);

        if (bacDisplay != null && bacMlDisplay != null && timeUntilSoberDisplay != null && btnStartRecording != null && btnInstructions != null) {
            SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay, btnStartRecording, btnInstructions, buttonEmergency);
        } else {
            Log.e(TAG, "One or more UI elements are null");
        }

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
        Log.d(TAG, "onDestroy, is from Notification " + isFromNotification);
        if (!isFromNotification) {
            super.onDestroy();
            if (isBound) {
                unbindService(serviceConnection);
                isBound = false;
            }
        }
        isFromNotification = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        setIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d(TAG, "Intent extra: " + key + " = " + extras.get(key));
            }
        }

        String fromString = intent.getStringExtra("From");
        if (fromString != null) {
            Log.d(TAG, "DEBUG HOMEAC - FROMSTRING NEW INTENT: " + fromString);
            isFromNotification = "Notification".equalsIgnoreCase(fromString);
        } else {
            Log.d(TAG, "DEBUG HOMEAC - FROMSTRING NEW INTENT: null");
        }

        if (bluetoothService != null) {
            bluetoothService.setBluetoothDataListener(this);
            updateBluetoothStatus();
        }
    }


    /*private void scheduleSoberNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SoberNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        double bacValue = 2;
        long soberTimeMillis = System.currentTimeMillis() + (long) (calculateTimeUntilSober(bacValue));
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, soberTimeMillis, pendingIntent);
        }
    }*/

    private String calculateTimeUntilSober(double bac) {
        double soberTimeHours = bac / 0.015; // On average, BAC decreases by 0.015% per hour
        int hours = (int) soberTimeHours;
        int minutes = (int) ((soberTimeHours - hours) * 60);
        return String.format(Locale.getDefault(), "%d hours %d minutes", hours, minutes);
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

            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
            currentUserId = sharedPreferences.getInt("currentUserId", -1);
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
                /*@SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_AGE));
                @SuppressLint("Range") String gender = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_GENDER));
                @SuppressLint("Range") double bmi = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_BMI));

                // Adjust BAC display based on age, gender, and BMI
                double adjustedBac = adjustBACForUserDetails(bacValue, age, gender, bmi);
                bacDisplay.setText(String.format(Locale.getDefault(), "%.2f%%", adjustedBac));

                // Calculate BAC in mL and time until sober
                double bacMl = adjustedBac * 1000; // Assuming the adjustment leads to a straightforward conversion
                bacMlDisplay.setText(String.format(Locale.getDefault(), "%.2f mL", bacMl));
                timeUntilSoberDisplay.setText(calculateTimeUntilSober(adjustedBac));*/
                cursor.close();
            } else {
                accountMenuItem.setTitle("Account");
            }
        }
    }
    private void startSimulation() {
        isSimulating = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isSimulating) {
                    simulateData();
                    try {
                        Thread.sleep(1000); // Sleep for 1 second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void stopSimulation() {
        isSimulating = false;
    }

    private void simulateData() {
        Random random = new Random();
        double randomBAC = random.nextDouble() * 0.15; // Generates a random BAC value between 0.000 and 0.150
        final String simulatedData = "BAC: " + String.format(Locale.ENGLISH, "%.3f", randomBAC);

        // Updating the UI should be done on the main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processReceivedData(simulatedData);
            }
        });
    }
    private double adjustBACForUserDetails(double bac, int age, String gender, double bmi) {
        // Adjust BAC
        double beta = 0.2; // coefficient for BMI adjustment, changeable
        double gamma = 0.1; // coefficient for age adjustment, changeable
        double rBase = gender.equalsIgnoreCase("male") ? 0.7 : 0.85; // rBase value based on gender

        // Calculate the adjusted BAC
        double adjustedBac = bac * (1 + beta * ((25.0 / bmi) - 1)) * (1 + gamma * ((30.0 / age) - 1)) * rBase;
        return adjustedBac;
    }
    @Override
    public void onBluetoothDataReceived(double bacValue) {
        if (currentUserId != -1) {
            //this.bacValue = bacValue; // Store the BAC value received, need this from sensor
            updateUI(currentUserId);
        }
    }

    private void applySettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Apply toolbar color
        int toolbarColor = sharedPreferences.getInt("toolbar_color", Color.BLUE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(toolbarColor);
        }

        // Apply status bar color
        setStatusBarColor(toolbarColor);

        // Apply text size
        int textSize = sharedPreferences.getInt("text_size", 15);
        TextView sampleTextView = findViewById(R.id.sample_text_view); // Adjust to your actual view
        if (sampleTextView != null) {
            sampleTextView.setTextSize(textSize);
        }

        // Apply font type
        int fontIndex = sharedPreferences.getInt("font_index", 0);
        String[] fonts = SettingsActivity.getFonts();
        if (fontIndex >= 0 && fontIndex < fonts.length) {
            Typeface typeface = Typeface.create(fonts[fontIndex], Typeface.NORMAL);
            if (sampleTextView != null) {
                sampleTextView.setTypeface(typeface);
            }
        }
    }

    private void setStatusBarColor(int color) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
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