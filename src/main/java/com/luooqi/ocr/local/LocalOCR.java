package com.luooqi.ocr.local;

import cn.hutool.log.StaticLog;
import com.benjaminwan.ocrlibrary.OcrEngine;

public enum LocalOCR {
  INSTANCE;

  private final OcrEngine ocrEngine;

  LocalOCR() {
    this.ocrEngine = new OcrEngine();
    StaticLog.info("version=" + ocrEngine.getVersion());
    ocrEngine.setNumThread(4);
    //------- init Logger -------
    ocrEngine.initLogger(
      true,
      false,
      false
    );
    //ocrEngine.enableResultText("");
    ocrEngine.setGpuIndex(-1);
    String modelsDir = "./";
    boolean initModelsRet = ocrEngine.initModels(modelsDir, "ch_PP-OCRv3_det_infer", "ch_ppocr_mobile_v2.0_cls_infer", "ch_PP-OCRv3_rec_infer", "ppocr_keys_v1.txt");
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