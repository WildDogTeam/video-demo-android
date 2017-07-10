package com.wilddog.conversationdemo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wilddog.conversationdemo.R;
import com.wilddog.conversationdemo.receiver.InviteCancelBroadcastReceiver;
import com.wilddog.conversationdemo.utils.AlertMessageUtil;
import com.wilddog.conversationdemo.utils.Contants;
import com.wilddog.conversationdemo.utils.SharedpereferenceTool;
import com.wilddog.conversationdemo.wilddogAuth.WilddogVideoManager;
import com.wilddog.video.Conversation;
import com.wilddog.video.IncomingInvite;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.Participant;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoClient;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.video.bean.ConnectOptions;
import com.wilddog.video.bean.LocalStats;
import com.wilddog.video.bean.RemoteStats;
import com.wilddog.video.bean.VideoException;
import com.wilddog.video.listener.ConversationCallback;
import com.wilddog.video.listener.RTCStatsListener;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = ConversationActivity.class.getName();

    private IncomingInvite incomingInvite;
    private String fromUid;

    private WilddogVideo video = WilddogVideo.getInstance();
    private WilddogVideoClient client = video.getClient();

    private CheckBox cbMic;
    private TextView tvHungup;
    private TextView tvFlipCamera;

    // 显示统计结果控件
    private TextView tvDimension;
    private TextView tvFps;
    private TextView tvRate;
    private TextView tvByte;

    private TextView tvCallState;

    private WilddogVideoView wwvBig;
    private WilddogVideoView wwvSmall;

    private LinearLayout llData;
    private LinearLayout llState;

    private ImageView ivState;

    private boolean isSelfInBig = true;
    private boolean isShowDetail = false;

    private boolean isAudioEnable = true;

    /*DecimalFormat decimalFormat = new DecimalFormat("0.00");
    BigDecimal bg ;*/

    private LocalStream localStream;

    private Conversation mConversation;

    private BroadcastReceiver broadcastReceiver;

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
            public void onByteFrame(byte[] bytes, int i, int i1) {
                //TODO 设置美颜效果
            }
        });
        localStream.attach(wwvBig);
        ConnectOptions connectOptions = new ConnectOptions(localStream, "conversationDemo");

        incomingInvite.accept(connectOptions, new ConversationCallback() {
            @Override
            public void onConversation(@Nullable Conversation conversation, @Nullable VideoException e) {
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

    private void initView() {
        cbMic = (CheckBox) findViewById(R.id.cb_mic);
        cbMic.setChecked(isAudioEnable);
        tvHungup = (TextView) findViewById(R.id.tv_hungup);
        tvFlipCamera = (TextView) findViewById(R.id.tv_flipCamera);

        tvDimension = (TextView) findViewById(R.id.tv_dimensions);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvByte = (TextView) findViewById(R.id.tv_bytes);

        tvCallState = (TextView) findViewById(R.id.tv_call_state);
        tvCallState.setVisibility(View.INVISIBLE);

        wwvBig = (WilddogVideoView) findViewById(R.id.wvv_big);
        wwvSmall = (WilddogVideoView) findViewById(R.id.wvv_small);
        wwvSmall.setZOrderMediaOverlay(true);

        llData = (LinearLayout) findViewById(R.id.ll_data);
        llState = (LinearLayout) findViewById(R.id.ll_state);

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

        cbMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (localStream != null) {
                    isAudioEnable = !isAudioEnable;
                    localStream.enableAudio(isChecked);
                }
            }
        });
        tvHungup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 对方已经接受，结束会话
                if (mConversation != null) {
                    mConversation.disconnect();
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
            participant.setListener(new Participant.Listener() {
                @Override
                public void onStreamAdded(final RemoteStream remoteStream) {
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

    private LocalStreamOptions genLocalStreamOptions() {
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        switch (SharedpereferenceTool.getDimension(ConversationActivity.this)) {
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

        client.dispose();
        video.dispose();
    }

}
