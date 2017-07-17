package com.wilddog.conversationdemo.wilddogAuth;

import android.content.Context;

import com.wilddog.conversationdemo.utils.Contants;


import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by fly on 17-6-9.
 */

public class WilddogManager {
    public static void initWilddog(Context appContext){
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl("http://" + Contants.APP_ID + ".wilddogio.com");
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(appContext, options);
    }
}
