package com.luooqi.ocr;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.luooqi.ocr.controller.CaptureWindowController;
import com.luooqi.ocr.utils.GlobalKeyListener;
import com.luooqi.ocr.utils.WidgetFactory;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.lang.reflect.Method;
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
    public static TextArea textArea;
    private static CaptureWindowController captureWindowController;
    public static BooleanProperty isOcr = new SimpleBooleanProperty(false);

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
        textArea.setWrapText(true);

        ToolBar footer = WidgetFactory.statsFooter(textArea.textProperty());
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(textArea);
        root.setBottom(footer);
        root.getStylesheets().addAll(
                getClass().getResource("/css/main.css").toExternalForm()
        );

        initStage(primaryStage);
        Scene mainScene = new Scene(root, 420, 475);
        stage.setScene(mainScene);
        stage.show();
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
                Class params[] = new Class[1];
                params[0] = Image.class;
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
