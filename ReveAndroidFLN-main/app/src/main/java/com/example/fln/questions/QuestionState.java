package com.example.fln.questions;

import com.example.fln.tokens.BaseValue;

public class QuestionState {
    private Node<BaseValue> currentNode;
    private int currentIndex = 0;
    private int blockCount = 0;
    private boolean correctAnswerFound = false;
    private boolean hasViolation = false;
    private boolean isCompleted = false;

    public void initialize(Question question) {
        this.currentNode = question.generateAnswerTree();
        resetState();
    }

    private void resetState() {
        currentIndex = 0;
        blockCount = 0;
        correctAnswerFound = false;
        hasViolation = false;
        isCompleted = false;
    }

    public void incrementIndex() {
        currentIndex++;
    }

    public void incrementBlockCount() {
        blockCount++;
    }

    public void decrementBlockCount() {
        blockCount = Math.max(0, blockCount - 1);
    }

    // Getters and setters
    public Node<BaseValue> getCurrentNode() { return currentNode; }
    public void setCurrentNode(Node<BaseValue> node) { this.currentNode = node; }
    public boolean isViolated() { return hasViolation; }
    public void setViolated(boolean violated) { this.hasViolation = violated; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public int getBlockCount() { return blockCount; }
}