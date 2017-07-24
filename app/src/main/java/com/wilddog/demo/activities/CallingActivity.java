package com.wilddog.demo.activities;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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

import com.wilddog.demo.R;
import com.wilddog.demo.utils.AlertMessageUtil;
import com.wilddog.demo.utils.Camera360Util;
import com.wilddog.demo.utils.Contants;
import com.wilddog.demo.utils.ConvertUtil;
import com.wilddog.demo.utils.SharedpereferenceTool;
import com.wilddog.demo.utils.TuSDKUtil;
import com.wilddog.video.Conversation;
import com.wilddog.video.ConversationCallback;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.OutgoingInvite;
import com.wilddog.video.Participant;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.VideoError;
import com.wilddog.video.VideoErrorCode;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoClient;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.video.bean.ConnectOptions;
import com.wilddog.video.bean.LocalStats;
import com.wilddog.video.bean.RemoteStats;
import com.wilddog.video.listener.RTCStatsListener;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class CallingActivity extends AppCompatActivity {
    private static final String TAG = CallingActivity.class.getCanonicalName();

    private String inviteUid;
    private OutgoingInvite outgoingInvite;
    private WilddogVideo video = WilddogVideo.getInstance();
    private WilddogVideoClient client = video.getClient();

    private CheckBox cbMic;
    private LinearLayout llHungup;
    private TextView tvHungup;
    private LinearLayout llFlipCamera;

    private TextView tvDimension;
    private TextView tvFps;
    private TextView tvRate;
    private TextView tvByte;

    private TextView tvState;
    private LinearLayout llCalled;

    private TextView tvTime;

    private WilddogVideoView wvvBig;
    private WilddogVideoView wvvSmall;


    private LinearLayout llData;
    private LinearLayout llState;

    private ImageView ivState;
    private ImageView ivRecordFile;
    private TextView tvRecordTime;

    private PopupWindow popupWindow;
    private RelativeLayout rlParent;

    private LocalStream localStream;

    private Conversation mConversation;

    private boolean isSelfInBig = true;
    private boolean isShowDetail = false;
    private boolean isAccept = false;
    private boolean mFirstFrame =true;

    private boolean isrecording = false;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private int timeout = 30000;

    private boolean isAudioEnable = true;
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(!isAccept){
                        AlertMessageUtil.showShortToast("对方暂时无人接听");
                        outgoingInvite.cancel();
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        inviteUid = getIntent().getStringExtra("inviteUid");
        initView();
        LocalStreamOptions localStreamOptions = genLocalStreamOptions();
        localStream = video.createLocalStream(localStreamOptions);
        localStream.setOnFrameListener(new WilddogVideo.CameraFrameListener() {
            @Override
            public void onByteFrame(byte[] bytes, int i, int i1,int var4, long var5) {
                //
                // TODO 设置美颜效果

                frameProcess(bytes, 0, mFirstFrame, true, i, i1, var4 );//data 可以传空 根据TextureId进行美颜
                mFirstFrame = false;
            }
        });
        localStream.attach(wvvBig);
        ConnectOptions connectOptions = new ConnectOptions(localStream, "conversationDemo");
        outgoingInvite = client.inviteToConversation(inviteUid, connectOptions, new ConversationCallback() {
            @Override
            public void onConversation(@Nullable Conversation conversation, @Nullable VideoError e) {

                if (conversation != null) {
                    // 对方接受  将呼叫中隐藏
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            llCalled.setVisibility(View.INVISIBLE);
                            tvHungup.setText("挂断");
                            llData.setVisibility(View.VISIBLE);
                        }
                    });
                    Log.d(TAG,"outgoingInvite status is "+outgoingInvite.getStatus()+"::"+outgoingInvite.getStatus().equals("accepted"));
                    isAccept = true;
                    mConversation = conversation;
                    mConversation.setConversationListener(listener);
                    mConversation.setRTCStatsListener(rtcStatsListener);
                } else {
                    Log.d(TAG,"outgoingInvite status is "+outgoingInvite.getStatus()+"::"+String.valueOf(outgoingInvite.getStatus().equals("busy") | outgoingInvite.getStatus().equals("cancel")));
                    //处理会话建立失败逻辑
                    if (e.getErrorCode() == VideoErrorCode.VIDEO_CONVERSATION_INVITATION_FAILED) {
                        AlertMessageUtil.showShortToast("对方拒绝邀请");
                        finish();
                    } else if(e.getErrorCode() == VideoErrorCode.VIDEO_CONVERSATION_INVITATION_IGNORED){
                        AlertMessageUtil.showShortToast("对方在通话中，请稍后");
                        finish();
                    }
                    else  {
                        //TODO 处理其他错误
                        Log.e(TAG, "create failured:" + e.getMessage());
                        AlertMessageUtil.showShortToast("呼叫失败");
                        finish();
                    }
                }

            }
        });
          //TODO 当前版本有问题，需要修复后使用
        //  Log.d(TAG,"outgoingInvite status is "+outgoingInvite.getStatus()+"::"+outgoingInvite.getStatus().equals("pendding"));

        // 做超时处理 呼叫超过三十秒，未接受
        handler.sendEmptyMessageDelayed(1,timeout);
    }

    public void frameProcess(byte[] data, int textureId, boolean isFirstFrame, boolean isInitEGL, int frameWidth, int frameHeight, int rotation) {
        switch (SharedpereferenceTool.getBeautyPlan(CallingActivity.this)){
            case "Camera360":
                if (isFirstFrame) Camera360Util.initEngine(CallingActivity.this,isInitEGL, frameWidth, frameHeight, rotation);//  在第一帧视频到来时，初始化，指定需要的输出大小以及方向
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


    private RTCStatsListener rtcStatsListener = new RTCStatsListener() {
        @Override
        public void onLocalStats(LocalStats localStats) {
            if(isSelfInBig){
            showStats(localStats, null);}
        }

        @Override
        public void onRemoteStats(RemoteStats remoteStats) {

            if(!isSelfInBig){
            showStats(null, remoteStats);}
        }
    };


    private void showStats(LocalStats localStats, RemoteStats remoteStats) {
        if (!isShowDetail) {
            return;
        }
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

    private String convertToMB(long value) {
        float result = Float.parseFloat(String.valueOf(value)) / (1024 * 1024);
        return decimalFormat.format(result);
       // return String.format("%.2f", value);
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
            String remoteParticipantId=participant.getParticipantId();
            Log.d(TAG,"remoteParticipantId:"+remoteParticipantId);
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
                            remoteStream.attach(wvvBig);
                            wvvSmall.setVisibility(View.VISIBLE);
                            localStream.attach(wvvSmall);
                            isSelfInBig =false;
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
                public void onDisconnected(final Participant participant, VideoError exception) {
                    Log.e(TAG, "Participant:onDisconnected");
                    //TODO 对方断开连接 如何处理？
                    if (exception != null) {
                        Log.d(TAG, "Participant onDisconnected failured,the detail:" + exception.getMessage());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertMessageUtil.showShortToast("用户：" + participant.getParticipantId() + "离开会话");
                        }
                    });
                    mConversation.disconnect();
                    finish();
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


    private void initView() {
        rlParent = (RelativeLayout) findViewById(R.id.rl_parent);
        cbMic = (CheckBox) findViewById(R.id.cb_mic);
        cbMic.setChecked(isAudioEnable);
        llHungup = (LinearLayout) findViewById(R.id.ll_reject);
        tvHungup = (TextView) findViewById(R.id.tv_hungup);
        tvHungup.setText("取消");
        llFlipCamera = (LinearLayout) findViewById(R.id.ll_filp_camera);

        tvTime = (TextView) findViewById(R.id.tv_time);

        tvDimension = (TextView) findViewById(R.id.tv_dimensions);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvByte = (TextView) findViewById(R.id.tv_bytes);

        llCalled = (LinearLayout) findViewById(R.id.ll_call);
        tvState = (TextView) findViewById(R.id.tv_call_state);
        tvState.setText(inviteUid);

        wvvBig = (WilddogVideoView) findViewById(R.id.wvv_big);
        wvvSmall = (WilddogVideoView) findViewById(R.id.wvv_small);
        // 两个surfaceview 重叠显示，会有一个不显示。设置此属性，让此控件放到窗口顶层
        wvvSmall.setZOrderMediaOverlay(true);


        llData = (LinearLayout) findViewById(R.id.ll_data);
        llData.setVisibility(View.INVISIBLE);
        llState = (LinearLayout) findViewById(R.id.ll_state);
        tvRecordTime = (TextView) findViewById(R.id.tv_record_time);

        ivState = (ImageView) findViewById(R.id.iv_report);
        ivRecordFile = (ImageView) findViewById(R.id.iv_record);

        ivState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDetail=!isShowDetail;
                    // 将图标隐藏，显示统计信息
                    llState.setVisibility(View.VISIBLE);
                    llData.setVisibility(View.INVISIBLE);


            }
        });

        llState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDetail=!isShowDetail;

                    llState.setVisibility(View.INVISIBLE);
                    llData.setVisibility(View.VISIBLE);
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
                    tvRecordTime.setVisibility(View.GONE);
                    mConversation.stopVideoRecording();
                    endRecordTime();
                    showSavePopupWindow();
                }else {
                    //开始录制
                    ivRecordFile.setBackgroundResource(R.drawable.record_selected);
                    isrecording = true;
                    mConversation.startVideoRecording(getRecordFile());
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
        llHungup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAccept) {
                    // 对方已经接受，结束会话
                    if (mConversation != null) {
                        mConversation.disconnect();
                    }
                } else {
                    // 对方未接受，取消邀请
                    if (outgoingInvite != null) {
                        outgoingInvite.cancel();

                    }
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

    private Timer recordTimer;
    private void startRecordTime() {
        if(recordTimer==null){
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
        recordTimer.schedule(recordTask,0,1000);
    }
    private int recordTime = 0;

    private TimerTask recordTask;

    private void endRecordTime(){
        recordTimer.cancel();
        recordTimer = null ;
        recordTime =0;
    }



    private String fileName;

    private String getFileName(){
        return fileName;
    }

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
        fileName = videoFile.getName();
        return videoFile;
    }



    private void showSavePopupWindow() {
        View view = View.inflate(CallingActivity.this, R.layout.popupwindow_record_file, null);
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
        switch (SharedpereferenceTool.getDimension(CallingActivity.this)) {
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
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
        if (localStream != null) {
            localStream.detach();
            localStream.close();
        }
        if (wvvBig != null) {
            wvvBig.release();
            wvvBig = null;
        }
        if (wvvSmall != null) {
            wvvSmall.release();
            wvvSmall = null;
        }
        if (mConversation != null) {
            mConversation.disconnect();
        }
/*
        client.dispose();
        video.dispose();*/
    }
}
