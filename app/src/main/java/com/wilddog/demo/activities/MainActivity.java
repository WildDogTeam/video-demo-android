package com.wilddog.demo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.wilddog.demo.R;
import com.wilddog.demo.fragments.OnlineFragment;
import com.wilddog.demo.fragments.SettingFragment;
import com.wilddog.demo.utils.AlertMessageUtil;
import com.wilddog.demo.utils.Contants;
import com.wilddog.demo.wilddogAuth.WilddogVideoManager;
import com.wilddog.video.CallStatus;
import com.wilddog.video.Conversation;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.wilddog.wilddogauth.WilddogAuth;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private RadioButton online;
    private RadioButton settings;
    private FragmentManager fragmentManager;
    private RadioGroup rgMain;
    private Fragment onlineFragment = new OnlineFragment();
    private Fragment settingFragment = new SettingFragment();
    private WilddogVideo video;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        initWilddogVideo();
        initView();
    }

    private void initWilddogVideo() {
        WilddogVideo.initialize(MainActivity.this, Contants.APP_ID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
        video = WilddogVideo.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListener();
    }

    private void setListener(){
        video.setListener(listener);
    }

    private WilddogVideo.Listener listener = new WilddogVideo.Listener() {
        @Override
        public void onCalled(Conversation conversation, String s) {
            String uid = conversation.getRemoteUid();
            WilddogVideoManager.saveConversation(conversation);
            conversation.setConversationListener(new Conversation.Listener() {
                @Override
                public void onCallResponse(CallStatus callStatus) {
                }

                @Override
                public void onStreamReceived(RemoteStream remoteStream) {

                }

                @Override
                public void onClosed() {

                    AlertMessageUtil.showShortToast("对方已取消");
                    // 发自定义广播，关闭界面回到主页
                    Intent intent = new Intent();
                    intent.setAction(Contants.INVITE_CANCEL);
                    sendBroadcast(intent);
                }

                @Override
                public void onError(WilddogVideoError wilddogVideoError) {

                }
            });
            Intent intent = new Intent(MainActivity.this,AcceptActivity.class);
            intent.putExtra("fromUid",uid);
            startActivity(intent);
        }

        @Override
        public void onTokenError(WilddogVideoError wilddogVideoError) {
            AlertMessageUtil.showShortToast("token 出错,请查看详细日志");
            Log.e("error",wilddogVideoError.toString());
        }
    };

    private void initView() {
        rgMain = (RadioGroup) findViewById(R.id.rg_main);
        online = (RadioButton) findViewById(R.id.rb_main_online);
        settings = (RadioButton) findViewById(R.id.rb_main_settings);
        rgMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_main_online:
                        changeFragment(onlineFragment);
                        break;
                    case R.id.rb_main_settings:
                        changeFragment(settingFragment);
                        break;
                    default:
                        break;

                }
            }
        });
        rgMain.check(R.id.rb_main_online);
    }

    private void changeFragment(Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.fl_content, fragment).commit();
    }


}
