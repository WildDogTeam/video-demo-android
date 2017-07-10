package com.wilddog.conversationdemo.activities;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wilddog.conversationdemo.R;
import com.wilddog.conversationdemo.utils.AlertMessageUtil;
import com.wilddog.conversationdemo.utils.SharedpereferenceTool;
import com.wilddog.video.Conversation;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.OutgoingInvite;
import com.wilddog.video.Participant;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoClient;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.video.bean.ConnectOptions;
import com.wilddog.video.bean.LocalStats;
import com.wilddog.video.bean.RemoteStats;
import com.wilddog.video.bean.VideoException;
import com.wilddog.video.bean.VideoExceptionCode;
import com.wilddog.video.listener.ConversationCallback;
import com.wilddog.video.listener.RTCStatsListener;

public class CallingActivity extends AppCompatActivity {
    private static final String TAG = CallingActivity.class.getCanonicalName();

    private String inviteUid;
    private OutgoingInvite outgoingInvite;
    private WilddogVideo video = WilddogVideo.getInstance();
    private WilddogVideoClient client = video.getClient();

    private TextView tvMic;
    private TextView tvHungup;
    private TextView tvFlipCamera;

    private TextView tvDimension;
    private TextView tvFps;
    private TextView tvRate;
    private TextView tvByte;

    private TextView tvState;

    private WilddogVideoView wwvBig;
    private WilddogVideoView wwvSmall;

    /*DecimalFormat decimalFormat = new DecimalFormat("0.00");
    BigDecimal bg ;*/

    private LinearLayout llData;
    private LinearLayout llState;

    private ImageView ivState;

    private LocalStream localStream;

    private Conversation mConversation;

    private boolean isSelfInBig = true;
    private boolean isShowDetail = false;
    private boolean isAccept = false;

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
            public void onByteFrame(byte[] bytes, int i, int i1) {
                //TODO 设置美颜效果
            }
        });
        localStream.attach(wwvBig);
        ConnectOptions connectOptions = new ConnectOptions(localStream, "conversationDemo");
        outgoingInvite = client.inviteToConversation(inviteUid, connectOptions, new ConversationCallback() {
            @Override
            public void onConversation(@Nullable Conversation conversation, @Nullable VideoException e) {

                if (conversation != null) {
                    // 对方接受  将呼叫中隐藏
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvState.setVisibility(View.INVISIBLE);
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
                    if (e.getErrorCode() == VideoExceptionCode.VIDEO_CONVERSATION_INVITATION_FAILED) {
                        AlertMessageUtil.showShortToast("对方拒绝邀请");
                        finish();
                    } else if(e.getErrorCode() == VideoExceptionCode.VIDEO_CONVERSATION_INVITATION_IGNORED){
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

    private RTCStatsListener rtcStatsListener = new RTCStatsListener() {
        @Override
        public void onLocalStats(LocalStats localStats) {
            showStats(localStats, null);
        }

        @Override
        public void onRemoteStats(RemoteStats remoteStats) {
            showStats(null, remoteStats);
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
        /*float result = Float.parseFloat(String.valueOf(value)) / (1024 * 1024);
        return decimalFormat.format(result);*/
        /* bg = new BigDecimal(value);
        double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return String.valueOf(f1);*/
        return String.format("%.2f", value);
    }


    private Conversation.Listener listener = new Conversation.Listener() {
        @Override
        public void onConnected(Conversation conversation) {

        }


        @Override
        public void onConnectFailed(Conversation conversation, VideoException e) {

        }

        @Override
        public void onDisconnected(Conversation conversation, VideoException e) {

        }

        @Override
        public void onParticipantConnected(Conversation conversation, Participant participant) {
            String remoteParticipantId=participant.getParticipantId();
            Log.d(TAG,"remoteParticipantId:"+remoteParticipantId);
            participant.setListener(new Participant.Listener() {
                @Override
                public void onStreamAdded(RemoteStream remoteStream) {
                    //有参与者成功加入会话后，会触发此方法
                    //设置音视频全部开启
                    remoteStream.enableAudio(true);
                    remoteStream.enableVideo(true);
                    //在视频展示控件中播放其他端媒体流
                    localStream.detach();
                    remoteStream.attach(wwvBig);
                    wwvSmall.setVisibility(View.VISIBLE);
                    localStream.attach(wwvSmall);

                }

                @Override
                public void onConnectFailed(Participant participant, VideoException exception) {

                    if (exception != null) {
                        Log.d(TAG, "Participant connect failed,the detail:" + exception.getMessage());
                    }
                }

                @Override
                public void onDisconnected(Participant participant, VideoException exception) {
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


    private void initView() {
        tvMic = (TextView) findViewById(R.id.tv_mic);
        tvHungup = (TextView) findViewById(R.id.tv_hungup);
        tvFlipCamera = (TextView) findViewById(R.id.tv_flipCamera);

        tvDimension = (TextView) findViewById(R.id.tv_dimensions);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvByte = (TextView) findViewById(R.id.tv_bytes);

        tvState = (TextView) findViewById(R.id.tv_call_state);
        tvState.setText("正在呼叫" + inviteUid);

        wwvBig = (WilddogVideoView) findViewById(R.id.wvv_big);
        wwvSmall = (WilddogVideoView) findViewById(R.id.wvv_small);
        // 两个surfaceview 重叠显示，会有一个不显示。设置此属性，让此控件放到窗口顶层
        wwvSmall.setZOrderMediaOverlay(true);


        llData = (LinearLayout) findViewById(R.id.ll_data);
        llData.setVisibility(View.INVISIBLE);
        llState = (LinearLayout) findViewById(R.id.ll_state);

        ivState = (ImageView) findViewById(R.id.iv_report);

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

        tvMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localStream != null) {
                    isAudioEnable = !isAudioEnable;
                    localStream.enableAudio(isAudioEnable);
                }
            }
        });
        tvHungup.setOnClickListener(new View.OnClickListener() {
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
        tvFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video != null) {
                    video.flipCamera();
                }
            }
        });


    }


    private LocalStreamOptions genLocalStreamOptions() {
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        switch (SharedpereferenceTool.getDimension(CallingActivity.this)) {
            case "360P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSIONS_360P);
                break;
            case "480P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSIONS_480P);
                break;
            case "720P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSIONS_720P);
                break;
            case "1080P":
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSIONS_1080P);
                break;
            default:
                builder.setDimension(LocalStreamOptions.Dimension.DIMENSIONS_480P);
                break;
        }
        return builder.setAudioEnabled(true).build();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
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

        client.dispose();
        video.dispose();
    }
}
