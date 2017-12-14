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
import com.wilddog.conversation.bean.BlackListUser;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.utils.MyOpenHelper;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogVideoManager;

public class DetailInfoActivity extends AppCompatActivity {

    private TextView tvNickname;
    private CircleImageView civHeadImage;
    private UserInfo user;
    private Button btnCall;
    private TextView tvUid;
    private LinearLayout llBlackList;
    private LinearLayout tvReport;
    private LinearLayout llParent;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_info);
        user = (UserInfo) getIntent().getSerializableExtra("user");
        initView();
        setData();
    }

    private void setData() {
        tvNickname.setText(user.getNickname());
        ImageManager.Load(user.getFaceurl(), civHeadImage);
        tvUid.setText("ID:"+user.getUid());
    }

    private void initView() {
        llParent = (LinearLayout) findViewById(R.id.ll_parent);
        tvNickname = (TextView) findViewById(R.id.tv_nickname);
        civHeadImage = (CircleImageView) findViewById(R.id.civ_photo);
        tvUid = (TextView) findViewById(R.id.tv_uid);
        btnCall = (Button) findViewById(R.id.btn_call);
        llBlackList = (LinearLayout) findViewById(R.id.ll_blacklist);
        tvReport = (LinearLayout) findViewById(R.id.tv_report);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WilddogVideoManager.setWilddogUser(user);
                Intent intent = new Intent(DetailInfoActivity.this, CallingActivity.class);
                startActivity(intent);
            }
        });

        llBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPupWindowView();
            }
        });
        tvReport.setOnClickListener(new View.OnClickListener() {
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
        TextView tvAddBlacklist = (TextView) view.findViewById(R.id.add_blacklist);
        TextView tvCancel = (TextView) view.findViewById(R.id.tv_cancel);

        tvAddBlacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mid = SharedPreferenceTool.getUserId(DetailInfoActivity.this);
                BlackListUser blackListUser =new BlackListUser();
                blackListUser.setLocalId(mid);
                blackListUser.setNickName(user.getNickname());
                blackListUser.setRemoteId(user.getUid());
                blackListUser.setTimeStamp(System.currentTimeMillis()+"");
                blackListUser.setFaceurl(user.getFaceurl());
                boolean isAdded = MyOpenHelper.getInstance().insertBlackList(blackListUser);
                if(isAdded) {
                    MyOpenHelper.getInstance().deleteConversationRecord(mid, user.getUid());
                    AlertMessageUtil.showShortToast("加入黑名单成功");
                    dismissPopupWindow();
                    finish();
                }else {
                    // 加入和名单失败
                    AlertMessageUtil.showShortToast("加入黑名单失败");
                    dismissPopupWindow();
                    finish();
                }
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopupWindow();
            }
        });
        showPopupWindow(view);
    }

    private void showPopupWindow(View view) {
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(llParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}
