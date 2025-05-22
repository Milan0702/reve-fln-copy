package com.example.fln;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.example.fln.initialize.BleService;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.S)
public class DeviceListActivity extends AppCompatActivity {

    private static final int REQUEST_BLE_PERMISSIONS = 199;
    private final List<String> deviceNames = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayAdapter<String> deviceAdapter;
    private final BroadcastReceiver bleDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.fln.devices".equals(intent.getAction())) {

                String mydevices = intent.getStringExtra("sensorDevices");
                if (!deviceNames.contains(mydevices)) {
                    deviceNames.add(mydevices);
                    deviceAdapter.notifyDataSetChanged();
                }


            } else if ("com.example.fln.cStatus".equals(intent.getAction())) {

                boolean cstaus = intent.getBooleanExtra("connectionStatus", false);

                if (cstaus) {
                    runOnUiThread(() -> {});
                }

            }
        }
    };

    public void close(android.view.View v) {
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.startactivity);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8); // 80% of screen width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.white);


        ListView deviceListView = findViewById(R.id.device_list_view);
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNames);
        deviceListView.setAdapter(deviceAdapter);

        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            BleService.DeviceName = deviceNames.get(position);
            SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("mydevice", BleService.DeviceName);
            editor.apply();
            this.finish();
        });

        checkLocationEnabled();
    }

    private void checkLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e("DeviceListActivity", "Error checking GPS status", ex);
        }

        if (!gpsEnabled) {
            new AlertDialog.Builder(this).setTitle("Enable Location").setMessage("Bluetooth LE scanning requires location services to be enabled. Please turn on GPS.").setCancelable(false).setPositiveButton("Open Settings", (dialog, which) -> {
                // Open the Location settings screen
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }).setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(DeviceListActivity.this, "Cannot scan for BLE devices without Location enabled", Toast.LENGTH_LONG).show();
                finish();  // close this activity
            }).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        regBrodcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(bleDataReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    protected void regBrodcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.fln.devices");
        filter.addAction("com.example.fln.cStatus");
        registerReceiver(bleDataReceiver, filter);
    }

    // List of required permissions

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                Log.d("Bluetooth", "Bluetooth enabled successfully");
                Intent serviceIntent = new Intent(this, BleService.class);
                startService(serviceIntent);
            } else {
                Log.d("Bluetooth", "User declined to enable Bluetooth");
                runOnUiThread(DeviceListActivity.this::finish);
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    void enableBleAndStartService() {
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is off, request user to turn it on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
            }
            //noinspection deprecation
            startActivityForResult(enableBtIntent, 111);
        } else {
            Intent serviceIntent = new Intent(this, BleService.class);
            startService(serviceIntent);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            //noinspection StatementWithEmptyBody
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_BLE_PERMISSIONS) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                enableBleAndStartService();
                // Permissions granted - proceed with BLE operations
                //startScanning();
            } else {
                // Permissions denied - notify the user and exit
                Toast.makeText(this, "Permissions are required for BLE functionality", Toast.LENGTH_SHORT).show();
                finish(); // Close the app or handle gracefully
            }
        }
    }

}