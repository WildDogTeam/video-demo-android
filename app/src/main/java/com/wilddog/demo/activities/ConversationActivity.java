package com.wilddog.demo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.demo.R;
import com.wilddog.demo.receiver.InviteCancelBroadcastReceiver;
import com.wilddog.demo.utils.AlertMessageUtil;
import com.wilddog.demo.utils.Camera360Util;
import com.wilddog.demo.utils.Contants;
import com.wilddog.demo.utils.ConvertUtil;
import com.wilddog.demo.utils.SharedpereferenceTool;
import com.wilddog.demo.utils.TuSDKUtil;
import com.wilddog.demo.wilddogAuth.WilddogVideoManager;
import com.wilddog.video.Conversation;
import com.wilddog.video.ConversationCallback;
import com.wilddog.video.IncomingInvite;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.Participant;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.VideoError;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoClient;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.video.bean.ConnectOptions;
import com.wilddog.video.bean.LocalStats;
import com.wilddog.video.bean.RemoteStats;
import com.wilddog.video.listener.RTCStatsListener;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = ConversationActivity.class.getName();

    private IncomingInvite incomingInvite;
    private String fromUid;

    private WilddogVideo video = WilddogVideo.getInstance();
    private WilddogVideoClient client = video.getClient();

    private CheckBox cbMic;
    private LinearLayout llHungup;
    private LinearLayout llFlipCamera;

    private TextView tvTime;

    // 显示统计结果控件
    private TextView tvDimension;
    private TextView tvFps;
    private TextView tvRate;
    private TextView tvByte;

    private WilddogVideoView wwvBig;
    private WilddogVideoView wwvSmall;

    private LinearLayout llData;
    private LinearLayout llState;

    private ImageView ivState;

    private boolean isSelfInBig = true;
    private boolean isShowDetail = false;

    private boolean isAudioEnable = true;

    private PopupWindow popupWindow;
    private RelativeLayout rlParent;
    private boolean isrecording = false;
    private ImageView ivRecordFile;

    private LocalStream localStream;

    private Conversation mConversation;

    private BroadcastReceiver broadcastReceiver;

    private boolean mFirstFrame =true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        incomingInvite = WilddogVideoManager.getIncomingInvite();
        fromUid = incomingInvite.getFromParticipantId();

        initView();

        broadcastReceiver = new InviteCancelBroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if(intent.getAction().equals(Contants.INVITE_CANCEL)){
                    finish();
                }
            }
        };


        LocalStreamOptions localStreamOptions = genLocalStreamOptions();
        localStream = video.createLocalStream(localStreamOptions);
        localStream.setOnFrameListener(new WilddogVideo.CameraFrameListener() {
            @Override
            public void onByteFrame(byte[] bytes, int i, int i1,int var4,long var5) {

                frameProcess(bytes, 0, mFirstFrame, true, i, i1, var4);//data 可以传空 根据TextureId进行美颜
                mFirstFrame = false;
                //TODO 设置美颜效果
            }
        });
        localStream.attach(wwvBig);
        ConnectOptions connectOptions = new ConnectOptions(localStream, "conversationDemo");

        incomingInvite.accept(connectOptions, new ConversationCallback() {
            @Override
            public void onConversation(@Nullable Conversation conversation, @Nullable VideoError e) {
                if (conversation != null) {
                    mConversation = conversation;
                    mConversation.setConversationListener(listener);
                   mConversation.setRTCStatsListener(rtcStatsListener);
                } else {
                    //处理会话建立失败逻辑
                    Log.e(TAG, "create failured:" + e.getMessage());
                    AlertMessageUtil.showShortToast("呼叫失败");
                    finish();
                }
            }
        });
    }


    private String fileName;

    private File getRecordFile(){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        if (!file.exists()) {
            boolean a = file.mkdirs();
        }
      File  videoFile = new File(file, "wilddog-" + System.currentTimeMillis() + ".mp4");
        try {
            boolean b = videoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoFile;
    }

    private File getRecordFile2() {
        long currentTime =System.currentTimeMillis();
        fileName = ConvertUtil.getDayString(currentTime)+"-"+currentTime+".mp4";

        File file = new File(Contants.filePath);
        if(!file.exists()){
            file.mkdirs();
        }

        File recordFile = new File(file.getAbsolutePath()+fileName);
        if(!recordFile.exists()){
            try {
                recordFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return recordFile;
    }

    private void showSavePopupWindow() {
        View view = View.inflate(ConversationActivity.this, R.layout.popupwindow_record_file, null);
        final EditText etFileName = (EditText) findViewById(R.id.et_file_name);
        etFileName.setText(fileName);
        TextView tvCancel = (TextView) findViewById(R.id.tv_cancel);
        TextView tvSave = (TextView) findViewById(R.id.tv_save);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除文件
                deletefile();
                popupWindow.dismiss();
            }
        });
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //存储重命名文件。。。

                String newFileName =  etFileName.getText().toString().trim();
                if(newFileName.contains("/")){
                    etFileName.setText("");
                    AlertMessageUtil.showShortToast("你的文件命名不符合规范，不应该存在"+"/");
                }else {
                    renameFile(newFileName);
                    popupWindow.dismiss();}
            }
        });
        showPopupWindow(view);
    }

    private void renameFile(String newFileName) {
        File file = new File(Contants.filePath+fileName);
        file.renameTo(new File(newFileName));
    }
    private void deletefile(){
        File file = new File(Contants.filePath+fileName);
        file.delete();
    }

    private void showPopupWindow(View view){
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(rlParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    private void initView() {
        rlParent = (RelativeLayout) findViewById(R.id.rl_parent);
        cbMic = (CheckBox) findViewById(R.id.cb_mic);
        cbMic.setChecked(isAudioEnable);
        llHungup = (LinearLayout) findViewById(R.id.ll_reject);
        llFlipCamera = (LinearLayout) findViewById(R.id.ll_filp_camera);

        tvTime = (TextView) findViewById(R.id.tv_time);

        tvDimension = (TextView) findViewById(R.id.tv_dimensions);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvByte = (TextView) findViewById(R.id.tv_bytes);


        wwvBig = (WilddogVideoView) findViewById(R.id.wvv_big);
        wwvSmall = (WilddogVideoView) findViewById(R.id.wvv_small);
        wwvSmall.setZOrderMediaOverlay(true);

        llData = (LinearLayout) findViewById(R.id.ll_data);
        llState = (LinearLayout) findViewById(R.id.ll_state);

        ivRecordFile = (ImageView) findViewById(R.id.iv_record);
        ivState = (ImageView) findViewById(R.id.iv_report);

        llData.setVisibility(View.VISIBLE);

        ivState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDetail=!isShowDetail;
                if(isShowDetail){
                    // 将图标隐藏，显示统计信息
                    llState.setVisibility(View.VISIBLE);
                    llData.setVisibility(View.INVISIBLE);
                }else {
                    llState.setVisibility(View.INVISIBLE);
                    llData.setVisibility(View.VISIBLE);
                }

            }
        });

        ivRecordFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始录制
                if(isrecording){
                    //结束录制，弹出对话框
                    ivRecordFile.setBackgroundResource(R.drawable.record_normal);
                    isrecording =false;
                    mConversation.stopVideoRecording();
                    showSavePopupWindow();
                }else {
                    //开始录制
                    ivRecordFile.setBackgroundResource(R.drawable.record_selected);
                    isrecording = true;
                    mConversation.startVideoRecording(getRecordFile());
                }
            }
        });

        cbMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (localStream != null) {
                    isAudioEnable = !isAudioEnable;
                    localStream.enableAudio(isChecked);
                }
            }
        });
        llHungup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 对方已经接受，结束会话
                if (mConversation != null) {
                    mConversation.disconnect();
                }
                finish();
            }
        });
        llFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video != null) {
                    video.flipCamera();
                }
            }
        });
    }


    private RTCStatsListener rtcStatsListener = new RTCStatsListener() {
        @Override
        public void onLocalStats(LocalStats localStats) {
            Log.e(TAG,localStats.toString());
            showStats(localStats, null);
        }

        @Override
        public void onRemoteStats(RemoteStats remoteStats) {

            Log.e(TAG,remoteStats.toString());
            showStats(null, remoteStats);
        }
    };

    private void showStats(final LocalStats localStats, final RemoteStats remoteStats) {
        if (!isShowDetail) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isSelfInBig) {
                    // 显示本地统计数据
                    tvDimension.setText(localStats.getWidth() + "x" + localStats.getHeight() + "px");
                    tvFps.setText(localStats.getFps() + "fps");
                    tvRate.setText(localStats.getTxBitRate() + "kpbs");
                    tvByte.setText("send " + convertToMB(localStats.getTxBytes()) + "MB");
                } else {
                    // 显示远程统计数据
                    tvDimension.setText(remoteStats.getWidth() + "x" + remoteStats.getHeight() + "px");
                    tvFps.setText(remoteStats.getFps() + "fps");
                    tvRate.setText(remoteStats.getRxBitRate() + "kpbs");
                    tvByte.setText("recv " + convertToMB(remoteStats.getRxBytes()) + "MB");
                }
            }
        });

    }

    public void frameProcess(byte[] data, int textureId, boolean isFirstFrame, boolean isInitEGL, int frameWidth, int frameHeight, int rotation) {
        switch (SharedpereferenceTool.getBeautyPlan(ConversationActivity.this)){
            case "Camera360":
                if (isFirstFrame) Camera360Util.initEngine(ConversationActivity.this,isInitEGL, frameWidth, frameHeight, rotation);//  在第一帧视频到来时，初始化，指定需要的输出大小以及方向
                Camera360Util.processFrame(data,frameWidth,frameHeight);
                break;
            case "TuSDK":
                if(isFirstFrame) TuSDKUtil.init(frameWidth,frameHeight);
                TuSDKUtil.processFrame(data);
                break;
            default:
                break;
        }


    }


    private String convertToMB(long value) {
        return String.format("%.2f", value);
    }

    private Conversation.Listener listener = new Conversation.Listener() {
        @Override
        public void onConnected(Conversation conversation) {

        }

        @Override
        public void onConnectFailed(Conversation conversation, VideoError e) {

        }

        @Override
        public void onDisconnected(Conversation conversation, VideoError e) {

        }

        @Override
        public void onParticipantConnected(Conversation conversation, Participant participant) {
            participant.setListener(new Participant.Listener() {
                @Override
                public void onStreamAdded(final RemoteStream remoteStream) {
                    //有参与者成功加入会话后，会触发此方法
                    //设置音视频全部开启
                    remoteStream.enableAudio(true);
                    remoteStream.enableVideo(true);
                    //在视频展示控件中播放其他端媒体流
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            localStream.detach();

                            remoteStream.attach(wwvBig);
                            wwvSmall.setVisibility(View.VISIBLE);
                            localStream.attach(wwvSmall);

                            tvTime.setVisibility(View.VISIBLE);
                            startTimer();

                        }
                    });

                }

                @Override
                public void onConnectFailed(Participant participant, VideoError exception) {

                    if (exception != null) {
                        Log.d(TAG, "Participant connect failed,the detail:" + exception.getMessage());
                    }
                }

                @Override
                public void onDisconnected(Participant participant, VideoError exception) {
                    Log.e(TAG, "Participant:onDisconnected");
                    //TODO 对方断开连接 如何处理？
                    if (exception != null) {
                        Log.d(TAG, "Participant onDisconnected failured,the detail:" + exception.getMessage());
                    }
                }
            });

        }

        @Override
        public void onParticipantDisconnected(Conversation conversation, Participant participant) {
            // 收到离开会话
            AlertMessageUtil.showShortToast("用户：" + participant.getParticipantId() + "离开会话");
            mConversation.disconnect();
            finish();
        }
    };
    private long conversationTime =0;
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTime.setText( ConvertUtil.secToTime((int) conversationTime));
                }
            });
            conversationTime++;
        }
    };
    private Timer timer;
    private void startTimer() {
        timer = new Timer();
        timer.schedule(task,0,1000);
    }

    private LocalStreamOptions genLocalStreamOptions() {
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        switch (SharedpereferenceTool.getDimension(ConversationActivity.this)) {
            case "360P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSION_360P);
                break;
            case "480P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSION_480P);
                break;
            case "720P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSION_720P);
                break;
            case "1080P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSION_1080P);
                break;
            default:
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSION_480P);
                break;
        }
        return builder.setAudioEnabled(true).build();
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

    @Override
    protected void onDestroy() {
        if(timer!=null){
            timer.cancel();
        }
        super.onDestroy();
        if (localStream != null) {
            localStream.detach();
            localStream.close();
        }
        if (wwvBig != null) {
            wwvBig.release();
            wwvBig = null;
        }
        if (wwvSmall != null) {
            wwvSmall.release();
            wwvSmall = null;
        }
        if (mConversation != null) {
            mConversation.disconnect();
        }
/*
        client.dispose();
        video.dispose();*/
    }

}
