package com.example.fln.initialize.managers;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fln.initialize.BleService;

public class BluetoothManager {
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private boolean receiverRegistered = false;

    public interface BleCallback {
        void onDataReceived(byte[] data);
        void onConnectionStatusChanged(boolean connected);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.fln.sData".equals(intent.getAction())) {
                byte[] data = intent.getByteArrayExtra("sensorData");
                if (data != null && callback != null) {
                    callback.onDataReceived(data);
                }
            } else if ("com.example.fln.cStatus".equals(intent.getAction())) {
                boolean status = intent.getBooleanExtra("connectionStatus", false);
                if (callback != null) callback.onConnectionStatusChanged(status);
            }
        }
    };

    private BleCallback callback;

    public BluetoothManager(Context context, BleCallback callback) {
        this.context = context;
        this.callback = callback;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void enableBluetooth(Activity activity) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            // Check for Bluetooth Connect permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    safelyStartBluetoothActivity(activity, enableBtIntent);
                } else {
                    Log.e("BleManager", "BLUETOOTH_CONNECT permission not granted");
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            111);
                }
            } else {
                // For versions before Android 12
                safelyStartBluetoothActivity(activity, enableBtIntent);
            }
        } else {
            startBleService();
        }
    }

    private void safelyStartBluetoothActivity(Activity activity, Intent intent) {
        try {
            activity.startActivityForResult(intent, 111);
        } catch (SecurityException e) {
            Log.e("BleManager", "SecurityException: BLUETOOTH_CONNECT permission not granted", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private void startBleService() {
        Intent serviceIntent = new Intent(context, BleService.class);
        context.startService(serviceIntent);
    }

    public void registerReceiver() {
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.example.fln.sData");
            filter.addAction("com.example.fln.cStatus");
            context.registerReceiver(receiver, filter);
            receiverRegistered = true;
        }
    }

    public void unregisterReceiver() {
        if (receiverRegistered) {
            context.unregisterReceiver(receiver);
            receiverRegistered = false;
        }
    }
}