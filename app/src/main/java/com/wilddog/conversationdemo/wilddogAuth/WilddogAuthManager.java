package com.wilddog.conversationdemo.wilddogAuth;


import android.content.Context;

import com.wilddog.wilddogauth.WilddogAuth;

/**
 * Created by fly on 17-6-9.
 */

public class WilddogAuthManager {
    private static WilddogAuth wilddogAuth;
    private static boolean isInit = false;
    private  static void init(Context context){
        WilddogManager.initWilddog(context);
        isInit = true;
    }

    public static WilddogAuth getWilddogAuth(Context context){
        if(!isInit){
            init(context);
        }
        if(wilddogAuth==null){
        wilddogAuth = WilddogAuth.getInstance();}
        return wilddogAuth;
    }
}
