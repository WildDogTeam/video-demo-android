package com.wilddog.demo.fragments;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.demo.R;
import com.wilddog.demo.activities.CallingActivity;
import com.wilddog.demo.activities.SendInviteActivity;
import com.wilddog.demo.utils.SharedpereferenceTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fly on 17-6-9.
 */

public class OnlineFragment extends BaseFragment {

    private ImageView ivInvite;
    private ListView lvUserList;
    private RelativeLayout rlNoUser;
    private LinearLayout llListView;

    private SyncReference mRef;
    private String mUid;

    private List<String> userList = new ArrayList<>();

    private MyAdapter adapter;

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot != null) {

                String key = dataSnapshot.getKey();
                 if (!mUid.equals(key)) {
                     userList.add(key);
                  }
                adapter.notifyDataSetChanged();
                showListViewOrTextView();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (dataSnapshot != null) {
                String key = dataSnapshot.getKey();
                if (!mUid.equals(key)) {
                    userList.remove(key);
                    adapter.notifyDataSetChanged();
                    showListViewOrTextView();
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(SyncError wilddogError) {

        }
    }; ;

    public OnlineFragment(){

    }

    private void showListViewOrTextView(){
        if(userList.size()>0){
            rlNoUser.setVisibility(View.GONE);
            llListView.setVisibility(View.VISIBLE);
        }else {
            rlNoUser.setVisibility(View.VISIBLE);
            llListView.setVisibility(View.GONE);
        }
    }

    @Override
    public View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_online,null);
        llListView = (LinearLayout) view.findViewById(R.id.ll_listview);
        ivInvite = (ImageView) view.findViewById(R.id.iv_invite);
        lvUserList = (ListView) view.findViewById(R.id.lv_user_id);
        rlNoUser = (RelativeLayout) view.findViewById(R.id.rl_no_user);
        ivInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSendInviteActivity();
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

    private void gotoCallingAcivity(String uid) {
        Intent intent = new Intent(getContext(), CallingActivity.class);
        intent.putExtra("inviteUid",uid);
        startActivity(intent);
    }

    private void gotoSendInviteActivity(){
        startActivity(new Intent(getContext(), SendInviteActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }



    private void initData() {
        mRef = WilddogSync.getInstance().getReference();
        mUid = SharedpereferenceTool.getUserId(getContext());
        if(userList.size()>0){
            userList.clear();
        }
        mRef.child("onlineusers").addChildEventListener(childEventListener);
    }

    class MyAdapter extends BaseAdapter {
        private List<String> mList = new ArrayList<>();
        private LayoutInflater mInflater;

        MyAdapter(List<String> userList, Context context) {
            mList = userList;
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
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder v;
            if(view==null) {
                view = mInflater.inflate(R.layout.item_online_user, null);
                v=new ViewHolder();
                v.id = (TextView) view.findViewById(R.id.tv_uid);
                view.setTag(v);
            }else {
               v= (ViewHolder) view.getTag();
            }
            v.id.setText(mList.get(i));
            return view;
        }

        public class ViewHolder{
            public TextView id;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRef.child("onlineusers").removeEventListener(childEventListener);
    }
}
