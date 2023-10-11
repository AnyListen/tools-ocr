package com.litongjava.ai.server.service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.paddlepaddle.zoo.cv.imageclassification.PpWordRotateTranslator;
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslator;
import ai.djl.paddlepaddle.zoo.cv.wordrecognition.PpWordRecognitionTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class PaddleOcrService {
  private static String detModelUrls = "https://resources.djl.ai/test-models/paddleOCR/mobile/det_db.zip";
  private static String clsModelUrls = "https://resources.djl.ai/test-models/paddleOCR/mobile/cls.zip";
  private static String recModelUrls = "https://resources.djl.ai/test-models/paddleOCR/mobile/rec_crnn.zip";

//  private String detModelUrls = "https://paddleocr.bj.bcebos.com/PP-OCRv3/chinese/ch_PP-OCRv3_det_infer.tar";
//  private String clsModelUrls = "https://paddleocr.bj.bcebos.com/dygraph_v2.0/ch/ch_ppocr_mobile_v2.0_cls_infer.tar";
//  private String recModelUrls = "https://paddleocr.bj.bcebos.com/PP-OCRv3/chinese/ch_PP-OCRv3_rec_infer.tar";

  Predictor<Image, DetectedObjects> detector;
  Predictor<Image, Classifications> rotateClassifier;
  Predictor<Image, String> recognizer;

  public DetectedObjects index(String url)
      throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
    // src phooto
    // String url = "https://resources.djl.ai/images/flight_ticket.jpg";
    Image src = ImageFactory.getInstance().fromUrl(url);
    return index(src);
  }

  public DetectedObjects index(File file)
      throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
    @SuppressWarnings("deprecation")
    Image src = ImageFactory.getInstance().fromUrl(file.toURL());
    return index(src);
  }
  public DetectedObjects index(byte[] fileData) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
    ByteArrayInputStream is = new ByteArrayInputStream(fileData);
    Image src = ImageFactory.getInstance().fromInputStream(is);
    return index(src);
  }

  public DetectedObjects index(Image src)

      throws TranslateException, ModelNotFoundException, MalformedModelException, IOException {
    if (detector == null) {
      detector = getDetector();
    }
    if (rotateClassifier == null) {
      rotateClassifier = getRotater();
    }
    if (recognizer == null) {
      recognizer = getRecognizer();
    }

    // 检测图片
    DetectedObjects detectedObj = detector.predict(src);
    List<DetectedObjects.DetectedObject> boxes = detectedObj.items();

    // 获取所有项
    List<String> names = new ArrayList<>();
    List<Double> prob = new ArrayList<>();
    List<BoundingBox> rect = new ArrayList<>();

    for (int i = 0; i < boxes.size(); i++) {
      Image subImg = getSubImage(src, boxes.get(i).getBoundingBox());
      if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
        subImg = rotateImg(subImg);
      }
      Classifications.Classification result = rotateClassifier.predict(subImg).best();
      if ("Rotate".equals(result.getClassName()) && result.getProbability() > 0.8) {
        subImg = rotateImg(subImg);
      }
      String name = recognizer.predict(subImg);
      names.add(name);
      prob.add(-1.0);
      rect.add(boxes.get(i).getBoundingBox());
    }
    // 显示结果
    return new DetectedObjects(names, prob, rect);
  }

  private Predictor<Image, String> getRecognizer() throws IOException, ModelNotFoundException, MalformedModelException {
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

  private Predictor<Image, Classifications> getRotater()
      throws IOException, ModelNotFoundException, MalformedModelException {

    Criteria<Image, Classifications> criteria2 = Criteria.builder()
        //
        .optEngine("PaddlePaddle")
        //
        .setTypes(Image.class, Classifications.class)
        //
        .optModelUrls(clsModelUrls)
        //
        .optTranslator(new PpWordRotateTranslator()).build();
    ZooModel<Image, Classifications> rotateModel = criteria2.loadModel();
    // 翻转器
    Predictor<Image, Classifications> rotateClassifier = rotateModel.newPredictor();
    return rotateClassifier;
  }

  private Predictor<Image, DetectedObjects> getDetector()
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

  public Image rotateImg(Image image) {
    try (NDManager manager = NDManager.newBaseManager()) {
      NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
      return ImageFactory.getInstance().fromNDArray(rotated);
    }
  }

  public Image getSubImage(Image img, BoundingBox box) {
    Rectangle rect = box.getBounds();
    double[] extended = extendRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    int width = img.getWidth();
    int height = img.getHeight();
    int[] recovered = { (int) (extended[0] * width), (int) (extended[1] * height), (int) (extended[2] * width),
        (int) (extended[3] * height) };
    return img.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]);
  }

  public double[] extendRect(double xmin, double ymin, double width, double height) {
    double centerx = xmin + width / 2;
    double centery = ymin + height / 2;
    if (width > height) {
      width += height * 2.0;
      height *= 3.0;
    } else {
      height += width * 2.0;
      width *= 3.0;
    }
    double newX = centerx - width / 2 < 0 ? 0 : centerx - width / 2;
    double newY = centery - height / 2 < 0 ? 0 : centery - height / 2;
    double newWidth = newX + width > 1 ? 1 - newX : width;
    double newHeight = newY + height > 1 ? 1 - newY : height;
    return new double[] { newX, newY, newWidth, newHeight };
  }
}