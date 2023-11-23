package com.benjaminwan.ocrlibrary;

import java.util.ArrayList;

public final class OcrResult extends OcrOutput {
  private final double dbNetTime;

  private final ArrayList<TextBlock> textBlocks;
  private double detectTime;

  private String strRes;

  public OcrResult copy(double dbNetTime, ArrayList<TextBlock> textBlocks, double detectTime, String strRes) {
    return new OcrResult(dbNetTime, textBlocks, detectTime, strRes);
  }

  public String toString() {
    return "OcrResult(dbNetTime=" + this.dbNetTime + ", textBlocks=" + this.textBlocks + ", detectTime=" + this.detectTime + ", strRes=" + this.strRes + ')';
  }

  public double getDbNetTime() {
    return this.dbNetTime;
  }


  public ArrayList<TextBlock> getTextBlocks() {
    return this.textBlocks;
  }

  public double getDetectTime() {
    return this.detectTime;
  }

  public void setDetectTime(double d) {
    this.detectTime = d;
  }


  public String getStrRes() {
    return this.strRes;
  }

  public void setStrRes(String str) {
    this.strRes = str;
  }

  public OcrResult(double dbNetTime, ArrayList<TextBlock> textBlocks, double detectTime, String strRes) {
    super();
    this.dbNetTime = dbNetTime;
    this.textBlocks = textBlocks;
    this.detectTime = detectTime;
    this.strRes = strRes;
  }
}
