package com.litongjava.djl.paddle.ocr.v4.recognition;

import ai.djl.Model;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 文字识别前后处理
 */
public class PpWordRecTranslator implements Translator<Image, String> {
  private List<String> table;
  private final boolean use_space_char;

  public PpWordRecTranslator(Map<String, ?> arguments) {
    use_space_char =
      arguments.containsKey("use_space_char")
        ? Boolean.parseBoolean(arguments.get("use_space_char").toString())
        : true;
  }

  @Override
  public void prepare(TranslatorContext ctx) throws IOException {
    Model model = ctx.getModel();
    try (InputStream is = model.getArtifact("dict.txt").openStream()) {
      table = Utils.readLines(is, true);
      table.add(0, "blank");
      if (use_space_char) {
        table.add(" ");
        table.add(" ");
      } else {
        table.add("");
        table.add("");
      }

    }
  }

  @Override
  public String processOutput(TranslatorContext ctx, NDList list) throws IOException {
    StringBuilder sb = new StringBuilder();
    NDArray tokens = list.singletonOrThrow();

    long[] indices = tokens.get(0).argMax(1).toLongArray();
    boolean[] selection = new boolean[indices.length];
    Arrays.fill(selection, true);
    for (int i = 1; i < indices.length; i++) {
      if (indices[i] == indices[i - 1]) {
        selection[i] = false;
      }
    }

    // 字符置信度
//        float[] probs = new float[indices.length];
//        for (int row = 0; row < indices.length; row++) {
//            NDArray value = tokens.get(0).get(new NDIndex(""+ row +":" + (row + 1) +"," + indices[row] +":" + ( indices[row] + 1)));
//            probs[row] = value.toFloatArray()[0];
//        }

    int lastIdx = 0;
    for (int i = 0; i < indices.length; i++) {
      if (selection[i] == true && indices[i] > 0 && !(i > 0 && indices[i] == lastIdx)) {
        sb.append(table.get((int) indices[i]));
      }
    }
    return sb.toString();
  }

  @Override
  public NDList processInput(TranslatorContext ctx, Image input) {
    NDArray img = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
    int imgC = 3;
    int imgH = 48;
    int imgW = 320;

    float max_wh_ratio = (float) imgW / (float) imgH;

    int h = input.getHeight();
    int w = input.getWidth();
    float wh_ratio = (float) w / (float) h;

    max_wh_ratio = Math.max(max_wh_ratio, wh_ratio);
    imgW = (int) (imgH * max_wh_ratio);

    int resized_w;
    if (Math.ceil(imgH * wh_ratio) > imgW) {
      resized_w = imgW;
    } else {
      resized_w = (int) (Math.ceil(imgH * wh_ratio));
    }
    NDArray resized_image = NDImageUtils.resize(img, resized_w, imgH);
    resized_image = resized_image.transpose(2, 0, 1).toType(DataType.FLOAT32, false);
    resized_image.divi(255f).subi(0.5f).divi(0.5f);
    NDArray padding_im = ctx.getNDManager().zeros(new Shape(imgC, imgH, imgW), DataType.FLOAT32);
    padding_im.set(new NDIndex(":,:,0:" + resized_w), resized_image);

    padding_im = padding_im.flip(0);
    padding_im = padding_im.expandDims(0);
    return new NDList(padding_im);
  }

  @Override
  public Batchifier getBatchifier() {
    return null;
  }

}