package com.example.fln.activities;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fln.DeviceListActivity;
import com.example.fln.R;
import com.example.fln.answers.AnswerToken;
import com.example.fln.answers.ResultState;
import com.example.fln.initialize.BleService;
import com.example.fln.initialize.Mappings;
import com.example.fln.questions.Node;
import com.example.fln.questions.Question;
import com.example.fln.tokens.BaseValue;
import com.example.fln.tokens.Block;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


@RequiresApi(api = Build.VERSION_CODES.S)
public abstract class QuestionActivity extends AppCompatActivity {
    protected static final int[] correct_audios = {R.raw.correct1, R.raw.correct2, R.raw.correct3};
    protected static final int[] incorrect_audios = {R.raw.incorrect1, R.raw.incorrect2, R.raw.incorrect3};
    protected static final int beep_audio = R.raw.beep;
    protected static final int remove_all_audio = R.raw.remove_all;
    protected static final int REQUEST_BLE_PERMISSIONS = 199;
    public static Mappings map = new Mappings();
    protected String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION};
    protected MediaPlayer player = null;
    protected MediaPlayer completionMediaPlayer;
    protected boolean completionMediaPlayerInUse = false;
    protected BaseValue[][] state;
    protected Question question;
    protected BaseValue[] answerStream;
    protected int currentIndex = 0;
    protected boolean correctAnswerFound = false;
    protected boolean hasViolated = false;
    protected boolean hasCompleted = false;
    protected ArrayList<Block> wrong;
    protected boolean completionAnnounced = false;
    protected TextView correctnessView;
    protected ImageView correctImageView;
    protected ImageView incorrectImageView;
    protected int numBlocks = 0;
    protected Node<BaseValue> node;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    TextView textView2;
    boolean hasSkipped = false;
    int numIncorrectBlock = 0;
    long time;
    private ArrayList<AnswerToken> answerState;
    private final ArrayList<Block> hasToPlace = new ArrayList<>();
    protected final BroadcastReceiver bleDataReceiver = new BroadcastReceiver() {
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
    private boolean receiverRegistered = false;

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        time = System.nanoTime();
        setViewsAndLayout();
        currentIndex = 0;

        correctnessView = findViewById(R.id.correctnessView);
        correctImageView = findViewById(R.id.correct);
        incorrectImageView = findViewById(R.id.incorrect);
        textView2 = findViewById(R.id.textView2);

        correctnessView.setVisibility(View.GONE);
        correctImageView.setVisibility(View.GONE);
        incorrectImageView.setVisibility(View.GONE);

        wrong = new ArrayList<>();

        state = new BaseValue[4][8];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                state[i][j] = map.getDefaultValue();
            }
        }

        if (getSupportActionBar() != null) {
            Objects.requireNonNull(getThis().getSupportActionBar()).hide();
        }

        answerState = new ArrayList<>();

        setQuestion();

        int q_no = getIntent().getIntExtra("question_number", -1) + 1;
        int total_questions = getIntent().getIntExtra("total_questions", -1);

        try{
            TextView tv = findViewById(R.id.questionNumberId);
            tv.setText(String.format("%d/%d", q_no, total_questions));
        }catch (NullPointerException e){
                e.printStackTrace();
        }


        ArrayList<BaseValue> temp = question.parseAnswer();
        answerStream = temp.toArray(new BaseValue[0]);

        correctAnswerFound = false;
        hasViolated = false;
        hasCompleted = false;
        completionAnnounced = false;
        currentIndex = 0;
        numBlocks = 0;
        wrong.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                permissions = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};
            }
        }

        if (!hasPermissions()) {
            requestPermissions();
        } else {
            enableBleAndStartService();
            if (!isLocationEnabled(getThis())) {
                checkAndPromptLocation();
            }
        }

    }

    /**
     * @noinspection BooleanMethodIsAlwaysInverted
     */
    @SuppressLint("ObsoleteSdkInt")
    protected boolean hasPermissions() {
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findInHasToPlace(BaseValue vale, int row, int col) {
        for (Block block : hasToPlace) {
            if (block.row == row && block.col == col && block.value.equals(vale)) {
                for (AnswerToken b : answerState) {
                    if (b.equals(block)) {
                        b.color = AnswerToken.Color.Green;
                        updateAnswer();
                    }
                }
                hasToPlace.remove(block);
                return true;
            }
        }
        return false;
    }

    protected void registerChange(int row, int col, BaseValue value) {
        BaseValue oldValue = state[row][col];
        state[row][col] = value;

        if (oldValue.equals(map.getDefaultValue()) && !value.equals(map.getDefaultValue())) {
            numBlocks++;
            if (!hasToPlace.isEmpty()) {
                if (findInHasToPlace(value, row, col)) {
                    return;
                }
            }
            if (hasCompleted) {
                return;
            }
            if (hasViolated) {
                wrong.add(new Block(row, col, value));
            } else {
                if (isCorrectCategory(value)) {
                    if (isCorrectInSequence(node, value)) {
                        currentIndex += 1;
                        node = getCorrectNode(node, value);
                        if (Objects.requireNonNull(node).children.isEmpty() && !correctAnswerFound) {
                            answerState.add(new AnswerToken(new Block(row, col, value), AnswerToken.Color.Green));
                            updateAnswer();
                            Toast toast = Toast.makeText(this /* MyActivity */, "Correct Answer", Toast.LENGTH_LONG);
                            toast.show();
                            correctAnswerFound = true;
                            hasCompleted = true;
                            correctnessView.setVisibility(View.VISIBLE);
                            correctImageView.setVisibility(View.VISIBLE);
                            incorrectImageView.setVisibility(View.GONE);
                            Toast toast_reset = Toast.makeText(this /* MyActivity */, "We will proceed once all the blocks are removed", Toast.LENGTH_LONG);
                            toast_reset.show();
                            Thread t = new Thread() {
                                @Override
                                public void run() {
                                    if (!completionAnnounced) {
                                        completionAnnounced = true;
                                        completionAudio(correct_audios[0]);
                                    }
                                }
                            };

                            t.start();

                            if (numBlocks == 0) {
                                currentIndex = 0;
                                node = question.generateAnswerTree();
                                endActivity();
                            }
                        } else {
                            answerState.add(new AnswerToken(new Block(row, col, value), AnswerToken.Color.Green));
                            updateAnswer();
                        }
                    } else {
                        // Incorrect
                        numIncorrectBlock++;
                        playSound(beep_audio);
                        Toast toast = Toast.makeText(this /* MyActivity */, "Incorrect, remove wrongly placed blocks !!", Toast.LENGTH_LONG);
                        toast.show();
                        playSound(incorrect_audios[new Random().nextInt(incorrect_audios.length)]);
                        incorrectImageView.setVisibility(View.VISIBLE);
                        correctImageView.setVisibility(View.GONE);
                        correctnessView.setVisibility(View.VISIBLE);
                        hasViolated = true;
                        answerState.add(new AnswerToken(new Block(row, col, value), AnswerToken.Color.Red));
                        updateAnswer();
                        wrong.add(new Block(row, col, value));
                    }
                } else {
                    // Incorrect
                    numIncorrectBlock++;
                    Toast toast = Toast.makeText(this /* MyActivity */, "Incorrect Category, remove wrongly placed blocks !!", Toast.LENGTH_LONG);
                    toast.show();
                    playSound(incorrect_audios[new Random().nextInt(incorrect_audios.length)]);
                    incorrectImageView.setVisibility(View.VISIBLE);
                    correctImageView.setVisibility(View.GONE);
                    correctnessView.setVisibility(View.VISIBLE);
                    hasViolated = true;
                    answerState.add(new AnswerToken(new Block(row, col, value), AnswerToken.Color.Red));
                    wrong.add(new Block(row, col, value));
                }
            }
        } else if (!oldValue.equals(map.getDefaultValue()) && value.equals(map.getDefaultValue())) {
            // We are removing a block
            if (hasCompleted) {
                numBlocks--;
                if (numBlocks == 0) {
                    endActivity();
                }
                return;
            } else if (hasViolated) {
                boolean validRemoved = true;
                for (int i = 0; i < wrong.size(); i++) {
                    if (wrong.get(i).row == row && wrong.get(i).col == col) {
                        wrong.remove(i);
                        validRemoved = false;
                        break;
                    }
                }
                if (validRemoved) {
                    hasToPlace.add(new Block(row, col, oldValue));
                    removeFromAnswer(oldValue, row, col, true);
                } else {
                    removeFromAnswer(oldValue, row, col);
                }
                if (wrong.isEmpty()) {
                    hasViolated = false;
                    Toast.makeText(getThis(), "Place Next Blocks..", Toast.LENGTH_LONG).show();
                    correctnessView.setVisibility(View.GONE);
                    correctImageView.setVisibility(View.GONE);
                    incorrectImageView.setVisibility(View.GONE);
                }
            } else {
                hasToPlace.add(new Block(row, col, oldValue));
                removeFromAnswer(oldValue, row, col, true);
            }
            updateAnswer();
            numBlocks--;
        }
    }

    private void endActivity(){
        long time_elapsed = (System.nanoTime() - time) / 1000;
        ResultState result = new ResultState(numIncorrectBlock, time_elapsed, hasSkipped);
        Intent result_intent = new Intent();
        result_intent.putExtra("result", result);
        setResult(RESULT_OK, result_intent);
        finish();
    }

    private void removeFromAnswer(BaseValue value, int row, int col) {
        for (int i = 0; i < answerState.size(); i++) {
            if (answerState.get(i).row == row && answerState.get(i).col == col) {
                answerState.remove(i);
                break;
            }
        }
    }

    private void removeFromAnswer(BaseValue value, int row, int col, boolean mark) {
        for (int i = 0; i < answerState.size(); i++) {
            if (answerState.get(i).row == row && answerState.get(i).col == col) {
                if (mark) {
                    answerState.get(i).color = AnswerToken.Color.Red;
                } else {
                    answerState.remove(i);
                    break;
                }
            }
        }
    }

    private boolean isCorrectInSequence(Node<BaseValue> node, BaseValue value) {
        for (Node<BaseValue> possible : node.children) {
            if (possible.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private Node<BaseValue> getCorrectNode(Node<BaseValue> node, BaseValue value) {
        for (Node<BaseValue> possible : node.children) {
            if (possible.value.equals(value)) {
                return possible;
            }
        }
        return null;
    }

    protected void playSound(int correctAudio) {
        if (player != null) {
            player.stop();
            player.release(); // Properly release previous instance
            player = null;
        }

        player = MediaPlayer.create(this, correctAudio);
        if (player != null) {
            player.setOnCompletionListener(mediaPlayer -> {
                player.release();
                player = null;
            });
            player.start(); // Start playback
        } else {
            Log.e("AudioPlay", "Error creating MediaPlayer for resource: " + correctAudio);
        }
    }

    protected synchronized void completionAudio(int correctAudio) {
        if (!completionMediaPlayerInUse && completionMediaPlayer == null) {
            completionMediaPlayerInUse = true;
            completionMediaPlayer = MediaPlayer.create(this, correctAudio);
            if (!completionMediaPlayer.isPlaying()) completionMediaPlayer.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            completionMediaPlayer = MediaPlayer.create(this, remove_all_audio);
            if (!completionMediaPlayer.isPlaying()) completionMediaPlayer.start();
        }
    }

    protected abstract boolean isCorrectCategory(BaseValue value);

    protected String generateAnswerString() {
        StringBuilder answerBuilder = new StringBuilder();
        for (int i = 0; i < answerState.size(); i++) {
            answerBuilder.append(answerState.get(i).generateHTMLString());
        }
        return answerBuilder.toString();
    }

    protected abstract void updateAnswer();

    public boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void showDevices(android.view.View v) {
        if (!BleService.isConnected) {
            Intent intent = new Intent(getThis(), DeviceListActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(getThis(), "Device already connected, Restart App or Switch Off Device for new connection", Toast.LENGTH_LONG).show();
        }
    }

    protected void requestPermissions() {
        if (!hasPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_BLE_PERMISSIONS);
            }
        }
    }

    public void checkAndPromptLocation() {
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true);

        SettingsClient client = LocationServices.getSettingsClient(getThis());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException exception) {
                    if (exception instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(getThis(), 1234);
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

    protected void onDataReceive(byte[] data) {
        int row = 3 - data[0];
        for (int col = 0; col < 8; col++) {
            BaseValue value = map.getMapping((data[col + 1] & 0xFF));
            if (value != null) {
                if (!state[row][col].equals(value)) {
                    registerChange(row, col, value);
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

    void enableBleAndStartService() {
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is off, request user to turn it on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(getThis(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            startActivityForResult(enableBtIntent, 111);
        } else {
            Intent serviceIntent = new Intent(getThis(), BleService.class);
            startService(serviceIntent);
        }

    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
        }
        if (completionMediaPlayer != null) {
            if (completionMediaPlayer.isPlaying()) {
                completionMediaPlayer.stop();
            }
            completionMediaPlayer.release();
        }
        try {
            unregisterReceiver(bleDataReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        super.onDestroy();
    }


    protected void regBroadcast() {
        if (receiverRegistered) return;
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.fln.sData");
        filter.addAction("com.example.fln.cStatus");
        registerReceiver(bleDataReceiver, filter);
        receiverRegistered = true;
    }

    protected abstract void setViewsAndLayout();

    protected abstract void setQuestion();

    protected abstract QuestionActivity getThis();
}