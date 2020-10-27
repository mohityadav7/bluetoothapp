package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class PairedDeviceFragment extends PreferenceFragmentCompat {

    public static final String DEVICE = "device_key";
    private static final String TAG = PairedDeviceFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.paired_device_preferences, rootKey);

        // get device from arguments
        Bundle args = getArguments();
        BluetoothDevice device = null;
        if (args != null) {
            device = args.getParcelable(DEVICE);
        }

        // add preference for device name
        final PreferenceScreen screen = getPreferenceScreen();
        Preference deviceNamePreference = new Preference(screen.getContext());
        deviceNamePreference.setIcon(R.drawable.ic_bluetooth);
        if (device != null && device.getName() != null) {
            deviceNamePreference.setTitle(device.getName());
        } else {
            deviceNamePreference.setTitle("Unknown Device");
        }
        screen.addPreference(deviceNamePreference);

        // add preference for device address
        if (device != null) {
            Preference deviceAddressPreference = new Preference(screen.getContext());
            deviceAddressPreference.setTitle(device.getAddress());
            deviceAddressPreference.setIcon(R.drawable.ic_address);
            screen.addPreference(deviceAddressPreference);
        }

        // add unpair preference
        Preference unpairPreference = new Preference(screen.getContext());
        unpairPreference.setTitle("Unpair device");
        unpairPreference.setSummary("Forget device");
        unpairPreference.setIcon(R.drawable.ic_unpair);
        final BluetoothDevice finalDevice = device;
        unpairPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(screen.getContext())
                        .setTitle("Unpair device")
                        .setMessage("Your phone will no longer be paired with " +
                                (finalDevice != null ? finalDevice.getName() : "this device"))
                        .setPositiveButton("Unpair", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (unpairDevice(finalDevice)) {
                                    Log.d(TAG, "onClick: Device removal in progress");
                                } else {
                                    Log.d(TAG, "onClick: Could not remove device");
                                }
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });
        screen.addPreference(unpairPreference);
    }

    private boolean unpairDevice(BluetoothDevice device) {
        try {
            Method removeBondMethod = device.getClass().getMethod("removeBond", (Class<?>[]) null);
            return (boolean) removeBondMethod.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }
}