package com.example.fln.tokens;

import androidx.annotation.NonNull;

public class Token extends BaseValue {

    public Token(String stringValue, String imagePath, String audioPath, byte byteKey) {
        super(stringValue, imagePath, audioPath, byteKey);
    }

    @NonNull
    @Override
    public Token clone() {
        try {
            return (Token) super.clone();
        } catch (AssertionError e) {
            throw new AssertionError();
        }
    }
}
