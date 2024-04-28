package com.litongjava;

import com.benjaminwan.ocrlibrary.OcrResult;

import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;

public class RapidOcrTest {
  public static void main(String[] args) {
    InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4_SERVER);
    OcrResult ocrResult = engine.runOcr("images/01.png");
    System.out.println(ocrResult.getStrRes().trim());
  }
}
