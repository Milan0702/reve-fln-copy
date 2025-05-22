package com.example.fln.tokens;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class BaseValue implements Cloneable, Serializable {
    public String stringValue;
    public String imagePath;
    public String audioPath;
    public byte byteKey;

    public BaseValue(String stringValue, String imagePath, String audioPath, byte byteKey) {
        this.stringValue = stringValue;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
        this.byteKey = byteKey;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public String toString() {
        final String format;
        format = String.format("BaseValue(%s, %s, %s, %d)", stringValue, imagePath, audioPath, byteKey);
        return format;
    }

    public boolean equals(BaseValue other) {
        return byteKey==other.byteKey;
    }

    @NonNull
    @Override
    public BaseValue clone() {
        try {
            return (BaseValue) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getStringValue() {
        return stringValue;
    }
}
