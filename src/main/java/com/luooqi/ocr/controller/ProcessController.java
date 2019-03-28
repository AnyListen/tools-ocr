package com.luooqi.ocr.controller;

import com.luooqi.ocr.utils.CommUtils;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProcessController extends Stage {

    public ProcessController(){
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle(CommUtils.STYLE_TRANSPARENT);
        int circleSize = 100;
        progressIndicator.setMaxWidth(circleSize);
        progressIndicator.setMaxHeight(circleSize);
        progressIndicator.setAccessibleText("正在识别...");
        Scene scene = new Scene(progressIndicator, circleSize, circleSize);
        setScene(scene);
        initStyle(StageStyle.TRANSPARENT);
    }
}
