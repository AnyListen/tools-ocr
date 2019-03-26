package com.luooqi.ocr.utils;

import com.luooqi.ocr.MainFm;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListener implements NativeKeyListener {
    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F4){
            MainFm.doSnap();
        }
        else if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE){
            MainFm.cancelSnap();
        }
        else if (e.getKeyCode() == NativeKeyEvent.VC_F6){
            MainFm.cancelSnap();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }
}
