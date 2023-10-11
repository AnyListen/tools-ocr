package com.litongjava.ai.server.service;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.translate.TranslateException;
import com.litongjava.jfinal.aop.Aop;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by litonglinux@qq.com on 10/11/2023_2:13 AM
 */
public class OcrRecognizerTest {

  @Test
  public void getRecognizer() throws IOException, TranslateException {
    String filePath = "D:\\images\\three_row.png";
    Path file = Paths.get(filePath);

    Image src = ImageFactory.getInstance().fromFile(file);


    OcrRecognizer ocrRecognizer = Aop.get(OcrRecognizer.class);
    String text = ocrRecognizer.getRecognizer().predict(src);
    System.out.println(text);
  }
}