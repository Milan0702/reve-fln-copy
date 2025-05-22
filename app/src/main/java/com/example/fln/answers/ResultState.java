package com.example.fln.answers;

import java.io.Serializable;

public class ResultState implements Serializable {
    int incorrectPlacements;
    long time;
    boolean skipped;

    public ResultState(int incorrectPlacements, long time, boolean skipped){
        this.incorrectPlacements = incorrectPlacements;
        this.skipped = skipped;
        this.time = time;
    }

    public long getTime(){
        return this.time;
    }

    public int getIncorrectPlacements(){
        return incorrectPlacements;
    }
}
