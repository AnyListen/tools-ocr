package com.luooqi.ocr.utils;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;

import java.util.Hashtable;
import java.util.Map;

/**
 * fish-web
 * Created by 何志龙 on 2018-03-25.
 */
@SuppressWarnings("SpellCheckingInspection")
public class WebUtils {

  static {
    HttpRequest.closeCookie();
  }

  public static String getSafeHtml(HttpResponse response) {
    if (response == null) {
      return "";
    }
    return response.body();
  }

  public static String getHtml(String url) {
    HttpResponse response = get(url);
    String html = getSafeHtml(response);
    if (response != null) {
      response.close();
    }
    return html;
  }

  public static HttpResponse get(String url) {
    return get(url, 0, null, true);
  }

  public static String getLocation(String url, String cookie) {
    try {
      HttpResponse response = get(url, 0, new Hashtable<String, String>() {{
        put("Cookie", cookie);
      }}, false);
      if (response == null) {
        return url;
      }
      String location = response.header(Header.LOCATION);
      response.close();
      return location;
    } catch (Exception ex) {
      return "";
    }
  }

  public static HttpResponse get(String url, String cookie) {
    return get(url, 0, new Hashtable<String, String>() {{
      put("Cookie", cookie);
    }}, true);
  }

  public static HttpResponse get(String url, int userAgent, String cookie) {
    return get(url, userAgent, new Hashtable<String, String>() {{
      put("Cookie", cookie);
    }}, true);
  }

  public static HttpResponse get(String url, int userAgent, Map<String, String> headers) {
    return get(url, userAgent, headers, true);
  }

  public static HttpResponse get(String url, int userAgent, Map<String, String> headers, boolean allowRedirct) {
    try {
      HttpRequest request = HttpUtil.createGet(url).timeout(10000).setFollowRedirects(allowRedirct);
      if (headers == null) {
        headers = new Hashtable<>();
      }
      switch (userAgent) {
        case 1:
          headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13F69 MicroMessenger/6.3.16 NetType/WIFI Language/zh_CN");
          break;
        case 2:
          headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-gb; GT-P1000 Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
          break;
        case 3:
          headers.put("User-Agent", "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; NOKIA; Lumia 930) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/13.10586");
          break;
        case 4:
          headers.put("User-Agent", "NativeHost");
          break;
        case 5:
          headers.put("User-Agent", "Dalvik/1.6.0 (Linux; U; Android 4.4.2; NoxW Build/KOT49H) ITV_5.7.1.46583");
          break;
        case 6:
          headers.put("User-Agent", "qqlive");
          break;
        case 7:
          headers.put("User-Agent", "Dalvik/1.6.0 (Linux; U; Android 4.2.2; 6S Build/JDQ39E)");
          break;
        case 8:
          headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) XIAMI-MUSIC/3.0.9 Chrome/56.0.2924.87 Electron/1.6.11 Safari/537.36");
          break;
        case 9:
          headers.put("User-Agent", "okhttp/2.7.5");
          break;
        case 10:
          headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; oppo r11 plus Build/LMY48Z) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/39.0.0.0 Mobile Safari/537.36 SogouSearch Android1.0 version3.0");
          break;
        default:
          headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
          break;
      }
      request.addHeaders(headers);
      return request.execute();
    } catch (Exception ex) {
      StaticLog.error(ex);
      return null;
    }
  }

  public static HttpResponse postRaw(String url, String data) {
    return postRaw(url, data, 0, null);
  }

  public static HttpResponse postRaw(String url, String data, int userAgent, Map<String, String> headers) {
    return postData(url, new Hashtable<String, Object>() {{
      put("FORM", data);
    }}, 2, userAgent, headers);
  }

  public static HttpResponse postJson(String url, String data, int userAgent, Map<String, String> headers) {
    return postData(url, new Hashtable<String, Object>() {{
      put("JSON", data);
    }}, 1, userAgent, headers);
  }

  public static HttpResponse postForm(String url, Map<String, Object> data, int userAgent, Map<String, String> headers) {
    return postData(url, data, 0, userAgent, headers);
  }

  private static HttpResponse postData(String url, Map<String, Object> data, int contentType, int userAgent, Map<String, String> headers) {
    try {
      HttpRequest request = HttpUtil.createPost(url).timeout(10000);
      if (contentType == 0) {
        request.contentType("application/x-www-form-urlencoded");
        request.form(data);
      } else if (contentType == 1) {
        request.body(data.values().iterator().next().toString(), "application/json;charset=UTF-8");
      } else {
        request.contentType("application/x-www-form-urlencoded");
        request.body(data.values().iterator().next().toString());
      }
      if (headers == null) {
        headers = new Hashtable<>();
      }
      switch (userAgent) {
        case 1:
          headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3");
          break;
        case 2:
          headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19");
          break;
        case 3:
          headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 920)");
          break;
        case 4:
          headers.put("User-Agent", "NativeHost");
          break;
        case 5:
          headers.put("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)");
          break;
        case 6:
          headers.put("User-Agent", "Mozilla/5.0 (iPad; CPU OS 8_1_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12B466 Safari/600.1.4");
          break;
        case 7:
          headers.put("User-Agent", "okhttp/2.7.5");
          break;
        case 10:
          headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; oppo r11 plus Build/LMY48Z) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/39.0.0.0 Mobile Safari/537.36 SogouSearch Android1.0 version3.0");
          break;
        default:
          headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
          break;
      }
      request.addHeaders(headers);
      return request.execute();
    } catch (Exception ex) {
      StaticLog.error(ex);
      return null;
    }
  }
}
