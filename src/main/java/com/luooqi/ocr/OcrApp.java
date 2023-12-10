package com.luooqi.ocr;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.luooqi.ocr.config.InitConfig;
import com.luooqi.ocr.controller.ProcessController;
import com.luooqi.ocr.local.PaddlePaddleOCRV4;
import com.luooqi.ocr.model.CaptureInfo;
import com.luooqi.ocr.model.StageInfo;
import com.luooqi.ocr.snap.ScreenCapture;
import com.luooqi.ocr.utils.CommUtils;
import com.luooqi.ocr.utils.OcrUtils;
import com.luooqi.ocr.windows.MainForm;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jnativehook.GlobalScreen;
import org.slf4j.LoggerFactory;
import sun.applet.Main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OcrApp extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() throws Exception {
    super.init();
    InitConfig.init();
  }


  @Override
  public void start(Stage primaryStage) {
    MainForm mainForm = new MainForm();
    mainForm.init(primaryStage);
    primaryStage.show();

  }

  @Override
  public void stop() throws Exception {
    GlobalScreen.unregisterNativeHook();
    PaddlePaddleOCRV4.INSTANCE.close();
  }
}