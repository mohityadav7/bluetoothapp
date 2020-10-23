package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, BluetoothBroadcastReceiver.BluetoothBroadcastReceiverListener {

    private static String TAG = MainActivity.class.getSimpleName();
    BluetoothAdapter bluetoothAdapter;
    BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
    SettingsFragment settingsFragment;
    HashSet<BluetoothDevice> discoveredDevices;
    Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add preferences fragment
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment, "SettingsFragment")
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
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver(this);
        registerReceiver(bluetoothBroadcastReceiver, bluetoothIntentFilter);
    }

    // inflate options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        optionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // start and stop discovery
        if (item.getItemId() == R.id.scan_menu_item) {
            if (item.getTitle().equals(this.getResources().getString(R.string.scan))) {
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    if (checkLocationPermission()) {
                        // clear previously discovered devices
                        discoveredDevices = new HashSet<>();
                        SettingsFragment fragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("SettingsFragment");
                        if (fragment != null && fragment.isAdded()) {
                            fragment.clearDiscoveredDevicePreferenceCategory();
                        }
                        // start discovery
                        bluetoothAdapter.startDiscovery();
                    } else {
                        Toast.makeText(this, "Allow location permission first", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // stop discovery
                if (bluetoothAdapter != null) bluetoothAdapter.cancelDiscovery();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        // turn on/off bluetooth based on bluetooth preference
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
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment(), "SettingsFragment").commitAllowingStateLoss();
    }

    // update scan menuItem text in options menu
    public void updateScanMenuItemText(boolean isDiscovering) {
        MenuItem menuItem = optionsMenu.findItem(R.id.scan_menu_item);
        if (isDiscovering) {
            menuItem.setTitle(this.getResources().getString(R.string.stop));
        } else {
            menuItem.setTitle(this.getResources().getString(R.string.scan));
        }
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
    public void handleNewDeviceDiscovered(BluetoothDevice device) {
        SettingsFragment fragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("SettingsFragment");
        if (fragment != null && fragment.isAdded()) {
            if (!discoveredDevices.contains(device)) {
                fragment.addDiscoveredDevicesPreferences(device);
                discoveredDevices.add(device);
            }
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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