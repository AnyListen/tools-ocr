package com.luooqi.ocr.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;

public class WidgetFactory {

    public static Button createButton(String id,int size, Runnable action, String toolTip) {
        Button button = new Button();
        button.setId(id);
        button.setOnAction(evt -> action.run());
        button.setMinSize(size, size);
        if (toolTip != null) {
            button.setTooltip(new Tooltip(toolTip));
        }
        return button;
    }

    public static ToolBar statsFooter(ObservableValue<String> textProperty) {
        ToolBar footerBar = new ToolBar();
        footerBar.setId("statsToolbar");
        Label statsLabel = new Label();
        SimpleStringProperty statsProperty = new SimpleStringProperty("总字数：0");
        textProperty.addListener((observable, oldValue, newValue) -> statsProperty.set("总字数：" + newValue.replaceAll("\\s+", "").length()));
        statsLabel.textProperty().bind(statsProperty);
        footerBar.getItems().addAll(statsLabel);
        return footerBar;
    }

}
