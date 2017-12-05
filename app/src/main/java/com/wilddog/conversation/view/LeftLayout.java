package com.wilddog.conversation.view;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;


import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.StreamHolder;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.video.base.WilddogVideoView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.leefeng.promptlibrary.PromptDialog;


public class LeftLayout extends RelativeLayout implements View.OnClickListener {

    private Context context;
    private WilddogVideoView remoteView1;
    private WilddogVideoView remoteView2;
    private WilddogVideoView remoteView3;
    private WilddogVideoView remoteView4;
    private WilddogVideoView remoteView5;
    private WilddogVideoView remoteView6;
    private WilddogVideoView remoteView7;
    private List<WilddogVideoView> remoteVideoViewsL = new ArrayList<>();
    private List<StreamHolder> streamHolders = new ArrayList<>();

    private PromptDialog promptDialog;


    public LeftLayout(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public LeftLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LeftLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.left, this);
        remoteView1 = (WilddogVideoView) findViewById(R.id.wvv_remote1_in);
        remoteView2 = (WilddogVideoView) findViewById(R.id.wvv_remote2_in);
        remoteView3 = (WilddogVideoView) findViewById(R.id.wvv_remote3_in);
        remoteView4 = (WilddogVideoView) findViewById(R.id.wvv_remote4_in);
        remoteView5 = (WilddogVideoView) findViewById(R.id.wvv_remote5_in);
        remoteView6 = (WilddogVideoView) findViewById(R.id.wvv_remote6_in);
        remoteView7 = (WilddogVideoView) findViewById(R.id.wvv_remote7_in);
        remoteVideoViewsL.add(remoteView1);
        remoteVideoViewsL.add(remoteView2);
        remoteVideoViewsL.add(remoteView3);
        remoteVideoViewsL.add(remoteView4);
        remoteVideoViewsL.add(remoteView5);
        remoteVideoViewsL.add(remoteView6);
        remoteVideoViewsL.add(remoteView7);

        findViewById(R.id.copy_invite_left).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.copy_invite_left:
                ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(Constant.INVITE_CANCEL);
                promptDialog.showSuccess("邀请链接已复制");
                break;

        }
    }
    public void showRemoteViews() {
        for(int i = 0;i<streamHolders.size();i++){
            StreamHolder streamHolder = streamHolders.get(i);
            streamHolder.getStream().attach(remoteVideoViewsL.get(i));
        }
        for (WilddogVideoView remote:
                remoteVideoViewsL) {
            remote.setVisibility(View.VISIBLE);
        }


    }

    public void deteachAll() {
        for(StreamHolder streamHolder:streamHolders){
            streamHolder.getStream().detach();
        }
        for (WilddogVideoView remote:
                remoteVideoViewsL) {
            remote.setVisibility(View.GONE);
        }
    }

    public void release() {
        for(WilddogVideoView videoView: remoteVideoViewsL){
            videoView.release();
        }
    }

    public void setStreamHolder(List<StreamHolder> paramstreamHolders) {
        this.streamHolders = paramstreamHolders;
    }

    public void addDialog(PromptDialog promptDialog) {

        this.promptDialog = promptDialog;
    }

    public void removeRemoteStream(long streamId) {
        Iterator<StreamHolder> iterator = streamHolders.iterator();
        while (iterator.hasNext()){
            StreamHolder holder = iterator.next();
            if(streamId==holder.getId()){
                holder.getStream().detach();
                holder.getStream().close();
                iterator.remove();
            }
        }
    }
}
