package com.example.bluetoothapp;

import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int START_DISCOVERY = 1;
    private static String TAG = MainActivity.class.getSimpleName();
    boolean foreground;
    BluetoothAdapter bluetoothAdapter;
    BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
    SettingsFragment settingsFragment;
    Set<BluetoothDevice> availableDevicesSet;
    Set<BluetoothDevice> pairedDevicesSet;
    Menu optionsMenu;
    Timer timer;

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
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver(this);
        registerReceiver(bluetoothBroadcastReceiver, bluetoothIntentFilter);

        // initialize pairedDevicesSet
        pairedDevicesSet = Utils.getPairedDevices();
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
                    requestLocationPermissionForStartingDiscovery();
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
        // turn on/off bluetooth based on bluetooth preference and make device discoverable
        if (key.equals("bluetooth")) {
            if (preferences.getBoolean(key, false)) {
                turnOnBluetooth();
                Utils.makeDiscoverableAfterDelay(timer, 2000);
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

    public void updateScanMenuItemVisibility(boolean visible) {
        MenuItem menuItem = optionsMenu.findItem(R.id.scan_menu_item);
        menuItem.setVisible(visible);
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

    public void startDiscovery() {
        // clear previously available devices
        availableDevicesSet = new HashSet<>();
        SettingsFragment fragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("SettingsFragment");
        if (fragment != null && fragment.isAdded()) {
            fragment.clearAvailableDevicePreferenceCategory();
        }
        // start discovery
        Utils.checkIfLocationEnabled(this);
        bluetoothAdapter.startDiscovery();
    }

    public void handleNewDeviceAvailable(BluetoothDevice device) {
        SettingsFragment fragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("SettingsFragment");
        if (fragment != null && fragment.isAdded()) {
            if (availableDevicesSet != null && !availableDevicesSet.contains(device)) {
                fragment.addAvailableDevicesPreferences(device);
                availableDevicesSet.add(device);
            }
        }
    }

    public void handleBondStateChanged(BluetoothDevice device, int bondState) {
        Set<BluetoothDevice> prevPairedDevicesSet = pairedDevicesSet;
        pairedDevicesSet = Utils.getPairedDevices();
        SettingsFragment fragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("SettingsFragment");
        if (fragment != null && fragment.isAdded()) {
            if (bondState == BluetoothDevice.BOND_BONDING) {
                fragment.updateAvailableDeviceSummary(device, bondState, null);
            } else if (bondState == BluetoothDevice.BOND_BONDED) {
                fragment.updatePairedDevicesPreferences();
                fragment.updateAvailableDeviceSummary(device, bondState, null);
            } else { // BOND_NONE
                fragment.updatePairedDevicesPreferences();
                if (!prevPairedDevicesSet.contains(device) && !pairedDevicesSet.contains(device)) {
                    fragment.updateAvailableDeviceSummary(device, bondState, null);
                }
                Log.d(TAG, "handleBondStateChanged: bond removed or could not pair");
            }
        }
    }

    private void requestLocationPermissionForStartingDiscovery() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Grant location permission")
                    .setMessage("Location permission is required for starting discovery")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission.ACCESS_FINE_LOCATION}, START_DISCOVERY);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission.ACCESS_FINE_LOCATION}, START_DISCOVERY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == START_DISCOVERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDiscovery();
            } else {
                Toast.makeText(this, "Could not start discovery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        Utils.makeDeviceNotDiscoverable();
        foreground = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        Utils.makeDeviceDiscoverable();
        foreground = true;
        super.onResume();
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