package com.wilddog.conversation.utils;

import android.content.Context;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by fly on 17-8-23.
 */

public class WXUtil {
    private static IWXAPI iwxapi;

    public static void initWeixin(Context context){
        iwxapi = WXAPIFactory.createWXAPI(context, Constant.WX_APP_ID);
        iwxapi.registerApp(Constant.WX_APP_ID);
    }

    public static IWXAPI getIwxapi(){
        return iwxapi;
    }

}
