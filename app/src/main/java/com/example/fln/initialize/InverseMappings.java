package com.example.fln.initialize;

import com.example.fln.tokens.BaseValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InverseMappings implements Serializable {
    private final HashMap<String, BaseValue> inverseMappings;
    private final BaseValue defaultValue;

    public InverseMappings(Mappings mappings) {
        inverseMappings = new HashMap<>();
        defaultValue = mappings.getDefaultValue();

        for (Map.Entry<Integer, BaseValue> entry : mappings.mappings.entrySet()) {
            BaseValue value = entry.getValue();
            if (value != null && value.getStringValue() != null) {
                inverseMappings.put(value.getStringValue(), value.clone());
            }
        }
    }

    public BaseValue getMapping(String key) {
        if (!inverseMappings.containsKey(key)) {
            return null;
        }
        return Objects.requireNonNull(inverseMappings.get(key)).clone();
    }

    public BaseValue getDefaultValue() {
        return defaultValue.clone();
    }
}

