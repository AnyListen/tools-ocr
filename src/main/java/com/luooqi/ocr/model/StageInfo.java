package com.luooqi.ocr.model;

public class StageInfo {
  private double x;
  private double y;
  private double width;
  private double height;
  private boolean fullScreenState;

  public StageInfo() {
  }

  public StageInfo(double x, double y, double width, double height, boolean fullScreenState) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.fullScreenState = fullScreenState;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public boolean isFullScreenState() {
    return fullScreenState;
  }

  public void setFullScreenState(boolean fullScreenState) {
    this.fullScreenState = fullScreenState;
  }
}
