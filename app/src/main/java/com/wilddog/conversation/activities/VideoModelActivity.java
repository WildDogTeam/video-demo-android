package com.wilddog.conversation.activities;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.adapters.MyGridViewAdapter;
import com.wilddog.conversation.bean.Callback;
import com.wilddog.conversation.bean.StreamHolder;
import com.wilddog.conversation.bean.VideoError;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.wilddog.WilddogSyncManager;
import com.wilddog.video.base.LocalStream;
import com.wilddog.video.base.LocalStreamOptions;
import com.wilddog.video.base.WilddogVideoError;
import com.wilddog.video.base.WilddogVideoInitializer;
import com.wilddog.video.base.util.LogUtil;
import com.wilddog.video.base.util.logging.Logger;
import com.wilddog.video.room.CompleteListener;
import com.wilddog.video.room.RoomStream;
import com.wilddog.video.room.WilddogRoom;
import com.wilddog.wilddogauth.WilddogAuth;

import java.util.ArrayList;
import java.util.List;

public class VideoModelActivity extends AppCompatActivity {
    private final String TAG = VideoModelActivity.class.getCanonicalName();
    private String roomId ;
    private CheckBox cbMic;
    private CheckBox cbSpeaker;
    private CheckBox cbCamera;
    private LinearLayout llFlipCamera;
    private ImageView ivLeave;
    private LinearLayout llParent;
    private TextView tvInvite;
    private boolean isAudioEnable = true;
    private boolean isVideoEnable = true;
    private boolean isSpeakerOn = true;
    private GridView gvStreams;
    private LocalStream localStream;
    private PopupWindow popupWindow;
    private WilddogRoom room;

    private int currVolume = 0;
    private AudioManager audioManager;

    private MyGridViewAdapter adapter;
    private List<StreamHolder> streamHolders = new ArrayList<>();
    private long startTimeStamp;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_model);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        roomId = getIntent().getStringExtra("roomId");
        startTimeStamp = getIntent().getLongExtra("time",System.currentTimeMillis());
        Log.d(TAG,"roomId: "+roomId+" time:"+startTimeStamp);
        initView();
        setListener();
        initRoomSDK();
        createLocalStream();
        joinRoom();
    }

    private void joinRoom() {
        room = new WilddogRoom(roomId, new WilddogRoom.Listener() {
            @Override
            public void onConnected(WilddogRoom wilddogRoom) {
                Log.d(TAG,"room connected");
                // 此时服务器返回用户id
                setLocalStreamId();
                room.publish(localStream, new CompleteListener() {
                    @Override
                    public void onComplete(final WilddogVideoError wilddogVideoError) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (wilddogVideoError != null) {
                                    //失败
                                    Log.d(TAG,"publish failed");
                                    Log.e("error", "error:" + wilddogVideoError.getMessage());
                                } else {
                                    Log.d(TAG,"publish succeed");
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onDisconnected(WilddogRoom wilddogRoom) {
                Log.d(TAG,"room disconnected");
                finish();
            }

            @Override
            public void onStreamAdded(WilddogRoom wilddogRoom, RoomStream roomStream) {
                Log.d(TAG,"room onStreamAdded roomStream id:"+ roomStream.getStreamId());
                room.subscribe(roomStream, new CompleteListener() {
                    @Override
                    public void onComplete(WilddogVideoError wilddogVideoError) {
                    }
                });
            }

            @Override
            public void onStreamRemoved(WilddogRoom wilddogRoom, RoomStream roomStream) {
                Log.d(TAG,"room onStreamRemoved roomStream id:"+ roomStream.getStreamId());
                removeRemoteStream(roomStream.getStreamId());
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onStreamReceived(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 在控件中显示
                Log.d(TAG,"room onStreamReceived roomStream id:"+ roomStream.getStreamId());
                StreamHolder holder = new StreamHolder(false, System.currentTimeMillis(), roomStream);
                holder.setId(roomStream.getStreamId());
                streamHolders.add(holder);
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onStreamChanged(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 混流使用
            }

            @Override
            public void onError(WilddogRoom wilddogRoom, final WilddogVideoError wilddogVideoError) {
                Log.d(TAG,"room onError error: "+wilddogVideoError.toString() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("error", "错误码:" + wilddogVideoError.getErrCode() + ",错误信息:" + wilddogVideoError.getMessage());
                    }
                });

            }
        });
        room.connect();
    }

    private void removeRemoteStream(long streamId) {
        for (StreamHolder holder : streamHolders) {
            if (streamId == holder.getId()) {
                streamHolders.remove(holder);
                break;
            }
        }
    }

    private void setLocalStreamId() {
        for (StreamHolder holder : streamHolders) {
            if (holder.isLocal()) {
                holder.setId(((LocalStream) holder.getStream()).getStreamId());
            }
        }
    }


    private void initRoomSDK() {
        LogUtil.setLogLevel(Logger.Level.DEBUG);
        WilddogVideoInitializer.initialize(VideoModelActivity.this, Constant.WILDDOG_VIDEO_APP_ID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
    }

    private void createLocalStream() {
        LocalStreamOptions options = new LocalStreamOptions.Builder().build();
        localStream = LocalStream.create(options);
        localStream.enableAudio(isAudioEnable);
        localStream.enableVideo(true);
        //将本地媒体流绑定到WilddogVideoView中
        StreamHolder holder = new StreamHolder(true, System.currentTimeMillis(), localStream);
        streamHolders.add(holder);
        handler.sendEmptyMessage(0);
    }

    private void initView() {
        llParent = (LinearLayout) findViewById(R.id.ll_parent);
        cbMic = (CheckBox) findViewById(R.id.cb_mic);
        cbMic.setChecked(isAudioEnable);
        cbSpeaker = (CheckBox) findViewById(R.id.cb_speaker);
        cbSpeaker.setChecked(isSpeakerOn);
        cbCamera = (CheckBox) findViewById(R.id.cb_camera);
        cbCamera.setChecked(isVideoEnable);
        llFlipCamera = (LinearLayout) findViewById(R.id.ll_filp_camera);
        ivLeave = (ImageView) findViewById(R.id.iv_leave);
        tvInvite = (TextView) findViewById(R.id.tv_invite_other);

        gvStreams = (GridView) findViewById(R.id.gv_streams);
        gvStreams.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new MyGridViewAdapter(this, streamHolders);
        gvStreams.setAdapter(adapter);
    }

    private void setListener(){
        cbMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (localStream != null) {
                    isAudioEnable = !isAudioEnable;
                    localStream.enableAudio(isAudioEnable);
                }
            }
        });
        cbCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (localStream != null) {
                    isVideoEnable = !isVideoEnable;
                    localStream.enableVideo(isVideoEnable);
                }
            }
        });
        cbSpeaker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 设置设置扬声器
                if (isSpeakerOn) {
                    // 关闭，设为false
                    closeSpeaker();
                } else {
                    openSpeaker();
                }
                isSpeakerOn = !isSpeakerOn;
            }
        });
        llFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localStream != null) {
                    localStream.switchCamera();
                }

            }
        });
        ivLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveRoom();
                finish();
            }
        });

        tvInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出对话框
                showInviteDialog();
            }
        });
    }

    private void showInviteDialog(){
        View view = View.inflate(VideoModelActivity.this, R.layout.popupwindow_invite, null);
        TextView tvRoomId = view.findViewById(R.id.tv_roomId);
        TextView tvCopyRoomId = view.findViewById(R.id.tv_copy_roomId);
        TextView tvCopyRoomUrl = view.findViewById(R.id.tv_copy_room_url);
        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        tvRoomId.setText("房间号: "+roomId);
        tvCopyRoomId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyRoomId();
                dismissPopupWindow();
            }
        });
        tvCopyRoomUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyRoomUrl();
                dismissPopupWindow();
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

    private void copyRoomId() {
        ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(Constant.INVITE_URL);
        AlertMessageUtil.showShortToast("房间号复制成功");
    }

    private void copyRoomUrl() {
        ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(Constant.INVITE_URL);
        AlertMessageUtil.showShortToast("房间地址复制成功");
    }

    private void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private void showPopupWindow(View view) {
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(llParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    /**
     * 打开扬声器
     */
    private void openSpeaker() {
        try {
            getAudioManager().setMode(AudioManager.ROUTE_SPEAKER);
            currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            if (!audioManager.isSpeakerphoneOn()) {
                //setSpeakerphoneOn() only work when audio mode set to MODE_IN_CALL.
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(true);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.STREAM_VOICE_CALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭扬声器
     */
    public void closeSpeaker() {
        try {
            if (getAudioManager() != null) {
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                            AudioManager.STREAM_VOICE_CALL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void leaveRoom() {
        if (room != null) {
            room.disconnect();
            room = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WilddogSyncManager.getWilddogSyncTool().judgeAndRemoveTime(roomId, new Callback<String>() {
            @Override
            public void onSuccess(String s) {
                WilddogSyncManager.getWilddogSyncTool().removeRoomUsers(roomId, SharedPreferenceTool.getUserId(VideoModelActivity.this));
            }

            @Override
            public void onFailed(VideoError error) {

            }
        });
        leaveRoom();
        if (!localStream.isClosed()) {
            localStream.close();
        }
    }
}
