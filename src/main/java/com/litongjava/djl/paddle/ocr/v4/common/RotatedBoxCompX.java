package com.litongjava.djl.paddle.ocr.v4.common;

import ai.djl.ndarray.NDArray;

/**
 */
public class RotatedBoxCompX implements Comparable<RotatedBoxCompX> {
  private NDArray box;
  private String text;

  public RotatedBoxCompX(NDArray box, String text) {
    this.box = box;
    this.text = text;
  }

  /**
   * 将左上角 X 坐标升序排序
   *
   * @param o
   * @return
   */
  @Override
  public int compareTo(RotatedBoxCompX o) {
    NDArray leftBox = this.getBox();
    NDArray rightBox = o.getBox();
    float leftX = leftBox.toFloatArray()[0];
    float rightX = rightBox.toFloatArray()[0];
    return (leftX < rightX) ? -1 : 1;
  }

  public NDArray getBox() {
    return box;
  }

  public void setBox(NDArray box) {
    this.box = box;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}