package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    private MainActivity main;

    public BluetoothBroadcastReceiver(MainActivity mainActivityContext) {
        super();
        main = mainActivityContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) throw new AssertionError();
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                if (state == BluetoothAdapter.STATE_ON) {
                    main.setBluetoothStatusInPreferences(true);
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    main.setBluetoothStatusInPreferences(false);
                }
                break;
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    main.handleNewDeviceAvailable(device);
                }
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                main.updateScanMenuItemText(true);
                Toast.makeText(context, "Discovery started", Toast.LENGTH_SHORT).show();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                main.updateScanMenuItemText(false);
                Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show();
                break;
            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Toast.makeText(context, "Discoverable now", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onReceive: Scan mode: " + BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                } else if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    Toast.makeText(context, "Not discoverable", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onReceive: Scan mode: " + BluetoothAdapter.SCAN_MODE_CONNECTABLE);
                } else if (scanMode == BluetoothAdapter.SCAN_MODE_NONE) {
                    Log.d(TAG, "onReceive: Scan mode: " + BluetoothAdapter.SCAN_MODE_NONE);
                }
        }
    }
}