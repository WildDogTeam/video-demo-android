package com.wilddog.conversationdemo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wilddog.conversationdemo.R;
import com.wilddog.conversationdemo.utils.AlertMessageUtil;

public class SendInviteActivity extends AppCompatActivity {
    private TextView tvCancel;
    private Button btnSendInvite;
    private EditText etUid;
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
                String uid = etUid.getText().toString().trim();
                if(uid==null || uid.equals("")){
                    AlertMessageUtil.showShortToast("邀请人的Uid不能为空");
                }else {
                    gotoCallingActivity(uid);
                }

            }
        });

    }

    private void gotoCallingActivity(String uid) {
        Intent intent = new Intent(SendInviteActivity.this, CallingActivity.class);
        intent.putExtra("inviteUid",uid);
        startActivity(intent);
        finish();
    }

}
