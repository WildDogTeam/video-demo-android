package com.wilddog.conversation.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.ObjectAndStringTool;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.wilddog.WilddogSyncManager;

public class SplashActivity extends AppCompatActivity {
    private final int gotoLoginAcitivity = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case gotoLoginAcitivity:
                    if (SharedPreferenceTool.getLoginStatus(SplashActivity.this)) {
                        gotoMainActivity();
                    } else {
                        gotoLoginAcitivity();
                    }
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 自动跳转设置时间为2秒。

        handler.sendEmptyMessageDelayed(gotoLoginAcitivity, Constant.AUTO_SKIP_TIME);
    }

    private void gotoLoginAcitivity() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
    }

    private void gotoMainActivity() {
        WilddogSyncManager.getWilddogSyncTool().writeToUserInfo(ObjectAndStringTool.getObjectFromJson(SharedPreferenceTool.getUserInfo(SplashActivity.this), UserInfo.class));
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }

}
