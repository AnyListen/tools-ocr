package com.luooqi.ocr.config;

import com.luooqi.ocr.utils.GlobalKeyListener;
import com.luooqi.ocr.utils.VoidDispatchService;
import org.jnativehook.GlobalScreen;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by litonglinux@qq.com on 10/11/2023_12:53 AM
 */
public class InitConfig {

  public static void initKeyHook() {
    try {
      Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.WARNING);
      logger.setUseParentHandlers(false);
      GlobalScreen.setEventDispatcher(new VoidDispatchService());
      GlobalScreen.registerNativeHook();
      GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
