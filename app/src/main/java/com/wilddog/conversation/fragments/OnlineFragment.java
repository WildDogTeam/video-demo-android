package com.wilddog.conversation.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.conversation.R;
import com.wilddog.conversation.activities.DetailInfoActivity;
import com.wilddog.conversation.activities.JoinRoomActivity;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ImageLoadingUtil;
import com.wilddog.conversation.db.MyOpenHelper;
import com.wilddog.conversation.utils.PingYinUtil;
import com.wilddog.conversation.utils.PinyinComparator;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.view.SideBar;
import com.wilddog.conversation.wilddog.WilddogSyncManager;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by fly on 17-6-9.
 */

public class OnlineFragment extends BaseFragment {

    private ImageView ivJoinRoom;
    private ListView lvUserList;
    private RelativeLayout rlNoUser;
    private RelativeLayout rlListView;
    private SideBar sideBarView;
    private TextView mDialogText;
    private SyncReference mRef;
    private String mUid;
    private List<String> userIds = new ArrayList<>();
    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private List<UserInfo> userList = new ArrayList<>();
    private List<String> blackUserIDs;

    private MyAdapter adapter;



    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String key = dataSnapshot.getKey();
                        if ((!mUid.equals(key)) && (!userIds.contains(key)) && dataSnapshot.getValue() instanceof Map) {
                            Map value = (Map) dataSnapshot.getValue();
                            UserInfo info = new UserInfo();
                            info.setUid(key);
                            String strFaceurl = value.get("faceurl")==null?"https://img.wdstatic.cn/imdemo/1.png":value.get("faceurl").toString();
                            info.setFaceurl(strFaceurl);
                            String strNickname = value.get("nickname")==null?key:value.get("nickname").toString();
                            info.setNickname(strNickname);
                            String devideId = value.get("deviceid")==null?UUID.randomUUID().toString():value.get("deviceid").toString();
                            info.setDeviceid(devideId);

                            if(!blackUserIDs.contains(key)){
                                userList.add(info);
                                userIds.add(key);
                            }

                        }
                        adapter.notifyDataSetChanged();
                        showListViewOrTextView();
                    }
                });

            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.e("onChildChanged","onChildChanged");
        }

        @Override
        public void onChildRemoved(final DataSnapshot dataSnapshot) {
            if (dataSnapshot != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String key = dataSnapshot.getKey();
                        if (!mUid.equals(key)) {
                           UserInfo removeUserInfo = getUserInfoByUid(key);
                            if(removeUserInfo!=null){
                            userList.remove(removeUserInfo);
                            userIds.remove(key);
                            adapter.notifyDataSetChanged();
                            showListViewOrTextView();}
                        }
                    }
                });

            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(SyncError wilddogError) {

        }
    };

    public OnlineFragment(){

    }


    private UserInfo getUserInfoByUid(String uid){
        for(UserInfo info : userList){
            if(info.getUid().equals(uid)){
                return info;
            }
        }
        return null;
    }

    private void showListViewOrTextView(){
        if(userList.size()>0){
            AlertMessageUtil.dismissprogressbar();
            rlNoUser.setVisibility(View.GONE);
            rlListView.setVisibility(View.VISIBLE);
        }else {
            AlertMessageUtil.dismissprogressbar();
            rlNoUser.setVisibility(View.VISIBLE);
            rlListView.setVisibility(View.GONE);
        }
    }

    @Override
    public View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_online,null);
        sideBarView = (SideBar) view.findViewById(R.id.sideBar);
        rlListView = (RelativeLayout) view.findViewById(R.id.rl_listview);
        ivJoinRoom = (ImageView) view.findViewById(R.id.iv_join_room);
        mDialogText = (TextView) LayoutInflater.from(getActivity()).inflate(
                R.layout.item_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        sideBarView.setTextView(mDialogText);
        lvUserList = (ListView) view.findViewById(R.id.lv_records);
        sideBarView.setListView(lvUserList);
        rlNoUser = (RelativeLayout) view.findViewById(R.id.rl_no_user);
        ivJoinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoJoinRoomActivity();
            }
        });
        showListViewOrTextView();
        adapter = new MyAdapter(userList,getContext());
        lvUserList.setAdapter(adapter);
        lvUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoCallingAcivity(userList.get(position));
            }
        });
        return view;
    }

    private void gotoCallingAcivity(UserInfo info) {
        Intent intent = new Intent(getContext(), DetailInfoActivity.class);
        intent.putExtra("user",info);
        startActivity(intent);
    }

    private void gotoJoinRoomActivity(){
        startActivity(new Intent(getContext(), JoinRoomActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        AlertMessageUtil.showprogressbar("载入数据中",getContext());
    }



    private void initData() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRef = WilddogSync.getInstance().getReference();
                mUid = SharedPreferenceTool.getUserId(getContext());
                blackUserIDs=MyOpenHelper.getInstance().selectBlackIds(mUid);
                if(userList.size()>0){
                    userList.clear();
                    userIds.clear();
                }
                mRef.child(WilddogSyncManager.ONLINEUSER).addChildEventListener(childEventListener);
            }
        });

    }

   public  class MyAdapter extends BaseAdapter implements SectionIndexer {
        private List<UserInfo> mList = new ArrayList<>();
        private LayoutInflater mInflater;

        MyAdapter(List<UserInfo> userList, Context context) {
            mList = userList;
            mInflater = LayoutInflater.from(context);
            // 排序(实现了中英文混排)
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
            if(view==null) {
                view = mInflater.inflate(R.layout.item_online_user, null);
                v=new ViewHolder();
                v.id = (TextView) view.findViewById(R.id.tv_uid);
                v.photoUrl = (CircleImageView) view.findViewById(R.id.civ_photo);
                v.tvCatalog = (TextView) view.findViewById(R.id.contactitem_catalog);
                view.setTag(v);
            }else {
               v= (ViewHolder) view.getTag();
            }
            UserInfo everyone = mList.get(position);
            v.id.setText(everyone.getNickname());
            String catalog = PingYinUtil.converterToFirstSpell(everyone.getNickname())
                    .substring(0, 1);
            if (position == 0) {
                v.tvCatalog.setVisibility(View.VISIBLE);
                v.tvCatalog.setText(catalog);
            } else {
                UserInfo Nextuser = mList.get(position - 1);
                String lastCatalog = PingYinUtil.converterToFirstSpell(
                        Nextuser.getNickname()).substring(0, 1);
                if (catalog.equals(lastCatalog)) {
                    v.tvCatalog.setVisibility(View.GONE);
                } else {
                    v.tvCatalog.setVisibility(View.VISIBLE);
                    v.tvCatalog.setText(catalog);
                }
            }
            ImageLoadingUtil.Load(everyone.getFaceurl(),v.photoUrl);
            return view;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < mList.size(); i++) {
                UserInfo user = mList.get(i);
                String l = PingYinUtil.converterToFirstSpell(user.getNickname())
                        .substring(0, 1);
                char firstChar = l.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        public class ViewHolder{
            public TextView id;
            public CircleImageView photoUrl;
            public TextView tvCatalog;
        }

       @Override
       public void notifyDataSetChanged() {
           Collections.sort(mList, new PinyinComparator());
           super.notifyDataSetChanged();
       }
   }

    @Override
    public void onPause() {
        super.onPause();
        mRef.child(WilddogSyncManager.ONLINEUSER).removeEventListener(childEventListener);
    }
}
