package com.example.fln.questions;

import java.util.ArrayList;
import java.util.List;

public class AnswerSequenceTreeParser {
    public static ASTNode parse(String expr) {
        return parseAddSub(new ParserState(expr.replaceAll("\\s+", "")));
    }

    private static ASTNode parseAddSub(ParserState st) {
        ASTNode left = parseMulDiv(st);
        while (st.hasNext() && (st.peek() == '+' || st.peek() == '-')) {
            char op = st.next();
            ASTNode right = parseMulDiv(st);
            left = new OpNode(op, left, right);
        }
        return left;
    }

    private static ASTNode parseMulDiv(ParserState st) {
        ASTNode left = parseFactor(st);
        while (st.hasNext() && (st.peek() == '*' || st.peek() == '/')) {
            char op = st.next();
            ASTNode right = parseFactor(st);
            left = new OpNode(op, left, right);
        }
        return left;
    }

    private static ASTNode parseFactor(ParserState st) {
        if (st.peek() == '(') {
            st.next();
            ASTNode inner = parseAddSub(st);
            if (!st.hasNext() || st.next() != ')') {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            return new ParenNode(inner);
        }

        StringBuilder num = new StringBuilder();
        while (st.hasNext() && Character.isDigit(st.peek())) {
            num.append(st.next());
        }
        if (num.length() == 0) {
            throw new IllegalArgumentException("Expected number at position " + st.pos);
        }
        return new NumNode(num.toString());
    }

    public static int evaluate(ASTNode node) {
        if (node instanceof NumNode) {
            return Integer.parseInt(((NumNode) node).val);
        }
        if (node instanceof ParenNode) {
            return evaluate(((ParenNode) node).inner);
        }
        OpNode opn = (OpNode) node;
        int l = evaluate(opn.left);
        int r = evaluate(opn.right);
        if (opn.op == '+') {
            return l + r;
        } else if (opn.op == '-') {
            return l - r;
        } else if (opn.op == '*') {
            return l * r;
        } else if (opn.op == '/') {
            return l / r;
        } else {
            throw new IllegalStateException("Invalid operator");
        }

    }

    public static List<List<String>> generateSequences(ASTNode node) {
        if (node instanceof NumNode) {
            List<String> seq = new ArrayList<>();
            for (char c : ((NumNode) node).val.toCharArray()) seq.add(String.valueOf(c));
            return List.of(seq);
        }
        if (node instanceof ParenNode) {
            List<List<String>> inner = generateSequences(((ParenNode) node).inner);
            List<List<String>> wrapped = new ArrayList<>();
            for (List<String> seq : inner) {
                List<String> w = new ArrayList<>();
                w.add("(");
                w.addAll(seq);
                w.add(")");
                wrapped.add(w);
            }
            return wrapped;
        }
        OpNode opn = (OpNode) node;
        List<List<String>> left = generateSequences(opn.left);
        List<List<String>> right = generateSequences(opn.right);
        boolean comm = (opn.op == '+' || opn.op == '*');

        List<List<String>> out = new ArrayList<>();
        for (List<String> l : left) {
            for (List<String> r : right) {
                List<String> lr = new ArrayList<>(l);
                lr.add(String.valueOf(opn.op));
                lr.addAll(r);
                out.add(lr);
                if (comm) {
                    List<String> rl = new ArrayList<>(r);
                    rl.add(String.valueOf(opn.op));
                    rl.addAll(l);
                    out.add(rl);
                }
            }
        }
        return out;
    }

    public interface ASTNode {
    }

    public static class NumNode implements ASTNode {
        final String val;

        public NumNode(String v) {
            val = v;
        }
    }

    public static class OpNode implements ASTNode {
        final char op;
        final ASTNode left, right;

        public OpNode(char op, ASTNode l, ASTNode r) {
            this.op = op;
            left = l;
            right = r;
        }
    }

    public static class ParenNode implements ASTNode {
        final ASTNode inner;

        public ParenNode(ASTNode inner) {
            this.inner = inner;
        }
    }

    private static class ParserState {
        final String s;
        int pos = 0;

        ParserState(String s) {
            this.s = s;
        }

        boolean hasNext() {
            return pos < s.length();
        }

        char peek() {
            return s.charAt(pos);
        }

        char next() {
            return s.charAt(pos++);
        }
    }
}
