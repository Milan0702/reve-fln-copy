package com.example.fln.questions.math;

import com.example.fln.initialize.InverseMappings;
import com.example.fln.initialize.Mappings;
import com.example.fln.questions.AnswerSequenceTreeParser;
import com.example.fln.questions.Node;
import com.example.fln.questions.Question;
import com.example.fln.tokens.BaseValue;

import java.util.ArrayList;
import java.util.List;

public class MathOperationQuestion extends Question {
    Mappings mappings = new Mappings();
    InverseMappings invMappings = new InverseMappings(mappings);

    public MathOperationQuestion(String question_text, String answer, String question_audio, boolean guide) {
        super(question_text, null, question_audio, answer);
        guidanceAudio = guide;
    }

    @Override
    public ArrayList<BaseValue> parseAnswer() {
        ArrayList<BaseValue> stream = new ArrayList<>();
        for (char c : getQuestion().toCharArray()) {
            if (c == ' ') {
                continue;
            }
            stream.add(invMappings.getMapping(String.valueOf(c)));
        }
        for (char c : getAnswerText().toCharArray()) {
            stream.add(invMappings.getMapping(String.valueOf(c)));
        }
        return stream;
    }

    @Override
    public Node<BaseValue> generateAnswerTree() {
        String questionExpr = getQuestion().trim();
        if (!questionExpr.endsWith("=")) {
            throw new IllegalArgumentException("Question must end with '='");
        }

        AnswerSequenceTreeParser.ASTNode ast = AnswerSequenceTreeParser.parse(questionExpr.substring(0, questionExpr.length() - 1));
        String resultStr = getAnswerText();

        List<List<String>> sequences = AnswerSequenceTreeParser.generateSequences(ast);

        Node<BaseValue> root = new Node<>(invMappings.getMapping("HEAD"));

        for (List<String> seq : sequences) {
            ArrayList<BaseValue> full = new ArrayList<>();
            for (String s : seq) {
                full.add(invMappings.getMapping(s));
            }
            full.add(invMappings.getMapping("="));
            for (char d : resultStr.toCharArray()) {
                full.add(invMappings.getMapping(String.valueOf(d)));
            }
            root.addPath(full);
        }

        return root;
    }
}
