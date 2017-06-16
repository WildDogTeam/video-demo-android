package com.wilddog.conversationdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by fly on 17-6-12.
 */

public class ConversationApplication extends Application{
    private static ConversationApplication application;

    public static ConversationApplication getInstance(){
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
    }

    public static Context getContext() {
        return application.getApplicationContext();
    }
}
