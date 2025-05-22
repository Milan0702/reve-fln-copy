package com.example.fln.tokens;

import androidx.annotation.NonNull;

public class Number extends Token {
    private final int value;

    public Number(String stringValue, String imagePath, String audioPath, byte byteKey) {
        super(stringValue, imagePath, audioPath, byteKey);
        this.value = Integer.parseInt(stringValue);
    }

    @NonNull
    @Override
    public Number clone() {
        try {
            return (Number) super.clone();
        } catch (AssertionError e) {
            throw new AssertionError();
        }
    }

    public int getValue() {
        return this.value;
    }
}
