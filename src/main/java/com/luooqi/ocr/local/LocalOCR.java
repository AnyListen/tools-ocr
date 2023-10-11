package com.luooqi.ocr.local;

import cn.hutool.log.StaticLog;
import com.benjaminwan.ocrlibrary.OcrEngine;
import com.luooqi.ocr.utils.LibraryUtils;

public enum LocalOCR {
  INSTANCE;

  private final OcrEngine ocrEngine;

  LocalOCR() {
    String libPath = "D:\\lib\\ocr-lib\\win64\\bin";

    String modelsDir = "D:\\model\\ppocr-v3-NCNN-models";
    String detName = "ch_PP-OCRv3_det_infer";
    String clsName = "ch_ppocr_mobile_v2.0_cls_infer";
    String recName = "ch_PP-OCRv3_rec_infer";
    String keysName = "ppocr_keys_v1.txt";

    LibraryUtils.addLibary(libPath);

    this.ocrEngine = new OcrEngine();
    StaticLog.info("version=" + ocrEngine.getVersion());
    ocrEngine.setNumThread(4);
    //------- init Logger -------
    ocrEngine.initLogger(true, false, false);
    //ocrEngine.enableResultText("");
    ocrEngine.setGpuIndex(-1);
    boolean initModelsRet = ocrEngine.initModels(modelsDir, detName, clsName, recName, keysName);
    if (!initModelsRet) {
      StaticLog.error("Error in models initialization, please check the models/keys path!");
      return;
    }
    StaticLog.info("padding(%d) boxScoreThresh(%f) boxThresh(%f) unClipRatio(%f) doAngle(%b) mostAngle(%b)", ocrEngine.getPadding(), ocrEngine.getBoxScoreThresh(), ocrEngine.getBoxThresh(), ocrEngine.getUnClipRatio(), ocrEngine.getDoAngle(), ocrEngine.getMostAngle());
  }

  public OcrEngine getOcrEngine() {
    return ocrEngine;
  }

  public void useGpu(Boolean isUse) {
    this.ocrEngine.setGpuIndex(isUse ? 0 : -1);
  }
}