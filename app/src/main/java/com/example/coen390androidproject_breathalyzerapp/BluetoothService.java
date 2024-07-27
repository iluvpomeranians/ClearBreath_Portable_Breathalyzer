package com.example.coen390androidproject_breathalyzerapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final String DEVICE_NAME = "ClearBreath";
    private static final String TAG = "BluetoothService";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private BluetoothDataListener dataListener;
    private Handler handler = new Handler();

    private final IBinder binder = new LocalBinder();

    public interface BluetoothDataListener {
        void onDataReceived(String data);
    }

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setBluetoothDataListener(BluetoothDataListener listener) {
        dataListener = listener;
    }

    public boolean isBluetoothSupported(Context context) {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
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
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(enableBtIntent);
    }

    @SuppressLint("MissingPermission")
    public void setupBluetooth(Context context, TextView bluetoothStatusDisplay) {
        if (!isBluetoothSupported(context)) {
            bluetoothStatusDisplay.setText("Status: Bluetooth not supported");
            return;
        }

        if (!isBluetoothEnabled()) {
            requestEnableBluetooth(context);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                if (DEVICE_NAME.equals(device.getName())) {
                    connectToDevice(context, device, bluetoothStatusDisplay);
                    break;
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(Context context, BluetoothDevice device, TextView bluetoothStatusDisplay) {
        new Thread(() -> {
            try {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    closeConnection();
                    handler.post(() -> {
                        Toast.makeText(context, "Bluetooth turned off", Toast.LENGTH_SHORT).show();
                        bluetoothStatusDisplay.setText("Status: Bluetooth turned off");
                        bluetoothStatusDisplay.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    });
                    return;
                }

                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                handler.post(() -> {
                    Toast.makeText(context, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
                    bluetoothStatusDisplay.setText("Status: Connected to " + device.getName());
                    bluetoothStatusDisplay.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                });
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

    @SuppressLint("MissingPermission")
    public void pairDevice(Context context, BluetoothDevice device, TextView bluetoothStatusDisplay) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            connectToDevice(context, device, bluetoothStatusDisplay);
        } else {
            device.createBond();
            final BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                        final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                        final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                            context.unregisterReceiver(this);
                            connectToDevice(context, device, bluetoothStatusDisplay);
                        } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING) {
                            context.unregisterReceiver(this);
                            handler.post(() -> {
                                Toast.makeText(context, "Pairing failed", Toast.LENGTH_SHORT).show();
                                bluetoothStatusDisplay.setText("Status: Pairing failed");
                                bluetoothStatusDisplay.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                            });
                        }
                    }
                }
            };

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            context.registerReceiver(receiver, filter);
        }
    }

    public boolean isDeviceConnected(BluetoothDevice device) {
        return bluetoothSocket != null && bluetoothSocket.isConnected() && bluetoothSocket.getRemoteDevice().equals(device);
    }


    private void listenForData() {
        new Thread(() -> {
            try {
                inputStream = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;
                while (true) {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "Incoming message: " + incomingMessage);
                    handler.post(() -> {
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

    @SuppressLint("MissingPermission")
    public String getConnectedDeviceName() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            String deviceName = bluetoothSocket.getRemoteDevice().getName();
            Log.d(TAG, "Connected device name: " + deviceName);
            return deviceName;
        }
        Log.d(TAG, "No connected device");
        return null;
    }

    public void closeConnection() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing connection", e);
        }
    }
}
