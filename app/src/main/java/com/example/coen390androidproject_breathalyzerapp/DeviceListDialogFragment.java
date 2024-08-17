package com.example.coen390androidproject_breathalyzerapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class DeviceListDialogFragment extends DialogFragment {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    // BroadcastReceiver to handle Bluetooth device discovery events
    private BroadcastReceiver receiver;

    // Interface to communicate the selected device back to the activity
    public interface DeviceListListener {
        void onDeviceSelected(BluetoothDevice device);
    }

    // Listener instance to notify the selected device
    private DeviceListListener listener;

    // Method to set the listener
    public void setDeviceListListener(DeviceListListener listener) {
        this.listener = listener;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_device_list, container, false);

        // Initialize the ListView and its adapter
        ListView listView = view.findViewById(R.id.device_list_view);
        deviceListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        listView.setAdapter(deviceListAdapter);

        // Set item click listener to handle device selection
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            if (listener != null) {
                listener.onDeviceSelected(deviceList.get(position));
            }
            dismiss();
        });

        // Get the default Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            // Cancel any ongoing discovery
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            // Start discovery for Bluetooth devices
            bluetoothAdapter.startDiscovery();

            // Register for broadcasts when a device is discovered
            receiver = new BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null && !deviceList.contains(device)) {
                            deviceList.add(device);
                            deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
                        }
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        // Discovery has finished
                        if (deviceListAdapter.getCount() == 0) {
                            deviceListAdapter.add("No devices found");
                        }
                    }
                }
            };

            // Register the receiver for the necessary actions
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            requireActivity().registerReceiver(receiver, filter);
        }

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the receiver when the view is destroyed
        if (receiver != null) {
            requireActivity().unregisterReceiver(receiver);
        }
        // Cancel discovery if it is still ongoing
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create and return a dialog with a title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Select Device");
        return dialog;
    }
}
