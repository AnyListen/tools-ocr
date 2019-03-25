
package com.luooqi.ocr.controller;

import com.luooqi.ocr.MainFm;
import com.luooqi.ocr.model.CaptureWindowModel;
import com.luooqi.ocr.utils.CommUtils;
import com.luooqi.ocr.utils.OcrUtils;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the Window which is used from the user to draw the rectangle representing an area on the screen to be captured.
 *
 * @author GOXR3PLUS
 */
public class CaptureWindowController extends Stage {

	/**
	 * The stack pane.
	 */
	@FXML
	private StackPane stackPane;

	/**
	 * The main canvas.
	 */
	@FXML
	private Canvas mainCanvas;

	// -----------------------------

	/**
	 * The Model of the CaptureWindow
	 */
	private CaptureWindowModel data = new CaptureWindowModel();

	/**
	 * The graphics context of the canvas
	 */
	private GraphicsContext gc;

	/**
	 * When a key is being pressed into the capture window then this Animation Timer is doing it's magic.
	 */
	private AnimationTimer yPressedAnimation = new AnimationTimer() {
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
				repaintCanvas();
			}
		}
	};

	/**
	 * This AnimationTimer waits until the canvas is cleared before it can capture the screen.
	 */
	private AnimationTimer waitFrameRender = new AnimationTimer() {
		private int frameCount = 0;

		@Override
		public void start() {
			frameCount = 0;
			super.start();
		}

		@Override
		public void handle(long timestamp) {
			frameCount++;
			if (frameCount >= 5) {
				stop();
				BufferedImage image;
				int[] rect = getRectangleBounds();
				try {
					image = new Robot().createScreenCapture(new Rectangle(rect[0], rect[1], rect[2], rect[3]));
				} catch (AWTException ex) {
					Logger.getLogger(getClass().getName()).log(Level.INFO, null, ex);
					return;
				} finally {
					mainCanvas.setDisable(false);
				}
				MainFm.stage.show();
				close();
				Platform.runLater(() -> {
					MainFm.doOCR(image);
				});
			}
		}
	};

	/**
	 * The counting thread.
	 */
	private Thread countingThread;

	/**
	 * Constructor.
	 */
	public CaptureWindowController() {
		setX(0);
		setY(0);
        setTitle("树洞OCR文字识别");
        getIcons().add(new Image(MainFm.class.getResource("/img/logo.png").toExternalForm()));
		initStyle(StageStyle.TRANSPARENT);
		setAlwaysOnTop(true);
	}

	/**
	 * Will be called as soon as FXML file is loaded.
	 */
	@FXML
	public void initialize() {

		// Scene
		Scene scene = new Scene(stackPane, data.screenWidth, data.screenHeight, Color.TRANSPARENT);
		scene.setCursor(Cursor.NONE);
		setScene(scene);
		addKeyHandlers();

		// Canvas
		mainCanvas.setWidth(data.screenWidth);
		mainCanvas.setHeight(data.screenHeight);
		mainCanvas.setOnMousePressed(m -> {
			if (m.getButton() == MouseButton.PRIMARY) {
				data.mouseXPressed = (int) m.getScreenX();
				data.mouseYPressed = (int) m.getScreenY();
			}
		});

		mainCanvas.setOnMouseDragged(m -> {
			if (m.getButton() == MouseButton.PRIMARY) {
				data.mouseXNow = (int) m.getScreenX();
				data.mouseYNow = (int) m.getScreenY();
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

		getScene().setOnKeyPressed(key -> {
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
		getScene().setOnKeyReleased(key -> {
			if (key.getCode() == KeyCode.SHIFT) {
				data.shiftPressed.set(false);
			}

			if (key.getCode() == KeyCode.RIGHT) {
				if (key.isControlDown()) {
					data.mouseXNow = (int) getWidth();
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
					data.mouseYNow = (int) getHeight();
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
			} else if (key.getCode() == KeyCode.ENTER || key.getCode() == KeyCode.SPACE) {
				deActivateAllKeys();
				prepareImage();
			}
		});

		data.anyPressed.addListener((obs, wasPressed, isNowPressed) ->
		{
			if (isNowPressed) {
				yPressedAnimation.start();
			} else {
				yPressedAnimation.stop();
			}
		});
	}

	public void cancelSnap(){
		if (countingThread != null){
			countingThread.interrupt();
		}
		deActivateAllKeys();
		MainFm.stage.show();
		MainFm.stage.requestFocus();
		close();
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
	 * Creates and saves the image.
	 */
	private void prepareImage() {
		//if ((countingThread != null && countingThread.isAlive()) || captureService.isRunning()) {
		if ((countingThread != null && countingThread.isAlive())) {
			return;
		}
		countingThread = new Thread(() -> {
			mainCanvas.setDisable(true);
			if (!Thread.interrupted()) {
				Platform.runLater(() -> {
					gc.clearRect(0, 0, getWidth(), getHeight());
					waitFrameRender.start();
				});
			}
		});
		countingThread.setDaemon(true);
		countingThread.start();
	}

	/**
	 * Repaint the canvas of the capture window.
	 */
	private void repaintCanvas() {
		gc.clearRect(0, 0, getWidth(), getHeight());
		gc.setFill(Color.rgb(0, 0, 0, 0.8));
		gc.fillRect(0, 0, getWidth(), getHeight());
		gc.setFont(data.font);
		// draw the actual rectangle
		gc.setStroke(Color.RED);
		// gc.setFill(model.background)
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

		gc.strokeRect(data.rectUpperLeftX - 1.00, data.rectUpperLeftY - 1.00, data.rectWidth + 2.00, data.rectHeight + 2.00);
		// gc.fillRect(model.rectUpperLeftX, model.rectUpperLeftY,model.rectWidth, model.rectHeight)
		gc.clearRect(data.rectUpperLeftX, data.rectUpperLeftY, data.rectWidth, data.rectHeight);

		// draw the text
		if (!data.hideExtraFeatures.getValue() && (data.rectWidth > 0 ||  data.rectHeight > 0)) {
			double middle = data.rectUpperLeftX + data.rectWidth / 2.00;
			gc.setLineWidth(1);
			gc.setFill(Color.FIREBRICK);
			gc.fillRect(middle - 77, data.rectUpperLeftY < 50 ? data.rectUpperLeftY + 2 : data.rectUpperLeftY - 18.00, 100, 18);
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
		data.mouseXNow = (int) getWidth();
		data.mouseYNow = (int) getHeight();
		repaintCanvas();
	}

	/**
	 * Prepares the Window for the User.
	 */
	public void prepareForCapture() {
		data = new CaptureWindowModel();
		show();
		repaintCanvas();
		MainFm.stage.close();
	}

	/**
	 * Return an array witch contains the (UPPER_LEFT) Point2D of the rectangle and the width and height of the rectangle.
	 *
	 * @return An array witch contains the (UPPER_LEFT) Point2D of the rectangle and the width and height of the rectangle
	 */
	private int[] getRectangleBounds() {
		return new int[]{data.rectUpperLeftX, data.rectUpperLeftY, data.rectWidth, data.rectHeight};
	}
}
