package com.luooqi.ocr.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by litonglinux@qq.com on 12/9/2023_6:28 PM
 */
public class BufferedImageUtils {
  public static InputStream toInputStream(BufferedImage bufferedImage) {
    // 将BufferedImage写入到一个ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ImageIO.write(bufferedImage, "png", baos); // 选择合适的格式，如 "png" 或 "jpg"
    } catch (IOException e) {
      e.printStackTrace();
    }

    // 使用输出流的字节数组来创建一个InputStream
    byte[] imageBytes = baos.toByteArray();
    InputStream inputStream = new ByteArrayInputStream(imageBytes);
    return inputStream;
  }
}
