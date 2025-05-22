package com.example.fln.initialize.managers;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.fln.R;

import java.util.Random;

public class SoundManager {
    private final Context context;
    private MediaPlayer currentPlayer;
    private MediaPlayer completionPlayer;

    private static final int[] CORRECT_SOUNDS = {R.raw.correct1, R.raw.correct2, R.raw.correct3};
    private static final int[] INCORRECT_SOUNDS = {R.raw.incorrect1, R.raw.incorrect2, R.raw.incorrect3};
    private static final int BEEP_SOUND = R.raw.beep;
    private static final int REMOVE_ALL_SOUND = R.raw.remove_all;

    public SoundManager(Context context) {
        this.context = context;
    }

    public void playCorrectSound() {
        playRandomSound(CORRECT_SOUNDS);
    }

    public void playIncorrectSound() {
        playRandomSound(INCORRECT_SOUNDS);
    }

    public void playBeep() {
        playSingleSound(BEEP_SOUND);
    }

    public void playRemoveAll() {
        playSingleSound(REMOVE_ALL_SOUND);
    }

    private void playRandomSound(int[] sounds) {
        int soundIndex = new Random().nextInt(sounds.length);
        playSingleSound(sounds[soundIndex]);
    }

    public void playSingleSound(int soundResId) {
        releaseCurrentPlayer();
        currentPlayer = MediaPlayer.create(context, soundResId);
        if (currentPlayer != null) {
            currentPlayer.setOnCompletionListener(this::releaseCurrentPlayer);
            currentPlayer.start();
        }
    }

    // Modified to handle currentPlayer without parameter
    private void releaseCurrentPlayer() {
        if (currentPlayer != null) {
            currentPlayer.release();
            currentPlayer = null;
        }
    }

    public void startCompletionSequence() {
        releaseCompletionPlayer();
        completionPlayer = MediaPlayer.create(context, CORRECT_SOUNDS[0]);
        completionPlayer.setOnCompletionListener(mp -> {
            releaseCompletionPlayer();
            playRemoveAll();
        });
        completionPlayer.start();
    }

    private void releaseCurrentPlayer(MediaPlayer mp) {
        if (mp != null) {
            mp.release();
            currentPlayer = null;
        }
    }

    private void releaseCompletionPlayer() {
        if (completionPlayer != null) {
            completionPlayer.release();
            completionPlayer = null;
        }
    }

    public void release() {
        releaseCurrentPlayer(currentPlayer);
        releaseCompletionPlayer();
    }
}