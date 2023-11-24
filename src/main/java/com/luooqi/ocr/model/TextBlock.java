package com.luooqi.ocr.model;

import java.awt.*;

public class TextBlock {
  private Point topLeft;
  private Point topRight;
  private Point bottomLeft;
  private Point bottomRight;
  private double angle;
  private double fontSize;
  private String text;

  public TextBlock() {
  }

  public TextBlock(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight, String text) {
    this.topLeft = topLeft;
    this.topRight = topRight;
    this.bottomLeft = bottomLeft;
    this.bottomRight = bottomRight;
    this.text = text;
    calcAngle();
  }

  public Point getTopLeft() {
    return topLeft;
  }

  public void setTopLeft(Point topLeft) {
    this.topLeft = topLeft;
    calcAngle();
  }

  public Point getTopRight() {
    return topRight;
  }

  public void setTopRight(Point topRight) {
    this.topRight = topRight;
    calcAngle();
  }

  public Point getBottomLeft() {
    return bottomLeft;
  }

  public void setBottomLeft(Point bottomLeft) {
    this.bottomLeft = bottomLeft;
    calcAngle();
  }

  public Point getBottomRight() {
    return bottomRight;
  }

  public void setBottomRight(Point bottomRight) {
    this.bottomRight = bottomRight;
    calcAngle();
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public double getFontSize() {
    return fontSize;
  }

  private void setFontSize(double fontSize) {
    this.fontSize = fontSize;
  }

  private void calcAngle() {
    if (this.topLeft != null && this.bottomLeft != null) {
      int x = this.topLeft.x - this.bottomLeft.x;
      int y = this.bottomLeft.y - this.topLeft.y;
      setAngle(x * 1.0 / y);
      setFontSize(Math.sqrt(x * x + y * y));
    }
  }

  public double getAngle() {
    return angle;
  }

  private void setAngle(double angle) {
    this.angle = angle;
  }
}
