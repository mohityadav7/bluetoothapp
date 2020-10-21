package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static String TAG = MainActivity.class.getSimpleName();
    BluetoothAdapter bluetoothAdapter;
    BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
    SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add preferences fragment
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();

        // register on preference change listener
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        preferences.registerOnSharedPreferenceChangeListener(this);

        // initialize bluetooth adapter and bluetooth status
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initBluetoothStatus(); // because bluetooth broadcast is not sticky

        // create and register bluetooth broadcast listener
        IntentFilter bluetoothIntentFilter = new IntentFilter();
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver(this);
        registerReceiver(bluetoothBroadcastReceiver, bluetoothIntentFilter);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals("bluetooth")) {
            if (preferences.getBoolean(key, false)) {
                turnOnBluetooth();
            } else {
                turnOffBluetooth();
            }
        }
    }

    // update bluetooth status preference and update ui
    public void setBluetoothStatusInPreferences(boolean isBtOn) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean("bluetooth", isBtOn).apply();
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commitAllowingStateLoss();
    }

    // initialize bluetooth preference, used when app is first launched
    private void initBluetoothStatus() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            setBluetoothStatusInPreferences(true);
        } else if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            setBluetoothStatusInPreferences(false);
        }
    }

    private void turnOnBluetooth() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    private void turnOffBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister broadcast receivers
        unregisterReceiver(bluetoothBroadcastReceiver);
        // unregister change listeners
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }
}