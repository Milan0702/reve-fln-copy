package com.example.fln.tokens;

import androidx.annotation.NonNull;

public class Alphabet extends BaseValue {
    public Alphabet(String stringValue, String imagePath, String audioPath, byte byteKey) {
        super(stringValue, imagePath, audioPath, byteKey);
    }

    @NonNull
    @Override
    public Alphabet clone() {
        try {
            return (Alphabet) super.clone();
        } catch (AssertionError e) {
            throw new AssertionError();
        }
    }
}
