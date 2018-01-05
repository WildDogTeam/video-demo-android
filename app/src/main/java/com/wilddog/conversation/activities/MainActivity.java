package com.wilddog.conversation.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.ValueEventListener;
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.floatingwindow.WindowService;
import com.wilddog.conversation.fragments.FriendsFragment;
import com.wilddog.conversation.fragments.OnlineFragment;
import com.wilddog.conversation.fragments.SettingFragment;
import com.wilddog.conversation.holders.StreamsHolder;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.wilddog.WilddogSyncManager;
import com.wilddog.conversation.wilddog.WilddogVideoManager;
import com.wilddog.video.base.WilddogVideoError;
import com.wilddog.video.call.CallStatus;
import com.wilddog.video.call.Conversation;
import com.wilddog.video.call.RemoteStream;
import com.wilddog.video.call.WilddogVideoCall;
import com.wilddog.wilddogauth.WilddogAuth;

import java.util.Map;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private FragmentManager fragmentManager;
    private RadioGroup rgMain;
    private Fragment onlineFragment = new OnlineFragment();
    private Fragment settingFragment = new SettingFragment();
    private Fragment friendsFragment = new FriendsFragment();
    private boolean isCancel = true;
    private WilddogVideoCall video;
    private WindowService.MyBinder mybinder;
    private MyServiceConnection serviceConnection;
    private UserInfo remoteUserInfo;
    public class MyServiceConnection implements ServiceConnection {
        private boolean isbind = false;

        public boolean getIsbind() {
            return isbind;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mybinder = (WindowService.MyBinder) iBinder;
            isbind = true;
            StreamsHolder.setMyBinder(mybinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        serviceConnection = new MyServiceConnection();
        Intent service = new Intent(this, WindowService.class);
        bindService(service, serviceConnection, Service.BIND_AUTO_CREATE);
        initWilddogVideoCall();
        initView();
    }

    private void initWilddogVideoCall() {
        WilddogVideoCall.initialize(MainActivity.this, Constant.WILDDOG_VIDEO_APP_ID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
        video = WilddogVideoCall.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListener();
    }

    private void setListener() {
        video.setListener(listener);
    }

    private Conversation.Listener conversationListener = new Conversation.Listener() {
        @Override
        public void onCallResponse(CallStatus callStatus) {
            switch (callStatus) {
                case ACCEPTED:
                    isCancel = false;
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
        public void onStreamReceived(final RemoteStream remoteStream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StreamsHolder.setRemoteStream(remoteStream);
                    Intent intent = new Intent();
                    intent.setAction(Constant.UPDATE_VIEW);
                    sendBroadcast(intent);
                }
            });
        }

        @Override
        public void onClosed() {
            if (isCancel) {
                // 取消邀请。 TODO 关闭被叫界面
                AlertMessageUtil.showShortToast("对方已取消");
                // 发自定义广播，关闭界面回到主页
                Intent intent = new Intent();
                intent.setAction(Constant.INVITE_CANCEL);
                sendBroadcast(intent);
                isCancel = true;
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertMessageUtil.showShortToast("用户：" + WilddogVideoManager.getConversation().getRemoteUid() + "离开会话");
                    }
                });
            }
            release();

        }

        @Override
        public void onError(WilddogVideoError wilddogVideoError) {
            Log.e(TAG, wilddogVideoError.toString());
        }

    };

    private void release() {
        if (WilddogVideoManager.getConversation() != null) {
            WilddogVideoManager.getConversation().close();
        }
        if (StreamsHolder.getLocalStream() != null && !StreamsHolder.getLocalStream().isClosed()) {
            StreamsHolder.getLocalStream().close();
        }
    }
    private WilddogVideoCall.Listener listener =
            new WilddogVideoCall.Listener() {
                @Override
                public void onCalled(Conversation conversation, String s) {
                    final String uid = conversation.getRemoteUid();
                    conversation.setConversationListener(conversationListener);
                    WilddogVideoManager.saveConversation(conversation);
                    WilddogSyncManager.getWilddogSyncTool().getonlineUserInfos(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                                return;
                            }
                            Map map = (Map) dataSnapshot.getValue();
                            if (map.containsKey(uid)) {
                                Map subMap = (Map) map.get(uid);
                                remoteUserInfo = new UserInfo();
                                String strFaceurl = subMap.get("faceurl") == null ? "https://img.wdstatic.cn/imdemo/1.png" : subMap.get("faceurl").toString();
                                remoteUserInfo.setFaceurl(strFaceurl);
                                String strNickname = subMap.get("nickname") == null ? uid : subMap.get("nickname").toString();
                                remoteUserInfo.setNickname(strNickname);
                                remoteUserInfo.setUid(uid);
                                remoteUserInfo.setDeviceid(subMap.get("deviceid")==null ? UUID.randomUUID().toString():subMap.get("deviceid").toString());
                                gotoAcceptActivity(remoteUserInfo);
                            } else {
                                Toast.makeText(MainActivity.this, "呼叫者已经离线", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(SyncError syncError) {

                        }
                    });

                }

                @Override
                public void onTokenError(WilddogVideoError WilddogVideoCallError) {
                    Toast.makeText(MainActivity.this, "token 存在问题,退出登录", Toast.LENGTH_SHORT).show();
                    Log.e("tokenerror", WilddogVideoCallError.getMessage());
                    WilddogSyncManager.getWilddogSyncTool().removeUserInfo(SharedPreferenceTool.getUserId(MainActivity.this));
                    SharedPreferenceTool.setUserInfo(MainActivity.this, "");
                    SharedPreferenceTool.setLoginStatus(MainActivity.this, false);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            };

    private void gotoAcceptActivity(UserInfo info) {
        Intent intent = new Intent(MainActivity.this, AcceptActivity.class);
        intent.putExtra("user", info);
        startActivity(intent);
    }

    private void initView() {
        rgMain = (RadioGroup) findViewById(R.id.rg_main);
        rgMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_main_online:
                        changeFragment(onlineFragment);
                        break;
                    case R.id.rb_main_me:
                        changeFragment(settingFragment);
                        break;
                    case R.id.rb_main_call:
                        changeFragment(friendsFragment);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        //解绑服务
        if (serviceConnection != null && serviceConnection.getIsbind()) {
            unbindService(serviceConnection);
        }
    }
}
