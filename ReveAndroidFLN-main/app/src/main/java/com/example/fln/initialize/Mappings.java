package com.example.fln.initialize;

import com.example.fln.tokens.Alphabet;
import com.example.fln.tokens.Animal;
import com.example.fln.tokens.BaseValue;
import com.example.fln.tokens.Bird;
import com.example.fln.tokens.Number;
import com.example.fln.tokens.Symbol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public class Mappings implements Serializable {
    public static final int startBlockCode = 100;
    public final HashMap<Integer, BaseValue> mappings;

    BaseValue defaultValue = new BaseValue("/\\", "NULL", "NULL", (byte) 255);

    public Mappings() {
        mappings = new HashMap<>();
        // Numbers: 0-9
        mappings.put(0, new Number("0", "NULL", "NULL", (byte) 0));
        mappings.put(1, new Number("1", "NULL", "NULL", (byte) 1));
        mappings.put(2, new Number("2", "NULL", "NULL", (byte) 2));
        mappings.put(3, new Number("3", "NULL", "NULL", (byte) 3));
        mappings.put(4, new Number("4", "NULL", "NULL", (byte) 4));
        mappings.put(5, new Number("5", "NULL", "NULL", (byte) 5));
        mappings.put(6, new Number("6", "NULL", "NULL", (byte) 6));
        mappings.put(7, new Number("7", "NULL", "NULL", (byte) 7));
        mappings.put(8, new Number("8", "NULL", "NULL", (byte) 8));
        mappings.put(9, new Number("9", "NULL", "NULL", (byte) 9));

        // Symbols: 10-31 (excluding "None")
        mappings.put(10, new Symbol("+", "NULL", "NULL", (byte) 10));
        mappings.put(11, new Symbol("-", "NULL", "NULL", (byte) 11));
        mappings.put(12, new Symbol("*", "NULL", "NULL", (byte) 12));
        mappings.put(13, new Symbol("/", "NULL", "NULL", (byte) 13));
        mappings.put(14, new Symbol("(", "NULL", "NULL", (byte) 14));
        mappings.put(15, new Symbol(")", "NULL", "NULL", (byte) 15));
        mappings.put(16, new Symbol("=", "NULL", "NULL", (byte) 16));

        // Alphabet: 32-63 (excluding None)
        mappings.put(32, new Alphabet("A", "NULL", "NULL", (byte) 32));
        mappings.put(33, new Alphabet("B", "NULL", "NULL", (byte) 33));
        mappings.put(34, new Alphabet("C", "NULL", "NULL", (byte) 34));
        mappings.put(35, new Alphabet("D", "NULL", "NULL", (byte) 35));
        mappings.put(36, new Alphabet("E", "NULL", "NULL", (byte) 36));
        mappings.put(37, new Alphabet("F", "NULL", "NULL", (byte) 37));
        mappings.put(38, new Alphabet("G", "NULL", "NULL", (byte) 38));
        mappings.put(39, new Alphabet("H", "NULL", "NULL", (byte) 39));
        mappings.put(40, new Alphabet("I", "NULL", "NULL", (byte) 40));
        mappings.put(41, new Alphabet("J", "NULL", "NULL", (byte) 41));
        mappings.put(42, new Alphabet("K", "NULL", "NULL", (byte) 42));
        mappings.put(43, new Alphabet("L", "NULL", "NULL", (byte) 43));
        mappings.put(44, new Alphabet("M", "NULL", "NULL", (byte) 44));
        mappings.put(45, new Alphabet("N", "NULL", "NULL", (byte) 45));
        mappings.put(46, new Alphabet("O", "NULL", "NULL", (byte) 46));
        mappings.put(47, new Alphabet("P", "NULL", "NULL", (byte) 47));
        mappings.put(48, new Alphabet("Q", "NULL", "NULL", (byte) 48));
        mappings.put(49, new Alphabet("R", "NULL", "NULL", (byte) 49));
        mappings.put(50, new Alphabet("S", "NULL", "NULL", (byte) 50));
        mappings.put(51, new Alphabet("T", "NULL", "NULL", (byte) 51));
        mappings.put(52, new Alphabet("U", "NULL", "NULL", (byte) 52));
        mappings.put(53, new Alphabet("V", "NULL", "NULL", (byte) 53));
        mappings.put(54, new Alphabet("W", "NULL", "NULL", (byte) 54));
        mappings.put(55, new Alphabet("X", "NULL", "NULL", (byte) 55));
        mappings.put(56, new Alphabet("Y", "NULL", "NULL", (byte) 56));
        mappings.put(57, new Alphabet("Z", "NULL", "NULL", (byte) 57));

        // Birds: 64-95
        mappings.put(64, new Bird("B1", "NULL", "NULL", (byte) 64));
        mappings.put(65, new Bird("B2", "NULL", "NULL", (byte) 65));
        mappings.put(66, new Bird("B3", "NULL", "NULL", (byte) 66));
        mappings.put(67, new Bird("B4", "NULL", "NULL", (byte) 67));
        mappings.put(68, new Bird("B5", "NULL", "NULL", (byte) 68));
        mappings.put(69, new Bird("B6", "NULL", "NULL", (byte) 69));
        mappings.put(70, new Bird("B7", "NULL", "NULL", (byte) 70));
        mappings.put(71, new Bird("B8", "NULL", "NULL", (byte) 71));
        mappings.put(72, new Bird("B9", "NULL", "NULL", (byte) 72));
        mappings.put(73, new Bird("B10", "NULL", "NULL", (byte) 73));
        mappings.put(74, new Bird("B11", "NULL", "NULL", (byte) 74));
        mappings.put(75, new Bird("B12", "NULL", "NULL", (byte) 75));
        mappings.put(76, new Bird("B13", "NULL", "NULL", (byte) 76));
        mappings.put(77, new Bird("B14", "NULL", "NULL", (byte) 77));
        mappings.put(78, new Bird("B15", "NULL", "NULL", (byte) 78));
        mappings.put(79, new Bird("B16", "NULL", "NULL", (byte) 79));
        mappings.put(80, new Bird("B17", "NULL", "NULL", (byte) 80));
        mappings.put(81, new Bird("B18", "NULL", "NULL", (byte) 81));
        mappings.put(82, new Bird("B19", "NULL", "NULL", (byte) 82));
        mappings.put(83, new Bird("B20", "NULL", "NULL", (byte) 83));
        mappings.put(84, new Bird("B21", "NULL", "NULL", (byte) 84));
        mappings.put(85, new Bird("B22", "NULL", "NULL", (byte) 85));
        mappings.put(86, new Bird("B23", "NULL", "NULL", (byte) 86));
        mappings.put(87, new Bird("B24", "NULL", "NULL", (byte) 87));
        mappings.put(88, new Bird("B25", "NULL", "NULL", (byte) 88));
        mappings.put(89, new Bird("B26", "NULL", "NULL", (byte) 89));
        mappings.put(90, new Bird("B27", "NULL", "NULL", (byte) 90));
        mappings.put(91, new Bird("B28", "NULL", "NULL", (byte) 91));
        mappings.put(92, new Bird("B29", "NULL", "NULL", (byte) 92));
        mappings.put(93, new Bird("B30", "NULL", "NULL", (byte) 93));
        mappings.put(94, new Bird("B31", "NULL", "NULL", (byte) 94));
        mappings.put(95, new Bird("B32", "NULL", "NULL", (byte) 95));

        // Animals: 96-127
        mappings.put(96, new Animal("A1", "NULL", "NULL", (byte) 96));
        mappings.put(97, new Animal("A2", "NULL", "NULL", (byte) 97));
        mappings.put(98, new Animal("A3", "NULL", "NULL", (byte) 98));
        mappings.put(99, new Animal("A4", "NULL", "NULL", (byte) 99));
        mappings.put(100, new Animal("A5", "NULL", "NULL", (byte) 100));
        mappings.put(101, new Animal("A6", "NULL", "NULL", (byte) 101));
        mappings.put(102, new Animal("A7", "NULL", "NULL", (byte) 102));
        mappings.put(103, new Animal("A8", "NULL", "NULL", (byte) 103));
        mappings.put(104, new Animal("A9", "NULL", "NULL", (byte) 104));
        mappings.put(105, new Animal("A10", "NULL", "NULL", (byte) 105));
        mappings.put(106, new Animal("A11", "NULL", "NULL", (byte) 106));
        mappings.put(107, new Animal("A12", "NULL", "NULL", (byte) 107));
        mappings.put(108, new Animal("A13", "NULL", "NULL", (byte) 108));
        mappings.put(109, new Animal("A14", "NULL", "NULL", (byte) 109));
        mappings.put(110, new Animal("A15", "NULL", "NULL", (byte) 110));
        mappings.put(111, new Animal("A16", "NULL", "NULL", (byte) 111));
        mappings.put(112, new Animal("A17", "NULL", "NULL", (byte) 112));
        mappings.put(113, new Animal("A18", "NULL", "NULL", (byte) 113));
        mappings.put(114, new Animal("A19", "NULL", "NULL", (byte) 114));
        mappings.put(115, new Animal("A20", "NULL", "NULL", (byte) 115));
        mappings.put(116, new Animal("A21", "NULL", "NULL", (byte) 116));
        mappings.put(117, new Animal("A22", "NULL", "NULL", (byte) 117));
        mappings.put(118, new Animal("A23", "NULL", "NULL", (byte) 118));
        mappings.put(119, new Animal("A24", "NULL", "NULL", (byte) 119));
        mappings.put(120, new Animal("A25", "NULL", "NULL", (byte) 120));
        mappings.put(121, new Animal("A26", "NULL", "NULL", (byte) 121));
        mappings.put(122, new Animal("A27", "NULL", "NULL", (byte) 122));
        mappings.put(123, new Animal("A28", "NULL", "NULL", (byte) 123));
        mappings.put(124, new Animal("A29", "NULL", "NULL", (byte) 124));
        mappings.put(125, new Animal("A30", "NULL", "NULL", (byte) 125));
        mappings.put(126, new Animal("A31", "NULL", "NULL", (byte) 126));
        mappings.put(127, new Animal("A32", "NULL", "NULL", (byte) 127));

        mappings.put(255, new BaseValue("/\\", "NULL", "NULL", (byte) 255));
    }

    public BaseValue getMapping(int key) {
        if (!mappings.containsKey(key)) {
            return null;
        }
        return mappings.get(key);
    }

    public BaseValue getDefaultValue() {
        return defaultValue.clone();
    }
}
