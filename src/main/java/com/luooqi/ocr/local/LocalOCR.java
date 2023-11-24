//package com.luooqi.ocr.local;
//
//import cn.hutool.log.StaticLog;
//import com.benjaminwan.ocrlibrary.OcrEngine;
//import com.litongjava.jfinal.aop.Aop;
//import com.litongjava.project.config.ConfigKeys;
//import com.litongjava.project.config.ProjectConfig;
//import com.luooqi.ocr.utils.LibraryUtils;
//
//public enum LocalOCR {
//  INSTANCE;
//
//  private final OcrEngine ocrEngine;
//
//  LocalOCR() {
//    ProjectConfig projectConfig = Aop.get(ProjectConfig.class);
//    String libPath = projectConfig.getStr(ConfigKeys.libPath);
//
//    String modelsDir = projectConfig.getStr(ConfigKeys.modelsDir);
//    String detName = projectConfig.getStr(ConfigKeys.detName);
//    String clsName = projectConfig.getStr(ConfigKeys.clsName);
//    String recName = projectConfig.getStr(ConfigKeys.recName);
//    String keysName = projectConfig.getStr(ConfigKeys.keysName);
//
//    LibraryUtils.addLibary(libPath);
//
//    this.ocrEngine = new OcrEngine();
//    StaticLog.info("version=" + ocrEngine.getVersion());
//    ocrEngine.setNumThread(4);
//    //------- init Logger -------
//    ocrEngine.initLogger(true, false, false);
//    //ocrEngine.enableResultText("");
//    ocrEngine.setGpuIndex(-1);
//    boolean initModelsRet = ocrEngine.initModels(modelsDir, detName, clsName, recName, keysName);
//    if (!initModelsRet) {
//      StaticLog.error("Error in models initialization, please check the models/keys path!");
//      return;
//    }
//    StaticLog.info("padding(%d) boxScoreThresh(%f) boxThresh(%f) unClipRatio(%f) doAngle(%b) mostAngle(%b)", ocrEngine.getPadding(), ocrEngine.getBoxScoreThresh(), ocrEngine.getBoxThresh(), ocrEngine.getUnClipRatio(), ocrEngine.getDoAngle(), ocrEngine.getMostAngle());
//  }
//
//  public OcrEngine getOcrEngine() {
//    return ocrEngine;
//  }
//
//  public void useGpu(Boolean isUse) {
//    this.ocrEngine.setGpuIndex(isUse ? 0 : -1);
//  }
//}