package com.wilddog.conversation.activities;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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

import com.wilddog.conversation.ConversationApplication;
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.ConversationRecord;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.floatingwindow.PermissionUtils;
import com.wilddog.conversation.floatingwindow.StreamsHolder;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.Camera360Util;
import com.wilddog.conversation.utils.ConvertUtil;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.utils.MyOpenHelper;
import com.wilddog.conversation.utils.ParamsStore;
import com.wilddog.conversation.utils.RingUtil;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.utils.TuSDKUtil;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogVideoManager;
import com.wilddog.video.base.LocalStream;
import com.wilddog.video.base.LocalStreamOptions;
import com.wilddog.video.base.WilddogVideoError;
import com.wilddog.video.base.WilddogVideoView;
import com.wilddog.video.call.CallStatus;
import com.wilddog.video.call.Conversation;
import com.wilddog.video.call.RemoteStream;
import com.wilddog.video.call.WilddogVideoCall;
import com.wilddog.video.call.stats.LocalStreamStatsReport;
import com.wilddog.video.call.stats.RemoteStreamStatsReport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class CallingActivity extends AppCompatActivity {
    private static final String TAG = CallingActivity.class.getCanonicalName();

    private String remoteId;

    private WilddogVideoCall video = WilddogVideoCall.getInstance();

    private CheckBox cbMic;
    private CheckBox cbSpeaker;
    private CheckBox cbCamera;
    private LinearLayout llHangup;
    private TextView tvHangup;
    private LinearLayout llFlipCamera;

    private CircleImageView civPhotoUrl;

    private TextView tvDimension;
    private TextView tvFps;
    private TextView tvRate;
    private TextView tvByte;

    private LinearLayout llCalled;
    private TextView tvCallNickname;
    private TextView tvNickname;
    private TextView tvTime;
    private RelativeLayout rlHidePlace;
    private WilddogVideoView wwvBig;
    private WilddogVideoView wwvSmall;


    private RelativeLayout rlData;
    private LinearLayout llState;

    private ImageView ivState;
    private ImageView ivRecordFile;
    private TextView tvRecordTime;

    private PopupWindow popupWindow;
    private RelativeLayout rlParent;

    private LocalStream localStream;

    private Conversation conversation;

    private boolean isSelfInBig = true;
    private boolean isShowDetail = false;
    private boolean isAccept = false;
    private boolean firstFrame = true;
    private boolean isButtonHide = false;

    private boolean isrecording = false;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private boolean isAudioEnable = true;
    private boolean isVideoEnable = true;
    private boolean isSpeakerOn = true;

    private int currVolume = 0;
    private UserInfo remoteUserInfo = null;

    private AudioManager audioManager;
    private ImageView ivFullScreen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        remoteUserInfo= WilddogVideoManager.getRemoteUser();
        remoteId = remoteUserInfo.getUid();
        initView();
        if(!StreamsHolder.getMyBinder().isWindowShow()) {
            LocalStreamOptions localStreamOptions = genLocalStreamOptions();
            localStream = video.createLocalStream(localStreamOptions);
            localStream.setOnFrameListener(new LocalStream.CameraFrameListener() {
                @Override
                public void onByteFrame(byte[] bytes, int i, int i1, int i2, long l) {
                    // TODO 设置美颜效果
                    frameProcess(bytes, 0, firstFrame, true, i, i1, i2);//data 可以传空 根据TextureId进行美颜
                    firstFrame = false;
                }
            });
            localStream.attach(wwvBig);

            if(StreamsHolder.getLocalStream()!=null&& !localStream.isClosed()){
                StreamsHolder.getLocalStream().close();
            }

            StreamsHolder.setLocalStream(localStream);

            conversation = video.call(remoteId, localStream, "conversationDemo");

            conversation.setConversationListener(listener);

            if(WilddogVideoManager.getConversation()!=null){
                WilddogVideoManager.getConversation().close();
            }

            WilddogVideoManager.saveConversation(conversation);
            startRing();
        }else  {
            conversation =WilddogVideoManager.getConversation();
            conversation.setConversationListener(listener);
            localStream=StreamsHolder.getLocalStream();
            llCalled.setVisibility(View.INVISIBLE);
            tvHangup.setText("挂断");
            rlData.setVisibility(View.VISIBLE);
        }

    }

    private void startRing() {
        RingUtil.paly(true, ConversationApplication.getContext().getApplicationContext());
    }

    private void stop(){
        if(RingUtil.isRing){
        RingUtil.stop();}
    }

    public void frameProcess(byte[] data, int textureId, boolean isFirstFrame, boolean isInitEGL, int frameWidth, int frameHeight, int rotation) {
        switch (SharedPreferenceTool.getBeautyPlan(CallingActivity.this)) {
            case "Camera360":
                if (isFirstFrame)
                    Camera360Util.initEngine(CallingActivity.this, isInitEGL, frameWidth, frameHeight, rotation);//  在第一帧视频到来时，初始化，指定需要的输出大小以及方向
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
                    if(localStats.getBytesSent()!=null){
                        tvByte.setText("send " + convertToMB(localStats.getBytesSent()) + "MB");}
                } else {
                    // 显示远程统计数据
                    tvDimension.setText(remoteStats.getWidth() + "x" + remoteStats.getHeight() + "px");
                    tvFps.setText(remoteStats.getFps() + "fps");
                    tvRate.setText(remoteStats.getBitsReceivedRate() + "kpbs");
                    if(remoteStats.getBytesReceived()!=null){
                        tvByte.setText("recv " + convertToMB(remoteStats.getBytesReceived()) + "MB");}
                }
            }
        });

    }

    private String convertToMB(BigInteger value) {

        float result = new BigDecimal(value.toString()).divide(new BigDecimal(new String ("1048576")),2,BigDecimal.ROUND_HALF_UP).floatValue();
        return decimalFormat.format(result);
    }


    private Conversation.Listener listener = new Conversation.Listener() {
        @Override
        public void onCallResponse(final CallStatus callStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (callStatus) {
                        case ACCEPTED:
                            stop();
                            llCalled.setVisibility(View.INVISIBLE);
                            tvHangup.setText("挂断");
                            rlData.setVisibility(View.VISIBLE);
                            ParamsStore.isInitiativeCall =true;
                            isAccept = true;
                            break;
                        case REJECTED:
                            stop();
                            AlertMessageUtil.showShortToast("对方拒绝邀请");
                            finish();
                            break;
                        case BUSY:
                            stop();
                            AlertMessageUtil.showShortToast("对方在通话中，请稍后");
                            finish();
                            break;
                        case TIMEOUT:
                            stop();
                            AlertMessageUtil.showShortToast("呼叫超时,请稍后重试");
                            finish();
                            break;
                        default:
                            break;
                    }
                }
            });

        }

        @Override
        public void onStreamReceived(final RemoteStream remoteStream) {
            //有参与者成功加入会话后，会触发此方法
            //设置音视频全部开启
            StreamsHolder.setRemoteStream(remoteStream);
//            StreamsHolder.setRemoteStream(remoteStream);
//
//            Intent intent = new Intent();
//            intent.setAction(Constant.UPDATE_VIEW);
//            sendBroadcast(intent);
            remoteStream.enableAudio(true);
            remoteStream.enableVideo(true);
            //在视频展示控件中播放其他端媒体流
            conversation.setStatsListener(statsListener);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView();
                }
            });
        }

        @Override
        public void onClosed() {
            // 对方离开通话回调
            Log.e(TAG, "Participant:onClosed");
            //TODO 对方断开连接 如何处理？
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertMessageUtil.showShortToast("用户：" + conversation.getRemoteUid() + "离开会话");
                }
            });
//            conversation.close();
            if(StreamsHolder.getMyBinder().isWindowShow())
                StreamsHolder.getMyBinder().hidFloatingWindow();
            release();
            finish();
        }

        @Override
        public void onError(WilddogVideoError wilddogVideoError) {
            Log.e(TAG, "create failured:" + wilddogVideoError.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertMessageUtil.showShortToast("呼叫失败");
                    finish();
                }
            });
        }

    };


    private void initView() {
        rlParent = (RelativeLayout) findViewById(R.id.rl_parent);
        cbMic = (CheckBox) findViewById(R.id.cb_mic);
        cbMic.setChecked(isAudioEnable);
        cbSpeaker = (CheckBox) findViewById(R.id.cb_speaker);
        cbSpeaker.setChecked(isSpeakerOn);
        cbCamera = (CheckBox) findViewById(R.id.cb_camera);
        cbCamera.setChecked(isVideoEnable);
        llHangup = (LinearLayout) findViewById(R.id.ll_reject);
        tvHangup = (TextView) findViewById(R.id.tv_hungup);
        civPhotoUrl = (CircleImageView) findViewById(R.id.civ_photo);
        tvNickname = (TextView) findViewById(R.id.tv_nickname);
        tvCallNickname = (TextView) findViewById(R.id.tv_call_nickname);
        tvNickname.setText(remoteUserInfo.getNickname());
        tvCallNickname.setText(remoteUserInfo.getNickname());
        ImageManager.Load(remoteUserInfo.getFaceurl(), civPhotoUrl);
        tvHangup.setText("取消");
        llFlipCamera = (LinearLayout) findViewById(R.id.ll_filp_camera);
        rlHidePlace = (RelativeLayout) findViewById(R.id.rl_can_hide_place);
        tvTime = (TextView) findViewById(R.id.tv_time);

        tvDimension = (TextView) findViewById(R.id.tv_dimensions);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvByte = (TextView) findViewById(R.id.tv_bytes);

        llCalled = (LinearLayout) findViewById(R.id.ll_call);

        wwvBig = (WilddogVideoView) findViewById(R.id.wvv_big);
        wwvBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAccept){
                    if(isButtonHide){
                        rlHidePlace.setVisibility(View.VISIBLE);
                    }else {
                        rlHidePlace.setVisibility(View.INVISIBLE);
                    }
                    isButtonHide = !isButtonHide;
                }
            }
        });
        wwvSmall = (WilddogVideoView) findViewById(R.id.wvv_small);
        // 两个surfaceview 重叠显示，会有一个不显示。设置此属性，让此控件放到窗口顶层
        wwvSmall.setZOrderMediaOverlay(true);


        rlData = (RelativeLayout) findViewById(R.id.rl_data);
        rlData.setVisibility(View.INVISIBLE);
        llState = (LinearLayout) findViewById(R.id.ll_state);
        tvRecordTime = (TextView) findViewById(R.id.tv_record_time);

        ivState = (ImageView) findViewById(R.id.iv_report);
        ivRecordFile = (ImageView) findViewById(R.id.iv_record);
        ivRecordFile.setBackgroundResource(R.drawable.record_normal);
        ivFullScreen = (ImageView) findViewById(R.id.iv_fullscreen);


        ivState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDetail = !isShowDetail;
                // 将图标隐藏，显示统计信息
                llState.setVisibility(View.VISIBLE);
                rlData.setVisibility(View.INVISIBLE);


            }
        });

        llState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDetail = !isShowDetail;
                llState.setVisibility(View.INVISIBLE);
                rlData.setVisibility(View.VISIBLE);
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
                    conversation.stopLocalRecording();
                    endRecordTime();
                    showSavePopupWindow();
                } else {
                    if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                        Toast.makeText(CallingActivity.this,"存储设备不存在,无法录制",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //开始录制
                    ivRecordFile.setBackgroundResource(R.drawable.record_selected);
                    isrecording = true;
                    conversation.startLocalRecording(getRecordFile(), wwvSmall, wwvBig);
                    tvRecordTime.setVisibility(View.VISIBLE);
                    recordTime = 0;
                    startRecordTime();
                }
            }
        });

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

        llHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (conversation != null) {
                    conversation.close();
                }
                stop();
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
        ivFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloatingWindow();
            }
        });


    }
    public void showFloatWindow() {
        //显示悬浮框
//        PermissionUtils.requestPermissionsResult(this, 1,
//                new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW
//                }, new PermissionUtils.OnPermissionListener() {
//                    @Override
//                    public void onPermissionGranted() {
//                        showFloatingWindow();
//                    }
//
//                    @Override
//                    public void onPermissionDenied() {
////                        showFloatingWindow();
//                        Toast.makeText(CallingActivity.this, "请打开悬浮窗权限", Toast.LENGTH_SHORT).show();
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
    private void release() {
        if (conversation != null) {
            conversation.close();
        }
        if (localStream != null && !localStream.isClosed()) {
            localStream.close();
        }
        String localid = SharedPreferenceTool.getUserId(getApplicationContext());
        ConversationRecord record = MyOpenHelper.getInstance().selectConversationRecord(localid, remoteId);
        if (record == null) {
            record = new ConversationRecord();
            record.setRemoteId(remoteId);
            record.setDuration(String.valueOf(conversationTime));
            record.setLocalId(localid);
            record.setNickName(remoteUserInfo.getNickname());
            record.setPhotoUrl(remoteUserInfo.getFaceurl());
            record.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            MyOpenHelper.getInstance().insertRecord(record);
        } else {
            record = new ConversationRecord();
            record.setRemoteId(remoteId);
            record.setDuration(String.valueOf(conversationTime));
            record.setNickName(remoteUserInfo.getNickname());
            record.setPhotoUrl(remoteUserInfo.getFaceurl());
            record.setLocalId(localid);
            record.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            MyOpenHelper.getInstance().updateRecord(localid, remoteId, record);
        }

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


    private String fileName;

    private String getFileName() {
        return fileName;
    }

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
        View view = View.inflate(CallingActivity.this, R.layout.popupwindow_record_file, null);
        final EditText etFileName = (EditText) view.findViewById(R.id.et_file_name);
        etFileName.setText(fileName.substring(0,fileName.indexOf(".mp4")));
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
                    renameFile(newFileName+".mp4");
                    popupWindow.dismiss();
                }
            }
        });
        showPopupWindow(view);
    }

    private void renameFile(String newFileName) {
        File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        File file = new File(filedir.getAbsolutePath() + "/" + fileName);
        file.renameTo(new File(newFileName));
    }

    private void deletefile() {
        File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        File file = new File(filedir.getAbsolutePath() + "/" + fileName);
        file.delete();
    }

    private void showPopupWindow(View view) {
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(rlParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
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
        builder.captureAudio(true).captureVideo(true).dimension(LocalStreamOptions.Dimension.DIMENSION_720P).maxFps(30);
        switch (SharedPreferenceTool.getDimension(CallingActivity.this)) {
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
    public void onBackPressed() {
        super.onBackPressed();
        release();
        finish();
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
//        if (localStream != null && !localStream.isClosed()) {
//            localStream.close();
//        }
        if (wwvBig != null) {
            wwvBig.release();
            wwvBig = null;
        }
        if (wwvSmall != null) {
            wwvSmall.release();
            wwvSmall = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        conversation.setStatsListener(statsListener);
        startTimer();
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
