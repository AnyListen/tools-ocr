package com.luooqi.ocr.local;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import com.litongjava.djl.paddle.ocr.v4.common.RotatedBox;
import com.litongjava.djl.paddle.ocr.v4.common.RotatedBoxCompX;
import com.litongjava.djl.paddle.ocr.v4.detection.OcrV4Detection;
import com.litongjava.djl.paddle.ocr.v4.recognition.OcrV4Recognition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by litonglinux@qq.com on 11/23/2023_2:09 AM
 */
public enum PaddlePaddleOCRV4 {
  INSTANCE;
  private OcrV4Detection detection;
  private OcrV4Recognition recognition;
  private Predictor<Image, NDList> detector;
  private Predictor<Image, String> recognizer;
  private NDManager manager;

  PaddlePaddleOCRV4() {
    detection = new OcrV4Detection();
    recognition = new OcrV4Recognition();
    ZooModel detectionModel = null;
    ZooModel recognitionModel = null;
    try {
      detectionModel = ModelZoo.loadModel(detection.chDetCriteria());
      recognitionModel = ModelZoo.loadModel(recognition.chRecCriteria());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ModelNotFoundException e) {
      e.printStackTrace();
    } catch (MalformedModelException e) {
      e.printStackTrace();
    }
    detector = detectionModel.newPredictor();

    recognizer = recognitionModel.newPredictor();
    manager = NDManager.newBaseManager();
  }


  //noting not to do.but init
  public void init() {

  }

  public String ocr(File imageFile) throws Exception {
    Path path = imageFile.toPath();
    Image image = OpenCVImageFactory.getInstance().fromFile(path);
    List<RotatedBox> detections = recognition.predict(manager, image, detector, recognizer);

    List<RotatedBox> initList = new ArrayList<>();
    for (RotatedBox result : detections) {
      // put low Y value at the head of the queue.
      initList.add(result);
    }
    Collections.sort(initList);

    List<ArrayList<RotatedBoxCompX>> lines = new ArrayList<>();
    List<RotatedBoxCompX> line = new ArrayList<>();
    RotatedBoxCompX firstBox = new RotatedBoxCompX(initList.get(0).getBox(), initList.get(0).getText());
    line.add(firstBox);
    lines.add((ArrayList) line);
    for (int i = 1; i < initList.size(); i++) {
      RotatedBoxCompX tmpBox = new RotatedBoxCompX(initList.get(i).getBox(), initList.get(i).getText());
      float y1 = firstBox.getBox().toFloatArray()[1];
      float y2 = tmpBox.getBox().toFloatArray()[1];
      float dis = Math.abs(y2 - y1);
      if (dis < 20) { // 认为是同 1 行  - Considered to be in the same line
        line.add(tmpBox);
      } else { // 换行 - Line break
        firstBox = tmpBox;
        Collections.sort(line);
        line = new ArrayList<>();
        line.add(firstBox);
        lines.add((ArrayList) line);
      }
    }


    StringBuffer fullText = new StringBuffer();
    for (int i = 0; i < lines.size(); i++) {
      for (int j = 0; j < lines.get(i).size(); j++) {
        String text = lines.get(i).get(j).getText();
        if (text.trim().equals(""))
          continue;
        fullText.append(text + " ");
      }
      fullText.append('\n');
    }
    return fullText.toString();
  }
}
