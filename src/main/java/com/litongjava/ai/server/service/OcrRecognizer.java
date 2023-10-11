package com.litongjava.ai.server.service;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.paddlepaddle.zoo.cv.wordrecognition.PpWordRecognitionTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;

import java.io.IOException;

/**
 * Created by litonglinux@qq.com on 10/11/2023_2:09 AM
 */
public class OcrRecognizer {

  //private static String recModelUrls = ModelUrls.recV4Server;
  private static String recModelUrls = ModelUrls.recV4Infer;

  Predictor<Image, String> recognizer;

  public OcrRecognizer() {
    if (recognizer == null) {
      try {
        recognizer = newRecognizer(recModelUrls);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ModelNotFoundException e) {
        e.printStackTrace();
      } catch (MalformedModelException e) {
        e.printStackTrace();
      }
    }
  }

  private Predictor<Image, String> newRecognizer(String recModelUrls) throws IOException, ModelNotFoundException, MalformedModelException {
    Criteria<Image, String> criteria3 = Criteria.builder()
      //
      .optEngine("PaddlePaddle").setTypes(Image.class, String.class)
      //
      .optModelUrls(recModelUrls)
      //
      .optTranslator(new PpWordRecognitionTranslator()).build();
    ZooModel<Image, String> recognitionModel = criteria3.loadModel();
    // 识别器
    Predictor<Image, String> recognizer = recognitionModel.newPredictor();
    return recognizer;
  }


  public Predictor<Image, String> getRecognizer() {
    return recognizer;
  }
}
