package com.wilddog.conversation.fragments;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.ValueEventListener;
import com.wilddog.conversation.R;
import com.wilddog.conversation.activities.CallingActivity;
import com.wilddog.conversation.bean.ConversationRecord;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.ImageLoadingUtil;
import com.wilddog.conversation.db.MyOpenHelper;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.utils.String2DateUtil;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogSyncManager;
import com.wilddog.conversation.wilddog.WilddogVideoManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fly on 17-6-9.
 */

public class FriendsFragment extends BaseFragment {
    private RelativeLayout rlListView;
    private ListView lvRecordList;
    private List<ConversationRecord> records = new ArrayList<>();
    private MyAdapter adapter;
    private RelativeLayout rlNoHistory;

    public FriendsFragment() {

    }

    @Override
    public View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_friends, null);
        rlListView = (RelativeLayout) view.findViewById(R.id.rl_listview);
        lvRecordList = (ListView) view.findViewById(R.id.lv_records);
        rlNoHistory = (RelativeLayout) view.findViewById(R.id.rl_no_friend);
        adapter = new MyAdapter(records, getContext());
        lvRecordList.setAdapter(adapter);
        lvRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String uid = records.get(position).getRemoteId();
                WilddogSyncManager.getWilddogSyncTool().getonlineUserInfos(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot==null || dataSnapshot.getValue()==null){return;}
                        Map map = (Map) dataSnapshot.getValue();
                        if(map.containsKey(uid)){
                            Map subMap = (Map) map.get(uid);
                           UserInfo remoteUserInfo = new UserInfo();
                            String strFaceurl = subMap.get("faceurl")==null?"https://img.wdstatic.cn/imdemo/1.png":subMap.get("faceurl").toString();
                            remoteUserInfo.setFaceurl(strFaceurl);
                            String strNickname = subMap.get("nickname")==null?uid:subMap.get("nickname").toString();
                            remoteUserInfo.setNickname(strNickname);
                            remoteUserInfo.setUid(uid);
                            gotoCallingActivity(remoteUserInfo);
                        }else {
                            Toast.makeText(getContext(),"你呼叫的用户不在线或者不存在",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(SyncError syncError) {

                    }
                });
            }
        });
        initDate();
        return view;
    }

    private void gotoCallingActivity(UserInfo info) {
        WilddogVideoManager.setWilddogUser(info);
        Intent intent = new Intent(getContext(), CallingActivity.class);
//        intent.putExtra("user",info);
        startActivity(intent);
    }


    private void showListViewOrTextView() {
        if (records.size() > 0) {
            rlNoHistory.setVisibility(View.GONE);
            rlListView.setVisibility(View.VISIBLE);
        } else {
            rlNoHistory.setVisibility(View.VISIBLE);
            rlListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initDate();
    }

    private void initDate() {
        if (records.size() > 0) {
            records.clear();
        }
        records.addAll(MyOpenHelper.getInstance().selectConversationRecords(SharedPreferenceTool.getUserId(getContext())));
        adapter.notifyDataSetChanged();
        showListViewOrTextView();
    }


    class MyAdapter extends BaseAdapter {
        private List<ConversationRecord> mList = new ArrayList<>();
        private LayoutInflater mInflater;

        MyAdapter(List<ConversationRecord> recordList, Context context) {
            mList = recordList;
            mInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder v;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_friend, null);
                v = new ViewHolder();
                v.tvNickName = (TextView) view.findViewById(R.id.tv_nickname);
                v.civPhoto = (CircleImageView) view.findViewById(R.id.civ_photo);
                v.tvDuration = (TextView) view.findViewById(R.id.tv_duration);
                v.tvCallTime = (TextView) view.findViewById(R.id.tv_call_time);
                view.setTag(v);
            } else {
                v = (ViewHolder) view.getTag();
            }
            ConversationRecord everyone = mList.get(position);
            v.tvNickName.setText(everyone.getNickname());
            v.tvCallTime.setText(String2DateUtil.getStandardDate(everyone.getTimestamp()));
            v.tvDuration.setText("通话时长:"+formatTime(everyone.getDuration()));
            ImageLoadingUtil.Load(everyone.getPhotoUrl(), v.civPhoto);
            return view;
        }

        public class ViewHolder {
            public TextView tvNickName;
            public CircleImageView civPhoto;
            public TextView tvDuration;
            public TextView tvCallTime;
        }
        private String formatTime(String duration){
            long time = Long.parseLong(duration);
            if(time<60){
                return duration+"秒";
            }else {
                if(time%60==0){
                    return time/60+"分钟";
                }else {
                    return (time/60+1)+"分钟";
                }
            }
        }
    }


}
