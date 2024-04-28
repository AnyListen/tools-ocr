package com.litongjava;

import com.benjaminwan.ocrlibrary.OcrResult;

import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;
import io.github.mymonstercat.ocr.config.HardwareConfig;

public class RapidOcrTest {
  public static void main(String[] args) {
    String imagePath = "C:\\Users\\Administrator\\Desktop\\01.jpg";

    // init
    HardwareConfig onnxConfig = HardwareConfig.getOnnxConfig();
    onnxConfig.setNumThread(2);
    InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4_SERVER, onnxConfig);

    // run
    OcrResult ocrResult = engine.runOcr(imagePath);    
    System.out.println(ocrResult.getStrRes().trim());
  }
}
