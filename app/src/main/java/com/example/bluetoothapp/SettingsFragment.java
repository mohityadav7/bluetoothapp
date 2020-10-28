package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Set;

import androidx.fragment.app.Fragment;
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
        updatePairedDevicesPreferences();
    }

    // add paired devices preferences, shown when bluetooth is on
    public void updatePairedDevicesPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        PreferenceCategory pairedDevicesPreferenceCategory = preferenceScreen
                .findPreference(preferenceScreen.getContext().getResources()
                        .getString(R.string.paired_devices_preferences_category_key));
        if (pairedDevicesPreferenceCategory == null) return;

        // set visibility of paired devices preference category
        Set<BluetoothDevice> pairedDevices = Utils.getPairedDevices();
        if (pairedDevices.size() != 0 && !pairedDevicesPreferenceCategory.isVisible()) {
            pairedDevicesPreferenceCategory.setVisible(true);
        } else if (pairedDevices.size() == 0) {
            pairedDevicesPreferenceCategory.setVisible(false);
        }

        pairedDevicesPreferenceCategory.removeAll();
        if (pairedDevices.size() > 0) {
            for (final BluetoothDevice device : pairedDevices) {
                addPairedDevicePreference(device);
            }
        }
    }

    private void addPairedDevicePreference(final BluetoothDevice device) {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        PreferenceCategory pairedDevicesPreferenceCategory = preferenceScreen
                .findPreference(preferenceScreen.getContext().getResources()
                        .getString(R.string.paired_devices_preferences_category_key));
        if (pairedDevicesPreferenceCategory == null) return;

        // create new paired device preference
        PairedDevicePreference pairedDevicePreference = new PairedDevicePreference(preferenceScreen.getContext());
        if (device.getName() != null) {
            pairedDevicePreference.setTitle(device.getName());
        } else {
            pairedDevicePreference.setTitle(device.getAddress());
        }
        pairedDevicePreference.setKey(device.getAddress());

        // listener for PairedDevicePreference
        PairedDevicePreference.PairedDevicePreferenceListener listener =
                new PairedDevicePreference.PairedDevicePreferenceListener() {
                    @Override
                    public void onPreferenceClick(View view) {
                        Log.d(TAG, "onPreferenceClick: " + device.getName() + " preference clicked.");
                    }

                    @Override
                    public void onWidgetClick(View view) {
                        Fragment pairedDeviceFragment = new PairedDeviceFragment();
                        Bundle args = new Bundle();
                        args.putParcelable(PairedDeviceFragment.DEVICE, device);
                        pairedDeviceFragment.setArguments(args);
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.settings, pairedDeviceFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                };
        pairedDevicePreference.setOnClickListener(listener);

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

    // update summary appropriately while pairing available device
    public void updateAvailableDeviceSummary(BluetoothDevice device, int bondState, String summary) {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        PreferenceCategory availableDevicesPreferenceCategory = preferenceScreen
                .findPreference(preferenceScreen.getContext().getResources()
                        .getString(R.string.available_devices_preference_category_key));
        if (availableDevicesPreferenceCategory == null) return;
        Preference preferenceToUpdate = availableDevicesPreferenceCategory.findPreference(device.getAddress());

        if (preferenceToUpdate != null && summary == null) {
            if (bondState == BluetoothDevice.BOND_BONDING) {
                preferenceToUpdate.setSummary("Pairing...");
            } else if (bondState == BluetoothDevice.BOND_BONDED) {
                preferenceToUpdate.setSummary("Paired");
                preferenceToUpdate.setSummary(null);
            } else if (bondState == BluetoothDevice.BOND_NONE) {
                preferenceToUpdate.setSummary("Could not pair");
            }
        } else if (preferenceToUpdate != null) {
            preferenceToUpdate.setSummary(summary);
        }
    }

    public void addAvailableDevicesPreferences(final BluetoothDevice device) {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();

        // get available devices preference category
        PreferenceCategory availableDevicesPreferenceCategory = preferenceScreen
                .findPreference(preferenceScreen.getContext().getResources()
                        .getString(R.string.available_devices_preference_category_key));
        if (availableDevicesPreferenceCategory == null) return;
        if (!availableDevicesPreferenceCategory.isVisible()) {
            availableDevicesPreferenceCategory.setVisible(true);
        }

        // check if device already exists in available devices, return add if already exists
        if (availableDevicesPreferenceCategory.findPreference(device.getAddress()) != null) {
            return;
        }

        // create new available device preference
        Preference availableDevicePreference = new Preference(preferenceScreen.getContext());
        if (device.getName() != null) {
            availableDevicePreference.setTitle(device.getName());
        } else {
            availableDevicePreference.setTitle(device.getAddress());
        }
        availableDevicePreference.setKey(device.getAddress());
        availableDevicePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Set<BluetoothDevice> pairedDevices = Utils.getPairedDevices();
                if (pairedDevices.contains(device)) {
                    Toast.makeText(getContext(), "Already paired", Toast.LENGTH_SHORT).show();
                    return true;
                }
                Log.d(TAG, "onPreferenceClick: available device preference clicked: " + device.getName() + ", starting pairing.");
                if (!device.createBond()) {
                    Log.d(TAG, "onPreferenceClick: Could not initiate pairing");
                    updateAvailableDeviceSummary(device, BluetoothDevice.BOND_NONE, null);
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

    public void removeAvailableDevicePreference(BluetoothDevice device) {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        // get available devices preference category
        PreferenceCategory availableDevicesPreferenceCategory = preferenceScreen
                .findPreference(preferenceScreen.getContext().getResources()
                        .getString(R.string.available_devices_preference_category_key));
        if (availableDevicesPreferenceCategory == null) return;
        // get preference to remove and remove it
        Preference preferenceToRemove = availableDevicesPreferenceCategory.findPreference(device.getAddress());
        if (preferenceToRemove != null) {
            availableDevicesPreferenceCategory.removePreference(preferenceToRemove);
        }
        //  hide category if no device is available
        if (availableDevicesPreferenceCategory.getPreferenceCount() == 0) {
            availableDevicesPreferenceCategory.setVisible(false);
        }
    }

    public void clearAvailableDevicePreferenceCategory() {
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();

        // get available devices preference category
        PreferenceCategory availableDevicesPreferenceCategory = preferenceScreen
                .findPreference(preferenceScreen.getContext().getResources()
                        .getString(R.string.available_devices_preference_category_key));
        if (availableDevicesPreferenceCategory == null) return;
        availableDevicesPreferenceCategory.removeAll();
        availableDevicesPreferenceCategory.setVisible(false);
    }
}
