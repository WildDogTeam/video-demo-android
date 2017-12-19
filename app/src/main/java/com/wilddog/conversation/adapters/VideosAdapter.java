package com.wilddog.conversation.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.StreamHolder;
import com.wilddog.video.base.WilddogVideoView;

import java.util.List;

/**
 * Created by fly on 17-12-11.
 */

public class VideosAdapter extends BaseAdapter{
    private List<StreamHolder> mlist;
    private Context mContext;
    private boolean isLocalAttach = false;
    public VideosAdapter(Context context, List<StreamHolder> list) {
        mContext = context;
        mlist = list;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        StreamHolder streamHolder = mlist.get(i);
        if (view == null) {
            view = View.inflate(mContext, R.layout.item_video, null);
            holder = new ViewHolder();
            holder.wilddogVideoView = (WilddogVideoView) view.findViewById(R.id.wvv_video);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (streamHolder.isLocal()) {
            // 本地流detach 需要时间,频繁detach再attach,可能detach完成在attch之后,导致本地视频画面卡住,所以如果是本地流attch之后就不反复操作了
            if (isLocalAttach == false) {
                streamHolder.getStream().attach(holder.wilddogVideoView);
                isLocalAttach = true;
            }
        } else {
            streamHolder.getStream().detach();
            streamHolder.getStream().attach(holder.wilddogVideoView);
        }
        return view;
    }

    class ViewHolder {
        WilddogVideoView wilddogVideoView;
    }
}
