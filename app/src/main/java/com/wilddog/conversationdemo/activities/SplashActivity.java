package com.wilddog.conversationdemo.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wilddog.conversationdemo.R;
import com.wilddog.conversationdemo.utils.Contants;

public class SplashActivity extends AppCompatActivity {
    private final int gotoLoginAcitivity=0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case gotoLoginAcitivity:
                    gotoLoginAcitivity();
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
        handler.sendEmptyMessageDelayed(gotoLoginAcitivity, Contants.AUTO_SKIP_TIME);
    }

    private void gotoLoginAcitivity(){
    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
    }
}
