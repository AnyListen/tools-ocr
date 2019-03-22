package com.luooqi.ocr;

import cn.hutool.core.util.StrUtil;
import com.luooqi.ocr.controller.CaptureWindowController;
import com.luooqi.ocr.utils.GlobalKeyListener;
import com.luooqi.ocr.utils.WidgetFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.application.Platform.exit;
import static javafx.application.Platform.runLater;

public class MainFm extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage stage;
    private static CaptureWindowController captureWindowController;
    private TextArea textArea;

    @Override
    public void start(Stage primaryStage) {
        initKeyHook();

        try {
            stage = primaryStage;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CaptureWindow.fxml"));
            loader.load();
            captureWindowController = loader.getController();
        }
        catch (Exception e) {
            exit();
        }
        HBox topBar = new HBox(
                WidgetFactory.createButton("snapBtn", 28, MainFm::doSnap, "截图"),
                WidgetFactory.createButton("copyBtn", 28, this::copyText, "复制"),
                WidgetFactory.createButton("pasteBtn", 28, this::pasteText, "粘贴"),
                WidgetFactory.createButton("clearBtn", 28, this::clearText, "清空")
        );
        topBar.setMinHeight(40);
        topBar.setSpacing(10);
        topBar.setPadding(new Insets(6, 8, 6, 8));

        textArea = new TextArea();
        ToolBar footer = WidgetFactory.statsFooter(textArea.textProperty());
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(textArea);
        root.setBottom(footer);
        root.getStylesheets().addAll(
                getClass().getResource("/css/main.css").toExternalForm()
        );

        try {
            String osName = System.getProperty("os.name", "generic").toLowerCase();
            if ((osName.contains("mac")) || (osName.contains("darwin"))) {
                URL iconURL = MainFm.class.getResource("/img/logo.png");
                java.awt.Image image = new ImageIcon(iconURL).getImage();
                com.apple.eawt.Application.getApplication().setDockIconImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("天若OCR");
        primaryStage.getIcons().add(new Image(getClass().getResource("/img/logo.png").toExternalForm()));
        Scene mainScene = new Scene(root, 700, 504);
        stage.setScene(mainScene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
    }

    private void clearText(){
        textArea.setText("");
    }

    private void pasteText(){
        textArea.setText(textArea.getText() + "\n" + Clipboard.getSystemClipboard().getString());
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
        runLater(captureWindowController::prepareForCapture);
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
