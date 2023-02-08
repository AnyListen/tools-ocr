package com.benjaminwan.ocrlibrary;

import java.util.ArrayList;
import java.util.Arrays;

public final class TextBlock {
    
    private final ArrayList<Point> boxPoint;
    private float boxScore;
    private final int angleIndex;
    private final float angleScore;
    private final double angleTime;
    
    private final String text;
    
    private final float[] charScores;
    private final double crnnTime;
    private final double blockTime;

    
    public String toString() {
        return "TextBlock(boxPoint=" + this.boxPoint + ", boxScore=" + this.boxScore + ", angleIndex=" + this.angleIndex + ", angleScore=" + this.angleScore + ", angleTime=" + this.angleTime + ", text=" + this.text + ", charScores=" + Arrays.toString(this.charScores) + ", crnnTime=" + this.crnnTime + ", blockTime=" + this.blockTime + ')';
    }

    public TextBlock( ArrayList<Point> boxPoint, float boxScore, int angleIndex, float angleScore, double angleTime,  String text,  float[] charScores, double crnnTime, double blockTime) {
        this.boxPoint = boxPoint;
        this.boxScore = boxScore;
        this.angleIndex = angleIndex;
        this.angleScore = angleScore;
        this.angleTime = angleTime;
        this.text = text;
        this.charScores = charScores;
        this.crnnTime = crnnTime;
        this.blockTime = blockTime;
    }

    
    public ArrayList<Point> getBoxPoint() {
        return this.boxPoint;
    }

    public float getBoxScore() {
        return this.boxScore;
    }

    public void setBoxScore(float f) {
        this.boxScore = f;
    }

    public int getAngleIndex() {
        return this.angleIndex;
    }

    public float getAngleScore() {
        return this.angleScore;
    }

    public double getAngleTime() {
        return this.angleTime;
    }

    
    public String getText() {
        return this.text;
    }

    
    public float[] getCharScores() {
        return this.charScores;
    }

    public double getCrnnTime() {
        return this.crnnTime;
    }

    public double getBlockTime() {
        return this.blockTime;
    }
}
