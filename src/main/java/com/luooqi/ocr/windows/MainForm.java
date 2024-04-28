package com.luooqi.ocr.windows;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.luooqi.ocr.config.InitConfig;
import com.luooqi.ocr.controller.ProcessController;
import com.luooqi.ocr.local.PaddlePaddleOCRV4;
import com.luooqi.ocr.model.CaptureInfo;
import com.luooqi.ocr.model.StageInfo;
import com.luooqi.ocr.snap.ScreenCapture;
import com.luooqi.ocr.utils.CommUtils;
import com.luooqi.ocr.utils.OcrUtils;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by litonglinux@qq.com on 12/9/2023_4:40 PM
 */
@Slf4j
public class MainForm {
  private static StageInfo stageInfo;
  public static Stage stage;
  private static Scene mainScene;

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  private static ScreenCapture screenCapture;
  private static ProcessController processController;
  private static TextArea textArea;
  // private static boolean isSegment = true;
  // private static String ocrText = "";

  public void init(Stage primaryStage) {

    log.info("primaryStage:{}", primaryStage);
    stage = primaryStage;
    setAutoResize();
    screenCapture = new ScreenCapture(stage);
    processController = new ProcessController();
    InitConfig.initKeyHook();

//        ToggleGroup segmentGrp = new ToggleGroup();
//        ToggleButton resetBtn = CommUtils.createToggleButton(segmentGrp, "resetBtn", this::resetText, "重置");
//        ToggleButton segmentBtn = CommUtils.createToggleButton(segmentGrp, "segmentBtn", this::segmentText, "智能分段");
//        resetBtn.setUserData("resetBtn");
//        segmentBtn.setUserData("segmentBtn");
//
//        segmentGrp.selectToggle(segmentBtn);
//        segmentGrp.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
//            isSegment = newValue.getUserData().toString().equals("segmentBtn");
//        });

    HBox topBar = getTopBar();
    textArea = getCenter();
    ToolBar footerBar = getFooterBar();
    BorderPane root = new BorderPane();
    root.setTop(topBar);
    root.setCenter(textArea);
    root.setBottom(footerBar);
    root.getStylesheets().addAll(getClass().getResource("/css/main.css").toExternalForm());
    CommUtils.initStage(primaryStage);
    mainScene = new Scene(root, 670, 470);
    stage.setScene(mainScene);
    // 启动引擎,加载模型,如果模型加载错误下屏幕显示错误
    try {
      PaddlePaddleOCRV4.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private TextArea getCenter() {
    TextArea textArea = new TextArea();
    textArea.setId("ocrTextArea");
    textArea.setWrapText(true);
    textArea.setBorder(
        new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    textArea.setFont(Font.font("Arial", FontPosture.REGULAR, 14));
    return textArea;
  }

  private ToolBar getFooterBar() {
    ToolBar footerBar = new ToolBar();
    footerBar.setId("statsToolbar");
    Label statsLabel = new Label();
    SimpleStringProperty statsProperty = new SimpleStringProperty("总字数：0");
    textArea.textProperty().addListener((observable, oldValue, newValue) -> statsProperty
        .set("总字数：" + newValue.replaceAll(CommUtils.SPECIAL_CHARS, "").length()));
    statsLabel.textProperty().bind(statsProperty);
    footerBar.getItems().add(statsLabel);
    return footerBar;
  }

  private HBox getTopBar() {
    HBox topBar = new HBox(CommUtils.createButton("snapBtn", MainForm::screenShotOcr, "截图"),
        CommUtils.createButton("openImageBtn", this::openImageOcr, "打开"),
        CommUtils.createButton("copyBtn", this::copyText, "复制"),
        CommUtils.createButton("pasteBtn", this::pasteText, "粘贴"),
        CommUtils.createButton("clearBtn", this::clearText, "清空"),
        CommUtils.createButton("wrapBtn", this::wrapText, "换行")
    // CommUtils.SEPARATOR, resetBtn, segmentBtn
    );
    topBar.setId("topBar");
    topBar.setMinHeight(40);
    topBar.setSpacing(8);
    topBar.setPadding(new Insets(6, 8, 6, 8));
    return topBar;
  }

  private void setAutoResize() {
    stageInfo = new StageInfo();
    stage.xProperty().addListener((observable, oldValue, newValue) -> {
      if (stage.getX() > 0) {
        stageInfo.setX(stage.getX());
      }
    });
    stage.yProperty().addListener((observable, oldValue, newValue) -> {
      if (stage.getY() > 0) {
        stageInfo.setY(stage.getY());
      }
    });
  }

  private void wrapText() {
    textArea.setWrapText(!textArea.isWrapText());
  }

  private void clearText() {
    textArea.setText("");
  }

  private void pasteText() {
    String text = Clipboard.getSystemClipboard().getString();
    if (StrUtil.isBlank(text)) {
      return;
    }
    textArea.setText(textArea.getText() + (StrUtil.isBlank(textArea.getText()) ? "" : "\n")
        + Clipboard.getSystemClipboard().getString());
  }

  private void copyText() {
    String text = textArea.getSelectedText();
    if (StrUtil.isBlank(text)) {
      text = textArea.getText();
    }
    if (StrUtil.isBlank(text)) {
      return;
    }
    Map<DataFormat, Object> data = new HashMap<>();
    data.put(DataFormat.PLAIN_TEXT, text);
    Clipboard.getSystemClipboard().setContent(data);
  }

  public static void screenShotOcr() {
    stageInfo.setWidth(stage.getWidth());
    stageInfo.setHeight(stage.getHeight());
    stageInfo.setFullScreenState(stage.isFullScreen());
    Platform.runLater(screenCapture::prepareForCapture);
  }

  /**
   * 打开图片
   */
  private void openImageOcr() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Please Select Image File");
    String[] extensions = { "*.png", "*.jpg", "*.pdf", "*.PDF" };
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", extensions));
    File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile == null || !selectedFile.isFile()) {
      return;
    }
    stageInfo = new StageInfo(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(), stage.isFullScreen());

    try {
      // BufferedImage image = ImageIO.read(selectedFile);
      doOcr(selectedFile);
    } catch (Exception e) {
      StaticLog.error(e);
    }
  }

  public static void cancelSnap() {
    Platform.runLater(screenCapture::cancelSnap);
  }

  public static void doOcr(BufferedImage image) {
    processController.setX(CaptureInfo.ScreenMinX + (CaptureInfo.ScreenWidth - 300) / 2);
    processController.setY(250);
    processController.show();

    ThreadUtil.execute(() -> {
      String text = null;
      try {
        text = OcrUtils.recImgLocal(image);
      } catch (Exception e) {
        text = e.getMessage();
      }

      String finalText = text;
      Platform.runLater(() -> {
        processController.close();
        textArea.setText(finalText);
        restore(true);
      });
    });
  }

  public static void doOcr(File selectedFile) {
    processController.setX(CaptureInfo.ScreenMinX + (CaptureInfo.ScreenWidth - 300) / 2);
    processController.setY(250);
    processController.show();
    ThreadUtil.execute(() -> {
      String text = null;
      try {
        String fileType = FileTypeUtil.getType(selectedFile);
        if ("pdf".equalsIgnoreCase(fileType)) {
          text = OcrUtils.recPdfLocal(selectedFile);
        } else {
          text = OcrUtils.recImgLocal(selectedFile);
        }

      } catch (Exception e) {
        text = e.getMessage();
        e.printStackTrace();
      }

      String finalText = text;
      Platform.runLater(() -> {
        processController.close();
        textArea.setText(finalText);
        restore(true);
      });
    });
  }

  public static void restore(boolean focus) {
    stage.setAlwaysOnTop(false);
    stage.setScene(mainScene);
    stage.setFullScreen(stageInfo.isFullScreenState());
    stage.setX(stageInfo.getX());
    stage.setY(stageInfo.getY());
    stage.setWidth(stageInfo.getWidth());
    stage.setHeight(stageInfo.getHeight());
    if (focus) {
      stage.setOpacity(1.0f);
      stage.requestFocus();
    } else {
      stage.setOpacity(0.0f);
    }
  }
}
