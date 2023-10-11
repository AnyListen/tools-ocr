package com.litongjava.ai.server.service;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslatorFactory;
import ai.djl.translate.TranslateException;
import com.litongjava.jfinal.aop.Aop;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by litonglinux@qq.com on 10/11/2023_1:43 AM
 */
public class OcrDetectorTest {

  @Test
  public void getDetector() throws IOException, TranslateException {
//    String filePath = "D:\\images\\three_row.png";
    String filePath = "D:\\images\\one_row.png";
    Path file = Paths.get(filePath);

    Image src = ImageFactory.getInstance().fromFile(file);
    OcrDetector ocrDetector = Aop.get(OcrDetector.class);
    // 检测图片
    DetectedObjects detectedObjects = ocrDetector.getDetector().predict(src);
    int numberOfObjects = detectedObjects.getNumberOfObjects();
    System.out.println(numberOfObjects);
    String s = detectedObjects.toJson();
    System.out.println(s);

  }
}