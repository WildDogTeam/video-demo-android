package com.wilddog.conversation.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.ConversationRecord;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.utils.MyOpenHelper;
import com.wilddog.conversation.utils.SharedpereferenceTool;
import com.wilddog.conversation.view.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fly on 17-6-9.
 */

public class CallFragment extends BaseFragment {
    private RelativeLayout rlListView;
    private ListView lvRecordList;
    private List<ConversationRecord> records = new ArrayList<>();
    private MyAdapter adapter;
    private RelativeLayout rlNoHistory;

    public CallFragment() {

    }

    @Override
    public View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_call_history, null);
        rlListView = (RelativeLayout) view.findViewById(R.id.rl_listview);
        lvRecordList = (ListView) view.findViewById(R.id.lv_records);
        rlNoHistory = (RelativeLayout) view.findViewById(R.id.rl_no_history);
        adapter = new MyAdapter(records, getContext());
        initDate();
        return view;
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
        records.addAll(MyOpenHelper.getInstance().selectConversationRecords(SharedpereferenceTool.getUserId(getContext())));
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
                view = mInflater.inflate(R.layout.item_recent_call, null);
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
            v.tvNickName.setText(everyone.getNickName());
            v.tvCallTime.setText(everyone.getTimeStamp());
            v.tvDuration.setText(everyone.getDuration());
            ImageManager.Load(everyone.getPhotoUrl(), v.civPhoto);
            return view;
        }

        public class ViewHolder {
            public TextView tvNickName;
            public CircleImageView civPhoto;
            public TextView tvDuration;
            public TextView tvCallTime;
        }
    }


}
