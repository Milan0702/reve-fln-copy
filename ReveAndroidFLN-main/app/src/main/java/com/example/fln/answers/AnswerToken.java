package com.example.fln.answers;

import com.example.fln.tokens.Block;

public class AnswerToken extends Block {
    public enum Color{
        Red,
        Green
    }

    public Color color;

    public AnswerToken(Block block, Color color){
        super(block.row, block.col, block.value);
        this.color = color;
    }

    public String generateHTMLString(){
        String color_string = "<font color=%s>%s</font>";
        if(color == Color.Red){
            return String.format(color_string, "#FF0000", value.stringValue);
        }else if(color == Color.Green) {
            return String.format(color_string, "#00FF00", value.stringValue);
        }else {
            return value.stringValue;
        }
    }
}
