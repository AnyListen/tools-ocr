package com.litongjava.djl.paddle.ocr.v4.recognition;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.Point;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import com.litongjava.djl.paddle.ocr.v4.common.RotatedBox;
import com.litongjava.djl.paddle.ocr.v4.opencv.NDArrayUtils;
import com.litongjava.djl.paddle.ocr.v4.opencv.OpenCVUtils;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文字识别
 */
public final class OcrV4Recognition {


  /**
   * 中文简体
   *
   * @return
   */
  public Criteria<Image, String> chRecCriteria() {
    Path modelPath = Paths.get("models/ch_PP-OCRv4_rec_infer/inference.onnx");
    Criteria<Image, String> criteria =
      Criteria.builder()
        .optEngine("OnnxRuntime")
        //.optModelName("inference")
        .setTypes(Image.class, String.class)
        .optModelPath(modelPath)
        .optProgress(new ProgressBar())
        .optTranslator(new PpWordRecTranslator(new ConcurrentHashMap<String, String>()))
        .build();
    return criteria;
  }


  /**
   * 图像推理
   *
   * @param manager
   * @param image
   * @param detector
   * @param recognizer
   * @return
   * @throws TranslateException
   */
  public List<RotatedBox> predict(NDManager manager,
                                  Image image, Predictor<Image, NDList> detector, Predictor<Image, String> recognizer)
    throws TranslateException {
    NDList boxes = detector.predict(image);
    // 交给 NDManager自动管理内存
    // attach to manager for automatic memory management
    boxes.attach(manager);

    List<RotatedBox> result = new ArrayList<>();

    Mat mat = (Mat) image.getWrappedImage();

    for (int i = 0; i < boxes.size(); i++) {
      NDArray box = boxes.get(i);

      float[] pointsArr = box.toFloatArray();
      float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
      float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
      float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);
      float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);
      int img_crop_width = (int) Math.max(distance(lt, rt), distance(rb, lb));
      int img_crop_height = (int) Math.max(distance(lt, lb), distance(rt, rb));
      List<Point> srcPoints = new ArrayList<>();
      srcPoints.add(new Point(lt[0], lt[1]));
      srcPoints.add(new Point(rt[0], rt[1]));
      srcPoints.add(new Point(rb[0], rb[1]));
      srcPoints.add(new Point(lb[0], lb[1]));
      List<Point> dstPoints = new ArrayList<>();
      dstPoints.add(new Point(0, 0));
      dstPoints.add(new Point(img_crop_width, 0));
      dstPoints.add(new Point(img_crop_width, img_crop_height));
      dstPoints.add(new Point(0, img_crop_height));

      Mat srcPoint2f = NDArrayUtils.toMat(srcPoints);
      Mat dstPoint2f = NDArrayUtils.toMat(dstPoints);

      Mat cvMat = OpenCVUtils.perspectiveTransform(mat, srcPoint2f, dstPoint2f);

      Image subImg = OpenCVImageFactory.getInstance().fromImage(cvMat);
//            ImageUtils.saveImage(subImg, i + ".png", "build/output");

      subImg = subImg.getSubImage(0, 0, img_crop_width, img_crop_height);
      if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
        subImg = rotateImg(manager, subImg);
      }

      String name = recognizer.predict(subImg);
      RotatedBox rotatedBox = new RotatedBox(box, name);
      result.add(rotatedBox);

      cvMat.release();
      srcPoint2f.release();
      dstPoint2f.release();

    }

    return result;
  }

  private BufferedImage get_rotate_crop_image(Image image, NDArray box) {
    return null;
  }

  /**
   * 欧式距离计算
   *
   * @param point1
   * @param point2
   * @return
   */
  private float distance(float[] point1, float[] point2) {
    float disX = point1[0] - point2[0];
    float disY = point1[1] - point2[1];
    float dis = (float) Math.sqrt(disX * disX + disY * disY);
    return dis;
  }

  /**
   * 图片旋转
   *
   * @param manager
   * @param image
   * @return
   */
  private Image rotateImg(NDManager manager, Image image) {
    NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
    return ImageFactory.getInstance().fromNDArray(rotated);
  }
}