package com.luooqi.ocr;

import cn.hutool.core.thread.GlobalThreadPool;
import com.luooqi.ocr.config.InitConfig;
import com.luooqi.ocr.local.PaddlePaddleOCRV4;
import com.luooqi.ocr.windows.MainForm;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jnativehook.GlobalScreen;

@Slf4j
public class OcrApp extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() throws Exception {
    super.init();
    //InitConfig.init();
  }


  @Override
  public void start(Stage primaryStage) {
    MainForm mainForm = new MainForm();
    mainForm.init(primaryStage);
    primaryStage.show();
  }

  @Override
  public void stop() throws Exception {
    log.info("close");
    GlobalScreen.unregisterNativeHook();
    PaddlePaddleOCRV4.INSTANCE.close();
    GlobalThreadPool.shutdown(true);
  }
}