package com.wilddog.conversation.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.BlackUser;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.utils.MyOpenHelper;
import com.wilddog.conversation.utils.SharedPereferenceTool;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogVideoManager;

public class DetailInfoActivity extends AppCompatActivity {

    private TextView nick;
    private CircleImageView headImage;
    private UserInfo user;
    private Button calling;
    private TextView uid;
    private LinearLayout blackTV;
    private LinearLayout report;
    private LinearLayout llParent;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_info);
//WilddogVideoManager.setWilddogUser(info);
        user = (UserInfo) getIntent().getSerializableExtra("user");

        initView();

        setData();
    }

    private void setData() {
        nick.setText(user.getNickname());
        ImageManager.Load(user.getFaceurl(), headImage);
        uid.setText(user.getUid());
    }

    private void initView() {
        llParent = (LinearLayout) findViewById(R.id.ll_parent);
        nick = (TextView) findViewById(R.id.tv_nickName);
        headImage = (CircleImageView) findViewById(R.id.civ_photo);
        uid = (TextView) findViewById(R.id.tv_uid);
        calling = (Button) findViewById(R.id.btn_call);
        blackTV = (LinearLayout) findViewById(R.id.tv_blacklist);
        report = (LinearLayout) findViewById(R.id.tv_report);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        calling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WilddogVideoManager.setWilddogUser(user);
                Intent intent = new Intent(DetailInfoActivity.this, CallingActivity.class);
                startActivity(intent);
            }
        });

        blackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPupWindowView();
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DetailInfoActivity.this,ReportActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });
    }

    private void showPupWindowView() {
        View view = View.inflate(this, R.layout.popupwindow_blacklist, null);
        TextView addBlack = (TextView) view.findViewById(R.id.add_blacklist);
        TextView cancel = (TextView) view.findViewById(R.id.tv_cancel);

        addBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mid = SharedPereferenceTool.getUserId(DetailInfoActivity.this);
                BlackUser blackUser=new BlackUser();
                blackUser.setLocalId(mid);
                blackUser.setNickName(user.getNickname());
                blackUser.setRemoteId(user.getUid());
                blackUser.setTimeStamp(System.currentTimeMillis()+"");
                blackUser.setFaceurl(user.getFaceurl());
                boolean isAdded = MyOpenHelper.getInstance().insertBlackList(blackUser);
                if(isAdded) {
                    MyOpenHelper.getInstance().deleteConversationRecord(mid, user.getUid());
                    AlertMessageUtil.showShortToast("加入黑名单成功");
                    popupWindowDismiss();
                    finish();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowDismiss();
            }
        });
        showPopupWindow(view);
    }

    private void showPopupWindow(View view) {
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(llParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void popupWindowDismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}
