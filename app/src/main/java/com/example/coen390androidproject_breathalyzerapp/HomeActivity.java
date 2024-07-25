package com.example.coen390androidproject_breathalyzerapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;
import android.view.Menu;
import android.widget.Button;
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

import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements BluetoothService.BluetoothDataListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private SharedPreferences sharedPreferences;
    private TextView bacDisplay;
    private TextView bacMlDisplay;
    private TextView timeUntilSoberDisplay;
    private CircularProgressBar circularProgressBar;
    private Button btnInstructions;
    private Button btnStartRecording;
    private Button btnBluetooth;
    private Button btnPairDevices;
    private TextView bluetoothStatusDisplay;
    private int currentUserId = -1;
    private BluetoothService bluetoothService;
    private boolean isBound = false;
    private static final String TAG = "HomeActivity";
    private NavigationView navigationView, navigationViewUI;
    private OnBackPressedCallback onBackPressedCallback;

    private static final int REQUEST_CODE_PERMISSIONS = 101;
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
        currentUserId = getIntent().getIntExtra("currentUserId", -1);
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        updateMenuItems();

        circularProgressBar = findViewById(R.id.circularProgressBar);
        bacDisplay = findViewById(R.id.bac_display);
        bacMlDisplay = findViewById(R.id.bac_ml_display);
        timeUntilSoberDisplay = findViewById(R.id.time_until_sober_display);
        btnInstructions = findViewById(R.id.btn_more_info);
        btnStartRecording = findViewById(R.id.btn_start_recording);
        btnBluetooth = findViewById(R.id.btn_bluetooth);
        btnPairDevices = findViewById(R.id.btn_pairdevices);
        bluetoothStatusDisplay = findViewById(R.id.bluetooth_status_display);


        SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay);

        btnInstructions.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoreInfoActivity.class);
            startActivity(intent);
        });

        btnStartRecording.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StartRecordingActivity.class);
            startActivity(intent);
        });

        SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay, btnStartRecording, btnInstructions);

        btnBluetooth.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            } else {
                setupBluetoothService();
            }
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

    }

    private void updateMenuItems() {
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
    }

    private void logOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();
        updateMenuItems();
        Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay);
        SettingsUtils.applySettings(this, bacDisplay, bacMlDisplay, timeUntilSoberDisplay, btnStartRecording, btnInstructions);
        updateBluetoothStatus();
        updateMenuItems();
        Log.d(TAG, "onResume");
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
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private String calculateTimeUntilSober(double bac) {
        double soberTimeHours = bac / 0.015; // On average, BAC decreases by 0.015% per hour
        int hours = (int) soberTimeHours;
        int minutes = (int) ((soberTimeHours - hours) * 60);
        return String.format("Time until sober: %d hours %d minutes", hours, minutes);
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
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                setupBluetoothService();
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
            data = data.replace("BAC:", "").trim(); // Remove "BAC:" prefix
            if (data.isEmpty()) {
                return;
            }

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

            // Save BAC data to the account
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            dbHelper.updateAccount(currentUserId, null, null, null, null, -1, null, -1.0);

        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid BAC data received", e);
        }
    }

    private void updateBluetoothStatus() {
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
        if (currentUserId == -1) {
            MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
            accountMenuItem.setTitle("Account");
        } else {
            Cursor cursor = dbHelper.getAccount(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
                MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
                accountMenuItem.setTitle(username);
                cursor.close();
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
