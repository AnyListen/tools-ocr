package com.litongjava.ai.server.service;

import ai.djl.MalformedModelException;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class PaddleOcrServiceTest {

  @Test
  public void index() {
    String filePath="D:\\images\\three_row.png";
    PaddleOcrService paddleOcrService=new PaddleOcrService();
    try {
      DetectedObjects index = paddleOcrService.index(new File(filePath));
      System.out.println(index);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ModelNotFoundException e) {
      throw new RuntimeException(e);
    } catch (MalformedModelException e) {
      throw new RuntimeException(e);
    } catch (TranslateException e) {
      throw new RuntimeException(e);
    }
  }
}