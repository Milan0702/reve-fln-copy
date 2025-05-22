package com.example.fln.questions;

import com.example.fln.tokens.BaseValue;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Question implements Serializable {
    protected final String question_text;
    private final String question_image;
    private final String question_audio;
    protected final String answer;
    public boolean guidanceAudio = false;

    public Question(String question_text, String question_image, String question_audio, String answer) {
        this.question_text = question_text;
        this.question_image = question_image;
        this.question_audio = question_audio;
        this.answer = answer;
    }

    public Question(){
        this.question_text = null;
        this.question_image = null;
        this.question_audio = null;
        this.answer = null;
    }

    public String getQuestion(){
        return this.question_text;
    }

    public String getImage(){
        return this.question_image;
    }

    public String getAudio(){
        return this.question_audio;
    }

    public String getAnswerText(){
        return this.answer;
    }

    public abstract ArrayList<BaseValue> parseAnswer();

    public abstract Node<BaseValue> generateAnswerTree();
}
