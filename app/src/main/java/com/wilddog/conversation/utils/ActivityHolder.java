package com.wilddog.conversation.utils;

import android.app.Activity;

/**
 * Created by fly on 17-8-24.
 */

public class ActivityHolder {
    private static Activity sActivity;
    public static void setActivity(Activity activity){
     sActivity = activity;
    }

    public static void finish(){
        sActivity.finish();
        sActivity = null ;
    }

}
