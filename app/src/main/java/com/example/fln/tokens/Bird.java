package com.example.fln.tokens;

import androidx.annotation.NonNull;

public class Bird extends BaseValue {
    public Bird(String stringValue, String imagePath, String audioPath, byte byteKey) {
        super(stringValue, imagePath, audioPath, byteKey);
    }

    @NonNull
    @Override
    public Bird clone() {
        try {
            return (Bird) super.clone();
        } catch (AssertionError e) {
            throw new AssertionError();
        }
    }
}
