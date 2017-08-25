package com.wilddog.conversation.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by fly on 17-8-17.
 */

public class CommonUtil {
    private static float sDensity = 0;
    /**
     * DP转换为像素
     *
     * @param context
     * @param nDip
     * @return
     */
    public static int dipToPixel(Context context, int nDip) {
        if (sDensity == 0) {
            final WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            sDensity = dm.density;
        }
        return (int) (sDensity * nDip);
    }
}
