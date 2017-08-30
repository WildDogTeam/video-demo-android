package com.wilddog.conversation.utils;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by fly on 17-8-29.
 */

public class VibratorUtil {
     private static Vibrator vibrator;
    public static void start(Context context){
        if(vibrator==null){
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        long [] pattern = {100,400,100,400};
        vibrator.vibrate(pattern,0);
    }

    public static void stop(){
        if(vibrator!=null){
            vibrator.cancel();
        }
    }
}
