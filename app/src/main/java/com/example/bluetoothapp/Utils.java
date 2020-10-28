package com.example.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;

import org.javatuples.Triplet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AlertDialog;
import androidx.core.location.LocationManagerCompat;

import static android.content.ContentValues.TAG;

public class Utils {

    public static final ParcelUuid A2DP_SINK =
            ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid A2DP_SOURCE =
            ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid ADV_AUDIO_DIST =
            ParcelUuid.fromString("0000110D-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HSP =
            ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HSP_AG =
            ParcelUuid.fromString("00001112-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HFP =
            ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HFP_AG =
            ParcelUuid.fromString("0000111F-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AVRCP_CONTROLLER =
            ParcelUuid.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid AVRCP_TARGET =
            ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid OBEX_OBJECT_PUSH =
            ParcelUuid.fromString("00001105-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid HID =
            ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid HOGP =
            ParcelUuid.fromString("00001812-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid PANU =
            ParcelUuid.fromString("00001115-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid NAP =
            ParcelUuid.fromString("00001116-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid BNEP =
            ParcelUuid.fromString("0000000f-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid PBAP_PCE =
            ParcelUuid.fromString("0000112e-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid PBAP_PSE =
            ParcelUuid.fromString("0000112f-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid MAP =
            ParcelUuid.fromString("00001134-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid MNS =
            ParcelUuid.fromString("00001133-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid MAS =
            ParcelUuid.fromString("00001132-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid SAP =
            ParcelUuid.fromString("0000112D-0000-1000-8000-00805F9B34FB");
    public static final ParcelUuid HEARING_AID =
            ParcelUuid.fromString("0000FDF0-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid BASE_UUID =
            ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");

    public static HashSet<Triplet<String, String, ParcelUuid>> getUuidsWithName(BluetoothDevice device) {
        if (device == null) return null;

        HashSet<Triplet<String, String, ParcelUuid>> uuidSet = new HashSet<>();
        uuidSet.add(Triplet.with("A2DP_SINK", "A2DP Sink Profile", A2DP_SINK));
        uuidSet.add(Triplet.with("A2DP_SOURCE", "A2DP Source Profile", A2DP_SOURCE));
        uuidSet.add(Triplet.with("ADV_AUDIO_DIST", "Advance Audio Distribution Profile", ADV_AUDIO_DIST));
        uuidSet.add(Triplet.with("HSP", "Headset Profile", HSP));
        uuidSet.add(Triplet.with("HSP_AG", "Headset Audio Gateway", HSP_AG));
        uuidSet.add(Triplet.with("HFP", "Hands-Free Profile", HFP));
        uuidSet.add(Triplet.with("HFP_AG", "Hands-Free Audio Gateway", HFP_AG));
        uuidSet.add(Triplet.with("AVRCP_CONTROLLER", "AV Remote Control Profile", AVRCP_CONTROLLER));
        uuidSet.add(Triplet.with("AVRCP_TARGET", "AV Remote Target Profile", AVRCP_TARGET));
        uuidSet.add(Triplet.with("OBEX_OBJECT_PUSH", "OBEX Object Push Profile", OBEX_OBJECT_PUSH));
        uuidSet.add(Triplet.with("HID", "Human Interface Device Profile", HID));
        uuidSet.add(Triplet.with("HOGP", "HID over GATT Profile", HOGP));
        uuidSet.add(Triplet.with("PANU", "PANU Profile", PANU));
        uuidSet.add(Triplet.with("NAP", "NAP Profile", NAP));
        uuidSet.add(Triplet.with("BNEP", "BNEP Profile", BNEP));
        uuidSet.add(Triplet.with("PBAP_PCE", "Phonebook Access Profile - Client", PBAP_PCE));
        uuidSet.add(Triplet.with("PBAP_PSE", "Phonebook Access Profile - Server", PBAP_PSE));
        uuidSet.add(Triplet.with("MAP", "Message Access Profile", MAP));
        uuidSet.add(Triplet.with("MNS", "Message Notification Service", MNS));
        uuidSet.add(Triplet.with("MAS", "Message Access Profile", MAS));
        uuidSet.add(Triplet.with("SAP", "SIM Access Profile", SAP));
        uuidSet.add(Triplet.with("HEARING_AID", "Hearing Aid", HEARING_AID));
        uuidSet.add(Triplet.with("BASE_UUID", "Base UUID", BASE_UUID));

        ParcelUuid[] uuids = device.getUuids();
        HashSet<Triplet<String, String, ParcelUuid>> output = new HashSet<>();
        for (ParcelUuid u : uuids) {
            boolean found = false;
            for (Triplet<String, String, ParcelUuid> uuidTriplet : uuidSet) {
                if (uuidTriplet.getValue2().equals(u)) {
                    found = true;
                    output.add(uuidTriplet);
                }
            }
            if (!found) {
                output.add(new Triplet<>("Custom Profile", u.toString(), u));
            }
        }
        return output;
    }

    // get paired devices
    public static Set<BluetoothDevice> getPairedDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return new HashSet<>();
        return bluetoothAdapter.getBondedDevices();
    }

    public static void makeDeviceDiscoverable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            try {
                Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.d(TAG, "makeDeviceDiscoverable: Failed to turn on discoverability");
            }
        }
    }

    public static void makeDeviceNotDiscoverable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
            } else {
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_NONE);
            }
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.d(TAG, "makeDeviceNotDiscoverable: Failed to turn off bluetooth device discoverability");
        }
    }

    // to make device discoverable after turning on bluetooth, enabling bluetooth is not instantaneous
    public static synchronized void makeDiscoverableAfterDelay(Timer timer, int timeInMilliseconds) {
        if (timer != null) timer.cancel();
        timer = new Timer();

        TimerTask action = new TimerTask() {
            public void run() {
                Log.d(TAG, "run: making discoverable");
                Utils.makeDeviceDiscoverable();
            }
        };

        timer.schedule(action, timeInMilliseconds);
    }

    public static void enableLocationIfDisabled(final Context context) {
        if (!isLocationEnabled(context)) {
            // notify user
            new AlertDialog.Builder(context)
                    .setTitle("Turn on location")
                    .setMessage("Location must be on in order to start device discovery")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    ((MainActivity) context).startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), MainActivity.START_DISCOVERY);
                                }
                            }
                    )
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    public static boolean isLocationEnabled(final Context context) {
        boolean gpsEnabled = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            gpsEnabled = LocationManagerCompat.isLocationEnabled(locationManager);
        } catch (Exception e) {
            Log.d(TAG, "checkIfGPSOn: Could not get location status, " + e.getMessage());
        }

        return gpsEnabled;
    }
}
