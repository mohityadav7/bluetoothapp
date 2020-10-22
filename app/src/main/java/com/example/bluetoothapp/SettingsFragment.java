package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import java.util.Set;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int AUDIO_VIDEO = 1024;
    private static final int COMPUTER = 256;
    private static final int PHONE = 512;

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
                pairedDevicePreference.setTitle(device.getName());
                pairedDevicePreference.setKey(device.getAddress());
                pairedDevicePreference.setSummary(device.getAddress());
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
}
