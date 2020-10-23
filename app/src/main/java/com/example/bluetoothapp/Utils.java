package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AlertDialog;
import androidx.core.location.LocationManagerCompat;

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

    public static void checkIfLocationEnabled(final Context context) {
        boolean gpsEnabled = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            gpsEnabled = LocationManagerCompat.isLocationEnabled(locationManager);
        } catch (Exception e) {
            Log.d(TAG, "checkIfGPSOn: Could not get location status, " + e.getMessage());
        }

        if (!gpsEnabled) {
            // notify user
            new AlertDialog.Builder(context)
                    .setTitle("Turn on location")
                    .setMessage("Location must be on in order to start device discovery")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }
                    )
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
