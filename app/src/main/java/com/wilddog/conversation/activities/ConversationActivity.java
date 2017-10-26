package com.wilddog.conversation.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.ConversationRecord;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.floatingwindow.PermissionUtils;
import com.wilddog.conversation.floatingwindow.StreamsHolder;
import com.wilddog.conversation.receiver.InviteCancelBroadcastReceiver;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.Camera360Util;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.ConvertUtil;
import com.wilddog.conversation.utils.MyOpenHelper;
import com.wilddog.conversation.utils.ParamsStore;
import com.wilddog.conversation.utils.SharedpereferenceTool;
import com.wilddog.conversation.utils.TuSDKUtil;
import com.wilddog.conversation.wilddog.WilddogVideoManager;
import com.wilddog.video.base.LocalStream;
import com.wilddog.video.base.LocalStreamOptions;
import com.wilddog.video.base.WilddogVideoView;
import com.wilddog.video.call.Conversation;
import com.wilddog.video.call.WilddogVideoCall;
import com.wilddog.video.call.stats.LocalStreamStatsReport;
import com.wilddog.video.call.stats.RemoteStreamStatsReport;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = ConversationActivity.class.getName();
    private String remoteid;
    private WilddogVideoCall video = WilddogVideoCall.getInstance();
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

    private TextView tvNickName;

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

    private BroadcastReceiver broadcastReceiver;

    private Conversation mConversation;
    private boolean mFirstFrame = true;

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private boolean isAudioEnable = true;
    private boolean isVideoEnable = true;
    private boolean isSpeakerOn = true;

    private int currVolume = 0;

    private AudioManager audioManager;

    private UserInfo remoteUserInfo;
    private ImageView ivFullScreen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        mConversation = WilddogVideoManager.getConversation();
//        remoteUserInfo = (UserInfo) getIntent().getSerializableExtra("user");

        remoteUserInfo = WilddogVideoManager.getRemoteUser();
        remoteid = remoteUserInfo.getUid();
        initView();

        broadcastReceiver = new InviteCancelBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if (intent.getAction().equals(Constant.INVITE_CANCEL)) {
                    release();
                    finish();
                } else if (intent.getAction().equals(Constant.UPDATE_VIEW)) {
                    updateView();
                }
            }
        };

        if(!StreamsHolder.getMyBinder().isWindowShow()){
            LocalStreamOptions localStreamOptions = genLocalStreamOptions();
            localStream = video.createLocalStream(localStreamOptions);
            localStream.setOnFrameListener(new LocalStream.CameraFrameListener() {
                @Override
                public void onByteFrame(byte[] bytes, int i, int i1, int var4, long var5) {

                    frameProcess(bytes, 0, mFirstFrame, true, i, i1, var4);//data 可以传空 根据TextureId进行美颜
                    mFirstFrame = false;
                    //TODO 设置美颜效果
                }
            });

            if(StreamsHolder.getLocalStream()!=null&& !localStream.isClosed()){
                StreamsHolder.getLocalStream().close();
            }
            StreamsHolder.setLocalStream(localStream);
            mConversation.accept(localStream);
        }else {
            localStream=StreamsHolder.getLocalStream();
        }

        localStream.attach(wwvBig);

//        mConversation.setConversationListener(listener);

    }

    private void release() {
        if (mConversation != null) {
            mConversation.close();
        }
        if (localStream != null && !localStream.isClosed()) {
            localStream.close();
        }
        String localid = SharedpereferenceTool.getUserId(getApplicationContext());
        ConversationRecord record = MyOpenHelper.getInstance().selectConversationRecord(localid, remoteid);
        if (record == null) {
            record = new ConversationRecord();
            record.setRemoteId(remoteid);
            record.setDuration(String.valueOf(conversationTime));
            record.setLocalId(localid);
            record.setNickName(remoteUserInfo.getNickname());
            record.setPhotoUrl(remoteUserInfo.getFaceurl());
            record.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            MyOpenHelper.getInstance().insertRecord(record);
        } else {
            record = new ConversationRecord();
            record.setRemoteId(remoteid);
            record.setDuration(String.valueOf(conversationTime));
            record.setNickName(remoteUserInfo.getNickname());
            record.setPhotoUrl(remoteUserInfo.getFaceurl());
            record.setLocalId(localid);
            record.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            MyOpenHelper.getInstance().updateRecord(localid, remoteid, record);
        }

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
        etFileName.setText(fileName.substring(0, fileName.indexOf(".mp4")));
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
                    renameFile(newFileName + ".mp4");
                    popupWindow.dismiss();
                }
            }
        });
        showPopupWindow(view);
    }

    private void renameFile(String newFileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/wilddog/" + fileName);
        file.renameTo(new File(newFileName));
    }

    private void deletefile() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/wilddog/" + fileName);
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
        cbSpeaker.setChecked(isSpeakerOn);
        cbCamera = (CheckBox) findViewById(R.id.cb_camera);
        cbCamera.setChecked(isVideoEnable);

        tvNickName = (TextView) findViewById(R.id.tv_nickname);
        tvNickName.setText(remoteUserInfo.getNickname());

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
        ivFullScreen = (ImageView) findViewById(R.id.iv_fullscreen);

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
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(ConversationActivity.this, "存储设备不存在,无法录制", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //开始录制
                    ivRecordFile.setBackgroundResource(R.drawable.record_selected);
                    isrecording = true;
                    mConversation.startLocalRecording(getRecordFile(), wwvSmall, wwvBig);
                    tvRecordTime.setVisibility(View.VISIBLE);
                    recordTime = 0;
                    startRecordTime();
                }
            }
        });

        ivFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showFloatingWindow();
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
                    localStream.enableVideo(isChecked);
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
                release();
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

    public void showFloatWindow() {
        //显示悬浮框
//        PermissionUtils.requestPermissionsResult(ConversationActivity.this, 1,
//                new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW
//                }, new PermissionUtils.OnPermissionListener() {
//                    @Override
//                    public void onPermissionGranted() {
//                        showFloatingWindow();
//                    }
//
//                    @Override
//                    public void onPermissionDenied() {
//                        showFloatingWindow();
//                        Toast.makeText(ConversationActivity.this, "请打开悬浮窗权限", Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    private void showFloatingWindow() {
        moveTaskToBack(false);
        StreamsHolder.getLocalStream().detach();
        StreamsHolder.getRemoteStream().detach();

        StreamsHolder.getMyBinder().showFloatingWindow();

        storeInstanceState();


        finish();

    }

    private void storeInstanceState() {
        ParamsStore.preconverstationTime =conversationTime;
        ParamsStore.precurrentTime =System.currentTimeMillis();
        ParamsStore.prerecordTime=recordTime;
        ParamsStore.isrecording=isrecording;
        ParamsStore.isAudioEnable=isAudioEnable;
        ParamsStore.isVideoEnable=isVideoEnable;
        ParamsStore.isSpeakerOn=isSpeakerOn;
        ParamsStore.fileName=fileName;

    }

    private Timer recordTimer;

    private void startRecordTime() {
        if (recordTimer == null) {
            recordTimer = new Timer();
        }

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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.INVITE_CANCEL);
        intentFilter.addAction(Constant.UPDATE_VIEW);
        registerReceiver(broadcastReceiver, intentFilter);

        if (StreamsHolder.getMyBinder().isWindowShow()) {
            StreamsHolder.getMyBinder().hidFloatingWindow();
            conversationTime=(System.currentTimeMillis()-ParamsStore.precurrentTime)/1000+ParamsStore.preconverstationTime;
            reStoreInstanceState();
            updateView();
        }
    }

    private void reStoreInstanceState() {
        if(ParamsStore.isrecording){
            fileName=ParamsStore.fileName;
            ivRecordFile.setBackgroundResource(R.drawable.record_selected);
            isrecording = true;
            tvRecordTime.setVisibility(View.VISIBLE);
            recordTime= (int) ((System.currentTimeMillis()-ParamsStore.precurrentTime)/1000+ParamsStore.prerecordTime);
            startRecordTime();
        }
        if(!ParamsStore.isAudioEnable){
            cbMic.setChecked(false);
            isAudioEnable=false;
        }
        if(!ParamsStore.isVideoEnable){
            cbCamera.setChecked(false);
            isVideoEnable=false;
        }
        if(!ParamsStore.isSpeakerOn){
            cbSpeaker.setChecked(false);
            isSpeakerOn=false;
        }
    }

    private void updateView() {
        localStream.detach();
        StreamsHolder.getRemoteStream().attach(wwvBig);
        wwvSmall.setVisibility(View.VISIBLE);
        localStream.attach(wwvSmall);
        isSelfInBig = false;
        tvTime.setVisibility(View.VISIBLE);
        mConversation.setStatsListener(statsListener);
        startTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        release();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {

        if (timer != null) {
            timer.cancel();
        }
        if(recordTimer!=null){
            recordTimer.cancel();
        }
        super.onDestroy();
        if (wwvBig != null) {
            wwvBig.release();
            wwvBig = null;
        }
        if (wwvSmall != null) {
            wwvSmall.release();
            wwvSmall = null;
        }


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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
