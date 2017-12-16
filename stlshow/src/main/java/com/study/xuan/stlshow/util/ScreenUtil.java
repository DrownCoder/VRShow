package com.study.xuan.stlshow.util;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

/**
 * Author : xuan.
 * Date : 2017/11/30.
 * Description :input the description of this file.
 */

public class ScreenUtil {
    /**
     * 获得屏幕宽高
     * 调用getWidth()，getHeight()
     */
    public static Display getWindowDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }
}
