package com.litongjava;

import com.benjaminwan.ocrlibrary.OcrResult;

import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;
import io.github.mymonstercat.ocr.config.HardwareConfig;

public class RapidOcrTest {
  public static void main(String[] args) {
    HardwareConfig onnxConfig = HardwareConfig.getOnnxConfig();
    onnxConfig.setNumThread(2);
    InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4_SERVER, onnxConfig);
    OcrResult ocrResult = engine.runOcr("C:\\Users\\Administrator\\Desktop\\01.jpg");
    System.out.println(ocrResult.getStrRes().trim());
  }
}
