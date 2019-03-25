package com.luooqi.ocr.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
import com.luooqi.ocr.model.TextBlock;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommUtils {

    private static final float IMAGE_QUALITY = 0.5f;
    private static final int SAME_LINE_LIMIT = 8;
    private static final int CHAR_WIDTH = 12;

    public static byte[] imageToBytes(BufferedImage img) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpeg", outputStream);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outputStream);
            JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(img);
            jep.setQuality(IMAGE_QUALITY, true);
            encoder.encode(img, jep);
            byte[] result = outputStream.toByteArray();
            System.out.println(result.length);
            outputStream.close();
            FileUtil.writeBytes(result, "demo.jpeg");
            return result;
        } catch (IOException e) {
            StaticLog.error(e);
            return new byte[0];
        }
    }

    public static String combineTextBlocks(List<TextBlock> textBlocks) {
        Collections.sort(textBlocks, Comparator.comparingInt(o -> o.getTopLeft().y));
        List<List<TextBlock>> lineBlocks = new ArrayList<>();
        int lastY = -1;
        List<TextBlock> lineBlock = new ArrayList<>();
        boolean sameLine = true;
        int minX = Integer.MAX_VALUE;
        for (TextBlock textBlock : textBlocks) {
            if (textBlock.getTopLeft().x < minX) {
                minX = textBlock.getTopLeft().x;
            }
            if (lastY == -1) {
                lastY = textBlock.getTopLeft().y;
            } else {
                sameLine = textBlock.getTopLeft().y - lastY <= SAME_LINE_LIMIT;
            }
            if (!sameLine) {
                Collections.sort(lineBlock, Comparator.comparingInt(o -> o.getTopLeft().x));
                lineBlocks.add(lineBlock);
                lineBlock = new ArrayList<>();
                sameLine = true;
                lastY = textBlock.getTopLeft().y;
            }
            lineBlock.add(textBlock);
        }
        if (lineBlock.size() > 0) {
            Collections.sort(lineBlock, Comparator.comparingInt(o -> o.getTopLeft().x));
            lineBlocks.add(lineBlock);
        }
        StringBuilder sb = new StringBuilder();
        int baseX = minX;
        lineBlocks.forEach(line -> {
            for (int i = 0, ln = (line.get(0).getTopLeft().x - baseX) / CHAR_WIDTH; i < ln; i++) {
                sb.append("  ");
            }
            line.forEach(text -> sb.append(text.getText()));
            sb.append("\n");
        });
        return sb.toString();
    }

    public static Point frameToPoint(String text) {
        String[] arr = text.split(",");
        return new Point(Integer.valueOf(arr[0].trim()), Integer.valueOf(arr[1].trim()));
    }

    public static String postMultiData(String url, byte[] data, String boundary) {
        return postMultiData(url, data, boundary, "", "");
    }

    public static String postMultiData(String url, byte[] data, String boundary, String cookie, String referer) {
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
            return WebHelper.getSafeHtml(response);
        } catch (Exception ex) {
            StaticLog.error(ex);
            return null;
        }
    }

    public static byte[] mergeByte(byte[]... bytes) {
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

    public static javafx.scene.control.Button createButton(String id, int size, Runnable action, String toolTip) {
        javafx.scene.control.Button button = new Button();
        button.setId(id);
        button.setOnAction(evt -> action.run());
        button.setMinSize(size, size);
        if (toolTip != null) {
            button.setTooltip(new Tooltip(toolTip));
        }
        return button;
    }
}
