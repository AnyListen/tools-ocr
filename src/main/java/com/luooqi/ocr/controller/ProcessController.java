package com.luooqi.ocr.controller;

import com.luooqi.ocr.utils.CommUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProcessController extends Stage {

  public ProcessController() {
    VBox vBox = new VBox();
    vBox.setAlignment(Pos.BASELINE_CENTER);
    vBox.setMinWidth(300);
    vBox.setBackground(new Background(new BackgroundFill(Color.rgb(250, 250, 250), CornerRadii.EMPTY, Insets.EMPTY)));
    ProgressIndicator progressIndicator = new ProgressIndicator();
    progressIndicator.setStyle(CommUtils.STYLE_TRANSPARENT);
    int circleSize = 75;
    progressIndicator.setMinWidth(circleSize);
    progressIndicator.setMinHeight(circleSize);
    Label topLab = new Label("正在识别图片，请稍等.....");
    topLab.setFont(Font.font(18));
    vBox.setSpacing(10);
    vBox.setPadding(new Insets(20, 0, 20, 0));
    vBox.getChildren().add(progressIndicator);
    vBox.getChildren().add(topLab);
    Scene scene = new Scene(vBox, Color.TRANSPARENT);
    setScene(scene);
    initStyle(StageStyle.TRANSPARENT);
    CommUtils.initStage(this);
  }
}
