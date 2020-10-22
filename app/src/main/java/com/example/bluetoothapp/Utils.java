package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.HashSet;
import java.util.Set;

public class Utils {

    // get paired devices
    public static Set<BluetoothDevice> getPairedDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return new HashSet<>();
        return bluetoothAdapter.getBondedDevices();
    }
}
