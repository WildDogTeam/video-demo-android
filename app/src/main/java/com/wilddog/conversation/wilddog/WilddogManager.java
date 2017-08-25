package com.wilddog.conversation.wilddog;

import android.content.Context;

import com.wilddog.conversation.utils.Constant;


import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by fly on 17-6-9.
 */

public class WilddogManager {
    private static boolean isInit = false;
    public static void initWilddog(Context appContext){
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl("http://" + Constant.WILDDOG_VIDEO_APP_ID + ".wilddogio.com");
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(appContext, options);
        isInit = true;
    }

    public static boolean IsInit(){
        return isInit;
    }

}
