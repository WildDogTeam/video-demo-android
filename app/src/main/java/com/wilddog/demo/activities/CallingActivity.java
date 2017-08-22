package com.wilddog.demo.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
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
import com.wilddog.demo.utils.ConvertUtil;
import com.wilddog.demo.utils.SharedpereferenceTool;
import com.wilddog.demo.utils.TuSDKUtil;
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

public class CallingActivity extends AppCompatActivity {
    private static final String TAG = CallingActivity.class.getCanonicalName();

    private String inviteUid;
    private WilddogVideo video = WilddogVideo.getInstance();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        inviteUid = getIntent().getStringExtra("inviteUid");
        initView();
        LocalStreamOptions localStreamOptions = genLocalStreamOptions();
        localStream = video.createLocalStream(localStreamOptions);
        localStream.setOnFrameListener(new LocalStream.CameraFrameListener() {
            @Override
            public void onByteFrame(byte[] bytes, int i, int i1,int var4, long var5) {
                //
                // TODO 设置美颜效果

                frameProcess(bytes, 0, mFirstFrame, true, i, i1, var4 );//data 可以传空 根据TextureId进行美颜
                mFirstFrame = false;
            }
        });
        localStream.attach(wvvBig);
        mConversation = video.call(inviteUid,localStream,"conversation extra info");
        mConversation.setConversationListener(new Conversation.Listener() {
            @Override
            public void onCallResponse(CallStatus callStatus) {
                switch (callStatus){
                    case BUSY:
                        AlertMessageUtil.showShortToast("对方在通话中，请稍后");
                        finish();
                        break;
                    case ACCEPTED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                llCalled.setVisibility(View.INVISIBLE);
                                tvHungup.setText("挂断");
                                llData.setVisibility(View.VISIBLE);
                            }
                        });
                        break;
                    case REJECTED:
                        AlertMessageUtil.showShortToast("对方拒绝邀请");
                        finish();
                        break;
                    case TIMEOUT:
                        // 做超时处理 呼叫超过三十秒，未接受
                        AlertMessageUtil.showShortToast("邀请超时");
                        finish();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onStreamReceived(final RemoteStream remoteStream) {
                mConversation.setStatsListener(rtcStatsListener);
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
            public void onClosed() {
                mConversation.close();
                finish();
            }

            @Override
            public void onError(WilddogVideoError wilddogVideoError) {

            }
        });

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


    private Conversation.StatsListener rtcStatsListener = new Conversation.StatsListener() {
        @Override
        public void onLocalStreamStatsReport(LocalStreamStatsReport localStreamStatsReport) {
            if(isSelfInBig){
                showStats(localStreamStatsReport, null);}
        }

        @Override
        public void onRemoteStreamStatsReport(RemoteStreamStatsReport remoteStreamStatsReport) {
            if(!isSelfInBig){
                showStats(null, remoteStreamStatsReport);}
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

    private String convertToMB(long value) {
        float result = Float.parseFloat(String.valueOf(value)) / (1024 * 1024);
        return decimalFormat.format(result);
       // return String.format("%.2f", value);
    }






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
                    mConversation.stopLocalRecording();
                    endRecordTime();
                    showSavePopupWindow();
                }else {
                    //开始录制
                    ivRecordFile.setBackgroundResource(R.drawable.record_selected);
                    isrecording = true;
                    mConversation.startLocalRecording(getRecordFile(),wvvSmall,wvvBig);
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
                mConversation.close();
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
        File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        File file = new File(filedir.getAbsolutePath()+"/"+fileName);
        file.renameTo(new File(newFileName));
    }
    private void deletefile(){
        File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        File file = new File(filedir.getAbsolutePath()+"/"+fileName);
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
        if(timer==null){
        timer = new Timer();
        timer.schedule(task,0,1000);}
    }

    private LocalStreamOptions genLocalStreamOptions() {
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        switch (SharedpereferenceTool.getDimension(CallingActivity.this)) {
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
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
        }
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
            mConversation.close();
        }
    }
}
