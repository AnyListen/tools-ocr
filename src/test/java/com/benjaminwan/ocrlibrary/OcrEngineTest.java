package com.benjaminwan.ocrlibrary;

import cn.hutool.log.StaticLog;
import com.luooqi.ocr.utils.LibraryUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by litonglinux@qq.com on 10/11/2023_3:01 AM
 */
public class OcrEngineTest {

  @Test
  public void test1() {
    // https://github.com/RapidAI/RapidOcrNcnnLibTest/tree/main/resource/models

    String libPath = "D:\\lib\\ocr-lib\\win64\\bin";
    LibraryUtils.addLibary(libPath);

    OcrEngine ocrEngine = new OcrEngine();
    StaticLog.info("version=" + ocrEngine.getVersion());
    ocrEngine.setNumThread(8);
    //------- init Logger -------
    ocrEngine.initLogger(true, false, false);
    //ocrEngine.enableResultText("");
    ocrEngine.setGpuIndex(-1);
    String modelsDir = "D:\\model\\ppocr-v3-NCNN-models";
    String detName = "ch_PP-OCRv3_det_infer";
    String clsName = "ch_ppocr_mobile_v2.0_cls_infer";
    String recName = "ch_PP-OCRv3_rec_infer";
    String keysName = "ppocr_keys_v1.txt";

    boolean initModelsRet = ocrEngine.initModels(modelsDir, detName, clsName, recName, keysName);
    if (!initModelsRet) {
      StaticLog.error("Error in models initialization, please check the models/keys path!");
      return;
    }
    StaticLog.info("padding(%d) boxScoreThresh(%f) boxThresh(%f) unClipRatio(%f) doAngle(%b) mostAngle(%b)", ocrEngine.getPadding(), ocrEngine.getBoxScoreThresh(), ocrEngine.getBoxThresh(), ocrEngine.getUnClipRatio(), ocrEngine.getDoAngle(), ocrEngine.getMostAngle());

    String imagePath = "D:\\images\\Snipaste_2023-10-11_02-08-03.png";
    OcrResult ocrResult = ocrEngine.detect(imagePath);
    System.out.println(ocrResult.getStrRes());

  }

}