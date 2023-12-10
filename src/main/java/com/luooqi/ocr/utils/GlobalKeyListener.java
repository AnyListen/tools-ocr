package com.luooqi.ocr.utils;

import cn.hutool.log.StaticLog;
import com.luooqi.ocr.OcrApp;
import com.luooqi.ocr.snap.ScreenCapture;
import com.luooqi.ocr.windows.MainForm;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.lang.reflect.Field;

public class GlobalKeyListener implements NativeKeyListener {
  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    if (e.getKeyCode() == NativeKeyEvent.VC_F4) {
      preventEvent(e);
      MainForm.screenShotOcr();
    } else if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
      if (ScreenCapture.isSnapping) {
        preventEvent(e);
        MainForm.cancelSnap();
      }
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent e) {
//        if (e.getKeyCode() == NativeKeyEvent.VC_F4){
//            preventEvent(e);
//        }
//        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
  }

  private void preventEvent(NativeKeyEvent e) {
    try {
      Field f = NativeInputEvent.class.getDeclaredField("reserved");
      f.setAccessible(true);
      f.setShort(e, (short) 0x01);
    } catch (Exception ex) {
      StaticLog.error(ex);
    }
  }
}
