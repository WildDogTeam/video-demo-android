package com.wilddog.conversation.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.ConversationRecord;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.receiver.InviteCancelBroadcastReceiver;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.Camera360Util;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.ConvertUtil;
import com.wilddog.conversation.utils.MyOpenHelper;
import com.wilddog.conversation.utils.SharedpereferenceTool;
import com.wilddog.conversation.utils.TuSDKUtil;
import com.wilddog.conversation.utils.WilddogVideoManager;
import com.wilddog.video.CallStatus;
import com.wilddog.video.Conversation;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.video.core.stats.LocalStreamStatsReport;
import com.wilddog.video.core.stats.RemoteStreamStatsReport;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = ConversationActivity.class.getName();

    private String remoteid;

    private WilddogVideo video = WilddogVideo.getInstance();

    private CheckBox cbMic;
    private CheckBox cbSpeaker;
    private CheckBox cbCamera;
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
    private TextView tvRecordTime;

    private ImageView ivState;

    private boolean isSelfInBig = true;
    private boolean isShowDetail = false;

    private PopupWindow popupWindow;
    private RelativeLayout rlParent;
    private boolean isrecording = false;
    private ImageView ivRecordFile;

    private LocalStream localStream;

    private Conversation mConversation;

    private BroadcastReceiver broadcastReceiver;

    private boolean mFirstFrame = true;

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private boolean isAudioEnable = true;
    private boolean isVideoEnable = true;
    private boolean isSpeakerOn = true;

    private int currVolume = 0;

    private AudioManager audioManager;

    private UserInfo remoteUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mConversation = WilddogVideoManager.getIncomingInvite();
        remoteUserInfo = (UserInfo) getIntent().getSerializableExtra("user");

        remoteid = remoteUserInfo.getUid();

        initView();

        broadcastReceiver = new InviteCancelBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if (intent.getAction().equals(Constant.INVITE_CANCEL)) {
                    finish();
                }
            }
        };


        LocalStreamOptions localStreamOptions = genLocalStreamOptions();
        localStream = video.createLocalStream(localStreamOptions);
        localStream.setOnFrameListener(
                new LocalStream.CameraFrameListener() {
                    @Override
                    public void onByteFrame(byte[] bytes, int i, int i1, int i2, long l) {
                        frameProcess(bytes, 0, mFirstFrame, true, i, i1, i2);//data 可以传空 根据TextureId进行美颜
                        mFirstFrame = false;
                        //TODO 设置美颜效果
                    }
                }
        );
        localStream.attach(wwvBig);
        mConversation.accept(localStream);
        mConversation.setConversationListener(listener);
    }


    private String fileName;

    private File getRecordFile() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        if (!file.exists()) {
            boolean a = file.mkdirs();
        }
        File videoFile = new File(file, "wilddog-" + System.currentTimeMillis() + ".mp4");
        try {
            boolean b = videoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileName = videoFile.getName();
        return videoFile;
    }

    private void showSavePopupWindow() {
        View view = View.inflate(ConversationActivity.this, R.layout.popupwindow_record_file, null);
        final EditText etFileName = (EditText) view.findViewById(R.id.et_file_name);
        etFileName.setText(fileName);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除文件
                deletefile();
                popupWindow.dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //存储重命名文件。。。

                String newFileName = etFileName.getText().toString().trim();
                if (newFileName.contains("/")) {
                    etFileName.setText("");
                    AlertMessageUtil.showShortToast("你的文件命名不符合规范，不应该存在" + "/");
                } else {
                    renameFile(newFileName);
                    popupWindow.dismiss();
                }
            }
        });
        showPopupWindow(view);
    }

    private void renameFile(String newFileName) {
        File file = new File(Constant.filePath + fileName);
        file.renameTo(new File(newFileName));
    }

    private void deletefile() {
        File file = new File(Constant.filePath + fileName);
        file.delete();
    }

    private void showPopupWindow(View view) {
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(rlParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    private void initView() {
        rlParent = (RelativeLayout) findViewById(R.id.rl_parent);

        cbMic = (CheckBox) findViewById(R.id.cb_mic);
        cbMic.setChecked(isAudioEnable);
        cbSpeaker = (CheckBox) findViewById(R.id.cb_speaker);
        cbSpeaker.setChecked(isAudioEnable);
        cbCamera = (CheckBox) findViewById(R.id.cb_camera);
        cbCamera.setChecked(isAudioEnable);

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
        tvRecordTime = (TextView) findViewById(R.id.tv_record_time);

        ivRecordFile = (ImageView) findViewById(R.id.iv_record);
        ivState = (ImageView) findViewById(R.id.iv_report);

        llData.setVisibility(View.VISIBLE);

        ivState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDetail = !isShowDetail;
                // 将图标隐藏，显示统计信息
                llState.setVisibility(View.VISIBLE);
                llData.setVisibility(View.INVISIBLE);


            }
        });

        llState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDetail = !isShowDetail;

                llState.setVisibility(View.INVISIBLE);
                llData.setVisibility(View.VISIBLE);
            }
        });

        ivRecordFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始录制
                if (isrecording) {
                    //结束录制，弹出对话框
                    ivRecordFile.setBackgroundResource(R.drawable.record_normal);
                    isrecording = false;
                    tvRecordTime.setVisibility(View.GONE);
                    mConversation.stopLocalRecording();
                    endRecordTime();
                    showSavePopupWindow();

                } else {
                    //开始录制
                    ivRecordFile.setBackgroundResource(R.drawable.record_selected);
                    isrecording = true;
                    mConversation.startLocalRecording(getRecordFile(), wwvSmall, wwvSmall);
                    tvRecordTime.setVisibility(View.VISIBLE);
                    startRecordTime();
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

        llHungup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 对方已经接受，结束会话
                if (mConversation != null) {
                    mConversation.close();
                }
                finish();
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
    }

    private Timer recordTimer;

    private void startRecordTime() {
        if (recordTimer == null) {
            recordTimer = new Timer();
        }
        recordTime = 0;
        recordTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvRecordTime.setText(ConvertUtil.secToTime(recordTime));
                    }
                });
                recordTime++;
            }
        };
        recordTimer.schedule(recordTask, 0, 1000);
    }

    private int recordTime = 0;

    private TimerTask recordTask;

    private void endRecordTime() {
        recordTimer.cancel();
        recordTimer = null;
        recordTime = 0;
    }

    private Conversation.StatsListener statsListener = new Conversation.StatsListener() {
        @Override
        public void onLocalStreamStatsReport(LocalStreamStatsReport localStreamStatsReport) {
            if (isSelfInBig) {
                showStats(localStreamStatsReport, null);
            }
        }

        @Override
        public void onRemoteStreamStatsReport(RemoteStreamStatsReport remoteStreamStatsReport) {

            if (!isSelfInBig) {
                showStats(null, remoteStreamStatsReport);
            }
        }

    };


    private void showStats(final LocalStreamStatsReport localStats, final RemoteStreamStatsReport remoteStats) {
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
                    tvRate.setText(localStats.getBitsSentRate() + "kpbs");
                    tvByte.setText("send " + convertToMB(localStats.getBytesSent()) + "MB");
                } else {
                    // 显示远程统计数据
                    tvDimension.setText(remoteStats.getWidth() + "x" + remoteStats.getHeight() + "px");
                    tvFps.setText(remoteStats.getFps() + "fps");
                    tvRate.setText(remoteStats.getBitsReceivedRate() + "kpbs");
                    tvByte.setText("recv " + convertToMB(remoteStats.getBytesReceived()) + "MB");
                }
            }
        });

    }

    public void frameProcess(byte[] data, int textureId, boolean isFirstFrame, boolean isInitEGL, int frameWidth, int frameHeight, int rotation) {
        switch (SharedpereferenceTool.getBeautyPlan(ConversationActivity.this)) {
            case "Camera360":
                if (isFirstFrame)
                    Camera360Util.initEngine(ConversationActivity.this, isInitEGL, frameWidth, frameHeight, rotation);//  在第一帧视频到来时，初始化，指定需要的输出大小以及方向
                Camera360Util.processFrame(data, frameWidth, frameHeight);
                break;
            case "TuSDK":
                if (isFirstFrame) TuSDKUtil.init(frameWidth, frameHeight);
                TuSDKUtil.processFrame(data);
                break;
            default:
                break;
        }


    }


    private String convertToMB(long value) {
        float result = Float.parseFloat(String.valueOf(value)) / (1024 * 1024);
        return decimalFormat.format(result);
        // return String.format("%.2f", value);
    }

    private Conversation.Listener listener = new Conversation.Listener() {
        @Override
        public void onCallResponse(CallStatus callStatus) {

        }

        @Override
        public void onStreamReceived(final RemoteStream remoteStream) {
            //有参与者成功加入会话后，会触发此方法
            //设置音视频全部开启
            //remoteStream.enableAudio(true);
            // remoteStream.enableVideo(true);
            //在视频展示控件中播放其他端媒体流
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    localStream.detach();
                    remoteStream.attach(wwvBig);
                    wwvSmall.setVisibility(View.VISIBLE);
                    localStream.attach(wwvSmall);
                    isSelfInBig = false;
                    mConversation.setStatsListener(statsListener);
                    tvTime.setVisibility(View.VISIBLE);
                    startTimer();

                }
            });
        }

        @Override
        public void onClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertMessageUtil.showShortToast("用户：" + mConversation.getRemoteUid() + "离开会话");
                }
            });
            mConversation.close();
            finish();
        }

        @Override
        public void onError(WilddogVideoError wilddogVideoError) {
            if (wilddogVideoError != null) {
                Log.d(TAG, "Participant connect failed,the detail:" + wilddogVideoError.getMessage());
            }
        }
    };
    private long conversationTime = 0;
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTime.setText(ConvertUtil.secToTime((int) conversationTime));
                }
            });
            conversationTime++;
        }
    };
    private Timer timer;

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(task, 0, 1000);
        }
    }

    private LocalStreamOptions genLocalStreamOptions() {
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        switch (SharedpereferenceTool.getDimension(ConversationActivity.this)) {
            case "360P":
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_360P);
                break;
            case "480P":
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_480P);
                break;
            case "720P":
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_720P);
                break;
            case "1080P":
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_1080P);
                break;
            default:
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_480P);
                break;
        }
        return builder.captureAudio(true).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constant.INVITE_CANCEL);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        String localid =SharedpereferenceTool.getUserId(getApplicationContext());
        ConversationRecord record = MyOpenHelper.getInstance().selectConversationRecord(localid,remoteid);
        if(record ==null){
            record = new ConversationRecord();
            record.setRemoteId(remoteid);
            record.setDuration(String.valueOf(conversationTime));
            record.setLocalId(localid);
            record.setNickName(remoteUserInfo.getNickName());
            record.setPhotoUrl(remoteUserInfo.getPhotoUrl());
            record.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            MyOpenHelper.getInstance().insertRecord(record);
        }else {
            record = new ConversationRecord();
            record.setRemoteId(remoteid);
            record.setDuration(String.valueOf(conversationTime));
            record.setNickName(remoteUserInfo.getNickName());
            record.setPhotoUrl(remoteUserInfo.getPhotoUrl());
            record.setLocalId(localid);
            record.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            MyOpenHelper.getInstance().updateRecord(localid,remoteid,record);
        }
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
        if(localStream!=null && !localStream.isClosed()){
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
            mConversation.close();
        }
/*
        client.dispose();
        video.dispose();*/
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


}
