package com.wilddog.conversation.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wilddog.conversation.ConversationApplication;
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.receiver.InvitationCanceledBroadcastReceiver;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.ImageLoadingUtil;
import com.wilddog.conversation.utils.ParamsStore;
import com.wilddog.conversation.utils.RingUtil;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogVideoManager;
import com.wilddog.video.call.Conversation;

public class AcceptActivity extends AppCompatActivity {
    private final String TAG = AcceptActivity.class.getName();

    private UserInfo remoteUserInfo;
    private TextView tvNickname;

    private LinearLayout llReject;
    private LinearLayout llAccept;
    private Conversation conversation;
    private CircleImageView civPhotoUrl;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);
        tvNickname = (TextView) findViewById(R.id.tv_nickname);
        llAccept = (LinearLayout) findViewById(R.id.ll_accept);
        llReject = (LinearLayout) findViewById(R.id.ll_reject);
        civPhotoUrl = (CircleImageView) findViewById(R.id.civ_photo);
        remoteUserInfo = (UserInfo) getIntent().getSerializableExtra("user");
        conversation = WilddogVideoManager.getConversation();
        tvNickname.setText(remoteUserInfo.getNickname());
        ImageLoadingUtil.Load(remoteUserInfo.getFaceurl(), civPhotoUrl);
        broadcastReceiver = new InvitationCanceledBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if (intent.getAction().equals(Constant.INVITE_CANCEL)) {
                    finish();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(Constant.INVITE_CANCEL);
        registerReceiver(broadcastReceiver, intentFilter);
        llAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 接受 进入通话页
                WilddogVideoManager.setWilddogUser(remoteUserInfo);
                ParamsStore.isInitiativeCall = false;
                Intent intent = new Intent(AcceptActivity.this, ConversationActivity.class);
                startActivity(intent);
                finish();
            }
        });
        llReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拒绝
                conversation.reject();
                finish();
            }
        });
        startRing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if (RingUtil.isRing) {
            RingUtil.stop();
        }

    }

    private void startRing() {
        RingUtil.paly(false, ConversationApplication.getContext().getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
