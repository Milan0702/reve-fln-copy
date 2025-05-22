package com.example.fln.questions.birds;

import com.example.fln.initialize.InverseMappings;
import com.example.fln.initialize.Mappings;
import com.example.fln.questions.Node;
import com.example.fln.questions.Question;
import com.example.fln.tokens.BaseValue;
import com.example.fln.tokens.Bird;

import java.util.ArrayList;

public class BirdFindQuestion extends Question {
    Mappings mappings = new Mappings();
    InverseMappings invMappings = new InverseMappings(mappings);

    public BirdFindQuestion() {
        super();
    }

    public BirdFindQuestion(String question_text, String answer) {
        super(question_text, null, null, answer);
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
        BaseValue birdValue = invMappings.getMapping(getAnswerText().toUpperCase());
        if (birdValue != null) {
            stream.add(birdValue);
        }
        return stream;
    }
}