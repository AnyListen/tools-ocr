package com.litongjava.djl.paddle.ocr.v4.opencv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class OpenCVUtils {

  /**
   * 透视变换
   *
   * @param src
   * @param srcPoints
   * @param dstPoints
   * @return
   */
  public static Mat perspectiveTransform(Mat src, Mat srcPoints, Mat dstPoints) {
    Mat dst = src.clone();
    Mat warp_mat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
    Imgproc.warpPerspective(src, dst, warp_mat, dst.size());
    warp_mat.release();

    return dst;
  }

  /**
   * Mat to BufferedImage
   *
   * @param mat
   * @return
   */
  public static BufferedImage mat2Image(Mat mat) {
    int width = mat.width();
    int height = mat.height();
    byte[] data = new byte[width * height * (int) mat.elemSize()];
    Imgproc.cvtColor(mat, mat, 4);
    mat.get(0, 0, data);
    BufferedImage ret = new BufferedImage(width, height, 5);
    ret.getRaster().setDataElements(0, 0, width, height, data);
    return ret;
  }

  /**
   * BufferedImage to Mat
   *
   * @param img
   * @return
   */
  public static Mat image2Mat(BufferedImage img) {
    int width = img.getWidth();
    int height = img.getHeight();
    byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
    Mat mat = new Mat(height, width, CvType.CV_8UC3);
    mat.put(0, 0, data);
    return mat;
  }
}