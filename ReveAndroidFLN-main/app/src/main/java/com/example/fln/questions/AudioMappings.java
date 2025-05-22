package com.example.fln.questions;

import com.example.fln.R;

import java.lang.reflect.Field;
import java.util.HashMap;

public class AudioMappings {
    public final HashMap<String, String> mappings;
    public AudioMappings() {
        mappings = new HashMap<>();
        mappings.put("/", "divide");
        mappings.put("+", "plus");
        mappings.put("=", "equals");
        mappings.put("2", "two");
        mappings.put("4", "four");
        mappings.put("8", "eight");
        mappings.put("<CORRECT_ANS>", "place_correct_answer");
    }

    public int getMapping(String key) {
        int result;
        if (!mappings.containsKey(key)) {
            result = -1;
        } else {
            result = getResId(mappings.get(key), R.raw.class);
        }
        return result;
    }

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
