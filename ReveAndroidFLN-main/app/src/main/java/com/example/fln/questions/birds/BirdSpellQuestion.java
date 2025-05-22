package com.example.fln.questions.birds;

import com.example.fln.initialize.InverseMappings;
import com.example.fln.initialize.Mappings;
import com.example.fln.questions.Node;
import com.example.fln.questions.Question;
import com.example.fln.tokens.BaseValue;

import java.util.ArrayList;

public class BirdSpellQuestion extends Question {
    Mappings mappings = new Mappings();
    InverseMappings invMappings = new InverseMappings(mappings);

    public BirdSpellQuestion() {
        super();
    }

    public BirdSpellQuestion(String question_image_url, String answer) {
        super(null, question_image_url, null, answer);
    }

    @Override
    public Node<BaseValue> generateAnswerTree() {
        Node<BaseValue> root = new Node<>(invMappings.getMapping("HEAD"));
        ArrayList<BaseValue> sequence = parseAnswer();
        root.addPath(sequence);
        return root;
    }

    @Override
    public ArrayList<BaseValue> parseAnswer() {
        ArrayList<BaseValue> stream = new ArrayList<>();
        for (char c : getAnswerText().toCharArray()) {
            stream.add(invMappings.getMapping(String.valueOf(c).toUpperCase()));
        }
        return stream;
    }
}