package com.luooqi.ocr.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * tools-ocr
 * Created by 何志龙 on 2019-03-22.
 */
public class OcrUtils {

    public static byte[] imageToBytes(BufferedImage img) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpeg", outputStream);
            byte[] result = outputStream.toByteArray();
            outputStream.close();
            return result;
        } catch (IOException e) {
            StaticLog.error(e);
            return new byte[0];
        }
    }

    public static String sogouMobileOcr(byte[] imgData) {
        String boundary = "------WebKitFormBoundary8orYTmcj8BHvQpVU";
        String url = "http://ocr.shouji.sogou.com/v2/ocr/json";
        String header = boundary + "\r\nContent-Disposition: form-data; name=\"pic\"; filename=\"pic.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n";
        String footer = "\r\n" + boundary + "--\r\n";
        byte[] postData = mergeByte(header.getBytes(CharsetUtil.CHARSET_ISO_8859_1), imgData, footer.getBytes(CharsetUtil.CHARSET_ISO_8859_1));
        return extractSogouResult(postMultiData(url, postData, boundary.substring(2)));
    }

    public static String sogouWebOcr(byte[] imgData) {
        String url = "https://deepi.sogou.com/api/sogouService";
        String referer = "https://deepi.sogou.com/?from=picsearch&tdsourcetag=s_pctim_aiomsg";
        String imageData = Base64.encode(imgData);
        long t = new Date().getTime();
        String sign = SecureUtil.md5("sogou_ocr_just_for_deepibasicOpenOcr" + t + imageData.substring(0, Math.min(1024, imageData.length())) + "7f42cedccd1b3917c87aeb59e08b40ad");
        Map<String, Object> data = new HashMap<>();
        data.put("image", imageData);
        data.put("lang", "zh-Chs");
        data.put("pid", "sogou_ocr_just_for_deepi");
        data.put("salt", t);
        data.put("service", "basicOpenOcr");
        data.put("sign", sign);
        HttpRequest request = HttpUtil.createPost(url).timeout(15000);
        request.form(data);
        request.header("Referer", referer);
        HttpResponse response = request.execute();
        return extractSogouResult(WebHelper.getSafeHtml(response));
    }

    private static String extractSogouResult(String html) {
        if (StrUtil.isBlank(html)) {
            return "";
        }
        JSONObject jsonObject = JSONUtil.parseObj(html);
        if (jsonObject.getInt("success", 0) != 1) {
            return "";
        }
        JSONArray jsonArray = jsonObject.getJSONArray("result");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject frame = jsonArray.getJSONObject(i);
            sb.append(frame.getStr("content"));
        }
        return sb.toString();
    }

    private static String postMultiData(String url, byte[] data, String boundary) {
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
            return WebHelper.getSafeHtml(response);
        } catch (Exception ex) {
            StaticLog.error(ex);
            return null;
        }
    }

    private static byte[] mergeByte(byte[]... bytes) {
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
}
