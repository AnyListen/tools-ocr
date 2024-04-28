package com.luooqi.ocr.local;

import java.io.File;

import com.benjaminwan.ocrlibrary.OcrResult;

import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;

/**
 * Created by litonglinux@qq.com on 11/23/2023_2:09 AM
 */
public enum PaddlePaddleOCRV4 {
  INSTANCE;

  static InferenceEngine engine = null;

  PaddlePaddleOCRV4() {

  }

  // noting not to do.but init
  public static void init() {
    engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4_SERVER);
  }

  public OcrResult ocr(File imageFile) {
    return engine.runOcr(imageFile.getAbsolutePath());
  }

  public void close() {

  }
}
