package com.luooqi.ocr.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.luooqi.ocr.model.TextBlock;

import java.util.*;

/**
 * tools-ocr
 * Created by 何志龙 on 2019-03-22.
 */
public class OcrUtils {

    public static String sogouMobileOcr(byte[] imgData) {
        String boundary = "------WebKitFormBoundary8orYTmcj8BHvQpVU";
        String url = "http://ocr.shouji.sogou.com/v2/ocr/json";
        String header = boundary + "\r\nContent-Disposition: form-data; name=\"pic\"; filename=\"pic.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n";
        String footer = "\r\n" + boundary + "--\r\n";
        byte[] postData = CommUtils.mergeByte(header.getBytes(CharsetUtil.CHARSET_ISO_8859_1), imgData, footer.getBytes(CharsetUtil.CHARSET_ISO_8859_1));
        return extractSogouResult(CommUtils.postMultiData(url, postData, boundary.substring(2)));
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
        return extractSogouResult(WebUtils.getSafeHtml(response));
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
        List<TextBlock> textBlocks = new ArrayList<>();
        boolean isEng = false;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jObj = jsonArray.getJSONObject(i);
            TextBlock textBlock = new TextBlock();
            textBlock.setText(jObj.getStr("content").trim());
            //noinspection SuspiciousToArrayCall
            String[] frames = jObj.getJSONArray("frame").toArray(new String[0]);
            textBlock.setTopLeft(CommUtils.frameToPoint(frames[0]));
            textBlock.setTopRight(CommUtils.frameToPoint(frames[1]));
            textBlock.setBottomRight(CommUtils.frameToPoint(frames[2]));
            textBlock.setBottomLeft(CommUtils.frameToPoint(frames[3]));
            textBlocks.add(textBlock);
        }
        isEng = jsonObject.getStr("lang", "zh-Chs").equals("zh-Chs");
        return CommUtils.combineTextBlocks(textBlocks, isEng);
    }

}
