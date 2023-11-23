package com.litongjava.djl.paddle.ocr.v4.detection;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Paths;

/**
 * 文字检测
 */
public final class OcrV4Detection {
  /**
   * 中文文本检测
   *
   * @return
   */
  public Criteria<Image, NDList> chDetCriteria() {
    Criteria<Image, NDList> criteria =
      Criteria.builder()
        .optEngine("OnnxRuntime")
        // .optModelName("inference")
        .setTypes(Image.class, NDList.class)
        .optModelPath(Paths.get("models/ch_PP-OCRv4_det_infer/inference.onnx"))
        .optTranslator(new OCRDetectionTranslator(new ConcurrentHashMap<String, String>()))
        .optProgress(new ProgressBar())
        .build();

    return criteria;
  }

}