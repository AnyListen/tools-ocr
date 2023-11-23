package com.litongjava.djl.paddle.ocr.v4.opencv;

import ai.djl.ndarray.NDArray;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class NDArrayUtils {
  /**
   * Mat To MatOfPoint
   *
   * @param mat
   * @return
   */
  public static MatOfPoint matToMatOfPoint(Mat mat) {
    int rows = mat.rows();
    MatOfPoint matOfPoint = new MatOfPoint();

    List<Point> list = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      Point point = new Point((float) mat.get(i, 0)[0], (float) mat.get(i, 1)[0]);
      list.add(point);
    }
    matOfPoint.fromList(list);

    return matOfPoint;
  }

  /**
   * float NDArray To float[][] Array
   *
   * @param ndArray
   * @return
   */
  public static float[][] floatNDArrayToArray(NDArray ndArray) {
    int rows = (int) (ndArray.getShape().get(0));
    int cols = (int) (ndArray.getShape().get(1));
    float[][] arr = new float[rows][cols];

    float[] arrs = ndArray.toFloatArray();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        arr[i][j] = arrs[i * cols + j];
      }
    }
    return arr;
  }

  /**
   * Mat To double[][] Array
   *
   * @param mat
   * @return
   */
  public static double[][] matToDoubleArray(Mat mat) {
    int rows = mat.rows();
    int cols = mat.cols();

    double[][] doubles = new double[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        doubles[i][j] = mat.get(i, j)[0];
      }
    }

    return doubles;
  }

  /**
   * Mat To float[][] Array
   *
   * @param mat
   * @return
   */
  public static float[][] matToFloatArray(Mat mat) {
    int rows = mat.rows();
    int cols = mat.cols();

    float[][] floats = new float[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        floats[i][j] = (float) mat.get(i, j)[0];
      }
    }

    return floats;
  }

  /**
   * Mat To byte[][] Array
   *
   * @param mat
   * @return
   */
  public static byte[][] matToUint8Array(Mat mat) {
    int rows = mat.rows();
    int cols = mat.cols();

    byte[][] bytes = new byte[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        bytes[i][j] = (byte) mat.get(i, j)[0];
      }
    }

    return bytes;
  }

  /**
   * float NDArray To float[][] Array
   *
   * @param ndArray
   * @param cvType
   * @return
   */
  public static Mat floatNDArrayToMat(NDArray ndArray, int cvType) {
    int rows = (int) (ndArray.getShape().get(0));
    int cols = (int) (ndArray.getShape().get(1));
    Mat mat = new Mat(rows, cols, cvType);

    float[] arrs = ndArray.toFloatArray();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        mat.put(i, j, arrs[i * cols + j]);
      }
    }
    return mat;
  }

  /**
   * float NDArray To Mat
   *
   * @param ndArray
   * @return
   */
  public static Mat floatNDArrayToMat(NDArray ndArray) {
    int rows = (int) (ndArray.getShape().get(0));
    int cols = (int) (ndArray.getShape().get(1));
    Mat mat = new Mat(rows, cols, CvType.CV_32F);

    float[] arrs = ndArray.toFloatArray();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        mat.put(i, j, arrs[i * cols + j]);
      }
    }

    return mat;

  }

  /**
   * uint8 NDArray To Mat
   *
   * @param ndArray
   * @return
   */
  public static Mat uint8NDArrayToMat(NDArray ndArray) {
    int rows = (int) (ndArray.getShape().get(0));
    int cols = (int) (ndArray.getShape().get(1));
    Mat mat = new Mat(rows, cols, CvType.CV_8U);

    byte[] arrs = ndArray.toByteArray();

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        mat.put(i, j, arrs[i * cols + j]);
      }
    }
    return mat;
  }

  /**
   * float[][] Array To Mat
   *
   * @param arr
   * @return
   */
  public static Mat floatArrayToMat(float[][] arr) {
    int rows = arr.length;
    int cols = arr[0].length;
    Mat mat = new Mat(rows, cols, CvType.CV_32F);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        mat.put(i, j, arr[i][j]);
      }
    }

    return mat;
  }

  /**
   * byte[][] Array To Mat
   *
   * @param arr
   * @return
   */
  public static Mat uint8ArrayToMat(byte[][] arr) {
    int rows = arr.length;
    int cols = arr[0].length;
    Mat mat = new Mat(rows, cols, CvType.CV_8U);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        mat.put(i, j, arr[i][j]);
      }
    }

    return mat;
  }

  /**
   * List To Mat
   *
   * @param points
   * @return
   */
  public static Mat toMat(List<ai.djl.modality.cv.output.Point> points) {
    Mat mat = new Mat(points.size(), 2, CvType.CV_32F);
    for (int i = 0; i < points.size(); i++) {
      ai.djl.modality.cv.output.Point point = points.get(i);
      mat.put(i, 0, (float) point.getX());
      mat.put(i, 1, (float) point.getY());
    }

    return mat;
  }
}