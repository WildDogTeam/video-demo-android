package com.wilddog.conversation.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.Callback;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.bean.VideoError;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ObjectAndStringTool;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.wilddog.WilddogSyncManager;

public class JoinRoomActivity extends AppCompatActivity {
    private EditText etRoomId;
    private RadioGroup rgMode;
    private RadioButton rbInteract;
    private RadioButton rbVideo;
    private ImageView ivBack;
    private Button btnJoin;
    /**
     * 1 表示多人互动
     * 2 表示多人视频
     */
    private int type = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        etRoomId = (EditText) findViewById(R.id.et_join_room_id);
        rgMode = (RadioGroup) findViewById(R.id.rg_choose_mode);
        rbInteract = (RadioButton) findViewById(R.id.rb_interact);
        rbVideo = (RadioButton) findViewById(R.id.rb_video);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        btnJoin = (Button) findViewById(R.id.btn_join);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join();
            }
        });
        rgMode.check(R.id.rb_video);
        rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_interact:
                        type = 2;
                        break;
                    case R.id.rb_video:
                        type = 1;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void join() {
        final String roomId = etRoomId.getText().toString().trim();
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(JoinRoomActivity.this, "房间号不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        WilddogSyncManager.getWilddogSyncTool().writeServerTimeStamp(roomId, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                writeUsers(roomId);
            }

            @Override
            public void onFailed(VideoError error) {
                Log.e("error", error.toString());
                if (error != null && error.getErrCode() == 1) {
                    writeUsers(roomId);
                } else {
                    AlertMessageUtil.showShortToast("加入房间出错,请重试");
                }
            }
        });

    }


    private void writeUsers(final String roomId) {
        WilddogSyncManager.getWilddogSyncTool().getServerTimeStamp(roomId, new Callback<Long>() {
            @Override
            public void onSuccess(Long aLong) {
                String uid = SharedPreferenceTool.getUserId(JoinRoomActivity.this);
                UserInfo info = ObjectAndStringTool.getObjectFromJson(SharedPreferenceTool.getUserInfo(JoinRoomActivity.this), UserInfo.class);
                WilddogSyncManager.getWilddogSyncTool().writeRoomUsers(roomId, uid,info.getNickname());
                SharedPreferenceTool.saveRoomId(JoinRoomActivity.this, roomId);
                if (type == 1) {
                    Intent intent = new Intent(JoinRoomActivity.this, VideoModelActivity.class);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("time", aLong);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(JoinRoomActivity.this, InteractModelActivity.class);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("time", aLong);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailed(VideoError error) {
                Log.e("error", error.toString());
                AlertMessageUtil.showShortToast("加入房间出错,请重试");
            }
        });
    }
}
