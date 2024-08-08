package com.example.coen390androidproject_breathalyzerapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {

    public interface BluetoothDataListener {
        void onDataReceived(String data);
    }

    private static final String DEVICE_NAME = "ClearBreath";
    private static final String TAG = "BluetoothHelper";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private TextView bluetoothStatusDisplay;
    private Handler handler;
    private BluetoothDataListener dataListener;

    public BluetoothHelper(TextView bluetoothStatusDisplay, Handler handler, BluetoothDataListener dataListener) {
        this.bluetoothStatusDisplay = bluetoothStatusDisplay;
        this.handler = handler;
        this.dataListener = dataListener;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothSupported(Context context) {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            bluetoothStatusDisplay.setText("Status: Bluetooth not supported");
            return false;
        }
        return true;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public void requestEnableBluetooth(Context context) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        context.startActivity(enableBtIntent);
    }

    @SuppressLint("MissingPermission")
    public void setupBluetooth(Context context) {
        // Check if Bluetooth is supported on the device
        if (!isBluetoothSupported(context)) {
            return;
        }

        // Check if Bluetooth is enabled, if not, request to enable it
        if (!isBluetoothEnabled()) {
            requestEnableBluetooth(context);
        }

        // Get the list of paired Bluetooth devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            // Iterate through the paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Check if the device name matches the target device name
                if (DEVICE_NAME.equals(device.getName())) {
                    // Connect to the target device
                    connectToDevice(context, device);
                    break;
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(Context context, BluetoothDevice device) {
        new Thread(() -> {
            try {
                // Check if already connected
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    closeConnection();
                    handler.post(() -> {
                        Toast.makeText(context, "Bluetooth turned off", Toast.LENGTH_SHORT).show();
                        bluetoothStatusDisplay.setText("Status: Bluetooth turned off");
                        bluetoothStatusDisplay.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    });
                    return;
                }

                // Create a socket to connect to the device
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                handler.post(() -> {
                    Toast.makeText(context, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
                    bluetoothStatusDisplay.setText("Status: Connected to " + device.getName());
                    bluetoothStatusDisplay.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                });
                // Start listening for data from the device
                listenForData();
            } catch (IOException e) {
                Log.e(TAG, "Can't connect to " + device.getName(), e);
                handler.post(() -> {
                    Toast.makeText(context, "Failed to connect to " + device.getName(), Toast.LENGTH_SHORT).show();
                    bluetoothStatusDisplay.setText("Status: Connection failed");
                    bluetoothStatusDisplay.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                });
            }
        }).start();
    }

    private void listenForData() {
        new Thread(() -> {
            try {
                // Get the input stream from the Bluetooth socket
                inputStream = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;
                while (true) {
                    // Read data from the input stream
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "Incoming message: " + incomingMessage);
                    handler.post(() -> {
                        // Notify the data listener with the received data
                        if (dataListener != null) {
                            dataListener.onDataReceived(incomingMessage.trim());
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading data", e);
            }
        }).start();
    }

    public void closeConnection() {
        try {
            // Close the input stream if it is not null
            if (inputStream != null) {
                inputStream.close();
            }
            // Close the Bluetooth socket if it is not null
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing connection", e);
        }
    }
}
