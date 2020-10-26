package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int AUDIO_VIDEO = 1024;
    private static final int COMPUTER = 256;
    private static final int PHONE = 512;
    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        addPairedDevicesPreferences();
    }

    private void addPairedDevicesPreferences() {
        Set<BluetoothDevice> pairedDevices = Utils.getPairedDevices();

        if (pairedDevices.size() > 0) {
            PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();

            // create paired devices preference category
            PreferenceCategory pairedDevicesPreferenceCategory = new PreferenceCategory(preferenceScreen.getContext());
            pairedDevicesPreferenceCategory.setTitle("Paired devices");
            preferenceScreen.addPreference(pairedDevicesPreferenceCategory);

            for (BluetoothDevice device : pairedDevices) {
                // create new paired device preference
                Preference pairedDevicePreference = new Preference(preferenceScreen.getContext());
                pairedDevicePreference.setLayoutResource(R.layout.paired_device_preference_layout);
                if(device.getName() != null) {
                    pairedDevicePreference.setTitle(device.getName());
                } else {
                    pairedDevicePreference.setTitle(device.getAddress());
                }
                pairedDevicePreference.setKey(device.getAddress());
                pairedDevicePreference.setWidgetLayoutResource(R.layout.preference_widget_layout);
                switch (device.getBluetoothClass().getMajorDeviceClass()) {
                    case AUDIO_VIDEO:
                        pairedDevicePreference.setIcon(R.drawable.ic_headset);
                        break;
                    case COMPUTER:
                        pairedDevicePreference.setIcon(R.drawable.ic_laptop);
                        break;
                    case PHONE:
                        pairedDevicePreference.setIcon(R.drawable.ic_phone);
                        break;
                    default:
                        pairedDevicePreference.setIcon(R.drawable.ic_other);
                }
                // add paired device preference to paired device preference category
                pairedDevicesPreferenceCategory.addPreference(pairedDevicePreference);
            }
        }
    }

    public void addPairedDevicesPreference(final BluetoothDevice device) {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        PreferenceCategory pairedDevicesPreferenceCategory = (PreferenceCategory) preferenceScreen.getPreference(2);

        // create new paired device preference
        Preference pairedDevicePreference = new Preference(preferenceScreen.getContext());
        pairedDevicePreference.setLayoutResource(R.layout.paired_device_preference_layout);
        if(device.getName() != null) {
            pairedDevicePreference.setTitle(device.getName());
        } else {
            pairedDevicePreference.setTitle(device.getAddress());
        }
        pairedDevicePreference.setKey(device.getAddress());
        pairedDevicePreference.setWidgetLayoutResource(R.layout.preference_widget_layout);
        switch (device.getBluetoothClass().getMajorDeviceClass()) {
            case AUDIO_VIDEO:
                pairedDevicePreference.setIcon(R.drawable.ic_headset);
                break;
            case COMPUTER:
                pairedDevicePreference.setIcon(R.drawable.ic_laptop);
                break;
            case PHONE:
                pairedDevicePreference.setIcon(R.drawable.ic_phone);
                break;
            default:
                pairedDevicePreference.setIcon(R.drawable.ic_other);
        }
        // add paired device preference to paired device preference category
        pairedDevicesPreferenceCategory.addPreference(pairedDevicePreference);
    }

    public void updateAvailableDeviceSummary(BluetoothDevice device, int bondState) {
        Preference preferenceToUpdate = findPreference(device.getAddress());
        if (preferenceToUpdate != null) {
            if (bondState == BluetoothDevice.BOND_BONDING) {
                preferenceToUpdate.setSummary("Pairing...");
            } else if (bondState == BluetoothDevice.BOND_BONDED) {
                preferenceToUpdate.setSummary("Paired");
            } else if (bondState == BluetoothDevice.BOND_NONE) {
                preferenceToUpdate.setSummary("Could not pair");
            }
        }
    }

    public void addAvailableDevicesPreferences(final BluetoothDevice device) {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();

        // get available devices preference category
        PreferenceCategory availableDevicesPreferenceCategory = (PreferenceCategory) preferenceScreen.getPreference(1);
        if (!availableDevicesPreferenceCategory.isVisible()) {
            availableDevicesPreferenceCategory.setVisible(true);
        }

        // create new available device preference
        Preference availableDevicePreference = new Preference(preferenceScreen.getContext());
        if(device.getName() != null) {
            availableDevicePreference.setTitle(device.getName());
        } else {
            availableDevicePreference.setTitle(device.getAddress());
        }
        availableDevicePreference.setKey(device.getAddress());
        availableDevicePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "onPreferenceClick: available device preference clicked: " + device.getName() + ", starting pairing.");
                if (!device.createBond()) {
                    Log.d(TAG, "onPreferenceClick: Could not initiate pairing");
                    updateAvailableDeviceSummary(device, BluetoothDevice.BOND_NONE);
                } else {
                    Log.d(TAG, "onPreferenceClick: pairing in progress");
                }
                return true;
            }
        });
        switch (device.getBluetoothClass().getMajorDeviceClass()) {
            case AUDIO_VIDEO:
                availableDevicePreference.setIcon(R.drawable.ic_headset);
                break;
            case COMPUTER:
                availableDevicePreference.setIcon(R.drawable.ic_laptop);
                break;
            case PHONE:
                availableDevicePreference.setIcon(R.drawable.ic_phone);
                break;
            default:
                availableDevicePreference.setIcon(R.drawable.ic_other);
        }
        // add available device preference to available device preference category
        availableDevicesPreferenceCategory.addPreference(availableDevicePreference);
    }

    public void clearAvailableDevicePreferenceCategory() {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();

        // get available devices preference category
        PreferenceCategory availableDevicesPreferenceCategory = (PreferenceCategory) preferenceScreen.getPreference(1);
        availableDevicesPreferenceCategory.removeAll();
        availableDevicesPreferenceCategory.setVisible(false);
    }
}
