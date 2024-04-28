
package com.luooqi.ocr.snap;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import com.luooqi.ocr.model.CaptureInfo;
import com.luooqi.ocr.utils.CommUtils;
import com.luooqi.ocr.windows.MainForm;

import cn.hutool.core.swing.ScreenUtil;
import cn.hutool.log.StaticLog;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * This is the Window which is used from the user to draw the rectangle representing an area on the screen to be captured.
 *
 * @author GOXR3PLUS
 */
public class ScreenCapture {

  private final BorderPane rootPane;
  private final Canvas mainCanvas;
  private final CaptureInfo data;
  private GraphicsContext gc;
  private Scene scene;
  private final Stage stage;
  public static boolean isSnapping = false;

  /**
   * When a key is being pressed into the capture window then this Animation Timer is doing it's magic.
   */
  private final AnimationTimer yPressedAnimation = new AnimationTimer() {
    private long nextSecond = 0L;
    private long precisionLevel;

    @Override
    public void start() {
      nextSecond = 0L;
      precisionLevel = 0L;
      super.start();
    }

    @Override
    public void handle(long nanos) {
      if (nanos >= nextSecond) {
        nextSecond = nanos + precisionLevel;

        // With special key pressed
        // (we want [LEFT] and [DOWN] side of the rectangle to be
        // movable)

        // No Special Key is Pressed
        // (we want [RIGHT] and [UP] side of the rectangle to be
        // movable)

        // ------------------------------
        if (data.rightPressed.get()) {
          if (data.shiftPressed.get()) { // Special Key?
            if (data.mouseXNow > data.mouseXPressed) { // Mouse gone Right?
              data.mouseXPressed += 1;
            } else {
              data.mouseXNow += 1;
            }
          } else {
            if (data.mouseXNow > data.mouseXPressed) { // Mouse gone Right?
              data.mouseXNow += 1;
            } else {
              data.mouseXPressed += 1;
            }
          }
        }

        if (data.leftPressed.get()) {
          if (data.shiftPressed.get()) { // Special Key?
            if (data.mouseXNow > data.mouseXPressed) { // Mouse gone Right?
              data.mouseXPressed -= 1;
            } else {
              data.mouseXNow -= 1;
            }
          } else {
            if (data.mouseXNow > data.mouseXPressed) { // Mouse gone Right?
              data.mouseXNow -= 1;
            } else {
              data.mouseXPressed -= 1;
            }
          }
        }

        if (data.upPressed.get()) {
          if (data.shiftPressed.get()) { // Special Key?
            if (data.mouseYNow > data.mouseYPressed) { // Mouse gone UP?
              data.mouseYNow -= 1;
            } else {
              data.mouseYPressed -= 1;
            }
          } else {
            if (data.mouseYNow > data.mouseYPressed) { // Mouse gone UP?
              data.mouseYPressed -= 1;
            } else {
              data.mouseYNow -= 1;
            }
          }
        }

        if (data.downPressed.get()) {
          if (data.shiftPressed.get()) { // Special Key?
            if (data.mouseYNow > data.mouseYPressed) { // Mouse gone UP?
              data.mouseYNow += 1;
            } else {
              data.mouseYPressed += 1;
            }
          } else {
            if (data.mouseYNow > data.mouseYPressed) { // Mouse gone UP?
              data.mouseYPressed += 1;
            } else {
              data.mouseYNow += 1;
            }
          }
        }

        if (data.mouseXPressed < 0) {
          data.mouseXPressed = 0;
        }
        if (data.mouseXNow < 0) {
          data.mouseXNow = 0;
        }
        if (data.mouseXPressed > CaptureInfo.ScreenWidth) {
          data.mouseXPressed = CaptureInfo.ScreenWidth;
        }
        if (data.mouseXNow > CaptureInfo.ScreenWidth) {
          data.mouseXNow = CaptureInfo.ScreenWidth;
        }
        repaintCanvas();
      }
    }
  };

  /**
   * Constructor.
   */
  public ScreenCapture(Stage mainStage) {
    data = new CaptureInfo();
    stage = mainStage;
    rootPane = new BorderPane();
    mainCanvas = new Canvas();
    mainCanvas.setCursor(Cursor.CROSSHAIR);
    mainCanvas.setStyle(CommUtils.STYLE_TRANSPARENT);
    rootPane.getChildren().add(mainCanvas);

    // Scene
    scene = new Scene(rootPane, CaptureInfo.ScreenWidth, CaptureInfo.ScreenHeight, Color.TRANSPARENT);
    scene.setCursor(Cursor.NONE);

    addKeyHandlers();

    // Canvas
    mainCanvas.setWidth(CaptureInfo.ScreenWidth);
    mainCanvas.setHeight(CaptureInfo.ScreenHeight);
    mainCanvas.setOnMousePressed(m -> {
      if (m.getButton() == MouseButton.PRIMARY) {
        data.mouseXPressed = (int) m.getX();
        data.mouseYPressed = (int) m.getY();
      }
    });

    mainCanvas.setOnMouseDragged(m -> {
      if (m.getButton() == MouseButton.PRIMARY) {
        if (m.getScreenX() >= CaptureInfo.ScreenMinX && m.getScreenX() <= CaptureInfo.ScreenMaxX) {
          data.mouseXNow = (int) m.getX();
        } else if (m.getScreenX() > CaptureInfo.ScreenMaxX) {
          data.mouseXNow = CaptureInfo.ScreenWidth;
        }

        if (m.getScreenY() <= CaptureInfo.ScreenHeight) {
          data.mouseYNow = (int) m.getY();
        } else {
          data.mouseYNow = CaptureInfo.ScreenHeight;
        }
        repaintCanvas();
      }
    });

    // graphics context 2D
    initGraphContent();
    // HideFeaturesPressed
    data.hideExtraFeatures.addListener((observable, oldValue, newValue) -> repaintCanvas());
  }

  private void initGraphContent() {
    gc = mainCanvas.getGraphicsContext2D();
    gc.setLineDashes(6);
    gc.setFont(Font.font("null", FontWeight.BOLD, 14));
  }

  /**
   * Adds the KeyHandlers to the Scene.
   */
  private void addKeyHandlers() {

    // -------------Read the below to understand the Code-------------------

    // the default prototype of the below code is
    // 1->when the user is pressing RIGHT ARROW -> The rectangle is
    // increasing from the RIGHT side
    // 2->when the user is pressing LEFT ARROW -> The rectangle is
    // decreasing from the RIGHT side
    // 3->when the user is pressing UP ARROW -> The rectangle is increasing
    // from the UP side
    // 4->when the user is pressing DOWN ARROW -> The rectangle is
    // decreasing from the UP side

    // when ->LEFT KEY <- is pressed
    // 1->when the user is pressing RIGHT ARROW -> The rectangle is
    // increasing from the LEFT side
    // 2->when the user is pressing LEFT ARROW -> The rectangle is
    // decreasing from the LEFT side
    // 3->when the user is pressing UP ARROW -> The rectangle is increasing
    // from the DOWN side
    // 4->when the user is pressing DOWN ARROW -> The rectangle is
    // decreasing from the DOWN side

    scene.setOnKeyPressed(key -> {
      if (key.isShiftDown())
        data.shiftPressed.set(true);

      if (key.getCode() == KeyCode.LEFT)
        data.leftPressed.set(true);

      if (key.getCode() == KeyCode.RIGHT)
        data.rightPressed.set(true);

      if (key.getCode() == KeyCode.UP)
        data.upPressed.set(true);

      if (key.getCode() == KeyCode.DOWN)
        data.downPressed.set(true);

      if (key.getCode() == KeyCode.H)
        data.hideExtraFeatures.set(true);
    });

    // keyReleased
    scene.setOnKeyReleased(key -> {
      if (key.getCode() == KeyCode.SHIFT) {
        data.shiftPressed.set(false);
      }

      if (key.getCode() == KeyCode.RIGHT) {
        if (key.isControlDown()) {
          data.mouseXNow = (int) stage.getWidth();
          repaintCanvas();
        }
        data.rightPressed.set(false);
      }

      if (key.getCode() == KeyCode.LEFT) {
        if (key.isControlDown()) {
          data.mouseXPressed = 0;
          repaintCanvas();
        }
        data.leftPressed.set(false);
      }

      if (key.getCode() == KeyCode.UP) {
        if (key.isControlDown()) {
          data.mouseYPressed = 0;
          repaintCanvas();
        }
        data.upPressed.set(false);
      }

      if (key.getCode() == KeyCode.DOWN) {
        if (key.isControlDown()) {
          data.mouseYNow = (int) stage.getHeight();
          repaintCanvas();
        }
        data.downPressed.set(false);
      }

      if (key.getCode() == KeyCode.A && key.isControlDown()) {
        selectWholeScreen();
      }

      if (key.getCode() == KeyCode.H) {
        data.hideExtraFeatures.set(false);
      }

      if (key.getCode() == KeyCode.ESCAPE || key.getCode() == KeyCode.BACK_SPACE) {
        cancelSnap();
        isSnapping = false;
      } else if (key.getCode() == KeyCode.ENTER || key.getCode() == KeyCode.SPACE) {
        deActivateAllKeys();
        isSnapping = false;
        prepareImage();
      }
    });

    data.anyPressed.addListener((obs, wasPressed, isNowPressed) -> {
      if (isNowPressed) {
        yPressedAnimation.start();
      } else {
        yPressedAnimation.stop();
      }
    });

    rootPane.setOnMouseClicked(event -> {
      if (event.getClickCount() > 1) {
        if (data.rectWidth * data.rectHeight > 0) {
          rootPane.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ENTER, false, false, false, false));
        }
      }
    });
  }

  /**
   * Deactivates the keys contained into this method.
   */
  private void deActivateAllKeys() {
    data.shiftPressed.set(false);
    data.upPressed.set(false);
    data.rightPressed.set(false);
    data.downPressed.set(false);
    data.leftPressed.set(false);
    data.hideExtraFeatures.set(false);
  }

  /**
   * Repaint the canvas of the capture window.
   */
  private void repaintCanvas() {
    gc.clearRect(0, 0, CaptureInfo.ScreenWidth, CaptureInfo.ScreenHeight);
    gc.setFill(CommUtils.MASK_COLOR);
    gc.fillRect(0, 0, CaptureInfo.ScreenWidth, CaptureInfo.ScreenHeight);

    gc.setFont(data.font);
    gc.setStroke(Color.RED);
    gc.setLineWidth(1);

    // smart calculation of where the mouse has been dragged
    data.rectWidth = (data.mouseXNow > data.mouseXPressed) ? data.mouseXNow - data.mouseXPressed // RIGHT
        : data.mouseXPressed - data.mouseXNow // LEFT
    ;
    data.rectHeight = (data.mouseYNow > data.mouseYPressed) ? data.mouseYNow - data.mouseYPressed // DOWN
        : data.mouseYPressed - data.mouseYNow // UP
    ;

    data.rectUpperLeftX = // -------->UPPER_LEFT_X
        (data.mouseXNow > data.mouseXPressed) ? data.mouseXPressed // RIGHT
            : data.mouseXNow// LEFT
    ;
    data.rectUpperLeftY = // -------->UPPER_LEFT_Y
        (data.mouseYNow > data.mouseYPressed) ? data.mouseYPressed // DOWN
            : data.mouseYNow // UP
    ;

    gc.strokeRect(data.rectUpperLeftX - 1.00, data.rectUpperLeftY - 1.00, data.rectWidth + 2.00,
        data.rectHeight + 2.00);
    gc.clearRect(data.rectUpperLeftX, data.rectUpperLeftY, data.rectWidth, data.rectHeight);

    // draw the text
    if (!data.hideExtraFeatures.getValue() && (data.rectWidth > 0 || data.rectHeight > 0)) {
      double middle = data.rectUpperLeftX + data.rectWidth / 2.00;
      gc.setLineWidth(1);
      gc.setFill(Color.FIREBRICK);
      gc.fillRect(middle - 77, data.rectUpperLeftY < 50 ? data.rectUpperLeftY + 2 : data.rectUpperLeftY - 18.00, 100,
          18);
      gc.setFill(Color.WHITE);
      gc.fillText(data.rectWidth + " * " + data.rectHeight, middle - 77 + 9,
          data.rectUpperLeftY < 50 ? data.rectUpperLeftY + 17.00 : data.rectUpperLeftY - 4.00);
    }
  }

  /**
   * Selects whole Screen.
   */
  private void selectWholeScreen() {
    data.mouseXPressed = 0;
    data.mouseYPressed = 0;
    data.mouseXNow = (int) stage.getWidth();
    data.mouseYNow = (int) stage.getHeight();
    repaintCanvas();
  }

  public void prepareForCapture() {
    isSnapping = true;
    MainForm.stage.setOpacity(0.0f);
    Platform.runLater(() -> {
      Rectangle rectangle = CommUtils.getDisplayScreen(MainForm.stage);
      data.reset();
      CaptureInfo.ScreenMinX = rectangle.x;
      CaptureInfo.ScreenMaxX = rectangle.x + rectangle.width;
      CaptureInfo.ScreenWidth = rectangle.width;
      CaptureInfo.ScreenHeight = rectangle.height;
      BufferedImage bufferedImage = ScreenUtil.captureScreen(rectangle);
      // bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, CaptureInfo.ScreenWidth * 2, CaptureInfo.ScreenHeight * 2);
      WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
      deActivateAllKeys();
      scene.setRoot(new Pane());
      scene = new Scene(rootPane, CaptureInfo.ScreenWidth, CaptureInfo.ScreenHeight, Color.TRANSPARENT);
      addKeyHandlers();
      mainCanvas.setWidth(CaptureInfo.ScreenWidth);
      mainCanvas.setHeight(CaptureInfo.ScreenHeight);
      mainCanvas.setCursor(Cursor.CROSSHAIR);
      initGraphContent();
      rootPane.setBackground(new Background(new BackgroundImage(fxImage, BackgroundRepeat.NO_REPEAT,
          BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
          new BackgroundSize(CaptureInfo.ScreenWidth, CaptureInfo.ScreenHeight, false, false, true, true))));
      repaintCanvas();
      stage.setScene(scene);
      stage.setFullScreenExitHint("");
      if (stage.isIconified()) {
        stage.setIconified(false);
      }
      stage.setFullScreen(true);
      stage.setAlwaysOnTop(true);
      stage.setOpacity(1.0f);
      stage.requestFocus();
    });
  }

  private void prepareImage() {
    gc.clearRect(0, 0, stage.getWidth(), stage.getHeight());
    BufferedImage image;
    try {
      mainCanvas.setDisable(true);
      image = new Robot().createScreenCapture(new Rectangle(data.rectUpperLeftX + CaptureInfo.ScreenMinX,
          data.rectUpperLeftY + (int) CommUtils.getCrtScreen(stage).getVisualBounds().getMinY(), data.rectWidth,
          data.rectHeight));
    } catch (AWTException ex) {
      StaticLog.error(ex);
      return;
    } finally {
      mainCanvas.setDisable(false);
      MainForm.restore(false);
    }
    MainForm.doOcr(image);
  }

  public void cancelSnap() {
    deActivateAllKeys();
    MainForm.restore(true);
  }
}
