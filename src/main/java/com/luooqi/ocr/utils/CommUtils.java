package com.luooqi.ocr.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
import com.luooqi.ocr.model.TextBlock;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class CommUtils {

    public static final Paint MASK_COLOR = Color.rgb(0, 0, 0, 0.4);
    public static final int BUTTON_SIZE = 28;
    public static Background BG_TRANSPARENT = new Background(new BackgroundFill(Color.TRANSPARENT,
            CornerRadii.EMPTY, Insets.EMPTY));
    private static Pattern NORMAL_CHAR = Pattern.compile("[\\u4e00-\\u9fa5\\w、-]");
    public static Separator SEPARATOR = new Separator(Orientation.VERTICAL);
    private static final float IMAGE_QUALITY = 0.5f;
    private static final int SAME_LINE_LIMIT = 8;
    private static final int CHAR_WIDTH = 12;
    public static final String STYLE_TRANSPARENT = "-fx-background-color: transparent;";
    public static final String SPECIAL_CHARS = "[\\s`~!@#$%^&*()_\\-+=|{}':;,\\[\\].<>/?！￥…（）【】‘；：”“’。，、？]+";

    public static byte[] imageToBytes(BufferedImage img) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream outputStream = new MemoryCacheImageOutputStream(byteArrayOutputStream);
        try {
            Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(IMAGE_QUALITY);
            writer.setOutput(outputStream);
            IIOImage image = new IIOImage(img, null, null);
            writer.write(null, image, iwp);
            writer.dispose();
            byte[] result = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            outputStream.close();
            return result;
        } catch (IOException e) {
            StaticLog.error(e);
            return new byte[0];
        }
    }

    static String combineTextBlocks(List<TextBlock> textBlocks) {
        textBlocks.sort(Comparator.comparingInt(o -> o.getTopLeft().y));
        List<List<TextBlock>> lineBlocks = new ArrayList<>();
        int lastY = -1;
        List<TextBlock> lineBlock = new ArrayList<>();
        boolean sameLine = true;
        int minX = Integer.MAX_VALUE;
        int maxX = -1;
        for (TextBlock textBlock : textBlocks) {
            if (textBlock.getTopLeft().x < minX) {
                minX = textBlock.getTopLeft().x;
            }
            if (textBlock.getTopRight().x > maxX) {
                maxX = textBlock.getTopRight().x;
            }
            if (lastY == -1) {
                lastY = textBlock.getTopLeft().y;
            } else {
                sameLine = textBlock.getTopLeft().y - lastY <= SAME_LINE_LIMIT;
            }
            if (!sameLine) {
                lineBlock.sort(Comparator.comparingInt(o -> o.getTopLeft().x));
                lineBlocks.add(lineBlock);
                lineBlock = new ArrayList<>();
                sameLine = true;
                lastY = textBlock.getTopLeft().y;
            }
            lineBlock.add(textBlock);
        }
        if (lineBlock.size() > 0) {
            lineBlock.sort(Comparator.comparingInt(o -> o.getTopLeft().x));
            lineBlocks.add(lineBlock);
        }
        StringBuilder sb = new StringBuilder();
        TextBlock lastBlock = null;
        for (List<TextBlock> line : lineBlocks) {
            TextBlock firstBlock = line.get(0);
            if (lastBlock != null) {
                String blockTxt = lastBlock.getText().trim();
                String endTxt = blockTxt.substring(blockTxt.length() - 1);
                if (maxX - lastBlock.getTopRight().x >= CHAR_WIDTH ||
                        (!NORMAL_CHAR.matcher(endTxt).find() &&
                                (firstBlock.getTopLeft().x - minX) >= CHAR_WIDTH)) {
                    sb.append("\n");
                    for (int i = 0, ln = (firstBlock.getTopLeft().x - minX) / CHAR_WIDTH; i < ln; i++) {
                        sb.append("  ");
                    }
                }
            }
            for (TextBlock text : line) {
                String ocrText = text.getText();
                sb.append(ocrText);
                if (ocrText.matches("^\\w+$")) {
                    sb.append(" ");
                }
            }
            lastBlock = line.get(line.size() - 1);
        }
        return sb.toString();
    }

    static Point frameToPoint(String text) {
        String[] arr = text.split(",");
        return new Point(Integer.valueOf(arr[0].trim()), Integer.valueOf(arr[1].trim()));
    }

    static String postMultiData(String url, byte[] data, String boundary) {
        return postMultiData(url, data, boundary, "", "");
    }

    private static String postMultiData(String url, byte[] data, String boundary, String cookie, String referer) {
        try {
            HttpRequest request = HttpUtil.createPost(url).timeout(15000);
            request.contentType("multipart/form-data; boundary=" + boundary);
            request.body(data);
            if (StrUtil.isNotBlank(referer)) {
                request.header("Referer", referer);
            }
            if (StrUtil.isNotBlank(cookie)) {
                request.cookie(cookie);
            }
            HttpResponse response = request.execute();
            return WebUtils.getSafeHtml(response);
        } catch (Exception ex) {
            StaticLog.error(ex);
            return null;
        }
    }

    static byte[] mergeByte(byte[]... bytes) {
        int length = 0;
        for (byte[] b : bytes) {
            length += b.length;
        }
        byte[] resultBytes = new byte[length];
        int offset = 0;
        for (byte[] arr : bytes) {
            System.arraycopy(arr, 0, resultBytes, offset, arr.length);
            offset += arr.length;
        }
        return resultBytes;
    }

    public static Button createButton(String id, Runnable action, String toolTip){
        return createButton(id, BUTTON_SIZE, action, toolTip);
    }

    public static Button createButton(String id, int size, Runnable action, String toolTip) {
        javafx.scene.control.Button button = new Button();
        initButton(button, id, size, action, toolTip);
        return button;
    }

    public static ToggleButton createToggleButton(ToggleGroup grp, String id, Runnable action, String toolTip){
        return createToggleButton(grp, id, BUTTON_SIZE, action, toolTip);
    }

    public static ToggleButton createToggleButton(ToggleGroup grp, String id, int size, Runnable action, String toolTip) {
        ToggleButton button = new ToggleButton();
        button.setToggleGroup(grp);
        initButton(button, id, size, action, toolTip);
        return button;
    }

    private static void initButton(ButtonBase button, String id, int size, Runnable action, String toolTip) {
        button.setId(id);
        button.setOnAction(evt -> action.run());
        button.setMinSize(size, size);
        if (toolTip != null) {
            button.setTooltip(new Tooltip(toolTip));
        }
    }
}
