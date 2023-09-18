
package com.luooqi.ocr.model;

import cn.hutool.core.swing.ScreenUtil;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;

/**
 * @author GOXR3PLUS
 */
public class CaptureInfo {

  /**
   * The x pressed.
   */
  public int mouseXPressed = 0;

  /**
   * The y pressed.
   */
  public int mouseYPressed = 0;

  /**
   * The x now.
   */
  public int mouseXNow = 0;

  /**
   * The y now.
   */
  public int mouseYNow = 0;

  /**
   * The upper left X.
   */
  public int rectUpperLeftX = 0;

  /**
   * The upper left Y.
   */
  public int rectUpperLeftY = 0;

  /**
   * The rectangle width.
   */
  public int rectWidth;

  /**
   * The rectangle height.
   */
  public int rectHeight;

  // ----------------

  /**
   * The font.
   */
  public Font font = Font.font("", FontWeight.BOLD, 14);

  // ---------------

  /**
   * The shift pressed.
   */
  public BooleanProperty shiftPressed = new SimpleBooleanProperty();

  /**
   * The up pressed.
   */
  public BooleanProperty upPressed = new SimpleBooleanProperty();

  /**
   * The right pressed.
   */
  public BooleanProperty rightPressed = new SimpleBooleanProperty();

  /**
   * The down pressed.
   */
  public BooleanProperty downPressed = new SimpleBooleanProperty();

  /**
   * The left pressed.
   */
  public BooleanProperty leftPressed = new SimpleBooleanProperty();

  /**
   * The any pressed.
   */
  public BooleanBinding anyPressed = upPressed.or(downPressed).or(leftPressed).or(rightPressed);

  /**
   * The hide extra features.
   */
  public BooleanProperty hideExtraFeatures = new SimpleBooleanProperty();

  // ------------

  /**
   * The screen width.
   */
  public static int ScreenWidth = ScreenUtil.getWidth();

  /**
   * The screen height.
   */
  public static int ScreenHeight = ScreenUtil.getHeight();

  public static int ScreenMinX = 0;
  public static int ScreenMaxX = 0;

  public void reset() {
    mouseXNow = 0;
    mouseXPressed = 0;
    mouseYNow = 0;
    mouseYPressed = 0;
    rectUpperLeftY = 0;
    rectUpperLeftX = 0;
    rectWidth = 0;
    rectHeight = 0;
  }

}
