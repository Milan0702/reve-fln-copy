package com.example.fln;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.example.fln.initialize.BleService;
import com.example.fln.initialize.Mappings;
import com.example.fln.tokens.BaseValue;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


@RequiresApi(api = Build.VERSION_CODES.S)
public class TestActivity extends AppCompatActivity {
    private static final int REQUEST_BLE_PERMISSIONS = 199;
    static Mappings map = new Mappings();
    boolean wasDisconnected = false;
    TextView[] gridcells = new TextView[32];
    GridLayout gridLayout;
    TextView textView2;
    private final BroadcastReceiver bleDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.fln.sData".equals(intent.getAction())) {
                byte[] my_data = intent.getByteArrayExtra("sensorData");
                if (my_data != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawData(my_data);
                        }
                    });
                }
            } else if ("com.example.fln.cStatus".equals(intent.getAction())) {
                boolean cstaus = intent.getBooleanExtra("connectionStatus", false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cstaus) {
                            textView2.setText(R.string.connected);
                        } else {
                            textView2.setText(R.string.not_connected);
                        }
                    }
                });
            }
        }
    };
    //FrameLayout loadingOverlay;
    SimpleDateFormat utcFormat;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
    boolean is_capturing = false;
    private String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION};

    public void showDevices(android.view.View v) {
        if (!BleService.isConnected) {
            Intent intent = new Intent(this, DeviceListActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Device already connected, Restart App or Switch Off Device for new connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
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

            } else {
                // Permissions denied - notify the user and exit
                Toast.makeText(this, "Permissions are required for BLE functionality", Toast.LENGTH_SHORT).show();
                finish(); // Close the app or handle gracefully
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.test_activity);

        utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        textView2 = findViewById(R.id.textView2);

        if (getSupportActionBar() != null) {
            this.getSupportActionBar().hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                permissions = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};
            }
        }

        if (!hasPermissions()) {
            requestPermissions();
        } else {
            enableBleAndStartService();
            if (!isLocationEnabled(this)) {
                checkAndPromptLocation();  // Show system dialog
            }
        }

        gridLayout = findViewById(R.id.gridLayout);

        // Total 32 cells: 4 rows × 8 columns
        for (int i = 0; i < 32; i++) {
            TextView cell = new TextView(this);
            cell.setText(map.getMapping(127).stringValue);
//            cell.setText("--");
            cell.setGravity(Gravity.CENTER);
            cell.setBackgroundColor(Color.LTGRAY);
            cell.setTextColor(Color.BLACK);
            cell.setPadding(8, 8, 8, 8);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            //params.width = 0;
            //params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);

            cell.setLayoutParams(params);
            gridLayout.addView(cell);
            gridcells[i] = cell;
        }

    }

    public boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    void enableBleAndStartService() {
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is off, request user to turn it on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            startActivityForResult(enableBtIntent, 111);
        } else {
            Intent serviceIntent = new Intent(this, BleService.class);
            Log.d("Created", "serviceIntent");
            startService(serviceIntent);
        }

    }

    protected void regBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.fln.sData");
        filter.addAction("com.example.fln.cStatus");
        registerReceiver(bleDataReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        regBroadcast();

        try {
            if (BleService.isConnected) {
                textView2.setText(R.string.connected);
            } else {
                textView2.setText(R.string.not_connected);
            }
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(bleDataReceiver);
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        is_capturing = false;
    }

    private void drawData(byte[] data) {
        int row = 3 - data[0];
        int whichcell = 0;
        for (int i = 0; i < 8; i++) {
            whichcell = (row * 8) + i;
            BaseValue value = map.getMapping((data[i + 1] & 0xFF));
            String valueString = (value != null) ? value.stringValue : "/\\";
            if (value == null) {
                Log.d("UNKNOWN BLOCK", String.valueOf(data[i + 1] & 0xFF));
            }
//            String binary = String.format("%8s", Integer.toBinaryString(data[i+1] & 0xFF)).replace(' ', '0');

            gridcells[whichcell].setText(valueString);
        }
    }

    // List of required permissions

    // Check if permissions are granted
    private boolean hasPermissions() {
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // Request permissions
    private void requestPermissions() {
        if (!hasPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_BLE_PERMISSIONS);
            }
        }
    }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TestActivity.this.finish();
                    }
                });
            }
        } else if (requestCode == 1234) {
            if (resultCode == RESULT_OK) {
                Log.d("Location ", "Location On");

            } else {
                Log.d("Location", "Location off shutting down");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TestActivity.this.finish();
                    }
                });
            }
        }
    }


    public void checkAndPromptLocation() {
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true); // Important: shows dialog even if location was previously denied

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                    // All good - location is already on
                } catch (ApiException exception) {
                    if (exception instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(TestActivity.this, 1234); // your request code
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Location settings are not satisfied and cannot be fixed
                    }
                }
            }
        });
    }
}