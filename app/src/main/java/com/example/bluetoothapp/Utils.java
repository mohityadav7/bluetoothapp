package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class Utils {

    // get paired devices
    public static Set<BluetoothDevice> getPairedDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return new HashSet<>();
        return bluetoothAdapter.getBondedDevices();
    }

    public static void makeDeviceDiscoverable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            try {
                Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.d(TAG, "makeDeviceDiscoverable: Failed to turn on discoverability");
            }
        }
    }

    public static void makeDeviceNotDiscoverable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
            } else {
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_NONE);
            }
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.d(TAG, "makeDeviceNotDiscoverable: Failed to turn off bluetooth device discoverability");
        }
    }

    // to make device discoverable after turning on bluetooth, enabling bluetooth is not instantaneous
    public static synchronized void makeDiscoverableAfterDelay(Timer timer, int timeInMilliseconds) {
        if (timer != null) timer.cancel();
        timer = new Timer();

        TimerTask action = new TimerTask() {
            public void run() {
                Log.d(TAG, "run: making discoverable");
                Utils.makeDeviceDiscoverable();
            }
        };

        timer.schedule(action, timeInMilliseconds);
    }
}
