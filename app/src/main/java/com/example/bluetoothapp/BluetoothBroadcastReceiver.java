package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
                    main.updateScanMenuItemVisibility(true);
                    if (main.foreground) main.startDiscovery();
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    main.setBluetoothStatusInPreferences(false);
                    main.updateScanMenuItemVisibility(false);
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
                Log.d(TAG, "onReceive: Discovery started");
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                main.updateScanMenuItemText(false);
                Log.d(TAG, "onReceive: Discovery finished");
                break;

            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Log.d(TAG, "onReceive: Scan mode: " + BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                } else if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    Log.d(TAG, "onReceive: Scan mode: " + BluetoothAdapter.SCAN_MODE_CONNECTABLE);
                } else if (scanMode == BluetoothAdapter.SCAN_MODE_NONE) {
                    Log.d(TAG, "onReceive: Scan mode: " + BluetoothAdapter.SCAN_MODE_NONE);
                }
                break;

            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);

                if (bondState == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "onReceive: bonding initiated");
                    main.handleBondStateChanged(btDevice, BluetoothDevice.BOND_BONDING);
                } else if (bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "onReceive: bonded");
                    main.handleBondStateChanged(btDevice, BluetoothDevice.BOND_BONDED);
                } else { // BOND_NONE
                    Log.d(TAG, "onReceive: bond removed");
                    main.handleBondStateChanged(btDevice, BluetoothDevice.BOND_NONE);
                }
                break;
        }
    }
}