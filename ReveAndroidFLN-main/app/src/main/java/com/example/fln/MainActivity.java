package com.example.fln;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fln.activities.animals.AnimalSpellActivity;
import com.example.fln.activities.birds.BirdFindActivity;
import com.example.fln.activities.birds.BirdSpellActivity;
import com.example.fln.activities.english.EnglishSpellActivity;
import com.example.fln.activities.maths.MathCountingActivity;
import com.example.fln.activities.maths.MathOperationActivity;
import com.example.fln.answers.ResultState;
import com.example.fln.initialize.BleService;
import com.example.fln.initialize.Mappings;
import com.example.fln.questions.Question;
import com.example.fln.questions.animals.AnimalSpellQuestion;
import com.example.fln.questions.birds.BirdFindQuestion;
import com.example.fln.questions.birds.BirdSpellQuestion;
import com.example.fln.questions.english.EnglishSpellQuestion;
import com.example.fln.questions.math.MathCountingQuestion;
import com.example.fln.questions.math.MathOperationQuestion;
import com.example.fln.tokens.BaseValue;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {
    private static final int REQ_QUESTION_FINISHED = 42;
    private static final int REQUEST_BLE_PERMISSIONS = 199;
    private final ArrayList<Question> questions = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    TextView textView2;
    MediaPlayer startMediaPlayer;
    ArrayList<ResultState> results = new ArrayList<>();
    Mappings map = new Mappings();
    private String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION};
    private boolean hasStarted = false;
    private boolean hasPlaced = false;
    private int startRow = -1;
    private int startCol = -1;
    private int currentQuestionIndex = 0;

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        questions.add(new BirdFindQuestion("peacockB29", "B29"));
//        questions.add(new EnglishSpellQuestion("table", "TABLE"));
//        questions.add(new AnimalSpellQuestion("cow", "COW"));
//        questions.add(new BirdSpellQuestion("owl", "OWL"));
//        questions.add(new MathCountingQuestion("sheep", "3"));
//        questions.add(new MathOperationQuestion("2 + 4 = ", "6", "moq1", true));
//        questions.add(new MathCountingQuestion("drops", "3"));
        questions.add(new MathOperationQuestion("4 * 2 = ", "8", "moq2", true));
        questions.add(new MathOperationQuestion("2 + 8 - 4 = ", "6", "moq3", true));


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

        startMediaPlayer = MediaPlayer.create(this, R.raw.start);
        startMediaPlayer.start();
    }

    private void restart() {
        hasPlaced = false;
        hasStarted = false;
        currentQuestionIndex = 0;
        startRow = -1;
        startCol = -1;

        double averageTime = 0;
        double averageIncorrect = 0;

        for(ResultState rs: results){
            averageTime += rs.getTime();
            averageIncorrect += rs.getIncorrectPlacements();
        }

        averageIncorrect /= questions.size();
        averageTime /= questions.size();

        Log.d("Average Time", String.format("%f", averageTime));
        Log.d("Average Incorrect", String.format("%f", averageIncorrect));

        results.clear();
        TextView centredText = findViewById(R.id.centeredText);
        centredText.setText(R.string.expecting_start);
        if (startMediaPlayer != null && startMediaPlayer.isPlaying()) {
            startMediaPlayer.stop();
            startMediaPlayer.release();
        }
        startMediaPlayer = MediaPlayer.create(this, R.raw.start);
        startMediaPlayer.start();
    }

    private void launchNextQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            restart();
            return;
        }
        Question q = questions.get(currentQuestionIndex);
        Intent i;
        if (q instanceof MathCountingQuestion) {
            i = new Intent(this, MathCountingActivity.class);
        } else if (q instanceof MathOperationQuestion) {
            i = new Intent(this, MathOperationActivity.class);
        } else if (q instanceof AnimalSpellQuestion) {
            i = new Intent(this, AnimalSpellActivity.class);
        } else if (q instanceof BirdSpellQuestion) {
            i = new Intent(this, BirdSpellActivity.class);
        } else if (q instanceof EnglishSpellQuestion) {
            i = new Intent(this, EnglishSpellActivity.class);
        } else if (q instanceof BirdFindQuestion) {
            i = new Intent(this, BirdFindActivity.class);
        } else {
            throw new RuntimeException("Enter a valid category");
        }

        i.putExtra("question", q);
        i.putExtra("question_number", currentQuestionIndex);
        i.putExtra("total_questions", questions.size());
        //noinspection deprecation
        startActivityForResult(i, REQ_QUESTION_FINISHED);
    }

    private void start() {
        if (hasStarted) return;
        hasStarted = true;
        try {
            unregisterReceiver(bleDataReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        launchNextQuestion();
    }

    public boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void showDevices(View v) {
        if (!BleService.isConnected) {
            Intent intent = new Intent(this, DeviceListActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Device already connected, Restart App or Switch Off Device for new connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_QUESTION_FINISHED && resultCode == RESULT_OK) {
            currentQuestionIndex++;
            ResultState current_result =
                    (ResultState) data.getSerializableExtra("result");
            Log.d("RESULT", current_result.toString());
            results.add(current_result);
            launchNextQuestion();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void requestPermissions() {
        if (!hasPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_BLE_PERMISSIONS);
            }
        }
    }

    public void checkAndPromptLocation() {
        //noinspection deprecation
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true); // Important: shows dialog even if location was previously denied

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnCompleteListener(task1 -> {
            try {
                task1.getResult(ApiException.class);
            } catch (ApiException exception) {
                //noinspection StatementWithEmptyBody
                if (exception instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                        resolvable.startResolutionForResult(MainActivity.this, 1234); // your request code
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Location settings are not satisfied and cannot be fixed
                }
            }
        });
    }

    /**
     * @noinspection BooleanMethodIsAlwaysInverted
     */
    @SuppressLint("ObsoleteSdkInt")
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    protected void regBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.fln.sData");
        filter.addAction("com.example.fln.cStatus");
        registerReceiver(bleDataReceiver, filter);
    }

    protected void onDataReceive(byte[] data) {
        int row = 3 - data[0];
        for (int col = 0; col < 8; col++) {
            BaseValue value = map.getMapping((data[col + 1] & 0xFF));
            if (value != null) {
                if (!hasPlaced) {
                    if (value.byteKey == Mappings.startBlockCode) {
                        hasPlaced = true;
                        if (startMediaPlayer != null && startMediaPlayer.isPlaying()) {
                            startMediaPlayer.stop();
                            startMediaPlayer.release();
                        }
                        startMediaPlayer = MediaPlayer.create(this, R.raw.lecture_intro);
                        startMediaPlayer.start();
                        TextView centredText = findViewById(R.id.centeredText);
                        centredText.setText(R.string.begin_learning);
                        startRow = row;
                        startCol = col;
                    }
                } else {
                    if (value.byteKey == map.getDefaultValue().byteKey && startRow == row && startCol == col) {
                        start();
                    }
                }
            }
        }
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

    private final BroadcastReceiver bleDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.fln.sData".equals(intent.getAction())) {
                byte[] my_data = intent.getByteArrayExtra("sensorData");
                if (my_data != null) {
                    runOnUiThread(() -> onDataReceive(my_data));
                }
            } else if ("com.example.fln.cStatus".equals(intent.getAction())) {
                boolean cstaus = intent.getBooleanExtra("connectionStatus", false);
                runOnUiThread(() -> {
                    if (cstaus) {
                        textView2.setText(R.string.connected);
                    } else {
                        textView2.setText(R.string.not_connected);
                    }
                });
            }
        }
    };


}