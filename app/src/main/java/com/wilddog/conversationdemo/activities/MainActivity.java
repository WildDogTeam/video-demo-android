package com.wilddog.conversationdemo.activities;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.wilddog.conversationdemo.R;
import com.wilddog.conversationdemo.fragments.OnlineFragment;
import com.wilddog.conversationdemo.fragments.SettingFragment;
import com.wilddog.conversationdemo.utils.AlertMessageUtil;
import com.wilddog.conversationdemo.utils.Contants;
import com.wilddog.conversationdemo.wilddogAuth.WilddogVideoManager;
import com.wilddog.video.IncomingInvite;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoClient;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private RadioButton online;
    private RadioButton settings;
    private FragmentManager fragmentManager;
    private RadioGroup rgMain;
    private Fragment onlineFragment = new OnlineFragment();
    private Fragment settingFragment = new SettingFragment();

    private WilddogVideoClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        initWilddogVideo();
        initView();
    }

    private void initWilddogVideo() {
        WilddogVideo.initializeWilddogVideo(MainActivity.this, Contants.APP_ID);
        client = WilddogVideo.getInstance().getClient();
        client.setInviteListener(new WilddogVideoClient.Listener() {
            @Override
            public void onIncomingInvite(WilddogVideoClient wilddogVideoClient, IncomingInvite incomingInvite) {
                // 有邀请过来，打开被叫界面
                String uid = incomingInvite.getFromParticipantId();
                Intent intent = new Intent(MainActivity.this,AcceptActivity.class);
                intent.putExtra("fromUid",uid);
                WilddogVideoManager.saveIncomingInvite(incomingInvite);
                startActivity(intent);

            }

            @Override
            public void onIncomingInviteCanceled(WilddogVideoClient wilddogVideoClient, IncomingInvite incomingInvite) {
                Log.d(TAG,"IncomingInviteCanceled");
                // 取消邀请。 TODO 关闭被叫界面
                AlertMessageUtil.showShortToast("对方已取消");
                // 发自定义广播，关闭界面回到主页
                Intent intent = new Intent();
                intent.setAction(Contants.INVITE_CANCEL);
                sendBroadcast(intent);
            }
        });
    }

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
