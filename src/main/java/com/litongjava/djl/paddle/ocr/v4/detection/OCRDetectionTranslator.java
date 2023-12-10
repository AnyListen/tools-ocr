package com.litongjava.djl.paddle.ocr.v4.detection;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.litongjava.djl.paddle.ocr.v4.opencv.NDArrayUtils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文字检测前后处理
 */
public class OCRDetectionTranslator implements Translator<Image, NDList> {
  // det_algorithm == "DB"
  private final float thresh = 0.3f;
  private final boolean use_dilation = false;
  private final String score_mode = "fast";
  private final String box_type = "quad";

  private final int limit_side_len;
  private final int max_candidates;
  private final int min_size;
  private final float box_thresh;
  private final float unclip_ratio;
  private float ratio_h;
  private float ratio_w;
  private int img_height;
  private int img_width;

  public OCRDetectionTranslator(Map<String, ?> arguments) {
    limit_side_len =
      arguments.containsKey("limit_side_len")
        ? Integer.parseInt(arguments.get("limit_side_len").toString())
        : 960;
    max_candidates =
      arguments.containsKey("max_candidates")
        ? Integer.parseInt(arguments.get("max_candidates").toString())
        : 1000;
    min_size =
      arguments.containsKey("min_size")
        ? Integer.parseInt(arguments.get("min_size").toString())
        : 3;
    box_thresh =
      arguments.containsKey("box_thresh")
        ? Float.parseFloat(arguments.get("box_thresh").toString())
        : 0.6f; // 0.5f
    unclip_ratio =
      arguments.containsKey("unclip_ratio")
        ? Float.parseFloat(arguments.get("unclip_ratio").toString())
        : 1.6f;
  }

  @Override
  public NDList processOutput(TranslatorContext ctx, NDList list) {
    NDManager manager = ctx.getNDManager();
    NDArray pred = list.get(0);
    pred = pred.squeeze();
    NDArray segmentation = pred.gt(thresh);   // thresh=0.3 .mul(255f)

    segmentation = segmentation.toType(DataType.UINT8, true);
    Shape shape = segmentation.getShape();
    int rows = (int) shape.get(0);
    int cols = (int) shape.get(1);

    Mat newMask = new Mat();
    if (this.use_dilation) {
      Mat mask = new Mat();
      //convert from NDArray to Mat
      Mat srcMat = NDArrayUtils.uint8NDArrayToMat(segmentation);
      // size 越小，腐蚀的单位越小，图片越接近原图
      // Mat dilation_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
      Mat dilation_kernel = NDArrayUtils.uint8ArrayToMat(new byte[][]{{1, 1}, {1, 1}});
      /**
       * 膨胀说明： 图像的一部分区域与指定的核进行卷积， 求核的最`大`值并赋值给指定区域。 膨胀可以理解为图像中`高亮区域`的'领域扩大'。
       * 意思是高亮部分会侵蚀不是高亮的部分，使高亮部分越来越多。
       */
      Imgproc.dilate(srcMat, mask, dilation_kernel);
      //destination Matrix
      Scalar scalar = new Scalar(255);
      Core.multiply(mask, scalar, newMask);
      // release Mat
      mask.release();
      srcMat.release();
      dilation_kernel.release();
    } else {
      Mat srcMat = NDArrayUtils.uint8NDArrayToMat(segmentation);
      //destination Matrix
      Scalar scalar = new Scalar(255);
      Core.multiply(srcMat, scalar, newMask);
      // release Mat
      srcMat.release();
    }

    NDList dt_boxes = null;
    NDArray boxes = boxes_from_bitmap(manager, pred, newMask);
    if (boxes != null) {
      //boxes[:, :, 0] = boxes[:, :, 0] / ratio_w
      NDArray boxes1 = boxes.get(":, :, 0").div(ratio_w);
      boxes.set(new NDIndex(":, :, 0"), boxes1);
      //boxes[:, :, 1] = boxes[:, :, 1] / ratio_h
      NDArray boxes2 = boxes.get(":, :, 1").div(ratio_h);
      boxes.set(new NDIndex(":, :, 1"), boxes2);

      dt_boxes = this.filter_tag_det_res(boxes);

      dt_boxes.detach();
    }

    // release Mat
    newMask.release();

    return dt_boxes;
  }


  private NDList filter_tag_det_res(NDArray dt_boxes) {
    NDList boxesList = new NDList();

    int num = (int) dt_boxes.getShape().get(0);
    for (int i = 0; i < num; i++) {
      NDArray box = dt_boxes.get(i);
      box = order_points_clockwise(box);
      box = clip_det_res(box);
      float[] box0 = box.get(0).toFloatArray();
      float[] box1 = box.get(1).toFloatArray();
      float[] box3 = box.get(3).toFloatArray();
      int rect_width = (int) Math.sqrt(Math.pow(box1[0] - box0[0], 2) + Math.pow(box1[1] - box0[1], 2));
      int rect_height = (int) Math.sqrt(Math.pow(box3[0] - box0[0], 2) + Math.pow(box3[1] - box0[1], 2));
      if (rect_width <= 3 || rect_height <= 3)
        continue;
      boxesList.add(box);
    }

    return boxesList;
  }

  private NDArray clip_det_res(NDArray points) {
    for (int i = 0; i < points.getShape().get(0); i++) {
      int value = Math.max((int) points.get(i, 0).toFloatArray()[0], 0);
      value = Math.min(value, img_width - 1);
      points.set(new NDIndex(i + ",0"), value);
      value = Math.max((int) points.get(i, 1).toFloatArray()[0], 0);
      value = Math.min(value, img_height - 1);
      points.set(new NDIndex(i + ",1"), value);
    }

    return points;
  }

  /**
   * sort the points based on their x-coordinates
   * 顺时针
   *
   * @param pts
   * @return
   */

  private NDArray order_points_clockwise(NDArray pts) {
    NDList list = new NDList();
    long[] indexes = pts.get(":, 0").argSort().toLongArray();

    // grab the left-most and right-most points from the sorted
    // x-roodinate points
    Shape s1 = pts.getShape();
    NDArray leftMost1 = pts.get(indexes[0] + ",:");
    NDArray leftMost2 = pts.get(indexes[1] + ",:");
    NDArray leftMost = leftMost1.concat(leftMost2).reshape(2, 2);
    NDArray rightMost1 = pts.get(indexes[2] + ",:");
    NDArray rightMost2 = pts.get(indexes[3] + ",:");
    NDArray rightMost = rightMost1.concat(rightMost2).reshape(2, 2);

    // now, sort the left-most coordinates according to their
    // y-coordinates so we can grab the top-left and bottom-left
    // points, respectively
    indexes = leftMost.get(":, 1").argSort().toLongArray();
    NDArray lt = leftMost.get(indexes[0] + ",:");
    NDArray lb = leftMost.get(indexes[1] + ",:");
    indexes = rightMost.get(":, 1").argSort().toLongArray();
    NDArray rt = rightMost.get(indexes[0] + ",:");
    NDArray rb = rightMost.get(indexes[1] + ",:");

    list.add(lt);
    list.add(rt);
    list.add(rb);
    list.add(lb);

    NDArray rect = NDArrays.concat(list).reshape(4, 2);
    return rect;
  }

  /**
   * Get boxes from the binarized image predicted by DB
   *
   * @param manager
   * @param pred    the binarized image predicted by DB.
   * @param bitmap  new 'pred' after threshold filtering.
   */
  private NDArray boxes_from_bitmap(NDManager manager, NDArray pred, Mat bitmap) {
    int dest_height = (int) pred.getShape().get(0);
    int dest_width = (int) pred.getShape().get(1);
    int height = bitmap.rows();
    int width = bitmap.cols();

    List<MatOfPoint> contours = new ArrayList<>();
    Mat hierarchy = new Mat();
    // 寻找轮廓
    Imgproc.findContours(
      bitmap,
      contours,
      hierarchy,
      Imgproc.RETR_LIST,
      Imgproc.CHAIN_APPROX_SIMPLE);

    int num_contours = Math.min(contours.size(), max_candidates);
    NDList boxList = new NDList();
    float[] scores = new float[num_contours];

    for (int index = 0; index < num_contours; index++) {
      MatOfPoint contour = contours.get(index);
      MatOfPoint2f newContour = new MatOfPoint2f(contour.toArray());
      float[][] pointsArr = new float[4][2];
      int sside = get_mini_boxes(newContour, pointsArr);
      if (sside < this.min_size)
        continue;
      NDArray points = manager.create(pointsArr);
      float score = box_score_fast(manager, pred, points);
      if (score < this.box_thresh)
        continue;

      NDArray box = unclip(manager, points); // TODO get_mini_boxes(box)

      // box[:, 0] = np.clip(np.round(box[:, 0] / width * dest_width), 0, dest_width)
      NDArray boxes1 = box.get(":,0").div(width).mul(dest_width).round().clip(0, dest_width);
      box.set(new NDIndex(":, 0"), boxes1);
      // box[:, 1] = np.clip(np.round(box[:, 1] / height * dest_height), 0, dest_height)
      NDArray boxes2 = box.get(":,1").div(height).mul(dest_height).round().clip(0, dest_height);
      box.set(new NDIndex(":, 1"), boxes2);

      boxList.add(box);
      scores[index] = score;

      // release memory
      contour.release();
      newContour.release();
    }

    // release
    hierarchy.release();

    NDArray boxes = null;
    if (boxList.size() > 0) {
      boxes = NDArrays.stack(boxList);
      return boxes;
    }

    return boxes;


  }

  /**
   * Shrink or expand the boxaccording to 'unclip_ratio'
   *
   * @param points The predicted box.
   * @return uncliped box
   */
  private NDArray unclip(NDManager manager, NDArray points) {
    points = order_points_clockwise(points);
    float[] pointsArr = points.toFloatArray();
    float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
    float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);

    float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
    float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);

    float width = distance(lt, rt);
    float height = distance(lt, lb);

    if (width > height) {
      float k = (lt[1] - rt[1]) / (lt[0] - rt[0]); // y = k * x + b

      float delta_dis = height;
      float delta_x = (float) Math.sqrt((delta_dis * delta_dis) / (k * k + 1));
      float delta_y = Math.abs(k * delta_x);

      if (k > 0) {
        pointsArr[0] = lt[0] - delta_x + delta_y;
        pointsArr[1] = lt[1] - delta_y - delta_x;
        pointsArr[2] = rt[0] + delta_x + delta_y;
        pointsArr[3] = rt[1] + delta_y - delta_x;

        pointsArr[4] = rb[0] + delta_x - delta_y;
        pointsArr[5] = rb[1] + delta_y + delta_x;
        pointsArr[6] = lb[0] - delta_x - delta_y;
        pointsArr[7] = lb[1] - delta_y + delta_x;
      } else {
        pointsArr[0] = lt[0] - delta_x - delta_y;
        pointsArr[1] = lt[1] + delta_y - delta_x;
        pointsArr[2] = rt[0] + delta_x - delta_y;
        pointsArr[3] = rt[1] - delta_y - delta_x;

        pointsArr[4] = rb[0] + delta_x + delta_y;
        pointsArr[5] = rb[1] - delta_y + delta_x;
        pointsArr[6] = lb[0] - delta_x + delta_y;
        pointsArr[7] = lb[1] + delta_y + delta_x;
      }
    } else {
      float k = (lt[1] - rt[1]) / (lt[0] - rt[0]); // y = k * x + b

      float delta_dis = width;
      float delta_y = (float) Math.sqrt((delta_dis * delta_dis) / (k * k + 1));
      float delta_x = Math.abs(k * delta_y);

      if (k > 0) {
        pointsArr[0] = lt[0] + delta_x - delta_y;
        pointsArr[1] = lt[1] - delta_y - delta_x;
        pointsArr[2] = rt[0] + delta_x + delta_y;
        pointsArr[3] = rt[1] - delta_y + delta_x;

        pointsArr[4] = rb[0] - delta_x + delta_y;
        pointsArr[5] = rb[1] + delta_y + delta_x;
        pointsArr[6] = lb[0] - delta_x - delta_y;
        pointsArr[7] = lb[1] + delta_y - delta_x;
      } else {
        pointsArr[0] = lt[0] - delta_x - delta_y;
        pointsArr[1] = lt[1] - delta_y + delta_x;
        pointsArr[2] = rt[0] - delta_x + delta_y;
        pointsArr[3] = rt[1] - delta_y - delta_x;

        pointsArr[4] = rb[0] + delta_x + delta_y;
        pointsArr[5] = rb[1] + delta_y - delta_x;
        pointsArr[6] = lb[0] + delta_x - delta_y;
        pointsArr[7] = lb[1] + delta_y + delta_x;
      }
    }
    points = manager.create(pointsArr).reshape(4, 2);

    return points;
  }

  private float distance(float[] point1, float[] point2) {
    float disX = point1[0] - point2[0];
    float disY = point1[1] - point2[1];
    float dis = (float) Math.sqrt(disX * disX + disY * disY);
    return dis;
  }

  /**
   * Get boxes from the contour or box.
   *
   * @param contour   The predicted contour.
   * @param pointsArr The predicted box.
   * @return smaller side of box
   */
  private int get_mini_boxes(MatOfPoint2f contour, float[][] pointsArr) {
    // https://blog.csdn.net/qq_37385726/article/details/82313558
    // bounding_box[1] - rect 返回矩形的长和宽
    RotatedRect rect = Imgproc.minAreaRect(contour);
    Mat points = new Mat();
    Imgproc.boxPoints(rect, points);

    float[][] fourPoints = new float[4][2];
    for (int row = 0; row < 4; row++) {
      fourPoints[row][0] = (float) points.get(row, 0)[0];
      fourPoints[row][1] = (float) points.get(row, 1)[0];
    }

    float[] tmpPoint = new float[2];
    for (int i = 0; i < 4; i++) {
      for (int j = i + 1; j < 4; j++) {
        if (fourPoints[j][0] < fourPoints[i][0]) {
          tmpPoint[0] = fourPoints[i][0];
          tmpPoint[1] = fourPoints[i][1];
          fourPoints[i][0] = fourPoints[j][0];
          fourPoints[i][1] = fourPoints[j][1];
          fourPoints[j][0] = tmpPoint[0];
          fourPoints[j][1] = tmpPoint[1];
        }
      }
    }

    int index_1 = 0;
    int index_2 = 1;
    int index_3 = 2;
    int index_4 = 3;

    if (fourPoints[1][1] > fourPoints[0][1]) {
      index_1 = 0;
      index_4 = 1;
    } else {
      index_1 = 1;
      index_4 = 0;
    }

    if (fourPoints[3][1] > fourPoints[2][1]) {
      index_2 = 2;
      index_3 = 3;
    } else {
      index_2 = 3;
      index_3 = 2;
    }

    pointsArr[0] = fourPoints[index_1];
    pointsArr[1] = fourPoints[index_2];
    pointsArr[2] = fourPoints[index_3];
    pointsArr[3] = fourPoints[index_4];

    int height = rect.boundingRect().height;
    int width = rect.boundingRect().width;
    int sside = Math.min(height, width);

    // release
    points.release();

    return sside;
  }

  /**
   * Calculate the score of box.
   *
   * @param bitmap The binarized image predicted by DB.
   * @param points The predicted box
   * @return
   */
  private float box_score_fast(NDManager manager, NDArray bitmap, NDArray points) {
    NDArray box = points.get(":");
    long h = bitmap.getShape().get(0);
    long w = bitmap.getShape().get(1);
    // xmin = np.clip(np.floor(box[:, 0].min()).astype(np.int), 0, w - 1)
    int xmin = box.get(":, 0").min().floor().clip(0, w - 1).toType(DataType.INT32, true).toIntArray()[0];
    int xmax = box.get(":, 0").max().ceil().clip(0, w - 1).toType(DataType.INT32, true).toIntArray()[0];
    int ymin = box.get(":, 1").min().floor().clip(0, h - 1).toType(DataType.INT32, true).toIntArray()[0];
    int ymax = box.get(":, 1").max().ceil().clip(0, h - 1).toType(DataType.INT32, true).toIntArray()[0];

    NDArray mask = manager.zeros(new Shape(ymax - ymin + 1, xmax - xmin + 1), DataType.UINT8);

    box.set(new NDIndex(":, 0"), box.get(":, 0").sub(xmin));
    box.set(new NDIndex(":, 1"), box.get(":, 1").sub(ymin));

    //mask - convert from NDArray to Mat
    Mat maskMat = NDArrayUtils.uint8NDArrayToMat(mask);

    //mask - convert from NDArray to Mat - 4 rows, 2 cols
    Mat boxMat = NDArrayUtils.floatNDArrayToMat(box, CvType.CV_32S);

//        boxMat.reshape(1, new int[]{1, 4, 2});
    List<MatOfPoint> pts = new ArrayList<>();
    MatOfPoint matOfPoint = NDArrayUtils.matToMatOfPoint(boxMat); // new MatOfPoint(boxMat);
    pts.add(matOfPoint);
    Imgproc.fillPoly(maskMat, pts, new Scalar(1));


    NDArray subBitMap = bitmap.get(ymin + ":" + (ymax + 1) + "," + xmin + ":" + (xmax + 1));
    Mat bitMapMat = NDArrayUtils.floatNDArrayToMat(subBitMap);

    Scalar score = Core.mean(bitMapMat, maskMat);
    float scoreValue = (float) score.val[0];
    // release
    maskMat.release();
    boxMat.release();
    bitMapMat.release();

    return scoreValue;
  }

  @Override
  public NDList processInput(TranslatorContext ctx, Image input) {
    NDArray img = input.toNDArray(ctx.getNDManager());
    int h = input.getHeight();
    int w = input.getWidth();
    img_height = h;
    img_width = w;

    // limit the max side
    float ratio = 1.0f;
    if (Math.max(h, w) > limit_side_len) {
      if (h > w) {
        ratio = (float) limit_side_len / (float) h;
      } else {
        ratio = (float) limit_side_len / (float) w;
      }
    }

    int resize_h = (int) (h * ratio);
    int resize_w = (int) (w * ratio);

    resize_h = Math.round((float) resize_h / 32f) * 32;
    resize_w = Math.round((float) resize_w / 32f) * 32;

    ratio_h = resize_h / (float) h;
    ratio_w = resize_w / (float) w;

    img = NDImageUtils.resize(img, resize_w, resize_h);

    img = NDImageUtils.toTensor(img);

    img =
      NDImageUtils.normalize(
        img,
        new float[]{0.485f, 0.456f, 0.406f},
        new float[]{0.229f, 0.224f, 0.225f});

    img = img.expandDims(0);

    return new NDList(img);
  }

  @Override
  public Batchifier getBatchifier() {
    return null;
  }
}