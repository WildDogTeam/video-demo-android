package com.wilddog.demo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wilddog.demo.R;
import com.wilddog.demo.receiver.InviteCancelBroadcastReceiver;
import com.wilddog.demo.utils.Contants;
import com.wilddog.demo.wilddogAuth.WilddogVideoManager;
import com.wilddog.video.IncomingInvite;

public class AcceptActivity extends AppCompatActivity {
    private final String TAG = AcceptActivity.class.getName();

    private String uid;
    private TextView tvUid;

    private LinearLayout llReject;
    private LinearLayout llAccept;

    private IncomingInvite incomingInvite;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);

        tvUid = (TextView) findViewById(R.id.tv_uid);
         llAccept= (LinearLayout) findViewById(R.id.ll_accept);
        llReject = (LinearLayout) findViewById(R.id.ll_reject);

        uid = getIntent().getStringExtra("fromUid");
        incomingInvite = WilddogVideoManager.getIncomingInvite();
        tvUid.setText(uid);

        broadcastReceiver = new InviteCancelBroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if(intent.getAction().equals(Contants.INVITE_CANCEL)){
                    finish();
                }
            }
        };

        llAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 接受 进入通话页

                Intent intent = new Intent(AcceptActivity.this,ConversationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        llReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拒绝
                incomingInvite.reject();
                incomingInvite.getStatus();
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter= new IntentFilter(Contants.INVITE_CANCEL);
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
