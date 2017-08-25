package com.wilddog.conversation.utils;

import android.os.Environment;
import android.provider.MediaStore;

/**
 * Created by fly on 17-6-9.
 */

public final class Constant {
    public static final int AUTO_SKIP_TIME = 2000;
    public static final String WILDDOG_VIDEO_APP_ID = <Your MediaStore.Video AppID>;
    public static final String INVITE_CANCEL = "com.wilddog.conversation.inviteCancel";

    // 微信appId
    public static final String WX_APP_ID = "wxbe69b0b88cf233b7";
    public static final String WX_APP_SECRECT = "a08bc6844b8334678a6b78f36a1a6c7b";

    //TuSDK key
    public static final String TUSDK_KEY = "";

    //Camera360 key
    public static final String SDK_KEY_NEW = "";

    //录制视频存储路径
    public static  String filePath = Environment.getExternalStorageDirectory().getPath()+"/wilddogConversaton/";

    public static boolean isLoginClickable = true;
}
