package com.wilddog.conversation.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.view.CircleImageView;

public class ReportActivity extends AppCompatActivity {

    private TextView nickName;
    private CircleImageView headImage;
    private EditText report;
    private TextView hint;
    private UserInfo user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        user = (UserInfo) getIntent().getSerializableExtra("user");

        initView();

        setData();

    }

    private void setData() {
        nickName.setText(user.getNickname());
        ImageManager.Load(user.getFaceurl(), headImage);
    }


    private void initView() {
        nickName = (TextView) findViewById(R.id.tv_nickName);
        headImage = (CircleImageView) findViewById(R.id.civ_photo);
        report = (EditText) findViewById(R.id.et_report);
        hint = (TextView) findViewById(R.id.tv_hint);
        findViewById(R.id.tv_commit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        report.addTextChangedListener(new TextWatcher() {
            public int MAX_LENGTH=200;
            public int restLength=MAX_LENGTH;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(restLength >= 0){
                    restLength = MAX_LENGTH - s.toString().length();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(restLength<0){
                    restLength=0;
                    s.delete(MAX_LENGTH,s.length());
                }
                hint.setText("还可以输入"+ restLength +"个字");
            }
        });
    }
}
