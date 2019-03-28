package com.luooqi.ocr;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.luooqi.ocr.model.StageInfo;
import com.luooqi.ocr.snap.ScreenCapture;
import com.luooqi.ocr.utils.CommUtils;
import com.luooqi.ocr.utils.GlobalKeyListener;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;

import javax.swing.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.application.Platform.runLater;

public class MainFm extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static StageInfo stageInfo;
    public static Stage stage;
    private static Scene mainScene;
    private static ScreenCapture screenCapture;
    public static TextArea textArea;
    //private static boolean isSegment = true;
    //private static String ocrText = "";
    //private static BooleanProperty isOcr = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        screenCapture = new ScreenCapture(stage);
        initKeyHook();

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

        HBox topBar = new HBox(
                CommUtils.createButton("snapBtn", MainFm::doSnap, "截图"),
                CommUtils.createButton("copyBtn", this::copyText, "复制"),
                CommUtils.createButton("pasteBtn", this::pasteText, "粘贴"),
                CommUtils.createButton("clearBtn", this::clearText, "清空"),
                CommUtils.createButton("wrapBtn", this::wrapText, "换行")
                //CommUtils.SEPARATOR, resetBtn, segmentBtn
        );
        topBar.setId("topBar");
        topBar.setMinHeight(40);
        topBar.setSpacing(8);
        topBar.setPadding(new Insets(6, 8, 6, 8));

        textArea = new TextArea();
        textArea.setFont(Font.font("Arial", FontPosture.REGULAR, 14));

        ToolBar footerBar = new ToolBar();
        footerBar.setId("statsToolbar");
        Label statsLabel = new Label();
        SimpleStringProperty statsProperty = new SimpleStringProperty("总字数：0");
        textArea.textProperty().addListener((observable, oldValue, newValue) -> statsProperty.set("总字数：" + newValue.replaceAll(CommUtils.SPECIAL_CHARS, "").length()));
        statsLabel.textProperty().bind(statsProperty);
        footerBar.getItems().add(statsLabel);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(textArea);
        root.setBottom(footerBar);
        root.getStylesheets().addAll(
                getClass().getResource("/css/main.css").toExternalForm()
        );

        initStage(primaryStage);
        mainScene = new Scene(root, 420, 475);
        stage.setScene(mainScene);
        stage.show();
    }

    private void wrapText() {
        textArea.setWrapText(!textArea.isWrapText());
    }

    private void initStage(Stage primaryStage) {
        try {
            String osName = System.getProperty("os.name", "generic").toLowerCase();
            if ((osName.contains("mac")) || (osName.contains("darwin"))) {
                URL iconURL = MainFm.class.getResource("/img/logo.png");
                java.awt.Image image = new ImageIcon(iconURL).getImage();
                Class appleApp = Class.forName("com.apple.eawt.Application");
                //noinspection unchecked
                Method getApplication = appleApp.getMethod("getApplication");
                Object application = getApplication.invoke(appleApp);
                Class[] params = new Class[1];
                params[0] = java.awt.Image.class;
                //noinspection unchecked
                Method setDockIconImage = appleApp.getMethod("setDockIconImage", params);
                setDockIconImage.invoke(application, image);
            }
        } catch (Exception e) {
            StaticLog.error(e);
        }
        primaryStage.setTitle("树洞OCR文字识别");
        primaryStage.getIcons().add(new Image(getClass().getResource("/img/logo.png").toExternalForm()));
    }

    @Override
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
    }

    private void clearText(){
        textArea.setText("");
    }

    private void pasteText() {
        String text = Clipboard.getSystemClipboard().getString();
        if (StrUtil.isBlank(text)) {
            return;
        }
        textArea.setText(textArea.getText()
                + (StrUtil.isBlank(textArea.getText()) ? "" : "\n")
                + Clipboard.getSystemClipboard().getString());
    }

    private void copyText(){
        String text = textArea.getSelectedText();
        if (StrUtil.isBlank(text)){
            text = textArea.getText();
        }
        if (StrUtil.isBlank(text)){
            return;
        }
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.PLAIN_TEXT, text);
        Clipboard.getSystemClipboard().setContent(data);
    }

    public static void doSnap() {
        stageInfo = new StageInfo(stage.getX(), stage.getY(),
                stage.getWidth(), stage.getHeight(), stage.isFullScreen());
        runLater(screenCapture::prepareForCapture);
    }

    public static void cancelSnap() {
        runLater(screenCapture::cancelSnap);
    }

//    public static void setOcrResult(String text){
//        ocrText = text;
//    }
//
//    private void segmentText() {
//        textArea.setText(ocrText);
//    }
//
//    private void resetText() {
//        textArea.setText(ocrText);
//    }

    public static void restore() {
        stage.setAlwaysOnTop(false);
        stage.setScene(mainScene);
        stage.setFullScreen(stageInfo.isFullScreenState());
        stage.setX(stageInfo.getX());
        stage.setY(stageInfo.getY());
        stage.setWidth(stageInfo.getWidth());
        stage.setHeight(stageInfo.getHeight());
        stage.requestFocus();
    }

    private static void initKeyHook(){
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.WARNING);
            logger.setUseParentHandlers(false);
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
