package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    private MainActivity main;

    public BluetoothBroadcastReceiver(MainActivity mainActivityContext) {
        super();
        main = mainActivityContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

            if (state == BluetoothAdapter.STATE_ON) {
                main.setBluetoothStatusInPreferences(true);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                main.setBluetoothStatusInPreferences(false);
            }
        }
    }
}