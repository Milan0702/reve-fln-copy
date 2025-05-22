package com.example.fln.tokens;

import androidx.annotation.NonNull;

public class Symbol extends Token {
    public Symbol(String stringValue, String imagePath, String audioPath, byte byteKey) {
        super(stringValue, imagePath, audioPath, byteKey);
    }

    @NonNull
    @Override
    public Symbol clone() {
        try {
            return (Symbol) super.clone();
        } catch (AssertionError e) {
            throw new AssertionError();
        }
    }
}
