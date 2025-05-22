package com.example.fln.initialize;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.UUID;

public class BleService extends Service {
    public static boolean isConnected = false;
    public static String DeviceName = "";
    private final IBinder binder = new LocalBinder();
    boolean isScanning = false;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
    BluetoothGatt myconection;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize BLE connection here (omitted for brevity)
        // Start BLE scan and data reception
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Stop BLE activities (scanner, GATT, etc.)
        stopSelf(); // This is crucial on 5.1
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if (myconection != null) {
                myconection.disconnect();
                myconection.close();
                myconection = null;
            }

            stopScanning();
        } catch (Exception ex) {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
            DeviceName = sharedPref.getString("mydevice", "");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            } else {
                Log.e("bleservice : ", "scanning started");

                startScanning();

            }
        } catch (Exception e) {

        }

        return START_STICKY; // This makes the system restart the service if killed
    }

    // This is a dummy method to simulate BLE data reception.
    // Method to send received BLE data to the activity via broadcast
    private void sendDataToActivity(byte[] data) {
        Intent intent = new Intent("com.example.fln.sData");
        intent.putExtra("sensorData", data);
        sendBroadcast(intent);  // Send broadcast with data to activities
    }

    private void sendConnectionStatusToActivity(boolean c) {
        Intent intent = new Intent("com.example.fln.cStatus");
        intent.putExtra("connectionStatus", c);
        sendBroadcast(intent);  // Send broadcast with data to activities
    }

    private void sendDevicesToActivity(String dname) {
        Intent intent = new Intent("com.example.fln.devices");
        intent.putExtra("sensorDevices", dname);
        sendBroadcast(intent);  // Send broadcast with data to activities
    }

    protected void startScanning() {
        if (!isScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            isScanning = true;

            scanner.startScan(scanCallback);
        }
    }

    protected void stopScanning() {
        if (isScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            isScanning = false;

            scanner.stopScan(scanCallback);
        }
    }

    public class LocalBinder extends Binder {
        BleService getService() {
            return BleService.this;
        }
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            // Check for the ESP32 device name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(BleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (result.getScanRecord().getDeviceName() != null) {
                if ((result.getScanRecord().getDeviceName()).trim().startsWith("REVE")) {
                    sendDevicesToActivity(result.getScanRecord().getDeviceName());
                }
                if (DeviceName != "") {
                    if (DeviceName.equals(result.getScanRecord().getDeviceName())) {
                        stopScanning();// Stop scanning
                        myconection = result.getDevice().connectGatt(getApplicationContext(), false, gattCallback);
                    }
                }
            }
        }
    };
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(BleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

//                boolean mtuRequested = gatt.requestMtu(517); // Request MTU size of 517 bytes
//                if (mtuRequested) {
//                    Log.d("BLE", "MTU request sent: 517 bytes");
//                } else {
//                    Log.d("BLE", "MTU request failed");
//                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                gatt.discoverServices(); // Discover services after connection
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                isConnected = false;
                sendConnectionStatusToActivity(isConnected);
                startScanning();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString("12345678-1234-5678-1234-56789abcdef0"));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("abcd1234-5678-1234-5678-1234567890ab"));
                    if (characteristic != null) {
                        // Enable notifications
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(BleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }


                        gatt.setCharacteristicNotification(characteristic, true);

                        // Set descriptor for notifications
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        isConnected = true;
                        sendConnectionStatusToActivity(isConnected);

                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Called when a notification is received
            byte[] data = characteristic.getValue();
            if (data != null) {
                sendDataToActivity(data);
                StringBuilder hex = new StringBuilder();
                for (byte b : data) {
                    hex.append(String.format("%02X ", b));
                }
//                Log.d("BLE", "Received Bytes: " + hex.toString().trim());
            }
        }
    };
}
