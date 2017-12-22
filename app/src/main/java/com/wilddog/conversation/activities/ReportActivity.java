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
import com.wilddog.conversation.utils.ImageLoadingUtil;
import com.wilddog.conversation.view.CircleImageView;

public class ReportActivity extends AppCompatActivity {

    private TextView tvNickname;
    private CircleImageView civHeadImage;
    private EditText etReport;
    private TextView tvHint;
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
        tvNickname.setText(user.getNickname());
        ImageLoadingUtil.Load(user.getFaceurl(), civHeadImage);
    }


    private void initView() {
        tvNickname = (TextView) findViewById(R.id.tv_nickname);
        civHeadImage = (CircleImageView) findViewById(R.id.civ_photo);
        etReport = (EditText) findViewById(R.id.et_report);
        tvHint = (TextView) findViewById(R.id.tv_hint);
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
        etReport.addTextChangedListener(new TextWatcher() {
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
                tvHint.setText("还可以输入"+ restLength +"个字");
            }
        });
    }
}
