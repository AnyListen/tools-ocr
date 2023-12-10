package com.luooqi.ocr.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfTest {

  @Test
  public void extraImageFromPdf() throws IOException {
    File pdfFile = new File("F:\\document\\dev-docs\\24.Internet_of_things\\02_C++\\2.1 面向C++模板库应用开发\\01 第一章C++.pdf");
    try (PDDocument document = PDDocument.load(pdfFile)) {
      PDFRenderer renderer = new PDFRenderer(document);

      for (int i = 0; i < document.getNumberOfPages(); ++i) {
        BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 300);
        FileOutputStream fileOutputStream = new FileOutputStream(i + ".png");
        ImageIO.write(bufferedImage, "png", fileOutputStream); // 选择合适的格式，如 "png" 或 "jpg"
      }
    }
  }
}
