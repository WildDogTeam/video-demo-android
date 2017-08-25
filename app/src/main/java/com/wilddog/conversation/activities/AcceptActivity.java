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
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.receiver.InviteCancelBroadcastReceiver;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogVideoManager;
import com.wilddog.video.Conversation;

public class AcceptActivity extends AppCompatActivity {
    private final String TAG = AcceptActivity.class.getName();

    private UserInfo remoteUserInfo;
    private TextView tvNickname;

    private LinearLayout llReject;
    private LinearLayout llAccept;
    private Conversation mConversation;
    private CircleImageView civPhotoUrl;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);

        tvNickname = (TextView) findViewById(R.id.tv_nickname);
         llAccept= (LinearLayout) findViewById(R.id.ll_accept);
        llReject = (LinearLayout) findViewById(R.id.ll_reject);
        civPhotoUrl = (CircleImageView) findViewById(R.id.civ_photo);


        remoteUserInfo = (UserInfo) getIntent().getSerializableExtra("user");
        mConversation = WilddogVideoManager.getConversation();
        tvNickname.setText(remoteUserInfo.getNickName());
        ImageManager.Load(remoteUserInfo.getPhotoUrl(),civPhotoUrl);
        broadcastReceiver = new InviteCancelBroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if(intent.getAction().equals(Constant.INVITE_CANCEL)){
                    finish();
                }
            }
        };

        llAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 接受 进入通话页

                Intent intent = new Intent(AcceptActivity.this,ConversationActivity.class);
                intent.putExtra("user",remoteUserInfo);
                startActivity(intent);
                finish();
            }
        });

        llReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拒绝
                mConversation.reject();
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter= new IntentFilter(Constant.INVITE_CANCEL);
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
