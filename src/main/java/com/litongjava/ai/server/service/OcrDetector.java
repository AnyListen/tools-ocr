package com.litongjava.ai.server.service;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by litonglinux@qq.com on 10/11/2023_1:36 AM
 */
public class OcrDetector {

  //private static String detModelUrls = ModelUrls.detV4Server;
  //ai.djl.translate.TranslateException: java.lang.IndexOutOfBoundsException: Incorrect number of elements in NDList.singletonOrThrow: Expected 1 and was 2
  private static String detModelUrls = ModelUrls.detV4Infer;

  Predictor<Image, DetectedObjects> detector;

  public OcrDetector() {
    if (detector == null) {
      try {
        detector = newDetector(detModelUrls);
      } catch (ModelNotFoundException e) {
        e.printStackTrace();
      } catch (MalformedModelException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public OcrDetector(Predictor<Image, DetectedObjects> detector) {
    this.detector = detector;
  }


  public Predictor<Image, DetectedObjects> newDetector(String detModelUrls)
    throws ModelNotFoundException, MalformedModelException, IOException {
    Criteria<Image, DetectedObjects> criteria1 = Criteria.builder()
      //
      .optEngine("PaddlePaddle")
      //
      .setTypes(Image.class, DetectedObjects.class)
      //
      .optModelUrls(detModelUrls)
      //
      .optTranslator(new PpWordDetectionTranslator(new ConcurrentHashMap<String, String>())).build();
    //
    ZooModel<Image, DetectedObjects> detectionModel = criteria1.loadModel();
    // 检测器
    Predictor<Image, DetectedObjects> detector = detectionModel.newPredictor();
    return detector;
  }

  public Predictor<Image, DetectedObjects> getDetector() {
    return detector;
  }
}
