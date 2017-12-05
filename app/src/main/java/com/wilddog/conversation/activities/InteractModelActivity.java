package com.wilddog.conversation.activities;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.wilddog.board.BoardOption;
import com.wilddog.board.WilddogBoard;
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.StreamHolder;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.SharedPereferenceTool;
import com.wilddog.conversation.view.LeftLayout;
import com.wilddog.conversation.view.RightLayout;
import com.wilddog.toolbar.boardtoolbar.ToolBarMenu;
import com.wilddog.video.base.LocalStream;
import com.wilddog.video.base.LocalStreamOptions;
import com.wilddog.video.base.WilddogVideoError;
import com.wilddog.video.base.WilddogVideoInitializer;
import com.wilddog.video.base.WilddogVideoView;
import com.wilddog.video.room.CompleteListener;
import com.wilddog.video.room.RoomStream;
import com.wilddog.video.room.WilddogRoom;
import com.wilddog.wilddogauth.WilddogAuth;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.leefeng.promptlibrary.PromptDialog;

import static com.wilddog.board.utils.BoardUtil.px2dip;


public class InteractModelActivity extends AppCompatActivity implements View.OnClickListener {

    private WilddogBoard boardView;
    private ToolBarMenu actionsMenu;
    private WilddogVideoInitializer initializer;
    private LocalStream localStream;
    private boolean isAudioEnable = true;
    private WilddogVideoView localView;
    private WilddogVideoView remoteView1;
    private WilddogVideoView remoteView2;
    private WilddogVideoView remoteView3;
    private WilddogVideoView remoteView4;
    private WilddogVideoView remoteView5;
    private WilddogVideoView remoteView6;
    private WilddogVideoView remoteView7;
    private String roomId = "roomid";
    private WilddogRoom room;
    private List<WilddogVideoView> remoteVideoViews = new ArrayList<>();
    private List<StreamHolder> streamHolders = new ArrayList<>();


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            deteachAll();
            if(!drawerLayout.isDrawerOpen(leftMenu)) {
                leftMenu.deteachAll();
                showRemoteViews();
            }else {
                leftMenu.showRemoteViews();
            }
        }
    };
    private DrawerLayout drawerLayout;
    private LeftLayout leftMenu;
    private RightLayout rightMenu;
    private WilddogVideoView localViewIn;
    private TextView roomNum;
    private TextView quit;
    private PromptDialog promptDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact_model);

        roomId = getIntent().getStringExtra("roomId");

        initView();
        initRoomSDK();
        createLocalStream();
        joinRoom();

        leftMenu.setStreamHolder(streamHolders);
    }



    private void deteachAll() {
        for (int i = 0;i<streamHolders.size();i++) {
            streamHolders.get(i).getStream().detach();
        }
        for (WilddogVideoView remote:
             remoteVideoViews) {
            remote.setVisibility(View.GONE);
        }

    }


    private void showRemoteViews() {
        for(int i = 0;i<streamHolders.size();i++){
            streamHolders.get(i).getStream().attach(remoteVideoViews.get(i));
        }
        for (WilddogVideoView remote:
                remoteVideoViews) {
            remote.setVisibility(View.VISIBLE);
        }
        roomNum.setText("成员("+(streamHolders.size()+1)+")");
    }

    private void joinRoom() {
        room = new WilddogRoom(roomId, new WilddogRoom.Listener() {
            @Override
            public void onConnected(WilddogRoom wilddogRoom) {
                Toast.makeText(InteractModelActivity.this,"已经连接上服务器", Toast.LENGTH_SHORT).show();
                room.publish(localStream, new CompleteListener() {
                    @Override
                    public void onComplete(WilddogVideoError wilddogVideoError) {
                        if(wilddogVideoError!=null){
                            //失败
                            Toast.makeText(InteractModelActivity.this,"推送流失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onDisconnected(WilddogRoom wilddogRoom) {
                Toast.makeText(InteractModelActivity.this,"服务器连接断开", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onStreamAdded(WilddogRoom wilddogRoom, RoomStream roomStream) {
                if(streamHolders.size()>=7)return;
                //订阅流 如果超过8个就不订阅流
                room.subscribe(roomStream);
            }

            @Override
            public void onStreamRemoved(WilddogRoom wilddogRoom, RoomStream roomStream) {

                if(roomStream==null) {
                    return;
                }
                //具体流 超过8个的退出可能不包含,所以移除时候判断是否包含
                removeRemoteStream(roomStream.getStreamId());
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onStreamReceived(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 在控件中显示
                if(streamHolders.size()>=7)return;
                StreamHolder holder = new StreamHolder(false, System.currentTimeMillis(),roomStream);
                holder.setId(roomStream.getStreamId());
                streamHolders.add(holder);
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onStreamChanged(WilddogRoom wilddogRoom, RoomStream roomStream) {
            }

            @Override
            public void onError(WilddogRoom wilddogRoom, WilddogVideoError wilddogVideoError) {
                Toast.makeText(InteractModelActivity.this,"发生错误,请产看日志", Toast.LENGTH_SHORT).show();
                Log.e("error","错误码:"+wilddogVideoError.getErrCode()+",错误信息:"+wilddogVideoError.getMessage());
            }
        });
        room.connect();
    }
    private void removeRemoteStream(long streamId) {
        Iterator<StreamHolder> iterator = streamHolders.iterator();
        while (iterator.hasNext()){
            StreamHolder holder = iterator.next();
            if(streamId==holder.getId()){
                holder.getStream().close();
                holder.getStream().detach();
                iterator.remove();
            }
        }

        leftMenu.removeRemoteStream(streamId);
    }

    private void createLocalStream() {
        LocalStreamOptions options = genLocalStreamOptions();

        localStream = LocalStream.create(options);
        localStream.enableAudio(isAudioEnable);
        localStream.enableVideo(true);
        localStream.attach(localView);
        localStream.attach(localViewIn);

    }
    private LocalStreamOptions genLocalStreamOptions() {
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();
        switch (SharedPereferenceTool.getDimension(this)) {
            case "360P":
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_360P);
                break;
            case "480P":
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_480P);
                break;
            case "720P":
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_720P);
                break;
            default:
                builder.dimension(LocalStreamOptions.Dimension.DIMENSION_480P);
                break;
        }

        return builder.captureAudio(true).build();
    }
    private void initView() {

        promptDialog = new PromptDialog(this);
        //设置自定义属性
        promptDialog.getDefaultBuilder().touchAble(true).round(3).loadingDuration(3000);

        localView = (WilddogVideoView) findViewById(R.id.wvv_local);
        localViewIn = (WilddogVideoView) findViewById(R.id.wvv_local_in);
        remoteView1 = (WilddogVideoView) findViewById(R.id.wvv_remote1);
        remoteView2 = (WilddogVideoView) findViewById(R.id.wvv_remote2);
        remoteView3 = (WilddogVideoView) findViewById(R.id.wvv_remote3);
        remoteView4 = (WilddogVideoView) findViewById(R.id.wvv_remote4);
        remoteView5 = (WilddogVideoView) findViewById(R.id.wvv_remote5);
        remoteView6 = (WilddogVideoView) findViewById(R.id.wvv_remote6);
        remoteView7 = (WilddogVideoView) findViewById(R.id.wvv_remote7);
        remoteVideoViews.add(remoteView1);
        remoteVideoViews.add(remoteView2);
        remoteVideoViews.add(remoteView3);
        remoteVideoViews.add(remoteView4);
        remoteVideoViews.add(remoteView5);
        remoteVideoViews.add(remoteView6);
        remoteVideoViews.add(remoteView7);

        roomNum = (TextView) findViewById(R.id.room_num);

        quit = (TextView) findViewById(R.id.quit_room);
        quit.setOnClickListener(this);

        findViewById(R.id.copy_invite).setOnClickListener(this);

        leftMenu = (LeftLayout) findViewById(R.id.rl_leftmenu);
        ViewGroup.LayoutParams leftParams = leftMenu.getLayoutParams();
        leftParams.width = getWindowWidth() / 3;
        leftParams.height = getWindowHeight();
        leftMenu.setLayoutParams(leftParams);
        leftMenu.addDialog(promptDialog);

        rightMenu = (RightLayout) findViewById(R.id.rl_rightmenu);
        ViewGroup.LayoutParams rightParams = rightMenu.getLayoutParams();
        rightParams.width = getWindowWidth() * 2 / 3;
        rightParams.height = getWindowHeight();
        rightMenu.setLayoutParams(rightParams);

        drawerLayout = (DrawerLayout) findViewById(R.id.v4_drawerlayout);

        findViewById(R.id.iv_show).setOnClickListener(this);

        findViewById(R.id.iv_hide).setOnClickListener(this);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                hideLocalVideoView();
                deteachAll();
                leftMenu.showRemoteViews();

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                showLocalVideoView();
                leftMenu.deteachAll();
                showRemoteViews();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        boardView = (WilddogBoard) findViewById(R.id.board);
        boardView.setup(Constant.WILDDOG_SYNC_APP_ID, roomId + "/board", "abc"+ System.currentTimeMillis(),new BoardOption(px2dip(1366),px2dip(768), BoardOption.WildBoardAuthorityMode.READWRITE));

        boardView.setBackgroundColor(Color.BLACK);
        actionsMenu = (ToolBarMenu) findViewById(R.id.graphic_menu);
        actionsMenu.bindingBoard(boardView, this);

    }
    private void showLocalVideoView() {
        localView.setVisibility(View.VISIBLE);
    }

    private int getWindowHeight() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    private int getWindowWidth() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    private void initRoomSDK() {
        WilddogVideoInitializer.initialize(this, Constant.WILDDOG_VIDEO_APP_ID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
        initializer = WilddogVideoInitializer.getInstance();
        initializer.addTokenListener(new WilddogVideoInitializer.TokenListener() {
            @Override
            public void onTokenChanged(String s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_show:
                drawerLayout.openDrawer(leftMenu);
                drawerLayout.openDrawer(rightMenu);
                break;
            case R.id.iv_hide:
                drawerLayout.closeDrawer(leftMenu);
                drawerLayout.closeDrawer(rightMenu);
                break;
            case R.id.quit_room:
                finish();
                break;
            case R.id.copy_invite:
                ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(Constant.INVITE_URL);

                promptDialog.showSuccess("邀请链接已复制");
                break;
        }
    }

    private void hideLocalVideoView() {
        localView.setVisibility(View.GONE);
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
        leaveRoom();
        if (!localStream.isClosed()) {
            localStream.detach();
            localStream.close();
        }
        localView.release();
        localViewIn.release();
        for (WilddogVideoView videoView : remoteVideoViews) {
            videoView.release();
        }

        leftMenu.release();
        rightMenu.release();
        promptDialog.onBackPressed();
    }

}
