package com.wilddog.demo.utils;

import android.os.Environment;

/**
 * Created by fly on 17-6-9.
 */

public final class Contants {
    public static final int AUTO_SKIP_TIME = 2000;
    public static final String APP_ID = <Your Video APPID>;
    public static final String INVITE_CANCEL = "com.wilddog.conversation.inviteCancel";
    //TuSDK key
    public static final String TUSDK_KEY = "";

    //Camera360 key
    public static final String SDK_KEY_NEW = "";
    //录制视频存储路径
    public static String filePath = Environment.getExternalStorageDirectory().getPath()+"/wilddogConversaton/";
}
