package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;

import org.javatuples.Triplet;

import java.lang.reflect.Method;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
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
        PreferenceCategory deviceDetailsPreferenceCategory = screen.findPreference(screen.getContext()
                .getResources().getString(R.string.paired_device_details_category_key));

        Preference deviceNamePreference = new Preference(screen.getContext());
        deviceNamePreference.setIcon(R.drawable.ic_bluetooth);
        if (device != null && device.getName() != null) {
            deviceNamePreference.setTitle(device.getName());
        } else {
            deviceNamePreference.setTitle("Unknown Device");
        }
        if (deviceDetailsPreferenceCategory != null) {
            deviceDetailsPreferenceCategory.addPreference(deviceNamePreference);
        }

        // add preference for device address
        if (device != null && deviceDetailsPreferenceCategory != null) {
            Preference deviceAddressPreference = new Preference(screen.getContext());
            deviceAddressPreference.setTitle(device.getAddress());
            deviceAddressPreference.setIcon(R.drawable.ic_address);
            deviceDetailsPreferenceCategory.addPreference(deviceAddressPreference);
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
        if (deviceDetailsPreferenceCategory != null) {
            deviceDetailsPreferenceCategory.addPreference(unpairPreference);
        }

        addProfilePreferences(device);
    }

    private void addProfilePreferences(BluetoothDevice device) {
        if (device != null) {
            HashSet<Triplet<String, String, ParcelUuid>> profiles = Utils.getUuidsWithName(device);
            if (profiles.size() == 0) return;

            // get preference screen and profiles category
            PreferenceScreen screen = getPreferenceScreen();
            PreferenceCategory profilesPreferencesCategory = screen.findPreference(screen.getContext()
                    .getResources().getString(R.string.supported_profiles_category_key));
            if (profilesPreferencesCategory == null) return;
            profilesPreferencesCategory.removeAll();
            profilesPreferencesCategory.setVisible(true);

            for (Triplet<String, String, ParcelUuid> profile : profiles) {
                // create new profile preference
                Preference pref = new Preference(screen.getContext());
                pref.setTitle(profile.getValue0());
                pref.setSummary(profile.getValue1());
                pref.setKey(profile.getValue2().toString());
                profilesPreferencesCategory.addPreference(pref);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MainActivity main = (MainActivity) getActivity();
        if (main != null) {
            main.updateScanMenuItemVisibility(false);
            main.bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity main = (MainActivity) getActivity();
        if (main != null) {
            main.updateScanMenuItemVisibility(true);
        }
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