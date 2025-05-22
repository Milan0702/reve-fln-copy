package com.example.fln.questions;

import java.util.ArrayList;

public class Node<T> {
    public final T value;
    public final ArrayList<Node<T>> children = new ArrayList<>();
    public Node<T> parent = null;

    public Node(T value) {
        this.value = value;
    }

    public Node<T> addChild(T val) {
        Node<T> child = new Node<>(val);
        child.parent = this;
        children.add(child);
        return child;
    }

    public void addPath(ArrayList<T> path) {
        Node<T> current = this;
        for (T val : path) {
            current = current.addChild(val);
        }
    }
}