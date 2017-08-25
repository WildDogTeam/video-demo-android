package com.wilddog.conversation.activities;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.ValueEventListener;
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.fragments.OnlineFragment;
import com.wilddog.conversation.fragments.MeFragment;
import com.wilddog.conversation.fragments.CallFragment;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.wilddog.WilddogSyncManager;
import com.wilddog.conversation.wilddog.WilddogVideoManager;
import com.wilddog.video.CallStatus;
import com.wilddog.video.Conversation;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.wilddog.wilddogauth.WilddogAuth;

import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private RadioButton online;
    private RadioButton me;
    private RadioButton call;
    private FragmentManager fragmentManager;
    private RadioGroup rgMain;
    private Fragment onlineFragment = new OnlineFragment();
    private Fragment meFragment = new MeFragment();
    private Fragment callFragment = new CallFragment();
    private boolean iscancel =true;
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
        WilddogVideo.initialize(MainActivity.this, Constant.WILDDOG_VIDEO_APP_ID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
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


    private Conversation.Listener conversationListener = new Conversation.Listener() {
        @Override
        public void onCallResponse(CallStatus callStatus) {
            switch (callStatus){
                case ACCEPTED:
                    iscancel =false;
                    break;
                case REJECTED:
                    break;
                case BUSY:
                    break;
                case TIMEOUT:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStreamReceived(RemoteStream remoteStream) {

        }

        @Override
        public void onClosed() {
            if(iscancel){
                // 取消邀请。 TODO 关闭被叫界面
                AlertMessageUtil.showShortToast("对方已取消");
                // 发自定义广播，关闭界面回到主页
                Intent intent = new Intent();
                intent.setAction(Constant.INVITE_CANCEL);
                sendBroadcast(intent);
                iscancel =true;
            }

        }

        @Override
        public void onError(WilddogVideoError wilddogVideoError) {
            Log.e(TAG,wilddogVideoError.toString());
        }
    };

    private UserInfo remoteUserInfo;

    private WilddogVideo.Listener listener =
            new WilddogVideo.Listener() {
                @Override
                public void onCalled(Conversation conversation, String s) {
                    final String uid = conversation.getRemoteUid();
                    conversation.setConversationListener(conversationListener);
                    WilddogVideoManager.saveConversation(conversation);
                    WilddogSyncManager.getWilddogSyncTool().getonlineUserInfos(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot==null || dataSnapshot.getValue()==null){return;}
                            Map map = (Map) dataSnapshot.getValue();
                            if(map.containsKey(uid)){
                                Map subMap = (Map) map.get(uid);
                                remoteUserInfo = new UserInfo();
                                remoteUserInfo.setPhotoUrl(subMap.get("photoUrl").toString());
                                remoteUserInfo.setUid(subMap.get("uid").toString());
                                remoteUserInfo.setNickName(subMap.get("nickName").toString());
                                gotoAcceptActivity(remoteUserInfo);
                            }else {
                                Toast.makeText(MainActivity.this,"呼叫者已经离线",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(SyncError syncError) {

                        }
                    });

                }

                @Override
                public void onTokenError(WilddogVideoError wilddogVideoError) {

                }
            };

            private void gotoAcceptActivity(UserInfo info){
                Intent intent = new Intent(MainActivity.this,AcceptActivity.class);
                intent.putExtra("user",info);
                startActivity(intent);
            }

    private void initView() {
        rgMain = (RadioGroup) findViewById(R.id.rg_main);
        online = (RadioButton) findViewById(R.id.rb_main_online);
        call = (RadioButton) findViewById(R.id.rb_main_call);
        me = (RadioButton) findViewById(R.id.rb_main_me);
        rgMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_main_online:
                        changeFragment(onlineFragment);
                        break;
                    case R.id.rb_main_me:
                        changeFragment(meFragment);
                        break;
                    case R.id.rb_main_call:
                        changeFragment(callFragment);
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
