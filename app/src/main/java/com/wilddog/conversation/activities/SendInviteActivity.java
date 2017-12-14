package com.wilddog.conversation.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.ValueEventListener;
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.wilddog.WilddogSyncManager;
import com.wilddog.conversation.wilddog.WilddogVideoManager;

import java.util.Map;

public class SendInviteActivity extends AppCompatActivity {
    private TextView tvCancel;
    private Button btnSendInvite;
    private EditText etUid;
    private UserInfo remoteUserInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_invite);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        btnSendInvite = (Button) findViewById(R.id.btn_create_convercation);
        etUid = (EditText) findViewById(R.id.et_invite_user_id);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSendInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uid = etUid.getText().toString().trim();
                if(uid==null || uid.equals("")){
                    AlertMessageUtil.showShortToast("邀请人的Uid不能为空");
                }else {
                    WilddogSyncManager.getWilddogSyncTool().getonlineUserInfos(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot==null || dataSnapshot.getValue()==null){return;}
                            Map map = (Map) dataSnapshot.getValue();
                            if(map.containsKey(uid)){
                               Map subMap = (Map) map.get(uid);
                                remoteUserInfo = new UserInfo();
                                String strFaceurl = subMap.get("faceurl")==null?"https://img.wdstatic.cn/imdemo/1.png":subMap.get("faceurl").toString();
                                remoteUserInfo.setFaceurl(strFaceurl);
                                String strNickname = subMap.get("nickname")==null?uid:subMap.get("nickname").toString();
                                remoteUserInfo.setNickname(strNickname);
                                remoteUserInfo.setUid(uid);
                                remoteUserInfo.setDeviceid(subMap.get("deviceid").toString());
                                gotoCallingActivity(remoteUserInfo);
                            }else {
                                Toast.makeText(SendInviteActivity.this,"你呼叫的用户不在线或者不存在",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(SyncError syncError) {

                        }
                    });
                }

            }
        });

    }

    private void gotoCallingActivity(UserInfo info) {
        WilddogVideoManager.setWilddogUser(info);
        Intent intent = new Intent(SendInviteActivity.this, CallingActivity.class);
        startActivity(intent);
        finish();
    }
}
