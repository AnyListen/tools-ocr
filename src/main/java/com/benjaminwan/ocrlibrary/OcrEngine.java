package com.benjaminwan.ocrlibrary;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.StaticLog;

import java.io.File;
import java.nio.charset.Charset;

public final class OcrEngine {
  /**
   * 图像外接白框，用于提升识别率，文字框没有正确框住所有文字时，增加此值。
   */
  private int padding;
  /**
   * 文字框置信度门限，文字框没有正确框住所有文字时，减小此值
   */
  private float boxScoreThresh;

  private float boxThresh;
  /**
   * 单个文字框大小倍率，越大时单个文字框越大
   */
  private float unClipRatio;
  /**
   * 启用(1)/禁用(0) 文字方向检测，只有图片倒置的情况下(旋转90~270度的图片)，才需要启用文字方向检测
   */
  private boolean doAngle;
  /**
   * 启用(1)/禁用(0) 角度投票(整张图片以最大可能文字方向来识别)，当禁用文字方向检测时，此项也不起作用
   */
  private boolean mostAngle;

  public native boolean setNumThread(int numThread);

  public native void initLogger(boolean isConsole, boolean isPartImg, boolean isResultImg);

  public native void enableResultText(String imagePath);

  public native boolean initModels(String modelsDir, String detName, String clsName, String recName, String keysName);

  /**
   * GPU0一般为默认GPU，参数选项：使用CPU(-1)/使用GPU0(0)/使用GPU1(1)/...
   */
  public native void setGpuIndex(int gpuIndex);

  public native String getVersion();

  public native OcrResult detect(String input, int padding, int maxSideLen, float boxScoreThresh, float boxThresh, float unClipRatio, boolean doAngle, boolean mostAngle);

  public OcrEngine() {
    try {
      StaticLog.info("java.library.path=" + System.getProperty("java.library.path"));
      System.loadLibrary("RapidOcrNcnn");
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.padding = 15;
    this.boxScoreThresh = 0.25f;
    this.boxThresh = 0.3f;
    this.unClipRatio = 1.6f;
    this.doAngle = true;
    this.mostAngle = true;
  }

  public int getPadding() {
    return this.padding;
  }

  public void setPadding(int i) {
    this.padding = i;
  }

  public float getBoxScoreThresh() {
    return this.boxScoreThresh;
  }

  public void setBoxScoreThresh(float f) {
    this.boxScoreThresh = f;
  }

  public float getBoxThresh() {
    return this.boxThresh;
  }

  public void setBoxThresh(float f) {
    this.boxThresh = f;
  }

  public float getUnClipRatio() {
    return this.unClipRatio;
  }

  public void setUnClipRatio(float f) {
    this.unClipRatio = f;
  }

  public boolean getDoAngle() {
    return this.doAngle;
  }

  public void setDoAngle(boolean z) {
    this.doAngle = z;
  }

  public boolean getMostAngle() {
    return this.mostAngle;
  }

  public void setMostAngle(boolean z) {
    this.mostAngle = z;
  }

  public OcrResult detect(String input) {
    return detect(input, 0);
  }

  public OcrResult detect(String input, int maxSideLen) {
    return detect(input, this.padding, maxSideLen, this.boxScoreThresh, this.boxThresh, this.unClipRatio, this.doAngle, this.mostAngle);
  }
}